package com.foxtrotfanatics.ftu;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.graphical.MainWindow;
import com.foxtrotfanatics.ftu.structures.DataBlock;
import com.foxtrotfanatics.ftu.structures.Report;

public class QueueStarter extends Thread//Create String Arrays for Commands instead of single String.
{

	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private Settings settings;
	private JProgressBar bar1;
	private JProgressBar bar2;
	private JProgressBar bar3;
	private JProgressBar bar4;
	private JProgressBar bar5;
	private JButton execute;
	private JButton pause;
	private JButton abort;
	private JButton fullAbort;
	private JTextField time;
	private JTextArea output;
	private JTextArea error;
	private String dir;
	private int totalProcessCount;
	private DataBlock block;
	private boolean continueProcess;
	private Timer timer;

	public QueueStarter(MainWindow window,
			Timer timer,
			Settings settings,
			String directory,
			JProgressBar bar1,
			JProgressBar bar2,
			JProgressBar bar3,
			JProgressBar bar4,
			JProgressBar bar5,
			JButton execute,
			JButton pause,
			JButton abort,
			JButton fullAbort,
			JTextField time,
			JTextArea output,
			JTextArea error)
	{
		this.window = window;
		this.timer = timer;
		dir = directory;
		this.settings = settings;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.bar3 = bar3;
		this.bar4 = bar4;
		this.bar5 = bar5;
		this.execute = execute;
		this.pause = pause;
		this.abort = abort;
		this.fullAbort = fullAbort;
		this.time = time;
		this.output = output;
		this.error = error;
		this.continueProcess = true;
	}

	@Override
	public void run()
	{
		int completedCount = 0;
		int errorCount = 0;
		int abortCount = 0;
		// Check number of Queue files. 0 or 999+ throws error
		totalProcessCount = new File(dir + File.separator + "Queue").list().length;
		bar5.setMaximum(totalProcessCount);
		bar5.setString(0 + "/" + totalProcessCount);
		if (totalProcessCount <= 0)
		{
			logger.error("Empty Queue, Ignoring Input");
			JOptionPane.showMessageDialog(null, "No Files in the Queue", "Input Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (totalProcessCount >= 1000)
		{
			logger.error("Queue Larger then 999 Files");
			JOptionPane.showMessageDialog(null, "Too many files in the Queue\nLimit is 999", "Program Limitation", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ArrayList<DataBlock> blocks = new ArrayList<DataBlock>();
		for (int x = 1; x <= totalProcessCount; x++)
		{
			String queueName = new String("Q" + String.format("%03d", x) + ".ser");
			String quickPath = new String(File.separator + "Queue" + File.separator + queueName);
			String filePath = new String(dir + quickPath);
			try (FileInputStream fileIn = new FileInputStream(filePath); ObjectInputStream in = new ObjectInputStream(fileIn);)
			{
				DataBlock tempBlock = (DataBlock) in.readObject();
				tempBlock.setDir(dir);
				tempBlock.setSettings(settings);
				tempBlock.init();
				blocks.add(tempBlock);
				logger.info("File parsed for Overwrite Verification and Report submission: {}", quickPath);
				if (!window.canContinue(File.separator + "Export" + File.separator + "Upscaled_" + tempBlock.getAudioName() + "_" + tempBlock.getEep()
						+ "_" + tempBlock.getShowName() + ".mkv"))
				{
					abortCount++;
					continueProcess = false;
				}
			}
			catch (ClassNotFoundException e)
			{
				logger.fatal("Process File Corrupted when already Verified?: {}", quickPath);
				System.exit(20);
			}
			catch (IOException i)
			{
				logger.fatal("Failed Parse Attempt: {}", quickPath);
				logger.fatal("Exception Infomation", i);
				JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
						JOptionPane.ERROR_MESSAGE);
				System.exit(20);
			}
		}
		if (continueProcess)
		{
			settings.submit(new Report(settings.getClientID(), 11)); //Declare this is a Queue
			for (int x = 0; x < blocks.size(); x++)
			{
				settings.submit(new Report(settings.getClientID(), 57, blocks.get(x), false));//Send Queue one by one over
			}
			settings.submit(new Report(settings.getClientID(), 15));//Declare end of Queue
		}
		for (int x = 1; x <= totalProcessCount; x++) // Beginning of loop for every Queue File
		{
			if (continueProcess)
			{
				// All Queue Files named "Q###.set" and incrementing.
				String queueName = new String("Q" + String.format("%03d", x) + ".ser");
				// Gets Path of File
				String quickPath = new String(File.separator + "Queue" + File.separator + queueName);
				String filePath = new String(dir + quickPath);
				// Attempts to Grab Serialized Object, and fills in all other
				// variables from Specific GUI instance, and File Locations.
				try (FileInputStream fileIn = new FileInputStream(filePath); ObjectInputStream in = new ObjectInputStream(fileIn);)
				{
					block = (DataBlock) in.readObject();
					block.setDir(dir);
					block.setSettings(settings);
					block.setBar1(bar1);
					bar1.setValue(0);
					bar1.setString("0%");
					block.setBar2(bar2);
					bar2.setValue(0);
					bar2.setString("0%");
					block.setBar3(bar3);
					bar3.setValue(0);
					bar3.setString("0%");
					block.setBar4(bar4);
					bar4.setValue(0);
					bar4.setString("0%");
					block.setBar5(bar5);
					block.setExecute(execute);
					block.setPause(pause);
					block.setAbort(abort);
					block.setFullAbort(fullAbort);
					block.setTime(time);
					block.setOutput(output);
					block.setError(error);
					block.setSelfLatch(new CountDownLatch(1));
					// init() takes all variables to create batch commands as
					// strings
					block.init();
					logger.info("File parsed: {}", quickPath);
				}
				catch (ClassNotFoundException | InvalidClassException | StreamCorruptedException | OptionalDataException b)
				{
					logger.error("Queue File Corrupted: {} - Deleting File", quickPath);
					block = new DataBlock();
					errorCount++;
					logger.fatal("Exception Infomation", b);
					JOptionPane.showMessageDialog(null,
							"Program Failed to interpret File:\n" + quickPath
									+ "\nIs this a actual Queue File?\nHas this been tampered with?\nNow Deleting...",
							"Corrupted File", JOptionPane.ERROR_MESSAGE);
					try
					{
						Files.deleteIfExists(new File(dir + quickPath).toPath());
					}
					catch (IOException e)
					{
						logger.fatal("Failed Delete Attempt: {}", quickPath);
						logger.fatal("Exception Infomation", e);
						JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
					block = new DataBlock();
					errorCount++;
				}
				catch (IOException i)
				{
					block = new DataBlock();
					logger.fatal("Failed Parse Attempt: {}", quickPath);
					logger.fatal("Exception Infomation", i);
					errorCount++;
					JOptionPane.showMessageDialog(window, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
				// Resets GUI and starts Process on selected Stage
				if (block.getStatus() == 0)
				{
					Report r = new Report(settings.getClientID(), 14, block, true);
					r.setOperating(true);
					r.setJobStarted(true);
					r.setCurrentFrame(0);
					r.setPaused(false);
					r.setEta("Calculating");
					r.addEvent(14, "Process Started");
					settings.submit(r);
					time.setEnabled(true);
					bar1.setIndeterminate(false);
					bar2.setIndeterminate(false);
					bar3.setIndeterminate(false);
					bar4.setIndeterminate(false);
					bar5.setIndeterminate(false);
					time.setText("Calculating");
					if (x == 1)
					{
						timer.start();
					}
					else
					{
						timer.restart();
					}
					if (block.isRadio1())
					{
						logger.info("Executing Queue {} of {} at Part 1", x, totalProcessCount);
						execute.setText("Executing Stage 1");
						Stage1 stage1 = new Stage1(window, block, timer);
						stage1.execute();
					}
					else if (block.isRadio2())
					{
						logger.info("Executing Queue {} of {} at Part 2", x, totalProcessCount);
						execute.setText("Executing Stage 2");
						Stage2 stage2 = new Stage2(window, block, timer);
						stage2.execute();
					}
					else if (block.isRadio3())
					{
						logger.info("Executing Queue {} of {} at Part 3", x, totalProcessCount);
						execute.setText("Executing Stage 3");
						Stage3 stage3 = new Stage3(window, block, timer);
						stage3.execute();
					}
					else if (block.isRadio4())
					{
						logger.info("Executing Queue {} of {} at Part 4", x, totalProcessCount);
						execute.setText("Executing Stage 4");
						Stage4 stage4 = new Stage4(window, block, timer);
						stage4.execute();
					}
					try
					{
						block.getSelfLatch().await(); // Will not Continue until Latch removed
						logger.trace("QueueStarter Latched Removed for: {}", quickPath);
					}
					catch (InterruptedException e1)
					{
						logger.fatal("Thread Interupted");
						logger.fatal("Exception Infomation", e1);
						JOptionPane.showMessageDialog(null, "Thread Interupted\nCrashing Program", "Program Interrupted", JOptionPane.ERROR_MESSAGE);
						System.exit(30);
					}
					if (block.getStatus() == 10 || block.getStatus() == 20)
					{
						abortCount++; // Keeps track of Aborted Upscales.
					}
					else if (block.getStatus() != 0)
					{
						errorCount++; // Keeps track Errored upscales.
					}
					else
					{
						try
						{
							Files.delete(Paths.get(block.getDir() + quickPath));
							logger.debug("File Deleted: {}", quickPath);
							completedCount++;
						}
						catch (IOException e)
						{
							logger.fatal("Failed Delete Attempt: {}", quickPath);
							logger.fatal("Exception Infomation", e);
							JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program", "IO Exception",
									JOptionPane.ERROR_MESSAGE);
							System.exit(20);
						}
					}
				}
				else
				{
					errorCount++;
				}
				bar5.setValue(x);
				bar5.setString(x + "/" + totalProcessCount);
			}
		}
		//Queue Loop Completed
		if (!continueProcess)
		{
			logger.info("Queue Ended Prematurely");
			pause.setEnabled(false);
			abort.setEnabled(false);
			fullAbort.setEnabled(false);
			execute.setEnabled(true);
			bar1.setIndeterminate(true);
			bar2.setIndeterminate(true);
			bar3.setIndeterminate(true);
			bar4.setIndeterminate(true);
			bar5.setIndeterminate(true);
			bar1.setForeground(Color.BLACK);
			bar2.setForeground(Color.BLACK);
			bar3.setForeground(Color.BLACK);
			bar4.setForeground(Color.BLACK);
			bar5.setForeground(Color.BLACK);
			window.toggleMenu(0, true);
			settings.submit(new Report(settings.getClientID(), 21));
			window.getUpscalePanel().updateQueue();
			logger.info("Queue Report> Completed: {} - Errored: {} - Aborted: {} - Unattempted: {}", completedCount, errorCount, abortCount,
					(totalProcessCount - (errorCount + abortCount + completedCount)));
			JOptionPane.showMessageDialog(null,
					"Queue Completed\nCompleted Processes : " + completedCount + "\nProblematic Processes: " + errorCount + "\nAborted Processes: "
							+ abortCount + "\nUnattempted Processes: " + (totalProcessCount - (errorCount + abortCount + completedCount))
							+ "\nErrored Queues still stored in " + File.separator + "Queue",
					"Reached end of Queue", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			logger.info("Reached End of Queue");
			pause.setEnabled(false);
			abort.setEnabled(false);
			fullAbort.setEnabled(false);
			execute.setEnabled(true);
			bar1.setIndeterminate(true);
			bar2.setIndeterminate(true);
			bar3.setIndeterminate(true);
			bar4.setIndeterminate(true);
			window.toggleMenu(0, true);
			window.getUpscalePanel().updateQueue();
			if (settings.isShutdown())
			{
				window.shutdown();
			}
			if (errorCount == 0 && abortCount == 0)
			{
				bar1.setForeground(Color.WHITE);
				bar2.setForeground(Color.WHITE);
				bar3.setForeground(Color.WHITE);
				bar4.setForeground(Color.WHITE);
				bar5.setForeground(Color.WHITE);
			}
			else
			{
				bar1.setForeground(Color.GRAY);
				bar2.setForeground(Color.GRAY);
				bar3.setForeground(Color.GRAY);
				bar4.setForeground(Color.GRAY);
				bar5.setForeground(Color.GRAY);
			}
			logger.info("Queue Report> Completed: {} - Errored: {} - Aborted: {}", completedCount, errorCount, abortCount);
			JOptionPane.showMessageDialog(null,
					"Queue Completed\nCompleted Processes : " + completedCount + "\nProblematic Processes: " + errorCount + "\nAborted Processes: "
							+ abortCount + "\nErrored Queues still stored in " + File.separator + "Queue",
					"Reached end of Queue", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void destroySingle() // Cancels current video and continues
	{
		block.setStatus(10);
		if (block != null && block.getA() != null && block.getA().isAlive())
		{
			block.getA().destroy();
			logger.debug("Stage A found and Destroyed");
		}
		if (block != null && block.getB() != null && block.getB().isAlive())
		{
			block.getB().destroy();
			logger.debug("Stage B found and Destroyed");
		}
		if (block != null && block.getC() != null && block.getC().isAlive())
		{
			block.getC().destroy();
			logger.debug("Stage C found and Destroyed");
		}
		if (block != null && block.getD() != null && block.getD().isAlive())
		{
			block.getD().destroy();
			logger.debug("Stage D found and Destroyed");
		}
	}

	public void pauseProcess(boolean confirmDirectly)
	{
		logger.info("Queue Pause Requested");
		if (confirmDirectly || JOptionPane.showConfirmDialog(null, "Are you sure you wish to Pause?\nAll progress on Stage 1 and 4 will be Deleted", "Pause Queue?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
		{
			logger.info("Queue Pause Requested");
			block.setStatus(30);
			CountDownLatch pauseLatch = new CountDownLatch(1);
			block.setPauseLatch(pauseLatch);
			int currentProcess = 0;
			if (block != null && block.getA() != null && block.getA().isAlive())
			{
				block.getA().destroy();
				logger.debug("Stage A found and Destroyed");
				currentProcess = 1;
			}
			if (block != null && block.getB() != null && block.getB().isAlive())
			{
				block.getB().destroy();
				logger.debug("Stage B found and Destroyed");
				currentProcess = 2;
			}
			if (block != null && block.getC() != null && block.getC().isAlive())
			{
				block.getC().destroy();
				logger.debug("Stage C found and Destroyed");
				currentProcess = 3;
			}
			if (block != null && block.getD() != null && block.getD().isAlive())
			{
				block.getD().destroy();
				logger.debug("Stage D found and Destroyed");
				currentProcess = 4;
			}
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e1)
			{
				logger.fatal("Exception Infomation", e1);
			}
			if (currentProcess == 1)
			{
				try
				{
					FileUtils.cleanDirectory(new File(block.getDir() + File.separator + "QueuedFrames"));
					logger.info("Files Deleted: {}QueuedFrames", File.separator);
				}
				catch (IOException e)
				{
					logger.fatal("Failed Delete Attempt: {}QueuedFrames", File.separator);
					logger.fatal("Exception Infomation", e);
					JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + File.separator + "QueuedFrames\nCrashing Program",
							"IO Exception", JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
			else if (currentProcess == 2)
			{
				String folderLabel;
				if (block.isUseRaw())
				{
					folderLabel = new String("IntermediateFrames");
				}
				else
				{
					folderLabel = new String("ProcessedFrames");
				}
				File resultFolder = new File(block.getDir() + File.separator + folderLabel);
				int finalframe = resultFolder.list().length - 3;
				if (finalframe < 0)
				{
					finalframe = 0;
				}
				for (int x = 1; x <= finalframe; x++)
				{
					String quickPart = new String(
							File.separator + "QueuedFrames" + File.separator + "OEP" + String.format("%06d", x) + "." + settings.getImage());
					try
					{
						String framePath = new String(block.getDir() + quickPart);
						Files.deleteIfExists(Paths.get(framePath));
						logger.info("Frames Deleted: {}", quickPart);
					}
					catch (IOException e)
					{
						logger.fatal("Failed Delete Attempt: {}", quickPart);
						logger.fatal("Exception Infomation", e);
						JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
				}
			}
			else if (currentProcess == 3)
			{
				File resultFolder = new File(block.getDir() + File.separator + "ProcessedFrames");
				int finalframe = resultFolder.list().length;
				int startframe = finalframe - 5;
				if (startframe < 0)
				{
					startframe = 0;
				}
				for (int x = startframe; x <= finalframe; x++)
				{
					String quickPart = new String(
							File.separator + "ProcessedFrames" + File.separator + "OEP" + String.format("%06d", x) + "." + settings.getImage());
					try
					{
						String framePath = new String(block.getDir() + quickPart);
						Files.deleteIfExists(Paths.get(framePath));
						logger.info("Frames Deleted: {}", quickPart);
					}
					catch (IOException p)
					{
						logger.fatal("Failed Delete Attempt: {}", quickPart);
						logger.fatal("Exception Infomation", p);
						JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
				}
			}
			else if (currentProcess == 4)
			{
				String quickPart = new String(File.separator + "Export" + File.separator + block.getEep() + "_" + block.getShowName() + ".mkv");
				try
				{
					Files.deleteIfExists(Paths.get(block.getDir() + quickPart));
					logger.info("File Deleted: {}", quickPart);
				}
				catch (IOException e)
				{
					logger.fatal("Failed Delete Attempt: {}", quickPart);
					logger.fatal("Exception Infomation", e);
					JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
			block.getBar1().setForeground(Color.RED);
			block.getBar2().setForeground(Color.RED);
			block.getBar3().setForeground(Color.RED);
			block.getBar4().setForeground(Color.RED);
			block.getBar5().setForeground(Color.RED);
			block.getPause().setText("Resume");
			block.getAbort().setEnabled(false);
			block.getFullAbort().setEnabled(false);
			logger.debug("Pause Completed. Waiting for Resume or Shutdown");
		}
		// Part 1 & 4, warn you lose progress, and if confirmed, kill process -
		// Then Delete video in /Export, or wipe frames in /QueuedFrames
		// Part 2 & 3, kill Process, and delete all frames that are already
		// processed
		// put a latch on which ever sequence, while inside a while loop, so
		// when latch breaks, program starts from beginning
		// depending on which part of the program you are on, serialize Count
		// Make sure UI updates
		// set colors to red
		// pause == Resume
		// abort and fullAbort not functional
		//
		// Latch break>
		// loop starts again, and initializes commands
		// UI Updates
		// colors to OG colors
		// Resume == Pause
		// abort and fullAbort functional
	}

	public void resumeProcess()
	{
		block.getBar1().setForeground(Color.ORANGE);
		block.getBar2().setForeground(Color.YELLOW);
		block.getBar3().setForeground(Color.GREEN);
		block.getBar4().setForeground(Color.CYAN);
		block.getBar5().setForeground(Color.MAGENTA);
		block.getPause().setText("Pause");
		block.setStatus(0);
		block.getPauseLatch().countDown();
		logger.info("Program Resumed, Latch Removed");
	}

	public void killAll()
	{
		continueProcess = false;
		block.setStatus(20);
		if (block != null && block.getA() != null && block.getA().isAlive())
		{
			block.getA().destroy();
			logger.debug("Stage A found and Destroyed");
		}
		if (block != null && block.getB() != null && block.getB().isAlive())
		{
			block.getB().destroy();
			logger.debug("Stage B found and Destroyed");
		}
		if (block != null && block.getC() != null && block.getC().isAlive())
		{
			block.getC().destroy();
			logger.debug("Stage C found and Destroyed");
		}
		if (block != null && block.getD() != null && block.getD().isAlive())
		{
			block.getD().destroy();
			logger.debug("Stage D found and Destroyed");
		}
	}

	public void kill()//Only for program quit
	{
		if (block != null && block.getA() != null && block.getA().isAlive())
		{
			block.getA().destroy();
			logger.debug("Stage A found and Destroyed");
		}
		if (block != null && block.getB() != null && block.getB().isAlive())
		{
			block.getB().destroy();
			logger.debug("Stage B found and Destroyed");
		}
		if (block != null && block.getC() != null && block.getC().isAlive())
		{
			block.getC().destroy();
			logger.debug("Stage C found and Destroyed");
		}
		if (block != null && block.getD() != null && block.getD().isAlive())
		{
			block.getD().destroy();
			logger.debug("Stage D found and Destroyed");
		}
	}

	/*
	 * private DataBlock readQueue(String filePath, int queueCount) { try {
	 * Scanner i = new Scanner(new File(filePath)); String season =
	 * i.nextLine().substring(13); String ep = i.nextLine().substring(9); Timing
	 * L1 = new Timing(); Timing L2 = new Timing(); Timing L3 = new Timing();
	 * Timing L4 = new Timing(); Timing L5 = new Timing(); Timing S1 = new
	 * Timing(); Timing S2 = new Timing(); Timing S3 = new Timing(); Timing S4 =
	 * new Timing(); Timing S5 = new Timing(); ArrayList<Timing> list = new
	 * ArrayList<Timing>(); list.add(L1); list.add(L2); list.add(L3);
	 * list.add(L4); list.add(L5); list.add(S1); list.add(S2); list.add(S3);
	 * list.add(S4); list.add(S5); for (int y = 0; y < 10; y++) { i.nextLine();
	 * list.get(y).sF(i.nextLine().substring(18));
	 * list.get(y).sM(i.nextLine().substring(9));
	 * list.get(y).sL(i.nextLine().substring(14));
	 * list.get(y).sO(i.nextLine().substring(12));
	 * list.get(y).sT(i.nextLine().substring(10)); } String title =
	 * i.nextLine().substring(6); String desc = i.nextLine().substring(12);
	 * boolean radio1 = false; boolean radio2 = false; boolean radio3 = false;
	 * int type = Integer.valueOf(i.nextLine().substring(13)); if (type == 1) {
	 * radio1 = true; } else if (type == 2) { radio2 = true; } else if (type ==
	 * 3) { radio3 = true; } else { status = 1; } int totalStreams =
	 * Integer.valueOf(i.nextLine().substring(13)); String eep =
	 * i.nextLine().substring(18); String vfilename =
	 * i.nextLine().substring(14); String afilename =
	 * i.nextLine().substring(14); String sfilename =
	 * i.nextLine().substring(17); String mappings = i.nextLine().substring(9);
	 * String isAudio = i.nextLine().substring(11); String isSubtitle =
	 * i.nextLine().substring(14); Calculations calc = new Calculations(dir,
	 * vfilename); int totalFrames =
	 * Integer.valueOf(i.nextLine().substring(12)); String duration =
	 * i.nextLine().substring(9); String startTime = i.nextLine().substring(10);
	 * String m = i.nextLine().substring(30); String h =
	 * i.nextLine().substring(24); String n = i.nextLine().substring(23); String
	 * p = i.nextLine().substring(26); String c = i.nextLine().substring(27);
	 * String d = i.nextLine().substring(36); String b =
	 * i.nextLine().substring(32); String gpu = i.nextLine().substring(34);
	 * String tta = i.nextLine().substring(36); String y =
	 * i.nextLine().substring(25); String r = i.nextLine().substring(33); String
	 * deinterlacing = i.nextLine().substring(62); String finalCompression =
	 * i.nextLine().substring(43); String frameFraction =
	 * i.nextLine().substring(48); i.close(); Reader scan = new Reader(dir, m,
	 * h, n, p, c, d, b, gpu, tta, y, r, deinterlacing, finalCompression,
	 * frameFraction); return new DataBlock(scan, season, ep, L1, L2, L3, L4,
	 * L5, S1, S2, S3, S4, S5, title, desc, radio1, radio2, radio3, bar1, bar2,
	 * bar3, execute, abort, time, output, error, eep, vfilename, afilename,
	 * sfilename, dir, mappings, totalStreams, isAudio, isSubtitle, calc,
	 * totalFrames, duration, startTime); } catch (FileNotFoundException e) {
	 * status = 5; DataBlock block = new DataBlock(); e.printStackTrace();
	 * JOptionPane.showMessageDialog(null, "Program Failed to read File for
	 * Queue: " + queueCount + "Will Skip and Continue", "Critical Error",
	 * JOptionPane.ERROR_MESSAGE); return block; } }
	 */
}
