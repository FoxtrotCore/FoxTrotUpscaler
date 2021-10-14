package com.foxtrotfanatics.ftu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.graphical.MainWindow;
import com.foxtrotfanatics.ftu.structures.DataBlock;

public class QueueMaker extends Thread
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private Settings settings;
	private String showName;
	private String ep;
	private boolean[] streamBooleans;
	private String[] streamLangs;
	private String[] streamHandles;
	private String[] streamOffsets;
	private String title;
	private String desc;
	private boolean useRaw;
	private JProgressBar bar1;
	private JProgressBar bar2;
	private JProgressBar bar3;
	private JProgressBar bar4;
	private JProgressBar bar5;
	private JButton execute;
	private JButton pause;
	private JButton abort;
	private JButton fullAbort;
	private JButton addToQueue;
	private JButton executeQueue;
	private JButton editQueue;
	private JTextField time;
	private JTextArea output;
	private JTextArea error;
	private String dir;
	private String duration;
	private String startTime;
	private String audioName;
	private String exportExtension;

	public QueueMaker(MainWindow window,
			Settings settings,
			String dir,
			String showName,
			String ep,
			boolean[] streamBooleans,
			String[] streamLangs,
			String[] streamHandles,
			String[] streamOffsets,
			String title,
			String desc,
			boolean useRaw,
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
			JTextArea error,
			String duration,
			String startTime,
			JButton addToQueue,
			JButton executeQueue,
			JButton editQueue,
			String audioName,
			String exportExtension)
	{
		this.window = window;
		this.settings = settings;
		this.dir = dir;
		this.showName = showName;
		this.ep = ep;
		this.streamBooleans = streamBooleans;
		this.streamLangs = streamLangs;
		this.streamHandles = streamHandles;
		this.streamOffsets = streamOffsets;
		this.title = title;
		this.desc = desc;
		this.useRaw = useRaw;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.bar3 = bar3;
		this.bar4 = bar4;
		this.bar5 = bar5;
		this.execute = execute;
		this.pause = pause;
		this.abort = abort;
		this.fullAbort = fullAbort;
		this.addToQueue = addToQueue;
		this.editQueue = editQueue;
		this.executeQueue = executeQueue;
		this.time = time;
		this.output = output;
		this.error = error;
		this.duration = duration;
		this.startTime = startTime;
		this.audioName = audioName;
		this.exportExtension = exportExtension;
	}

	public void run()
	{
		int status = 0;
		int totalProcessCount = new File(dir + File.separator + "Queue").list().length; //Wont allow creation of more Queue is current Queue is over 998
		if (totalProcessCount >= 999)
		{
			logger.error("Queue Larger then 999 Files");
			JOptionPane.showMessageDialog(null, "Too many files in the Queue\nLimit is 999", "Program Limitation", JOptionPane.ERROR_MESSAGE);
			status = 1;
			return;
		}
		totalProcessCount++;
		output.append(
				"\nAttempting Save of Process" + "--------------------------------------------------------------------------------------------\n");
		addToQueue.setEnabled(false);
		executeQueue.setEnabled(false);
		editQueue.setEnabled(false);
		execute.setEnabled(false);
		addToQueue.setText("Analyzing Process...");
		executeQueue.setText("Analyzing Process...");
		editQueue.setText("Analyzing Process...");
		Stage0 sequence = new Stage0(window, null, settings, dir, showName, ep, streamBooleans, streamLangs, streamHandles, streamOffsets, title,
				desc, true, false, false, false, useRaw, bar1, bar2, bar3, bar4, bar5, null, execute, pause, abort, fullAbort, time, output, error,
				duration, startTime, audioName, exportExtension, 1, true); //Creation of this Object will prepare a object, while Calling .checkEverything() will verify if all parameters are correct and allow the Program to Continue
		DataBlock block = sequence.checkEverything();
		pause.setEnabled(false);
		abort.setEnabled(false);
		fullAbort.setEnabled(false);
		String quickPath = new String("");
		if (block.getStatus() == 0)//If everything checks out, Creates the Queue File with a File name "Q###.ser" number incremented on how many queue files their are.
		{
			try
			{
				String queueName = new String("Q" + String.format("%03d", totalProcessCount) + ".ser");
				quickPath = new String(File.separator + "Queue" + File.separator + queueName);
				String filePath = new String(dir + quickPath);
				FileOutputStream fileOut = new FileOutputStream(filePath);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(block);
				out.close();
				fileOut.close();
				logger.info("Queue File Saved: {}", quickPath);
			}
			catch (IOException i)
			{
				logger.fatal("Failed Save Attempt: {}", quickPath);
				logger.fatal("Exception Infomation", 1);
				JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program", "IO Exception",
						JOptionPane.ERROR_MESSAGE);
				System.exit(20);
			}
		}
		else
		{
			logger.error("Bad Configuration of Process, Not Saving");
			output.append("\nUpscale Process had Bad Input"
					+ "--------------------------------------------------------------------------------------------\n");
			addToQueue.setEnabled(true);
			execute.setEnabled(true);
			addToQueue.setText("Save For Queue");
			executeQueue.setText("Start Queue");
			editQueue.setText("Delete Queue");
			bar1.setIndeterminate(false);
			bar2.setIndeterminate(false);
			bar3.setIndeterminate(false);
			bar4.setIndeterminate(false);
			bar5.setIndeterminate(false);
			status = 2;
		}
		if (status == 0)
		{
			output.append(
					"\nUpscale Process Saved" + "--------------------------------------------------------------------------------------------\n");
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					window.toggleMenu(0, true);
					addToQueue.setEnabled(true);
					executeQueue.setEnabled(true);
					editQueue.setEnabled(true);
					execute.setEnabled(true);
					addToQueue.setText("Save For Queue");
					executeQueue.setText("Start Queue");
					editQueue.setText("Delete Queue");
					bar1.setIndeterminate(false);
					bar2.setIndeterminate(false);
					bar3.setIndeterminate(false);
					bar4.setIndeterminate(false);
					bar5.setIndeterminate(false);
				}
			});
		}
	}
}
/*
 * initGUI();
 * cleanUpQueueNames();
 * if(incompleteProcessExists())
 * {
 * if(UserWishes to restart)
 * {
 * if(QueueFile)
 * {
 * Place in beginning
 * Figure out where it left off
 * if(Part 1 or 4)
 * {
 * wipe();
 * }
 * else
 * {
 * delete frames where needed for part 2, part 3 fine to recall
 * }
 * startQueue on incomplete Process;
 * }
 * else
 * {
 * Figure out where it left off
 * if(Part 1 or 4)
 * {
 * wipe();
 * }
 * else
 * {
 * delete frames where needed for part 2, part 3 fine to recall
 * }
 * update progress bars based on intended start sequence.
 * startUpscale on incomplete Process;
 * }
 * }
 * else
 * {
 * delete
 * }
 * }
 * 
 */