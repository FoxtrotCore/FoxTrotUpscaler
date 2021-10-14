package com.foxtrotfanatics.ftu;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

public class Stage3 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private DataBlock block;
	private Process c;
	private int totalFrames;
	private Timer timer;

	private File resultFolder;
	private volatile int oldCount;
	private volatile String timeEstimate;
	private volatile int y;
	private volatile int z;
	private volatile int totalRate;
	private ScheduledExecutorService scheduler;

	public Stage3(MainWindow window, DataBlock c, Timer timer)
	{
		this.window = window;
		this.timer = timer;
		this.block = c;
		totalFrames = block.getTotalFrames();
		oldCount = 0;
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		Thread.currentThread().setName("Stage3Thread");
		block.setProgress(3);
		block.recordProgress();
		block.getBar3().setMaximum(totalFrames);
		try
		{
			block.getOutput().append("\nStarting RawTherapee Touchup Stage"
					+ "--------------------------------------------------------------------------------------------\n");
			if (!block.isRecovered())
			{
				try
				{
					FileUtils.cleanDirectory(new File(block.getDir() + File.separator + "ProcessedFrames"));
					logger.info("Files Deleted: {}ProcessedFrames", File.separator);
				}
				catch (IOException p)
				{
					logger.fatal("Failed Delete Attempt: {}ProcessedFrames", File.separator);
					logger.fatal("Exception Infomation", p);
					JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + "ProcessedFrames\nCrashing Program",
							"IO Exception", JOptionPane.ERROR_MESSAGE);
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
			r.addEvent(5, "Stage C Started");
			r.setCurrentStage(3);
			block.getSettings().submit(r);
			boolean processResume = true;
			while (processResume)
			{
				processResume = false;
				scheduler = Executors.newScheduledThreadPool(1);
				logger.info("Stage C: ({})", block.getCommand3());
				c = Runtime.getRuntime().exec(block.getCommand3());
				block.setC(c);
				StreamGobbler reader = new StreamGobbler(c.getInputStream(), block.getOutput(), false);
				StreamGobbler eater = new StreamGobbler(c.getErrorStream(), block.getError(), false);
				reader.start();
				eater.start();
				block.getPause().setEnabled(true);
				block.getAbort().setEnabled(true);
				if (block.isQueue())
				{
					block.getFullAbort().setEnabled(true);
				}
				resultFolder = new File(block.getDir() + File.separator + "ProcessedFrames");
				oldCount = resultFolder.list().length;
				y = 0;
				z = 0;
				totalRate = 0;
				timeEstimate = null;
				scheduler.scheduleAtFixedRate(new ProgressCheck(), 1, 1, TimeUnit.SECONDS);
				scheduler.awaitTermination(100, TimeUnit.DAYS);
				int exitValue = c.waitFor();
				logger.info("Exit Value for Stage C: " + exitValue);
				if (exitValue != 0 && block.getStatus() != 10 && block.getStatus() != 20 && block.getStatus() != 30)
				{
					logger.error("Stage C Crashed");
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
					r3.addEvent(50, "Process Errored at Stage C");
					block.getSettings().submit(r3);
					window.toggleMenu(0, true);
					JOptionPane.showMessageDialog(window, "Fatal Crash of Stage 3\nPlease look at the Error for assistance\nThen Restart the Program",
							"Process Crash", JOptionPane.ERROR_MESSAGE);
				}
				if (block.getStatus() == 30)
				{
					processResume = true;
					Report r4 = block.getSettings().getStoredReport();
					r4.setOperating(false);
					r4.setPaused(true);
					r4.addEvent(30, "Process Paused at Stage C");
					block.getSettings().submit(r4);
					block.setPaused(true);
					block.recordProgress();
					block.getPauseLatch().await();
					block.setPaused(false);
					Report r5 = block.getSettings().getStoredReport();
					r5.setOperating(true);
					r5.setPaused(false);
					r5.addEvent(32, "Process Resumed");
					block.getSettings().submit(r5);
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
					Report r6 = block.getSettings().getStoredReport();
					r6.addEvent(6, "Stage C Completed");
					block.getSettings().submit(r6);
					block.getPause().setEnabled(false);
					block.getAbort().setEnabled(false);
					block.getFullAbort().setEnabled(false);
				}
			}
		}
		catch (IOException e)
		{
			logger.fatal("Failed Delete Attempt: {}ProcessedFrames", File.separator);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		return null;
	}

	@Override
	protected void process(List<ProgressTime> chunks)
	{
		ProgressTime i = chunks.get(chunks.size() - 1);
		int value = (i.getTime() * 100) / totalFrames;
		block.getBar3().setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
		block.getBar3().setValue(i.getTime());
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
			logger.info("Stage C Completed");
			block.getTime().setText("Calculating");
			block.getExecute().setText("Executing Stage 3");
			Stage4 stage4 = new Stage4(window, block, timer);
			stage4.execute();
		}
		if (block.getStatus() == 10 || block.getStatus() == 20)
		{
			if (!block.isQueue())
			{
				block.getExecute().setText("Execution Aborted");
				block.getBar4().setIndeterminate(true);
				block.getBar1().setForeground(Color.black);
				block.getBar2().setForeground(Color.black);
				block.getBar3().setForeground(Color.black);
				block.getBar4().setForeground(Color.black);
				block.getTime().setText("Complete");
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
			if (isRunning(c))
			{
				y++;
				int count = resultFolder.list().length;
				Report r2 = block.getSettings().getStoredReport();
				r2.setStatus(0);
				r2.setCurrentFrame(count);
				if (timeEstimate != null && !timeEstimate.equals("TOO SLOW"))
				{
					timeEstimate = timeEstimate.substring(0, 11) + String.format("%02d", (60 - y)) + timeEstimate.substring(13);
				}
				if (y == 60)
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
				publish(new ProgressTime(count, timeEstimate));
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
					block.getError().append("Warning: Unexpected Number of Frames received from Stage 3 ");
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
							block.getBar3().setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
							block.getBar3().setValue(lastCount);
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