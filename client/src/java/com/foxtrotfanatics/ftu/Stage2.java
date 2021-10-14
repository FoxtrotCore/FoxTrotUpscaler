package com.foxtrotfanatics.ftu;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.graphical.MainWindow;
import com.foxtrotfanatics.ftu.structures.DataBlock;
import com.foxtrotfanatics.ftu.structures.Report;

public class Stage2 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private DataBlock block;
	private Process b;
	private int totalFrames;
	private Timer timer;

	private File resultFolder;
	private volatile int oldCount;
	private volatile String timeEstimate;
	private volatile int y;
	private volatile int z;
	private volatile int totalRate;
	private ScheduledExecutorService scheduler;

	public Stage2(MainWindow window, DataBlock block, Timer timer)
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
		Thread.currentThread().setName("Stage2Thread");
		block.setProgress(2);
		block.recordProgress();
		block.getBar2().setMaximum(totalFrames);
		boolean waifu2xFault = true;
		String folderLabel;
		if (block.isUseRaw())
		{
			folderLabel = new String("IntermediateFrames");
		}
		else
		{
			folderLabel = new String("ProcessedFrames");
		}
		block.getOutput().append(
				"\nStarting Upscale Stage" + "--------------------------------------------------------------------------------------------\n");
		if (!block.isRecovered())
		{
			try
			{
				FileUtils.cleanDirectory(new File(block.getDir() + File.separator + folderLabel));
				logger.info("Files Deleted: {}{}", File.separator, folderLabel);
			}
			catch (IOException p)
			{
				logger.fatal("Failed Delete Attempt: {}{}", File.separator, folderLabel);
				logger.fatal("Exception Infomation", p);
				JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + folderLabel + "\nCrashing Program", "IO Exception",
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
		r.addEvent(5, "Stage B Started");
		r.setCurrentStage(2);
		block.getSettings().submit(r);
		boolean processResume = true;
		while (processResume)
		{
			processResume = false;
			waifu2xFault = true;
			int interval = 0;
			while (waifu2xFault)
			{
				try
				{
					interval++;
					logger.info("Attempting Run {} of Waifu2x", interval);
					scheduler = Executors.newScheduledThreadPool(1);
					logger.info("Stage B: (" + block.getCommand2() + ")");
					b = Runtime.getRuntime().exec(block.getCommand2());
					block.setB(b);
					StreamGobbler reader = new StreamGobbler(b.getInputStream(), block.getError(), true);
					StreamGobbler eater = new StreamGobbler(b.getErrorStream(), block.getOutput(), true);
					reader.start();
					eater.start();
					resultFolder = new File(block.getDir() + File.separator + folderLabel);
					oldCount = resultFolder.list().length;
					block.getPause().setEnabled(true);
					block.getAbort().setEnabled(true);
					if (block.isQueue())
					{
						block.getFullAbort().setEnabled(true);
					}
					y = 0;
					z = 0;
					totalRate = 0;
					timeEstimate = null;
					scheduler.scheduleAtFixedRate(new ProgressCheck(), 1, 1, TimeUnit.SECONDS);
					scheduler.awaitTermination(100, TimeUnit.DAYS);
					int exitValue = b.waitFor();
					logger.info("Exit Value for Stage B: " + exitValue);
					if (resultFolder.list().length >= 4 && exitValue != 0 && block.getStatus() == 0)
					{
						logger.error("Waifu2x-caffe Crashed with Progress Made");
						Report r3 = block.getSettings().getStoredReport();
						r3.addEvent(8, "Waifu2x crashed, but recoverable");
						r3.setEta("Reset");
						block.getSettings().submit(r3);
						block.getPause().setEnabled(false);
						block.getAbort().setEnabled(false);
						block.getFullAbort().setEnabled(false);
						block.getError().append(
								"\nWaifu2x Crashed, Restarting Stage, if this occurs often, possibly scale down your speed settings in \"config.txt\"--------------------------------------------------------------------------------------------\n");
						int finalframe = resultFolder.list().length - 3;
						for (int x = 1; x <= finalframe; x++)
						{
							String quickPath = new String(File.separator + "QueuedFrames" + File.separator + "OEP" + String.format("%06d", x) + "."
									+ block.getSettings().getImage());
							try
							{
								Files.deleteIfExists(Paths.get(block.getDir() + quickPath));
								logger.info("Frame Deleted: {}", quickPath);
							}
							catch (NoSuchFileException p)
							{
								logger.error("Failed to Delete Frame\n" + quickPath + "\nContinuing...");
							}
						}
						block.getError().append("Waifu2x CleanUp Completed, Restarting Stage");
					}
					else
					{
						waifu2xFault = false;
					}
					if (exitValue != 0 && block.getStatus() != 10 && block.getStatus() != 20 && block.getStatus() != 30 && !waifu2xFault)
					{
						logger.error("Stage B Crashed");
						block.getPause().setEnabled(false);
						block.getAbort().setEnabled(false);
						block.getFullAbort().setEnabled(false);
						block.setStatus(1);
						Report r4 = block.getSettings().getStoredReport();
						r4.setJobFailed(true);
						r4.setJobCompleted(false);
						r4.setJobStarted(true);
						r4.setOperating(false);
						r4.setJobAborted(false);
						r4.addEvent(50, "Process Errored at Stage B");
						block.getSettings().submit(r4);
						window.toggleMenu(0, true);
						JOptionPane.showMessageDialog(window,
								"Fatal Crash of Stage 2\nPlease look at the Error for assistance\nThen Restart the Program", "Process Crash",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (block.getStatus() == 30)
					{
						processResume = true;
						Report r5 = block.getSettings().getStoredReport();
						r5.setOperating(false);
						r5.setPaused(true);
						r5.addEvent(30, "Process Paused at Stage A");
						block.getSettings().submit(r5);
						block.setPaused(true);
						block.recordProgress();
						block.getPauseLatch().await();
						block.setPaused(false);
						Report r6 = block.getSettings().getStoredReport();
						r6.setOperating(true);
						r6.setPaused(false);
						r6.addEvent(32, "Process Resumed");
						block.getSettings().submit(r6);
						logger.debug("Latch Removed, Restarting");
					}
					else if (block.getStatus() == 10)
					{
						Report r8 = block.getSettings().getStoredReport();
						r8.setJobFailed(false);
						r8.setJobCompleted(false);
						r8.setJobStarted(true);
						r8.setOperating(false);
						r8.setJobAborted(true);
						r8.addEvent(20, "Process Aborted");
						block.getSettings().submit(r8);
					}
					else
					{
						Report r6 = block.getSettings().getStoredReport();
						r6.addEvent(6, "Stage B Completed");
						block.getSettings().submit(r6);
						block.getPause().setEnabled(false);
						block.getAbort().setEnabled(false);
						block.getFullAbort().setEnabled(false);
					}
				}
				catch (IOException e)
				{
					logger.fatal("Failed Delete Attempt: {}", File.separator, folderLabel);
					logger.fatal("Exception Infomation", e);
					JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + folderLabel + "\nCrashing Program",
							"IO Exception", JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
		}
		return null;
	}

	@Override
	protected void process(List<ProgressTime> chunks)
	{
		ProgressTime i = chunks.get(chunks.size() - 1);
		int value = (i.getTime() * 100) / totalFrames;
		block.getBar2().setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
		block.getBar2().setValue(i.getTime());
		if (timeEstimate != null)
		{
			block.getTime().setText(i.getProgress());
		}
	}

	@Override
	protected void done()
	{
		if (block.getStatus() == 0)
		{
			logger.info("Stage B Completed");
			block.getTime().setText("Calculating");
			block.getExecute().setText("Executing Stage 3");
			if (block.isUseRaw())
			{
				Stage3 stage3 = new Stage3(window, block, timer);
				stage3.execute();
			}
			else
			{
				Stage4 stage4 = new Stage4(window, block, timer);
				stage4.execute();
			}
		}
		if (block.getStatus() == 10 || block.getStatus() == 20)
		{
			if (!block.isQueue())
			{
				block.getExecute().setText("Execution Aborted");
				if (block.isUseRaw())
				{
					block.getBar3().setIndeterminate(true);
				}
				block.getBar4().setIndeterminate(true);
				block.getBar1().setForeground(Color.black);
				block.getBar2().setForeground(Color.black);
				block.getBar3().setForeground(Color.black);
				block.getBar4().setForeground(Color.black);
				window.toggleMenu(0, true);
				block.getTime().setText("Complete");
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
			if (block.isQueue())
			{
				block.getSelfLatch().countDown();
			}
		}
	}

	class ProgressCheck implements Runnable
	{

		@Override
		public void run()
		{
			if (isRunning(b))
			{
				y++;
				int count = resultFolder.list().length; // Get number of frames produced
				Report r2 = block.getSettings().getStoredReport();
				r2.setStatus(0);
				r2.setCurrentFrame(count);
				if (timeEstimate != null && !timeEstimate.equals("TOO SLOW"))
				{
					timeEstimate = timeEstimate.substring(0, 11) + String.format("%02d", (60 - y)) + timeEstimate.substring(13);
				}
				if (y == 60) // Only execute every minute based on Scheduler
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
					oldCount = count; // Sets up next Calculation
					y = 0;
					r2.setEta(timeEstimate);
				}
				block.getSettings().submit(r2);
				publish(new ProgressTime(count, timeEstimate)); // Update either ProgressBar and/or ETA
			}
			else
			{
				scheduler.shutdown();
				//One last CountDown after scheduled completion
				int lastCount = resultFolder.list().length;
				String resultWarning;
				if(lastCount != totalFrames && block.getStatus() != 30)
				{
					resultWarning = new String("!!!");
					block.getError().append("Warning: Unexpected Number of Frames received from Stage 2 ");
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
							block.getBar2().setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
							block.getBar2().setValue(lastCount);
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
}