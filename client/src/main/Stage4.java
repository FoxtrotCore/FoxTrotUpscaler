/**
 * Copyright 2018 Christian Devile
 * 
 * This file is part of FoxTrotUpscaler.
 * 
 * FoxTrotUpscaler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FoxTrotUpscaler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FoxTrotUpscaler.  If not, see <http://www.gnu.org/licenses/>.
 */
package main;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.apache.commons.io.input.ReversedLinesFileReader;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import graphical.MainWindow;
import structures.DataBlock;
import structures.Report;

public class Stage4 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private DataBlock block;
	private Process d;
	private int totalFrames;
	private Timer timer;

	private File upFolder;
	private volatile int oldCount;
	private volatile String timeEstimate;
	private volatile int y;
	private volatile int z;
	private volatile int totalRate;
	private ScheduledExecutorService scheduler;

	public Stage4(MainWindow window, DataBlock block, Timer timer)
	{
		this.window = window;
		this.timer = timer;
		this.block = block;
		totalFrames = block.getTotalFrames();
		oldCount = 0;
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		Thread.currentThread().setName("Stage4Thread");
		block.setProgress(4);
		block.recordProgress();
		block.getBar4().setMaximum(totalFrames);
		String quickPath = new String(File.separator + "Export" + File.separator + block.getEep() + "_" + block.getShowName() + ".mkv");
		try
		{
			block.getOutput().append("\nStarting Video Export Stage"
					+ "--------------------------------------------------------------------------------------------\n");
			logger.debug("Duration of Video: ", Integer.valueOf(((int) (totalFrames / Double.valueOf(block.getScan().getR2())))));
			if (!block.isRecovered())
			{
				try
				{
					Files.deleteIfExists(Paths.get(block.getDir() + quickPath));
					logger.info("File Deleted: {}", quickPath);
				}
				catch (IOException p)
				{
					logger.fatal("Failed Delete Attempt: {}", quickPath);
					logger.fatal("Exception Infomation", p);
					JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + quickPath + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
			else
			{
				block.setRecovered(false);
			}
			Report r = block.getSettings().getStoredReport();
			r.setCurrentFrame(0);
			r.setOperating(true);
			r.setJobStarted(true);
			r.setEta("Calculating");
			r.addEvent(5, "Stage D Started");
			r.setCurrentStage(4);
			block.getSettings().submit(r);
			boolean processResume = true;
			while (processResume)
			{
				processResume = false;
				logger.info("Stage D: ({})", block.getCommand4());
				scheduler = Executors.newScheduledThreadPool(1);
				d = Runtime.getRuntime().exec(block.getCommand4());
				block.setD(d);
				StreamGobbler reader = new StreamGobbler(d.getInputStream(), block.getError(), false);
				StreamGobbler eater = new StreamGobbler(d.getErrorStream(), block.getOutput(), false);
				reader.start();
				eater.start();
				block.getPause().setEnabled(true);
				block.getAbort().setEnabled(true);
				if (block.isQueue())
				{
					block.getFullAbort().setEnabled(true);
				}
				upFolder = new File(block.getDir() + File.separator + "Temp" + File.separator + "log.txt");
				oldCount = 0;
				y = 0;
				z = 0;
				totalRate = 0;
				timeEstimate = null;
				scheduler.scheduleAtFixedRate(new ProgressCheck(), 1, 5, TimeUnit.SECONDS);
				scheduler.awaitTermination(100, TimeUnit.DAYS);
				int exitValue = d.waitFor();
				logger.info("Exit Value for Stage D: " + exitValue);

				if (exitValue != 0 && block.getStatus() != 10 && block.getStatus() != 20 && block.getStatus() != 30)
				{
					logger.error("Stage D Crashed");
					block.getPause().setEnabled(false);
					block.getAbort().setEnabled(false);
					block.getFullAbort().setEnabled(false);
					block.setStatus(1);
					Report r3 = block.getSettings().getStoredReport();
					r3.setJobFailed(true);
					r3.setJobCompleted(false);
					r3.setJobStarted(true);
					r3.setOperating(false);
					r3.setJobAborted(false);
					r3.addEvent(50, "Process Errored at Stage D");
					block.getSettings().submit(r3);
					window.toggleMenu(0, true);
					JOptionPane.showMessageDialog(window, "Fatal Crash of Stage 4\nPlease look at the Error for assistance\nThen Restart the Program",
							"Process Crash", JOptionPane.ERROR_MESSAGE);
				}
				if (block.getStatus() == 30)
				{
					processResume = true;
					Report r4 = block.getSettings().getStoredReport();
					r4.setOperating(false);
					r4.setPaused(true);
					r4.addEvent(30, "Process Paused at Stage D");
					block.getSettings().submit(r4);
					block.setPaused(true);
					Report r5 = block.getSettings().getStoredReport();
					r5.setOperating(true);
					r5.setPaused(false);
					r5.addEvent(32, "Process Resumed");
					block.getSettings().submit(r5);
					block.recordProgress();
					block.getPauseLatch().await();
					block.setPaused(false);
					logger.debug("Latch Removed, Restarting");
				}
				else if (block.getStatus() == 10)
				{
					Report r7 = block.getSettings().getStoredReport();
					r7.setJobFailed(false);
					r7.setJobCompleted(false);
					r7.setJobStarted(true);
					r7.setOperating(false);
					r7.setJobAborted(true);
					r7.addEvent(20, "Process Aborted");
					block.getSettings().submit(r7);
				}
				else
				{
					File product = new File(
							block.getDir() + File.separator + "Export" + File.separator + block.getEep() + "_" + block.getShowName() + ".mkv");
					String endName = new String("Upscaled_" + block.getAudioName() + "_" + block.getEep() + "_" + block.getShowName() + ".mkv");
					File fullName = new File(block.getDir() + File.separator + "Export" + File.separator + endName);
					Files.deleteIfExists(fullName.toPath());
					product.renameTo(fullName);
					Report r6 = block.getSettings().getStoredReport();
					r6.addEvent(6, "Stage D Completed");
					block.getSettings().submit(r6);
					block.getSettings().upload(window, fullName.getParent(), endName, block.getOutput());
				}
			}
		}
		catch (IOException e)
		{
			logger.fatal("Failed Delete Attempt: {}", quickPath);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + quickPath + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}

		return null;
	}

	class ProgressCheck implements Runnable
	{

		@Override
		public void run()
		{
			if (isRunning(d))
			{
				y++;
				int count = readFrameCount();
				Report r2 = block.getSettings().getStoredReport();
				r2.setStatus(0);
				r2.setCurrentFrame(count);
				if (timeEstimate != null && !timeEstimate.equals("TOO SLOW"))
				{
					timeEstimate = timeEstimate.substring(0, 11) + String.format("%02d", (60 - (y * 5))) + timeEstimate.substring(13);
				}
				if (y == 12)
				{
					z++;
					totalRate += (count - oldCount); // rates all added together
					int tempRate = totalRate / z; // averaged rate
					int eta = (totalFrames - count) / tempRate; // frames left/rate to get minutes left
					int minutesLeft = (eta % 60);
					int hoursLeft = (eta / 60);
					timeEstimate = new String("ETA:(" + String.format("%02d", hoursLeft).substring(0, 2) + ":"
							+ String.format("%02d", minutesLeft).substring(0, 2) + ".59) Rate: " + (count - oldCount) + "fpm");
					if (count - oldCount == 0) // If Computer too slow
					{
						timeEstimate = new String("TOO SLOW");
					}
					oldCount = count; // resets variables for next minute
					y = 0;
					r2.setEta(timeEstimate);
				}
				block.getSettings().submit(r2);
				publish(new ProgressTime(count, timeEstimate));
			}
			else
			{
				scheduler.shutdown();
				//One last CountDown after scheduled completion
				int lastCount = readFrameCount();
				String resultWarning;
				if(lastCount != totalFrames && block.getStatus() != 30)
				{
					resultWarning = new String("!!!");
					block.getError().append("Warning: Unexpected Number of Frames received from Stage 4 ");
				}
				else
					resultWarning = new String("");
				try
				{
					EventQueue.invokeAndWait(new Runnable()
					{
						public void run()
						{
							int value = (lastCount * 100) / totalFrames;
							block.getBar4().setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
							block.getBar4().setValue(lastCount);
						}
					});
				}
				catch (InvocationTargetException | InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		private boolean isRunning(Process process)
		{
			try
			{
				process.exitValue();
				return false;
			}
			catch (Exception e)
			{
				return true;
			}
		}

	}

	private int readFrameCount()
	{
		int count = 0;
		try (ReversedLinesFileReader fr = new ReversedLinesFileReader(upFolder, Charset.forName("UTF-8"));)
		{
			for (int i = 0; i < 11; i++)
			{
				String ch = fr.readLine();
				if(ch == null)
				{
					logger.warn("Unable to read FFMPEG log file, process may have not started yet");
					break;
				}
				if (ch.contains("frame="))
				{
					count = Integer.valueOf(ch.substring(6));
					break;
				}
			}
		}
		catch (IOException e)
		{
			logger.error("Unable to read Frame Count", e);
		}
		return count;
	}

	@Override
	protected void process(List<ProgressTime> chunks)
	{
		ProgressTime i = chunks.get(chunks.size() - 1);
		int value = (i.getTime() * 100) / totalFrames;
		block.getBar4().setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
		block.getBar4().setValue(i.getTime());
		if (timeEstimate != null)
		{
			block.getTime().setText(i.getProgress());
		}
	}

	@Override
	protected void done()
	{
		timer.stop();
		block.getPause().setEnabled(false);
		block.getAbort().setEnabled(false);
		block.getFullAbort().setEnabled(false);
		if (block.getStatus() == 0)
		{
			Report r7 = block.getSettings().getStoredReport();
			r7.addEvent(13, "Process Fully Completed");
			r7.setOperating(false);
			r7.setJobCompleted(true);
			r7.setJobStarted(true);
			r7.setJobFailed(false);
			r7.setJobAborted(false);
			r7.setCurrentFrame(r7.getTotalFrames());
			r7.setPaused(false);
			r7.setEta("Complete");
			block.getSettings().submit(r7);
		}
		if (block.getStatus() == 0 && !block.isQueue())
		{
			logger.info("Stage D Complete");
			block.getExecute().setEnabled(true);
			block.getExecute().setText("Execution Complete");
			//TODO Potentially fake the string to say 100%?
			block.getTime().setText("Complete");
			window.getUpscalePanel().updateQueue();
			window.toggleMenu(0, true);
			if (block.getSettings().isShutdown())
			{
				window.shutdown();
			}
			JOptionPane.showMessageDialog(window,
					"Program Successfully Created Video\nOutput Folder: " + File.separator + "Export" + File.separator + "Upscaled_"
							+ block.getAudioName() + "_" + block.getEep() + "_" + block.getShowName() + ".mkv",
					"Process Complete", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (block.getStatus() == 0 && block.isQueue())
		{
			logger.info("Stage D Complete");
			block.getExecute().setText("Execution Complete");
			block.getBar4().setString("(" + totalFrames + "/" + totalFrames + ") - 100%");
			block.getBar1().setValue(0);
			block.getBar2().setValue(0);
			block.getBar3().setValue(0);
			block.getBar4().setValue(0);
			block.getTime().setText("Calculating");
		}
		if (block.getStatus() == 10 || block.getStatus() == 20)
		{
			if (!block.isQueue())
			{
				block.getExecute().setText("Execution Aborted");
				block.getBar1().setForeground(Color.black);
				block.getBar2().setForeground(Color.black);
				block.getBar3().setForeground(Color.black);
				block.getBar4().setForeground(Color.black);
				block.getTime().setText("Complete");
				block.getPause().setEnabled(false);
				block.getAbort().setEnabled(false);
				block.getFullAbort().setEnabled(false);
				block.getExecute().setEnabled(true);
				window.toggleMenu(0, true);
			}
			else
			{
				if (block.getStatus() == 20)
				{

					block.getExecute().setText("Execution Aborted");
				}
				else
				{

					block.getExecute().setText("Skipped");
				}
				block.getBar1().setValue(0);
				block.getBar2().setValue(0);
				block.getBar3().setValue(0);
				block.getBar4().setValue(0);
				block.getTime().setText("Calculating");
			}
		}
		String quickPath = new String(File.separator + "Temp" + File.separator + "currentProcess.ser");
		try
		{
			Files.delete(Paths.get(block.getDir() + quickPath));
			logger.info("Files Deleted: {}", quickPath);
		}
		catch (IOException e)
		{
			logger.fatal("Failed Delete Attempt: {}", quickPath);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + quickPath + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		if (block.isQueue())
		{
			block.getSelfLatch().countDown();
		}
	}
}