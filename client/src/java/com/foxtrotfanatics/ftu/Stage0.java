package com.foxtrotfanatics.ftu;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.graphical.MainWindow;
import com.foxtrotfanatics.ftu.graphical.TrackSelection;
import com.foxtrotfanatics.ftu.structures.DataBlock;
import com.foxtrotfanatics.ftu.structures.Reader;
import com.foxtrotfanatics.ftu.structures.Report;
import com.foxtrotfanatics.ftu.structures.Timing;

public class Stage0 extends SwingWorker<Void, Void>
{
	private static Logger logger = LogManager.getLogger();
	private int status;
	private MainWindow window;
	private Settings settings;
	private String showName;
	private String ep;
	private Timing[] streams = new Timing[10];//Must be a Even number of Streams!
	private boolean[] streamBooleans;
	private String[] streamLangs;
	private String[] streamHandles;
	private String[] streamOffsets;
	private String title;
	private String desc;
	private boolean p1;
	private boolean p2;
	private boolean p3;
	private boolean p4;
	private JProgressBar bar1;
	private JProgressBar bar2;
	private JProgressBar bar3;
	private JProgressBar bar4;
	private JProgressBar bar5;
	private JProgressBar barM;
	private boolean useRaw;
	private JButton execute;
	private JButton pause;
	private JButton abort;
	private JButton fullAbort;
	private JTextField time;
	private JTextArea output;
	private JTextArea error;
	private String eep;
	private String vfilename;
	private String sourceAudioName;
	private String asfilename;
	private String dir;
	private String mappings;
	private int totalStreams;
	private Calculations calc;
	private String isAudio;
	private String isSubtitle;
	private DataBlock block;
	private String duration;
	private String startTime;
	private String exportExtension;
	private int operatingMode;
	private Reader scan;
	private Timer timer;
	private String videoOffset;
	private boolean fullyVerify;

	/**
	 * Grabs Data from File and GUI, Checks if Execution is Possible, Grabs
	 * Total Frame Count, and starts Part1
	 */
	public Stage0(MainWindow window,
			Timer timer,
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
			boolean p1,
			boolean p2,
			boolean p3,
			boolean p4,
			boolean useRaw,
			JProgressBar bar1,
			JProgressBar bar2,
			JProgressBar bar3,
			JProgressBar bar4,
			JProgressBar bar5,
			JProgressBar barM,
			JButton execute,
			JButton pause,
			JButton abort,
			JButton fullAbort,
			JTextField time,
			JTextArea output,
			JTextArea error,
			String duration,
			String startTime,
			String sourceAudioName,
			String exportExtension,
			int operatingMode,
			boolean fullyVerify)
	{
		this.window = window;
		this.settings = settings;
		this.dir = dir;
		this.showName = showName;
		this.ep = ep;
		eep = new String(String.format("%03d", Integer.parseInt(ep)));
		this.streamBooleans = streamBooleans;
		this.streamLangs = streamLangs;
		this.streamHandles = streamHandles;
		this.streamOffsets = streamOffsets;
		this.title = title;
		this.desc = desc;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
		this.useRaw = useRaw;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.bar3 = bar3;
		this.bar4 = bar4;
		this.bar5 = bar5;
		this.barM = barM;
		this.execute = execute;
		this.pause = pause;
		this.abort = abort;
		this.fullAbort = fullAbort;
		this.time = time;
		this.output = output;
		this.error = error;
		totalStreams = 0;
		this.duration = duration;
		this.startTime = startTime;
		this.sourceAudioName = sourceAudioName;
		this.exportExtension = exportExtension;
		this.operatingMode = operatingMode;
		this.timer = timer;
		this.fullyVerify = fullyVerify;
	}

	@Override
	protected Void doInBackground()
	{
		block = checkEverything();
		return null;
	}

	public DataBlock checkEverything()
	{
		status = 0;
		logger.info("Starting Process Validation");
		if (streamBooleans[4])
		{
			if (!streamBooleans[1] || !streamBooleans[2] || !streamBooleans[3] || !streamBooleans[0])
			{
				status = 3;
				logger.error("Audio Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Audio Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[3])
		{
			if (!streamBooleans[0] || !streamBooleans[1] || !streamBooleans[2] || streamBooleans[4])
			{
				status = 3;
				logger.error("Audio Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Audio Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[2])
		{
			if (!streamBooleans[0] || !streamBooleans[1] || streamBooleans[3] || streamBooleans[4])
			{
				status = 3;
				logger.error("Audio Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Audio Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[1])
		{
			if (!streamBooleans[0] || streamBooleans[2] || streamBooleans[3] || streamBooleans[4])
			{
				status = 3;
				logger.error("Audio Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Audio Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[0])
		{
			if (streamBooleans[1] || streamBooleans[2] || streamBooleans[3] || streamBooleans[4])
			{
				status = 3;
				logger.error("Audio Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Audio Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (streamBooleans[9])
		{
			if (!streamBooleans[6] || !streamBooleans[7] || !streamBooleans[8] || !streamBooleans[5])
			{
				status = 3;
				logger.error("Subtitle Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Subtitles Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[8])
		{
			if (!streamBooleans[5] || !streamBooleans[6] || !streamBooleans[7] || streamBooleans[9])
			{
				status = 3;
				logger.error("Subtitle Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Subtitles Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[7])
		{
			if (!streamBooleans[5] || !streamBooleans[6] || streamBooleans[8] || streamBooleans[9])
			{
				status = 3;
				logger.error("Subtitle Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Subtitles Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[6])
		{
			if (!streamBooleans[5] || streamBooleans[7] || streamBooleans[8] || streamBooleans[9])
			{
				status = 3;
				logger.error("Subtitle Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Subtitles Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (streamBooleans[5])
		{
			if (streamBooleans[6] || streamBooleans[7] || streamBooleans[8] || streamBooleans[9])
			{
				status = 3;
				logger.error("Subtitle Selection not in Sequential Order");
				JOptionPane.showMessageDialog(window, "Subtitles Selection not in Sequential Order", "Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		checkOffset(1, streamOffsets[0]);
		checkOffset(2, streamOffsets[1]);
		checkOffset(3, streamOffsets[2]);
		checkOffset(4, streamOffsets[3]);
		checkOffset(5, streamOffsets[4]);
		checkOffset(6, streamOffsets[5]);
		checkOffset(7, streamOffsets[6]);
		checkOffset(8, streamOffsets[7]);
		checkOffset(9, streamOffsets[8]);
		checkOffset(10, streamOffsets[9]);
		checkOffset(11, duration);
		checkOffset(12, startTime);
		String adir = new String(File.separator + "ImportAudio" + File.separator);
		String sdir = new String(File.separator + "ImportSubtitle" + File.separator);
		String importFolderName;
		if (operatingMode == 2)
		{
			importFolderName = new String("Export");
			vfilename = new String("Upscaled_" + sourceAudioName + "_" + eep + "_" + showName);
		}
		else
		{
			importFolderName = new String("ImportVideo");
			vfilename = new String("Input_" + eep + "_" + showName);
		}
		String quickPath = new String(File.separator + importFolderName + File.separator + vfilename);
		String extension = TrackSelection.findExtension(window, dir + quickPath);
		if (extension == null)
		{
			logger.error("Input Not Found or Invalid Extension: {}", quickPath);
			status = 12;
			JOptionPane.showMessageDialog(window,
					"Video Source not found, or not Labeled as\n" + quickPath + ".<extension>\nEntered EP may be different then intended",
					"Source Not Found", JOptionPane.ERROR_MESSAGE);
			window.toggleMenu(0, true);
			block = new DataBlock();
			logger.error("Process Validation Failed");
			return block;
		}
		vfilename += extension;
		quickPath += extension;
		asfilename = new String("_" + eep + "_" + showName);
		logger.debug("Video Source: {}", quickPath);
		if (fullyVerify && (!new File(dir + quickPath).exists()))
		{
			logger.error("Input Not Found: {}", quickPath);
			status = 12;
			JOptionPane.showMessageDialog(window,
					"Video Source not found, or not Labeled as\n" + quickPath + "\nEntered EP may be different then intended", "Source Not Found",
					JOptionPane.ERROR_MESSAGE);
		}
		if (title.isEmpty())
		{
			title = "";
		}
		else
		{
			title = new String("-metadata title=\"" + title + "\" ");
		}
		if (desc.isEmpty())
		{
			desc = "";
		}
		else
		{
			desc = new String("-metadata description=\"" + desc + "\" ");
		}
		output.append("\nAnalyzing Video" + "--------------------------------------------------------------------------------------------\n");
		logger.debug("ShowName: " + showName + "\nEpisode: " + ep + "\nFile Name: " + vfilename + "\nDirectory: " + dir + "\nTitle: " + title
				+ "\nDescription: " + desc);
		scan = new Reader(dir);
		calc = new Calculations(settings.getFfprobe(), vfilename);
		streams[0] = new Timing();
		streams[1] = new Timing();
		streams[2] = new Timing();
		streams[3] = new Timing();
		streams[4] = new Timing();
		streams[5] = new Timing();
		streams[6] = new Timing();
		streams[7] = new Timing();
		streams[8] = new Timing();
		streams[9] = new Timing();
		mappings = new String("-map 0:v ");
		isAudio = "";
		isSubtitle = "";
		for (int x = 0; x < streams.length; x++)
		{
			if (streamBooleans[x])
			{
				String langString = new String(streamLangs[x]);
				String quickStream;
				String streamInitial;
				String streamDir;
				String streamFileName;
				String asextension;
				if (x < (streams.length / 2) - 1)
				{
					streamInitial = new String("a");
					streamDir = new String(adir);
					asextension = TrackSelection.findExtension(window, dir + adir + langString + asfilename,
							TrackSelection.audioExtensions, x + 1);
					isAudio = "-c:a copy ";
				}
				else
				{
					streamInitial = new String("s");
					streamDir = new String(sdir);
					asextension = TrackSelection.findExtension(window, dir + sdir + langString + asfilename,
							TrackSelection.subtitleExtensions, x + 1);
					isSubtitle = "-c:s copy ";
				}
				if(asextension == null)
				{
					status = 2;
					logger.error("File Not Found: {}{}.<extension>", streamDir,asfilename);
					JOptionPane.showMessageDialog(window, "Audio/Subtitle Source not found, or not Labeled as (" + langString + asfilename
							+ ".<extension>)\nEntered EP may be different then intended", "Source Not Found", JOptionPane.ERROR_MESSAGE);
				}
				streamFileName = asfilename + asextension;
				quickStream = new String(streamDir + langString + streamFileName);
				if (fullyVerify && (!new File(dir + quickStream).exists()) && status != 2)
				{
					status = 2;
					logger.error("File Not Found: {}", quickStream);
					JOptionPane.showMessageDialog(window, "Audio/Subtitle Source not found, or not Labeled as (" + langString + "_" + eep + "_" + showName
							+ "." + asextension + ")\nEntered EP may be different then intended", "Source Not Found", JOptionPane.ERROR_MESSAGE);

				}
				String temp;
				totalStreams++;
				if (!streamOffsets[x].equals("00:00:00.000"))
				{
					temp = new String("-itsoffset " + streamOffsets[x]);
				}
				else
				{
					temp = new String("");
				}

				mappings = (mappings + "-map " + totalStreams + ":" + streamInitial + " ");
				streams[x] = new Timing(streamDir + langString + streamFileName, "-map " + totalStreams + ":" + streamInitial, langString, streamHandles[x], temp,
						totalStreams + ":" + streamInitial);
			}
		}
		logger.debug(streams[0]);
		logger.debug(streams[1]);
		logger.debug(streams[2]);
		logger.debug(streams[3]);
		logger.debug(streams[4]);
		logger.debug(streams[5]);
		logger.debug(streams[6]);
		logger.debug(streams[7]);
		logger.debug(streams[8]);
		logger.debug(streams[9]);
		int totalFrames;
		if (duration.equals("00:00:00.000"))
		{
			if (operatingMode != 2)
			{
				if (fullyVerify)
				{
					calc.getTotalFrameCount(dir);
					totalFrames = calc.getTotalCount();
				}
				else
					totalFrames = -12345;
			}
			else
			{
				totalFrames = 1;
			}
			if (totalFrames < 0 && fullyVerify)
			{
				status = 26;
				totalFrames = 1;
			}
			else if (totalFrames == 0)
			{
				status = -3;
				totalFrames = 1;
			}
			duration = "";
		}
		else
		{
			if (operatingMode != 2)
			{
				double rate = Double.valueOf(scan.getR1());
				long dur = offsetMilliseconds(duration);
				totalFrames = (int) ((rate * dur) / 1000);
				logger.debug("Total Frames: {}", totalFrames);
				calc.setTotalCount(totalFrames);
			}
			else
			{
				totalFrames = 1;
			}
			duration = ("-t " + duration + " ");
		}
		if (!startTime.equals("00:00:00.000"))
		{
			videoOffset = ("-itsoffset \"" + startTime + "\" ");
			startTime = ("-ss \"" + startTime + "\" ");
		}
		else
		{
			startTime = "";
			videoOffset = "";
		}
		if (status == 0 && operatingMode == 1)// Operating Modes: 1 = Executing
												// from Queue, 2 = Remuxing
												// Channels, 0 = Executing as
												// Single Process
		{
			block = new DataBlock(scan, settings, showName, ep, streams[0], streams[1], streams[2], streams[3], streams[4], streams[5], streams[6],
					streams[7], streams[8], streams[9], title, desc, p1, p2, p3, p4, useRaw, bar1, bar2, bar3, bar4, bar5, barM, execute, pause,
					abort, fullAbort, time, output, error, eep, vfilename, dir, mappings, totalStreams, isAudio, isSubtitle,
					calc, totalFrames, duration, startTime, videoOffset, sourceAudioName, exportExtension, 1);
			block.init();
			logger.info("Process Validation Completed: Acceptable for Multi Process");
			logger.info(block);
		}
		else if (status == 0 && operatingMode == 0)
		{
			block = new DataBlock(scan, settings, showName, ep, streams[0], streams[1], streams[2], streams[3], streams[4], streams[5], streams[6],
					streams[7], streams[8], streams[9], title, desc, p1, p2, p3, p4, useRaw, bar1, bar2, bar3, bar4, bar5, barM, execute, pause,
					abort, fullAbort, time, output, error, eep, vfilename, dir, mappings, totalStreams, isAudio, isSubtitle,
					calc, totalFrames, duration, startTime, videoOffset, sourceAudioName, exportExtension, 0);
			block.init();
			logger.info("Process Validation Completed: Acceptable for Single Process");
			logger.info(block);
			if(fullyVerify)
			{
				settings.submit(new Report(settings.getClientID(), 10, block, true));
			}
		}
		else if (status == 0 && operatingMode == 2)
		{
			block = new DataBlock(scan, settings, showName, ep, streams[0], streams[1], streams[2], streams[3], streams[4], streams[5], streams[6],
					streams[7], streams[8], streams[9], title, desc, p1, p2, p3, p4, useRaw, bar1, bar2, bar3, bar4, bar5, barM, execute, pause,
					abort, fullAbort, time, output, error, eep, vfilename, dir, mappings, totalStreams, isAudio, isSubtitle,
					calc, totalFrames, duration, startTime, videoOffset, sourceAudioName, exportExtension, 2);
			block.init();
			logger.info("Process Validation Completed: Acceptable for Remuxing Process");
		}
		else
		{
			window.toggleMenu(0, true);
			block = new DataBlock();
			logger.error("Process Validation Failed");
		} // TODO?
		return block;
	}

	@Override
	protected void done()
	{
		bar1.setIndeterminate(false);
		bar2.setIndeterminate(false);
		bar3.setIndeterminate(false);
		bar4.setIndeterminate(false);
		if (status == 0)
		{
			time.setEnabled(true);
			time.setText("Calculating");
			timer.start();
			if (p1)
			{
				logger.info("Starting Process at Stage 1");
				execute.setText("Executing Stage 1");
				Stage1 stage1 = new Stage1(window, block, timer);
				stage1.execute();
			}
			else if (p2)
			{
				logger.info("Starting Process at Stage 2");
				execute.setText("Executing Stage 2");
				Stage2 stage2 = new Stage2(window, block, timer);
				stage2.execute();
			}
			else if (p3)
			{
				logger.info("Starting Process at Stage 3");
				execute.setText("Executing Stage 3");
				Stage3 stage3 = new Stage3(window, block, timer);
				stage3.execute();
			}
			else if (p4)
			{
				logger.info("Starting Process at Stage 4");
				execute.setText("Executing Stage 4");
				Stage4 stage4 = new Stage4(window, block, timer);
				stage4.execute();
			}
		}
		else
		{
			window.toggleMenu(0, true);
			execute.setText("Execute");
			execute.setEnabled(true);
		}
	}

	/**
	 * Verify if User Entered Offset is Acceptable Verifies Twice, As the User
	 * types in the Field, and again on Process Verification to Prevent the
	 * Program from Continuing
	 * 
	 * @param box
	 * The JTextField in Question, used to see if incorrectly
	 * formatted, what string to return
	 * @param text
	 * The String in the JTextField
	 */
	private void checkOffset(int box, String text) // Verify if User Entered
													// Offset is Acceptable
	{
		if (!(((box == 1 || box == 2 || box == 3 || box == 4 || box == 5 || box == 6 || box == 7 || box == 8 || box == 9 || box == 10 || box == 11
				|| box == 12) && text.equals("00:00:00.000"))))
		{
			boolean wrong = true;
			if (text.startsWith("-"))
			{
				if (text.length() == 13 && StringUtils.isNumeric(text.substring(1, 3)) && text.substring(3, 4).equals(":")
						&& StringUtils.isNumeric(text.substring(4, 6)) && text.substring(6, 7).equals(":")
						&& StringUtils.isNumeric(text.substring(7, 9)) && text.substring(9, 10).equals(".")
						&& StringUtils.isNumeric(text.substring(10, 13)))
				{
					wrong = false;
				}
			}
			else
			{
				if (text.length() == 12 && StringUtils.isNumeric(text.substring(0, 2)) && text.substring(2, 3).equals(":")
						&& StringUtils.isNumeric(text.substring(3, 5)) && text.substring(5, 6).equals(":")
						&& StringUtils.isNumeric(text.substring(6, 8)) && text.substring(8, 9).equals(".")
						&& StringUtils.isNumeric(text.substring(9, 12)))
				{
					wrong = false;
				}
			}
			if (wrong)
			{
				status = 1;
				String wrongBox;
				if (box == 12)
				{
					wrongBox = new String("Start Time Box");
				}
				else if (box == 11)
				{
					wrongBox = new String("Duration Box");
				}
				else
				{
					wrongBox = new String("Stream Offset Box: " + box);
				}
				logger.error("Offset Verification Failed for Box: {} - Value: {}", wrongBox, text);
				JOptionPane.showMessageDialog(window, "Time Offset in \"" + wrongBox + "\" not in\n\"(-)HH:MM:SS.mss\" format", "Input Error",
						JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				logger.trace("Offset Verified: {}", text);
			}
		}
	}

	/**
	 * Convert Offset to MilliSeconds
	 * 
	 * @param box
	 * The Actual JComponent that is Being Verified
	 * @param text
	 * The Text in the JTextBox
	 * @return the Value in MilliSeconds
	 */
	private long offsetMilliseconds(String text)
	{
		long time;
		if (text.equals("00:00:00.000"))
		{
			return 0;
		}
		else if (text.startsWith("-"))
		{
			time = 0;
			time += (Integer.valueOf(text.substring(1, 3)) * 3600000);
			time += (Integer.valueOf(text.substring(4, 6)) * 60000);
			time += (Integer.valueOf(text.substring(7, 9)) * 1000);
			time += Integer.valueOf(text.substring(10, 13));
			return -time;
		}
		else
		{
			time = 0;
			time += (Integer.valueOf(text.substring(0, 2)) * 3600000);
			time += (Integer.valueOf(text.substring(3, 5)) * 60000);
			time += (Integer.valueOf(text.substring(6, 8)) * 1000);
			time += Integer.valueOf(text.substring(9, 12));
			return time;
		}
	}

	/**
	 * Subtracts Offsets by Converting to longs, subtracting, and reconverting
	 * to a String
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unused")
	private String subtractOffsets(String a, String b)
	{
		long acount = offsetMilliseconds(a);
		long bcount = offsetMilliseconds(b);
		long tcount = acount - bcount;
		logger.debug("Millisecond subtractOffsets() Calculation: {} - {} = {}", acount, bcount, tcount);
		boolean tnegative = false;
		if (tcount < 0)
		{
			tcount = -tcount;
			tnegative = true;
		}
		String hours = String.format("%02d", (tcount / 360000)).substring(0, 2);
		tcount -= Integer.valueOf(hours) * 360000;
		String minutes = String.format("%02d", (tcount / 6000)).substring(0, 2);
		tcount -= Integer.valueOf(minutes) * 6000;
		String seconds = String.format("%02d", (tcount / 100)).substring(0, 2);
		tcount -= Integer.valueOf(seconds) * 100;
		String milli = String.format("%03d", tcount).substring(0, 3);
		String result = new String(hours + ":" + minutes + ":" + seconds + "." + milli);
		if (tnegative)
		{
			result = ("-" + result);
		}
		logger.debug("subTractOffsets() Final Strings A:({}) + B:({}) = ({})", a, b, result);
		return result;
	}

	public void destroy()
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
		Report r = settings.getStoredReport();
		r.setJobFailed(false);
		r.setJobCompleted(false);
		r.setJobStarted(true);
		r.setOperating(false);
		r.setJobAborted(true);
		r.addEvent(20, "Process Aborted");
		settings.submit(r);
	}

	public DataBlock getBlock()
	{
		return block;
	}

	public void pauseProcess(boolean confirmDirectly)
	{
		logger.info("Process Pause Requested");
		if (confirmDirectly || JOptionPane.showConfirmDialog(window, "Are you sure you wish to Pause?\nAll progress on Stge 1 and 4 will be Deleted", "Clear Queue?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
		{
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
				String quickPart = new String(File.separator + "Export" + File.separator + block.getEep() + "_" + showName + "." + exportExtension);
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
			block.getPause().setText("Resume");
			block.getAbort().setEnabled(false);
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
		block.getPause().setText("Pause");
		block.setStatus(0);
		block.getPauseLatch().countDown();
		logger.info("Program Resumed, Latch Removed");
	}

	public void kill()// Only called on Shutdown
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
}
