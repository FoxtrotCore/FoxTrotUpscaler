package com.foxtrotfanatics.ftu.structures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.Calculations;
import com.foxtrotfanatics.ftu.Settings;

public class DataBlock implements java.io.Serializable
{
	/**
	 * Version 2.10.0
	 */
	private static final long serialVersionUID = 5175729069865517377L;
	private static Logger logger = LogManager.getLogger();
	private int status;
	private transient Settings settings;
	private String showName;
	private String ep;
	private Timing L1;
	private Timing L2;
	private Timing L3;
	private Timing L4;
	private Timing L5;
	private Timing S1;
	private Timing S2;
	private Timing S3;
	private Timing S4;
	private Timing S5;
	private String title;
	private String desc;
	private boolean radio1;
	private boolean radio2;
	private boolean radio3;
	private boolean radio4;
	private boolean useRaw;
	private String eep;
	private String vfilename;
	private String audioName;
	private transient String dir;
	private String mappings;
	private int totalStreams;
	private Calculations calc;
	private int totalFrames;
	private Reader scan;
	private String isAudio;
	private String isSubtitle;
	private int oldCount;
	private transient JProgressBar bar1;
	private transient JProgressBar bar2;
	private transient JProgressBar bar3;
	private transient JProgressBar bar4;
	private transient JProgressBar bar5;
	private transient JProgressBar barM;
	private transient JButton execute;
	private transient JButton pause;
	private transient JButton abort;
	private transient JButton fullAbort;
	private transient JTextField time;
	private transient JTextArea output;
	private transient JTextArea error;
	private String duration;
	private String startTime;
	private transient String command1;
	private transient String command2;
	private transient String command3;
	private transient String command4;
	private transient String commandM4;
	private transient Process a;
	private transient Process b;
	private transient Process c;
	private transient Process d;
	private transient Process m;
	private int mode;
	private transient CountDownLatch selfLatch;
	private transient CountDownLatch pauseLatch;
	private boolean paused;
	private boolean recovered;
	private int progress;
	private String videoOffset;
	private String exportExtension;

	public DataBlock()
	{
		logger.warn("Dead Process Created");
		status = 1;
	}

	public DataBlock(Reader scan,
			Settings settings,
			String showName,
			String ep,
			Timing L1,
			Timing L2,
			Timing L3,
			Timing L4,
			Timing L5,
			Timing S1,
			Timing S2,
			Timing S3,
			Timing S4,
			Timing S5,
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
			String eep,
			String vfilename,
			String dir,
			String mappings,
			int totalStreams,
			String isAudio,
			String isSubtitle,
			Calculations calc,
			int totalFrames,
			String duration,
			String startTime,
			String videoOffset,
			String audioName,
			String exportExtension,
			int mode)
	{
		this.scan = scan;
		this.settings = settings;
		this.showName = showName;
		this.ep = ep;
		this.L1 = L1;
		this.L2 = L2;
		this.L3 = L3;
		this.L4 = L4;
		this.L5 = L5;
		this.S1 = S1;
		this.S2 = S2;
		this.S3 = S3;
		this.S4 = S4;
		this.S5 = S5;
		this.title = title;
		this.desc = desc;
		this.radio1 = p1;
		this.radio2 = p2;
		this.radio3 = p3;
		this.radio4 = p4;
		this.useRaw = useRaw;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.bar3 = bar3;
		this.bar4 = bar4;
		this.bar5 = bar5;
		this.barM = barM;
		this.useRaw = useRaw;
		this.execute = execute;
		this.pause = pause;
		this.abort = abort;
		this.fullAbort = fullAbort;
		this.time = time;
		this.output = output;
		this.error = error;
		this.duration = duration;
		this.startTime = startTime;
		this.videoOffset = videoOffset;
		this.totalStreams = totalStreams;
		this.eep = eep;
		this.vfilename = vfilename;
		this.audioName = audioName;
		this.dir = dir;
		this.mappings = mappings;
		this.totalStreams = totalStreams;
		this.isAudio = isAudio;
		this.isSubtitle = isSubtitle;
		this.oldCount = 0;
		this.calc = calc;
		this.totalFrames = totalFrames;
		this.mode = mode;
		this.paused = false;
		this.recovered = false;
		if (radio1)
		{
			progress = 1;
		}
		else if (radio2)
		{
			progress = 2;
		}
		else if (radio3)
		{
			progress = 3;
		}
		else if (radio4)
		{
			progress = 4;
		}
		this.exportExtension = exportExtension;
	}

	public void init()
	{
		logger.debug("Initializing Command Strings");
		String image1;
		String image2;
		String image3;
		if (settings.isImage())
		{
			image1 = "tiff";
			image2 = "tif";
			image3 = "-t";
		}
		else
		{
			image1 = "png";
			image2 = "png";
			image3 = "-n";
		}
		if (mode == 1 || mode == 0)
		{
			calc.setffmpeg(settings.getFfprobe());
			command1 = new String(settings.getFfmpeg() + " -y -r " + scan.getR1() + " " + scan.getAInputString() + "-i \"" + dir + File.separator
					+ "ImportVideo" + File.separator + vfilename + "\"" + " -c:v " + image1 + " " + scan.getAExportString() + startTime + duration
					+ "\"" + dir + File.separator + "QueuedFrames" + File.separator + "OEP%06d." + image2 + "\"");
			if (useRaw)
			{
				command2 = new String(settings.getWaifu() + " -i \"" + dir + File.separator + "QueuedFrames\" -e " + image2 + " -l " + image2 + " -m "
						+ scan.getM() + " -d " + scan.getD() + " -h " + scan.getH() + " -n " + scan.getN() + " -p " + scan.getP() + " -c "
						+ scan.getC() + " -b " + scan.getB() + " --auto_start 1 --auto_exit 1 --no_overwrite 1 -y " + scan.getY() + " -o \"" + dir
						+ File.separator + "IntermediateFrames\"");
				command3 = new String(settings.getRawTherapee() + " -w -o \"" + dir + File.separator + "ProcessedFrames\"" + " -a\"" + dir
						+ File.separator + "IntermediateFrames\" " + "-p \"" + dir + File.separator + "rawTherapee.pp3\" -b8 " + image3 + " -c \""
						+ dir + File.separator + "IntermediateFrames\"");
			}
			else
			{
				command2 = new String(settings.getWaifu() + " -i \"" + dir + File.separator + "QueuedFrames\" -e " + image2 + " -l " + image2 + " -m "
						+ scan.getM() + " -d " + scan.getD() + " -h " + scan.getH() + " -n " + scan.getN() + " -p " + scan.getP() + " -c "
						+ scan.getC() + " -b " + scan.getB() + " --auto_start 1 --auto_exit 1 --no_overwrite 1 -y " + scan.getY() + " -o \"" + dir
						+ File.separator + "ProcessedFrames\"");
				command3 = new String("");
			}
			command4 = new String(settings.getFfmpeg() + " -y -progress \"" + dir + File.separator + "Temp" + File.separator + "log.txt\" -framerate "
					+ scan.getR2() + " " + videoOffset + scan.getDInputString() + "-i \"" + dir + File.separator + "ProcessedFrames" + File.separator
					+ "OEP%06d." + image2 + "\" " + L1.full(dir) + L2.full(dir) + L3.full(dir) + L4.full(dir) + L5.full(dir) + S1.full(dir)
					+ S2.full(dir) + S3.full(dir) + S4.full(dir) + S5.full(dir) + startTime + mappings + scan.getDExportString() + isAudio
					+ isSubtitle + L1.meta() + L2.meta() + L3.meta() + L4.meta() + L5.meta() + S1.meta() + S2.meta() + S3.meta() + S4.meta()
					+ S5.meta() + title + desc + "-r " + scan.getR2() + " -t " + String.valueOf(((int) (totalFrames / Double.valueOf(scan.getR2()))))
					+ " -movflags +faststart \"" + dir + File.separator + "Export" + File.separator + eep + "_" + showName + "." + exportExtension
					+ "\"");
			status = 0;
		}
		else
		{
			commandM4 = new String(settings.getFfmpeg() + " -progress \"" + dir + File.separator + "Temp" + File.separator + "log.txt\" "
					+ videoOffset + " -i \"" + dir + File.separator + "Export" + File.separator + vfilename + "\" " + L1.full(dir) + L2.full(dir)
					+ L3.full(dir) + L4.full(dir) + L5.full(dir) + S1.full(dir) + S2.full(dir) + S3.full(dir) + S4.full(dir) + S5.full(dir)
					+ startTime + mappings + scan.getRExportString() + L1.meta() + L2.meta() + L3.meta() + L4.meta() + L5.meta() + S1.meta()
					+ S2.meta() + S3.meta() + S4.meta() + S5.meta() + "-r " + scan.getR2() + " " + duration + "-movflags +faststart \"" + dir
					+ File.separator + "Export" + File.separator + eep + "_" + showName + "." + exportExtension + "\"");
			status = 0;
			logger.info("Command Strings Initialized");
		}
	}

	public boolean recordProgress()
	{
		String filePath = new String(File.separator + "Temp" + File.separator + "currentProcess.ser");
		try
		{
			FileOutputStream fileOut = new FileOutputStream(dir + filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			logger.info("File Saved: {}", filePath);
			return true;
		}
		catch (IOException i)
		{
			logger.fatal("Failed Save Attempt: {}", filePath);
			logger.fatal("Exception Infomation", i);
			JOptionPane.showMessageDialog(null, "Unable to Access File\n" + filePath + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
			status = 3;
			return false;
		}
	}

	public int getStatus()
	{
		return status;
	}

	public Settings getSettings()
	{
		return settings;
	}

	public String getShowName()
	{
		return showName;
	}

	public String getEp()
	{
		return ep;
	}

	public Timing getL2()
	{
		return L2;
	}

	public Timing getL3()
	{
		return L3;
	}

	public Timing getL4()
	{
		return L4;
	}

	public Timing getL5()
	{
		return L5;
	}

	public Timing getL1()
	{
		return L1;
	}

	public Timing getS1()
	{
		return S1;
	}

	public Timing getS2()
	{
		return S2;
	}

	public Timing getS3()
	{
		return S3;
	}

	public Timing getS4()
	{
		return S4;
	}

	public Timing getS5()
	{
		return S5;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDesc()
	{
		return desc;
	}

	public boolean isRadio1()
	{
		return radio1;
	}

	public boolean isRadio2()
	{
		return radio2;
	}

	public boolean isRadio3()
	{
		return radio3;
	}

	public boolean isRadio4()
	{
		return radio4;
	}

	public boolean isUseRaw()
	{
		return useRaw;
	}

	public String getEep()
	{
		return eep;
	}

	public String getVfilename()
	{
		return vfilename;
	}

	public String getDir()
	{
		return dir;
	}

	public String getMappings()
	{
		return mappings;
	}

	public int getTotalStreams()
	{
		return totalStreams;
	}

	public Calculations getCalc()
	{
		return calc;
	}

	public int getTotalFrames()
	{
		return totalFrames;
	}

	public Reader getScan()
	{
		return scan;
	}

	public String getIsAudio()
	{
		return isAudio;
	}

	public String getIsSubtitle()
	{
		return isSubtitle;
	}

	public int getOldCount()
	{
		return oldCount;
	}

	public JProgressBar getBar1()
	{
		return bar1;
	}

	public JProgressBar getBar2()
	{
		return bar2;
	}

	public JProgressBar getBar3()
	{
		return bar3;
	}

	public JProgressBar getBar4()
	{
		return bar4;
	}

	public JProgressBar getBar5()
	{
		return bar5;
	}

	public JProgressBar getBarM()
	{
		return barM;
	}

	public JButton getExecute()
	{
		return execute;
	}

	public JButton getPause()
	{
		return pause;
	}

	public JButton getAbort()
	{
		return abort;
	}

	public JButton getFullAbort()
	{
		return fullAbort;
	}

	public JTextField getTime()
	{
		return time;
	}

	public JTextArea getOutput()
	{
		return output;
	}

	public JTextArea getError()
	{
		return error;
	}

	public String getDuration()
	{
		return duration;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public String getCommand1()
	{
		return command1;
	}

	public String getCommand2()
	{
		return command2;
	}

	public String getCommand3()
	{
		return command3;
	}

	public String getCommand4()
	{
		return command4;
	}

	public String getCommandM4()
	{
		return commandM4;
	}

	public Process getA()
	{
		return a;
	}

	public Process getB()
	{
		return b;
	}

	public Process getC()
	{
		return c;
	}

	public Process getD()
	{
		return d;
	}

	public Process getM()
	{
		return m;
	}

	public int getMode()
	{
		return mode;
	}

	public boolean isQueue()
	{
		if (mode == 1)
		{
			return true;
		}
		else if (mode == 0)
		{
			return false;
		}
		else
		{
			logger.fatal("Program asked isQueue() of a Process when it in mode 2-Muxing (Should not happen)!");
			System.exit(20);
			return false;
		}
	}

	public CountDownLatch getSelfLatch()
	{
		return selfLatch;
	}

	public CountDownLatch getPauseLatch()
	{
		return pauseLatch;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public int getProgress()
	{
		return progress;
	}

	public boolean isRecovered()
	{
		return recovered;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	public void setShowName(String show)
	{
		this.showName = show;
	}

	public void setEp(String ep)
	{
		this.ep = ep;
	}

	public void setL2(Timing l2)
	{
		L2 = l2;
	}

	public void setL3(Timing l3)
	{
		L3 = l3;
	}

	public void setL4(Timing l4)
	{
		L4 = l4;
	}

	public void setL5(Timing l5)
	{
		L5 = l5;
	}

	public void setL1(Timing l1)
	{
		L1 = l1;
	}

	public void setS1(Timing s1)
	{
		S1 = s1;
	}

	public void setS2(Timing s2)
	{
		S2 = s2;
	}

	public void setS3(Timing s3)
	{
		S3 = s3;
	}

	public void setS4(Timing s4)
	{
		S4 = s4;
	}

	public void setS5(Timing s5)
	{
		S5 = s5;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public void setRadio1(boolean radio1)
	{
		this.radio1 = radio1;
	}

	public void setRadio2(boolean radio2)
	{
		this.radio2 = radio2;
	}

	public void setRadio3(boolean radio3)
	{
		this.radio3 = radio3;
	}

	public void setRadio4(boolean radio4)
	{
		this.radio4 = radio4;
	}

	public void setUseRaw(boolean useRaw)
	{
		this.useRaw = useRaw;
	}

	public void setEep(String eep)
	{
		this.eep = eep;
	}

	public void setVfilename(String vfilename)
	{
		this.vfilename = vfilename;
	}

	public void setDir(String dir)
	{
		this.dir = dir;
	}

	public void setMappings(String mappings)
	{
		this.mappings = mappings;
	}

	public void setTotalStreams(int totalStreams)
	{
		this.totalStreams = totalStreams;
	}

	public void setCalc(Calculations calc)
	{
		this.calc = calc;
	}

	public void setTotalFrames(int totalFrames)
	{
		this.totalFrames = totalFrames;
	}

	public void setScan(Reader scan)
	{
		this.scan = scan;
	}

	public void setIsAudio(String isAudio)
	{
		this.isAudio = isAudio;
	}

	public void setIsSubtitle(String isSubtitle)
	{
		this.isSubtitle = isSubtitle;
	}

	public void setOldCount(int oldCount)
	{
		this.oldCount = oldCount;
	}

	public void setBar1(JProgressBar bar1)
	{
		this.bar1 = bar1;
	}

	public void setBar2(JProgressBar bar2)
	{
		this.bar2 = bar2;
	}

	public void setBar3(JProgressBar bar3)
	{
		this.bar3 = bar3;
	}

	public void setBar4(JProgressBar bar4)
	{
		this.bar4 = bar4;
	}

	public void setBar5(JProgressBar bar5)
	{
		this.bar5 = bar5;
	}

	public void setBarM(JProgressBar barM)
	{
		this.barM = barM;
	}

	public void setExecute(JButton execute)
	{
		this.execute = execute;
	}

	public void setPause(JButton pause)
	{
		this.pause = pause;
	}

	public void setAbort(JButton abort)
	{
		this.abort = abort;
	}

	public void setFullAbort(JButton fullAbort)
	{
		this.fullAbort = fullAbort;
	}

	public void setTime(JTextField time)
	{
		this.time = time;
	}

	public void setOutput(JTextArea output)
	{
		this.output = output;
	}

	public void setError(JTextArea error)
	{
		this.error = error;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public void setCommand1(String command1)
	{
		this.command1 = command1;
	}

	public void setCommand2(String command2)
	{
		this.command2 = command2;
	}

	public void setCommand3(String command3)
	{
		this.command3 = command3;
	}

	public void setCommand4M(String commandM4)
	{
		this.command4 = commandM4;
	}

	public void setCommandM(String string)
	{
		this.commandM4 = string;
	}

	public void setA(Process a)
	{
		this.a = a;
	}

	public void setB(Process b)
	{
		this.b = b;
	}

	public void setC(Process c)
	{
		this.c = c;
	}

	public void setD(Process d)
	{
		this.d = d;
	}

	public void setM(Process m)
	{
		this.m = m;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public void setSelfLatch(CountDownLatch selfLatch)
	{
		this.selfLatch = selfLatch;
	}

	public void setPauseLatch(CountDownLatch pauseLatch)
	{
		this.pauseLatch = pauseLatch;
	}

	public void setPaused(boolean wait)
	{
		this.paused = wait;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public void setRecovered(boolean recovered)
	{
		this.recovered = recovered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		if (status == 0)
		{
			builder.append("Process Parameters \n[");
			builder.append("Showname = ");
			builder.append(showName);
			builder.append("\n Episode Number = ");
			builder.append(ep);
			builder.append("\n Audio 1 = ");
			builder.append(L1);
			builder.append("\n Audio 2 = ");
			builder.append(L2);
			builder.append("\n Audio 3 = ");
			builder.append(L3);
			builder.append("\n Audio 4 = ");
			builder.append(L4);
			builder.append("\n Audio 5 = ");
			builder.append(L5);
			builder.append("\n Subtitle 1 = ");
			builder.append(S1);
			builder.append("\n Subtitle 2 = ");
			builder.append(S2);
			builder.append("\n Subtitle 3 = ");
			builder.append(S3);
			builder.append("\n Subtitle 4 = ");
			builder.append(S4);
			builder.append("\n Subtitle 5 = ");
			builder.append(S5);
			builder.append("\n Title = ");
			builder.append(title);
			builder.append("\n Description = ");
			builder.append(desc);
			builder.append("\n Starting Stage = ");
			if (radio1)
			{
				builder.append("S1");
			}
			else if (radio2)
			{
				builder.append("S2");
			}
			else if (radio2)
			{
				builder.append("S3");
			}
			else if (radio2)
			{
				builder.append("S4");
			}
			else
			{
				builder.append("null");
			}
			builder.append("\n RawTherapee Used = ");
			builder.append(useRaw);
			builder.append("\n Input Video Filename = ");
			builder.append(vfilename);
			builder.append("\n Current Parent Directory = ");
			builder.append(dir);
			builder.append("\n A/S Stream Count = ");
			builder.append(totalStreams);
			builder.append("\n Frame Count = ");
			builder.append(totalFrames);
			builder.append("\n Config Parameters = ");
			builder.append(scan);
			builder.append("\n Finished Video Duration = ");
			builder.append(duration);
			builder.append("\n Video Start Offset = ");
			builder.append(startTime);
			builder.append("\n Current Workload = ");
			if (mode == 0)
			{
				builder.append("Single Process");
			}
			else if (mode == 1)
			{
				builder.append("Queue of Processes");
			}
			else if (mode == 2)
			{
				builder.append("Remuxing Process");
			}
			builder.append("\n Recovered from Crash = ");
			builder.append(recovered);
			if (recovered)
			{

				builder.append("\n progress = ");
				builder.append(progress);
			}
			builder.append("]\n");
		}
		else
		{
			builder.append("Dead Process, Incorrectly Formatted");
		}
		return builder.toString();
	}

	public String getAudioName()
	{
		return audioName;
	}

	public String getExportExtension()
	{
		return exportExtension;
	}

	public void setExportExtension(String exportExtension)
	{
		this.exportExtension = exportExtension;
	}
}
