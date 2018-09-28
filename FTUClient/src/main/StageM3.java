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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import graphical.MainWindow;
import structures.Reader;
import structures.Report;

public class StageM3 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private int status;
	private MainWindow window;
	private Process M3A;
	private Process M3B;
	private Settings settings;
	private String dir;
	private File inputFolder;
	private File middleFolder;
	private File outputFolder;
	private int totalFrames;
	private int initalCount;
	private int mode;
	private boolean stage1;
	private JButton execute;
	private JProgressBar bar1;
	private JProgressBar bar2;
	private JTextField timeField;
	private JTextArea reportArea;
	private JButton abort;

	private ScheduledExecutorService scheduler;
	private volatile int oldCount;
	private volatile String timeEstimate;
	private volatile int y;
	private volatile int z;
	private volatile int totalRate;

	public StageM3(MainWindow window,
			Settings settings,
			String dir,
			int mode,
			JButton execute,
			JProgressBar bar1,
			JProgressBar bar2,
			JTextField timeField,
			JTextArea reportArea,
			JButton abort)
	{
		status = 0;
		this.window = window;
		this.settings = settings;
		inputFolder = new File(dir + File.separator + "QueuedFrames");
		middleFolder = new File(dir + File.separator + "IntermediateFrames");
		outputFolder = new File(dir + File.separator + "ProcessedFrames");
		oldCount = 0;
		stage1 = true;
		this.dir = dir;
		this.mode = mode;
		this.execute = execute;
		this.abort = abort;
		this.bar1 = bar1;
		bar1.setMinimum(0);
		bar2.setMinimum(0);
		this.bar2 = bar2;
		this.timeField = timeField;
		this.reportArea = reportArea;
	}

	@Override
	protected Void doInBackground()
	{
		Thread.currentThread().setName("StageM3Thread");
		logger.debug("Initializing Command Strings");
		String image2;
		String image3;
		if (settings.isImage())
		{
			image2 = "tif";
			image3 = "-t";
		}
		else
		{
			image2 = "png";
			image3 = "-n";
		}
		Reader scan = new Reader(dir);
		String commandM3A;
		String commandM3B;
		boolean canContinue = true;
		if (mode == 1)// mode = 1 Delete Processed Frames first, then Intermediate Frames
		{
			totalFrames = inputFolder.list().length;
			File[] frameList = inputFolder.listFiles();
			Arrays.sort(frameList);
			if (frameList.length != 0)
			{
				for (int x = 0; x < totalFrames; x++)
				{
					if (canContinue && !deleteFrame(new String(File.separator + "ProcessedFrames" + File.separator + frameList[x].getName())))
					{
						canContinue = false;
					}
					if (canContinue && !deleteFrame(new String(File.separator + "IntermediateFrames" + File.separator + frameList[x].getName())))
					{
						canContinue = false;
					}
				}
			}
			frameList = middleFolder.listFiles();
			Arrays.sort(frameList);
			if (frameList.length != 0)
			{
				for (int x = 0; x < totalFrames; x++)
				{
					if (canContinue && !deleteFrame(new String(File.separator + "ProcessedFrames" + File.separator + frameList[x].getName())))
					{
						canContinue = false;
					}
				}
			}
			initalCount = middleFolder.list().length;
			bar1.setMaximum(totalFrames);
		}
		else if (mode == 2)//Rawtherapee Only
		{
			totalFrames = middleFolder.list().length;
			File[] frameList = middleFolder.listFiles();
			Arrays.sort(frameList);
			if (frameList.length != 0)
			{
				for (int x = 0; x < totalFrames; x++)
				{
					if (canContinue && !deleteFrame(new String(File.separator + "ProcessedFrames" + File.separator + frameList[x].getName())))
					{
						canContinue = false;
					}
				}
			}
			initalCount = outputFolder.list().length;
			bar2.setMaximum(totalFrames);
		}
		else//Waifu2x-caffe Only
		{
			totalFrames = inputFolder.list().length;
			File[] frameList = inputFolder.listFiles();
			Arrays.sort(frameList);
			if (frameList.length != 0)
			{
				for (int x = 0; x < totalFrames; x++)
				{
					if (canContinue && !deleteFrame(new String(File.separator + "ProcessedFrames" + File.separator + frameList[x].getName())))
					{
						canContinue = false;
					}
				}
			}
			initalCount = outputFolder.list().length;
			bar1.setMaximum(totalFrames);
		}
		if (!canContinue)
		{
			logger.error("Can Not Overwrite, Quiting");
			status = 20;
			return null;
		}
		timeField.setText("Calculating");
		logger.info("Command Strings Initialized");
		reportArea.append(
				"\nStarting Upscale Process" + "--------------------------------------------------------------------------------------------\n");
		try
		{
			if (mode == 0)//Stage B Only
			{
				boolean waifu2xFault = true;
				commandM3A = new String(settings.getWaifu() + " -i \"" + dir + File.separator + "QueuedFrames\" -e " + image2 + " -l " + image2
						+ " -m " + scan.getM() + " -d " + scan.getD() + " -h " + scan.getH() + " -n " + scan.getN() + " -p " + scan.getP() + " -c "
						+ scan.getC() + " -b " + scan.getB() + " --auto_start 1 --auto_exit 1 --no_overwrite 1 -y " + scan.getY() + " -o \"" + dir
						+ File.separator + "ProcessedFrames\"");
				commandM3B = new String("");
				execute.setText("Executing Stage 2");
				Report r = new Report(settings.getClientID(), 12);
				r.addEvent(12, "Manual Upscale");
				settings.submit(r);
				bar2.setString("N/A");
				while (waifu2xFault)
				{
					logger.info("Stage M3A: ({})", commandM3A);
					M3A = Runtime.getRuntime().exec(commandM3A);
					StreamGobbler reader = new StreamGobbler(M3A.getInputStream(), reportArea, true);
					StreamGobbler eater = new StreamGobbler(M3A.getErrorStream(), reportArea, true);
					reader.start();
					eater.start();
					abort.setEnabled(true);
					oldCount = outputFolder.list().length;
					y = 0;
					z = 0;
					totalRate = 0;
					scheduler = Executors.newScheduledThreadPool(1);
					scheduler.scheduleAtFixedRate(new ProgressCheck(M3A, outputFolder), 0, 1, TimeUnit.SECONDS);
					scheduler.awaitTermination(100, TimeUnit.DAYS);
					int exitValue = M3A.waitFor();
					abort.setEnabled(false);
					logger.info("Exit Value for Stage M3A: " + exitValue);
					if (outputFolder.list().length >= 4 && exitValue != 0 && status == 0)//TODO WARNING if waifu2x fails, can NOT delete corrupted frames
					{
						logger.error("Waifu2x-caffe Crashed with Progress Made");
						reportArea.append(
								"\nWaifu2x Crashed, Restarting Stage, if this occurs often, possibly scale down your speed settings in \"config.txt\"--------------------------------------------------------------------------------------------\n");
						File[] frameList = outputFolder.listFiles();
						Arrays.sort(frameList);
						for (int x = 0; x < totalFrames; x++)
						{
							String quickPath = new String(File.separator + "QueuedFrames" + File.separator + frameList[x].getName());
							File duplicateFile = new File(dir + quickPath);
							if (duplicateFile.exists())
							{
								duplicateFile.delete();
								logger.info("Frame Deleted: {}", quickPath);
							}
						}
						reportArea.append("Waifu2x CleanUp Completed, Restarting Stage");
					}
					else
					{
						waifu2xFault = false;
					}
					if(exitValue == 0 && status == 0)
					{
						Report r2 = settings.getStoredReport();
						r2.setJobCompleted(true);
						r2.addEvent(16, "Manual Upscale Complete");
						settings.submit(r2);
					}
					else if (exitValue != 0 && !waifu2xFault && status != 10)
					{
						logger.error("Stage M3A Crashed");
						Report r2 = settings.getStoredReport();
						r2.setJobFailed(true);
						r2.addEvent(17, "Manual Upscale Failed");
						settings.submit(r2);
						status = 1;
						window.toggleMenu(1, true);
						JOptionPane.showMessageDialog(window,
								"Fatal Crash of Stage 2\nPlease look at the Error for assistance\nThen Restart the Program", "Process Crash",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (exitValue != 0 && status == 10)
					{
						Report r2 = settings.getStoredReport();
						r2.setJobAborted(true);
						r2.addEvent(18, "Manual Upscale Aborted");
						settings.submit(r2);
					}
				}
			}
			else if (mode == 1)
			{
				boolean waifu2xFault = true;
				commandM3A = new String(settings.getWaifu() + " -i \"" + dir + File.separator + "QueuedFrames\" -e " + image2 + " -l " + image2
						+ " -m " + scan.getM() + " -d " + scan.getD() + " -h " + scan.getH() + " -n " + scan.getN() + " -p " + scan.getP() + " -c "
						+ scan.getC() + " -b " + scan.getB() + " --auto_start 1 --auto_exit 1 --no_overwrite 1 -y " + scan.getY() + " -o \"" + dir
						+ File.separator + "IntermediateFrames\"");
				commandM3B = new String(settings.getRawTherapee() + " -w -o \"" + dir + File.separator + "ProcessedFrames\"" + " -a\"" + dir
						+ File.separator + "IntermediateFrames\" " + "-p \"" + dir + File.separator + "rawTherapee.pp3\" -b8 " + image3 + " -c \""
						+ dir + File.separator + "IntermediateFrames\"");
				execute.setText("Executing Stage 2");
				Report r = new Report(settings.getClientID(), 12);
				r.addEvent(12, "Manual Upscale and Retouch");
				settings.submit(r);
				while (waifu2xFault)
				{
					logger.info("Stage M3A: ({})", commandM3A);
					M3A = Runtime.getRuntime().exec(commandM3A);
					StreamGobbler reader = new StreamGobbler(M3A.getInputStream(), reportArea, true);
					StreamGobbler eater = new StreamGobbler(M3A.getErrorStream(), reportArea, true);
					reader.start();
					eater.start();
					abort.setEnabled(true);
					oldCount = middleFolder.list().length;
					y = 0;
					z = 0;
					totalRate = 0;
					scheduler = Executors.newScheduledThreadPool(1);
					scheduler.scheduleAtFixedRate(new ProgressCheck(M3A, middleFolder), 0, 1, TimeUnit.SECONDS);
					scheduler.awaitTermination(10, TimeUnit.DAYS);
					int exitValue = M3A.waitFor();
					abort.setEnabled(false);
					logger.info("Exit Value for Stage M3A: " + exitValue);
					if (middleFolder.list().length >= 4 && exitValue != 0 && status == 0 && status != 10) // TODO Verify this works!
					{
						logger.error("Waifu2x-caffe Crashed with Progress Made");
						reportArea.append(
								"\nWaifu2x Crashed, Restarting Stage, if this occurs often, possibly scale down your speed settings in \"config.txt\"--------------------------------------------------------------------------------------------\n");
						File[] frameList = middleFolder.listFiles();
						Arrays.sort(frameList);
						for (int x = 0; x < totalFrames; x++)
						{
							String quickPath = new String(File.separator + "QueuedFrames" + File.separator + frameList[x].getName());
							File duplicateFile = new File(dir + quickPath);
							if (duplicateFile.exists())
							{
								duplicateFile.delete();
								logger.info("Corrupted Frame Deleted: {}", quickPath);
							}
						}
						reportArea.append("Waifu2x CleanUp Completed, Restarting Stage");
					}
					else
					{
						waifu2xFault = false;
					}
					if (exitValue != 0 && !waifu2xFault && status != 10)
					{
						logger.error("Stage M3A Crashed");
						Report r2 = settings.getStoredReport();
						r2.setJobFailed(true);
						r2.addEvent(17, "Manual Upscale and Retouch Failed on Stage B");
						settings.submit(r2);
						status = 1;
						window.toggleMenu(1, true);
						JOptionPane.showMessageDialog(window,
								"Fatal Crash of Stage B\nPlease look at the Error for assistance\nThen Restart the Program", "Process Crash",
								JOptionPane.ERROR_MESSAGE);
					}

					else if (exitValue != 0 && status == 10)
					{
						Report r2 = settings.getStoredReport();
						r2.addEvent(18, "Manual Upscale and Retouch Aborted on Stage B");
						r2.setJobAborted(true);
						settings.submit(r2);
					}
				}
				if (status == 0)// Continue with RawTherapee
				{
					logger.info("Stage M3A Completed");
					timeField.setText("Calculating");
					execute.setText("Executing Stage 3");
					oldCount = 0;
					totalFrames = middleFolder.list().length;
					initalCount = outputFolder.list().length;
					bar2.setMaximum(totalFrames);
					stage1 = false;
					logger.info("Stage M3B: ({})", commandM3B);
					M3B = Runtime.getRuntime().exec(commandM3B);
					StreamGobbler reader = new StreamGobbler(M3B.getInputStream(), reportArea, false);
					StreamGobbler eater = new StreamGobbler(M3B.getErrorStream(), reportArea, false);
					reader.start();
					eater.start();
					abort.setEnabled(true);
					oldCount = outputFolder.list().length;
					y = 0;
					z = 0;
					totalRate = 0;
					scheduler = Executors.newScheduledThreadPool(1);
					scheduler.scheduleAtFixedRate(new ProgressCheck(M3B, outputFolder), 0, 1, TimeUnit.SECONDS);
					scheduler.awaitTermination(10, TimeUnit.DAYS);
					int exitValue = M3B.waitFor();
					abort.setEnabled(false);
					logger.info("Exit Value for Stage M3B: {}", exitValue);
					if (exitValue != 0 && status != 10)
					{
						logger.error("Stage M3B Crashed");
						Report r2 = settings.getStoredReport();
						r2.setJobFailed(true);
						r2.addEvent(17, "Manual Upscale and Retouch Failed on Stage C");
						settings.submit(r2);
						status = 1;
						JOptionPane.showMessageDialog(window,
								"Fatal Crash of Stage C\nPlease look at the Error for assistance\nThen Restart the Program", "Process Crash",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (exitValue == 0 && status == 0)
					{
						Report r2 = settings.getStoredReport();
						r2.setJobCompleted(true);
						r2.addEvent(16, "Manual Upscale and Retouch Completed");
						settings.submit(r2);
					}
					else if (exitValue != 0 && status == 10)
					{
						Report r2 = settings.getStoredReport();
						r2.setJobAborted(true);
						r2.addEvent(18, "Manual Upscale and Retouch Aborted on Stage C");
						settings.submit(r2);
					}
				}

			}
			else if (mode == 2)
			{
				stage1 = false;
				bar1.setString("N/A");
				execute.setText("Executing Stage 3");
				commandM3A = new String("");
				commandM3B = new String(settings.getRawTherapee() + " -w -o \"" + dir + File.separator + "ProcessedFrames\"" + " -a\"" + dir
						+ File.separator + "IntermediateFrames\" " + "-p \"" + dir + File.separator + "rawTherapee.pp3\" -b8 " + image3 + " -c \""
						+ dir + File.separator + "IntermediateFrames\"");
				Report r = new Report(settings.getClientID(), 12);
				r.addEvent(12, "Manual Retouch");
				settings.submit(r);
				logger.info("Stage M3B: ({})", commandM3B);
				M3B = Runtime.getRuntime().exec(commandM3B);
				StreamGobbler reader = new StreamGobbler(M3B.getInputStream(), reportArea, false);
				StreamGobbler eater = new StreamGobbler(M3B.getErrorStream(), reportArea, false);
				reader.start();
				eater.start();
				abort.setEnabled(true);
				oldCount = outputFolder.list().length;
				y = 0;
				z = 0;
				totalRate = 0;
				scheduler = Executors.newScheduledThreadPool(1);
				scheduler.scheduleAtFixedRate(new ProgressCheck(M3B, outputFolder), 0, 1, TimeUnit.SECONDS);
				scheduler.awaitTermination(10, TimeUnit.DAYS);
				int exitValue = M3B.waitFor();
				abort.setEnabled(false);
				logger.info("Exit Value for Stage M3B: {}", exitValue);
				if (exitValue != 0 && status != 10)
				{
					logger.error("Stage M3B Crashed");
					Report r2 = settings.getStoredReport();
					r2.setJobFailed(true);
					r2.addEvent(17, "Manual Retouch Failed");
					settings.submit(r2);
					status = 1;
					JOptionPane.showMessageDialog(window,
							"Fatal Crash of Stage M3B\nPlease look at the Error for assistance\nThen Restart the Program", "Process Crash",
							JOptionPane.ERROR_MESSAGE);
				}
				else if (exitValue == 0 && status == 0)
				{
					Report r2 = settings.getStoredReport();
					r2.setJobCompleted(true);
					r2.addEvent(16, "Manual Retouch Completed");
					settings.submit(r2);
				}
				else if (exitValue != 0 && status == 10)
				{
					Report r2 = settings.getStoredReport();
					r2.setJobAborted(true);
					r2.addEvent(18, "Manual Retouch Aborted");
					settings.submit(r2);
				}
			}
		}
		catch (IOException e)
		{
			logger.fatal("Process Failed");
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Process Failed\nCrashing Program", "IO Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		catch (InterruptedException i)
		{
			logger.fatal("Process Failed");
			logger.fatal("Exception Infomation", i);
			JOptionPane.showMessageDialog(null, "Process Failed\nCrashing Program", "Interrupted Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		return null;
	}

	@Override
	protected void process(List<ProgressTime> chunks)
	{
		ProgressTime i = chunks.get(chunks.size() - 1);
		int value;
		try
		{
			value = (i.getTime() * 100) / totalFrames;
			if (stage1)
			{
				bar1.setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
				bar1.setValue(i.getTime());
			}
			else
			{
				bar2.setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
				bar2.setValue(i.getTime());
			}
			if (i.getProgress() != null)
			{
				timeField.setText(i.getProgress());
			}
		}
		catch (Exception m)
		{
			logger.fatal("Exception Infomation", m);
		}
	}

	@Override
	protected void done()
	{
		window.toggleMenu(1, true);
		abort.setEnabled(false);
		if (status == 0)
		{
			logger.info("Manual Retouching Complete");
			timeField.setText("Completed");
			execute.setText("Manual Retouching Completed");
			execute.setEnabled(true);
		}
		else if (status == 10)
		{
			execute.setText("Manual Retouching Aborted");
			execute.setEnabled(true);
			bar1.setForeground(Color.BLACK);
			bar1.setIndeterminate(true);
			bar2.setForeground(Color.BLACK);
			bar2.setIndeterminate(true);
		}
		else
		{
			execute.setText("Manual Retouching Failed");
			bar1.setForeground(Color.BLACK);
			bar2.setForeground(Color.BLACK);
		}
	}

	private boolean deleteFrame(String quickPath)
	{
		if (window.canContinue(quickPath))
		{
			File f;
			if ((f = new File(dir + quickPath)).exists())
			{
				f.delete();
				logger.debug("Deleted Unsuccessful Frame: {}", quickPath);
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	class ProgressCheck implements Runnable
	{
		private Process p;
		private File output;

		public ProgressCheck(Process p, File output)
		{
			this.p = p;
			this.output = output;
		}

		@Override
		public void run()
		{
			if (isRunning(p))
			{
				y++;
				int count = output.list().length - initalCount;
				if (timeEstimate != null && !timeEstimate.equals("TOO SLOW"))
				{
					timeEstimate = timeEstimate.substring(0, 11) + String.format("%02d", (60 - y)) + timeEstimate.substring(13);
				}
				if (y == 60)
				{
					z++;
					totalRate += (count - oldCount);
					int tempRate = totalRate / z;
					int eta = (totalFrames - count) / tempRate;
					int minutesLeft = (eta % 60);
					int hoursLeft = (eta / 60);
					timeEstimate = new String("ETA:(" + String.format("%02d", hoursLeft).substring(0, 2) + ":"
							+ String.format("%02d", minutesLeft).substring(0, 2) + ".59) Rate: " + (count - oldCount) + "fpm");
					if (count - oldCount == 0)
					{
						timeEstimate = new String("TOO SLOW");
					}
					oldCount = count;
					y = 0;
				}
				publish(new ProgressTime(count, timeEstimate));
			}
			else
			{
				scheduler.shutdown();
				int lastCount = output.list().length - initalCount;
				String resultWarning;
				if(lastCount != totalFrames && status != 10)
				{
					resultWarning = new String("!!!");
					reportArea.append("Warning: Unexpected Number of Frames received from Stage M3 ");
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
							if (stage1)
							{
								bar1.setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
								bar1.setValue(lastCount);
							}
							else
							{
								bar2.setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
								bar2.setValue(lastCount);
							}
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

	public void destroy()
	{
		status = 10;
		if (M3A != null && M3A.isAlive())
		{
			M3A.destroy();
			logger.debug("Stage M3A found and Destroyed");
		}
		if (M3B != null && M3B.isAlive())
		{
			M3B.destroy();
			logger.debug("Stage M3B found and Destroyed");
		}
	}
}
