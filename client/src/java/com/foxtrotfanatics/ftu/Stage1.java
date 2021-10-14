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

public class Stage1 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private DataBlock block;
	private Process a;
	private int totalFrames;
	private Timer timer;

	private File downFolder;
	private volatile int oldCount;
	private volatile String timeEstimate;
	private volatile int y;
	private volatile int z;
	private volatile int totalRate;
	private ScheduledExecutorService scheduler;

	public Stage1(MainWindow window, DataBlock b, Timer timer)
	{
		this.window = window;
		this.timer = timer;
		this.block = b;
		totalFrames = block.getTotalFrames();
		oldCount = 0;
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		try
		{
			Thread.currentThread().setName("Stage1Thread");
			block.getOutput().append("\nStarting Frame Extraction Stage"
					+ "--------------------------------------------------------------------------------------------\n");
			block.setProgress(1);
			block.recordProgress();
			block.getBar1().setMaximum(totalFrames);
			if (!block.isRecovered())
			{
				try
				{
					FileUtils.cleanDirectory(new File(block.getDir() + File.separator + "QueuedFrames"));
					logger.info("Files Deleted: {}QueuedFrames", File.separator);
				}
				catch (IOException p)
				{
					logger.fatal("Failed Delete Attempt: {}QueuedFrames", File.separator);
					logger.fatal("Exception Infomation", p);
					JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + "QueuedFrames\nCrashing Program",
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
			r.addEvent(5, "Stage A Started");
			r.setCurrentStage(1);
			block.getSettings().submit(r);
			boolean processResume = true;
			while (processResume)
			{
				processResume = false;
				scheduler = Executors.newScheduledThreadPool(1);
				logger.info("Stage A: ({})", block.getCommand1());
				a = Runtime.getRuntime().exec(block.getCommand1());
				block.setA(a);
				StreamGobbler reader = new StreamGobbler(a.getInputStream(), block.getError(), false);
				StreamGobbler eater = new StreamGobbler(a.getErrorStream(), block.getOutput(), false);
				reader.start();
				eater.start();
				block.getPause().setEnabled(true);
				block.getAbort().setEnabled(true);
				if (block.isQueue())
				{
					block.getFullAbort().setEnabled(true);
				}
				downFolder = new File(block.getDir() + File.separator + "QueuedFrames" + File.separator);
				oldCount = 0;
				y = 0;
				z = 0;
				totalRate = 0;
				timeEstimate = null;
				scheduler.scheduleAtFixedRate(new ProgressCheck(), 1000, 500, TimeUnit.MILLISECONDS);
				scheduler.awaitTermination(100, TimeUnit.DAYS);
				int exitValue = a.waitFor();
				logger.info("Exit Value for Stage A: {}", exitValue);
				if (exitValue != 0 && block.getStatus() != 10 && block.getStatus() != 20 && block.getStatus() != 30)
				{
					block.setStatus(1);
					Report r3 = block.getSettings().getStoredReport();
					r3.setJobFailed(true);
					r3.setJobCompleted(false);
					r3.setJobStarted(true);
					r3.setOperating(false);
					r3.setJobAborted(false);
					r3.addEvent(50, "Process Errored at Stage A");
					block.getSettings().submit(r3);
					block.getPause().setEnabled(false);
					block.getAbort().setEnabled(false);
					block.getFullAbort().setEnabled(false);
					window.toggleMenu(0, true);
					logger.error("Stage A Crashed");
					JOptionPane.showMessageDialog(window, "Fatal Crash of Stage 1\nPlease look at the Error for assistance\nThen Restart the Program",
							"Process Crash", JOptionPane.ERROR_MESSAGE);
				}
				if (block.getStatus() == 30)
				{
					processResume = true;
					Report r4 = block.getSettings().getStoredReport();
					r4.setOperating(false);
					r4.setPaused(true);
					r4.addEvent(30, "Process Paused at Stage A");
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
					r6.setStatus(6);
					r6.addEvent(6, "Stage A Completed");
					block.getSettings().submit(r6);
					block.getPause().setEnabled(false);
					block.getAbort().setEnabled(false);
					block.getFullAbort().setEnabled(false);
				}
			}
		}
		catch (IOException e)
		{
			logger.fatal("Failed Delete Attempt: {}QueuedFrames", File.separator);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + "QueuedFrames\nCrashing Program", "IO Exception",
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
		block.getBar1().setString("(" + i.getTime() + "/" + totalFrames + ") - " + value + "%");
		block.getBar1().setValue(i.getTime());
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
			logger.info("Stage A Completed");
			block.getTime().setText("Calculating");
			block.getExecute().setText("Executing Stage 2");
			Stage2 stage2 = new Stage2(window, block, timer);
			stage2.execute();
		}
		if (block.getStatus() == 10 || block.getStatus() == 20)
		{
			if (!block.isQueue())
			{
				block.getExecute().setText("Execution Aborted");
				block.getBar2().setIndeterminate(true);
				if (block.isUseRaw())
				{
					block.getBar3().setIndeterminate(true);
				}
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
					window.toggleMenu(0, true);
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
			if (isRunning(a))
			{
				y++;
				int count = downFolder.list().length;
				Report r2 = block.getSettings().getStoredReport();
				r2.setStatus(0);
				r2.setCurrentFrame(count);
				if (timeEstimate != null && !timeEstimate.equals("TOO SLOW"))
				{
					timeEstimate = timeEstimate.substring(0, 11) + String.format("%02d", (60 - (y / 2))) + timeEstimate.substring(13);
				}
				if (y == 120)
				{
					z++;
					totalRate += (count - oldCount); // rates all added
													// together
					int tempRate = totalRate / z; // averaged rate
					int eta = (totalFrames - count) / tempRate; // frames left/rate to get minutes left
					int minutesLeft = (eta % 60); // get mod of minutes left
					int hoursLeft = (eta / 60); // get hours left
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
				int lastCount = downFolder.list().length;
				String resultWarning;
				if(lastCount != totalFrames && block.getStatus() != 30)
				{
					resultWarning = new String("!!!");
					block.getError().append("Warning: Unexpected Number of Frames received from Stage 1 ");
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
							block.getBar1().setString("(" + lastCount + "/" + totalFrames + ") - " + value + "%" + resultWarning);
							block.getBar1().setValue(lastCount);
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