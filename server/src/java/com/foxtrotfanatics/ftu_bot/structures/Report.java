package com.foxtrotfanatics.ftu_bot.structures;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Status Variable
 * 1 = *Connection Made 
 * 100 = *Disconnected
 * 9 = *Message for UserActivity
 * 12 = +QuickScript Executed
 * 16 = -QuickScript Completed
 * 17 = -QuickScript Failed
 * 18 = -QuickScript Aborted
 * 11 = *(+)Multi Queue Execution Started
 * 15 = *Queue Job submission terminator signal
 * 21 = *(a)fully Aborted
 * ^Signals vFilled Processes
 * 0= -Ongoing
 * 10 = +Single Process Execution Started
 * 13 = -Process Completed
 * 14 = *(-)One Process in Queue Started
 * 57 = *Queue job to store/Unstarted Job
 * 50 = -failed (verify in Report Class)
 * 30 = -paused
 * 32 = -resumed
 * 20 = -aborted (single)
 * 5 = -Stage # started
 * 6 = -Stage # completed
 * 8 = -Waifu2x updated
 */

/**
 * 
 * @author Christian77777
 *
 */
public class Report implements java.io.Serializable, Comparable<Report>
{
	/**
	 * Version 3.2.5
	 */
	private static final long serialVersionUID = -4063795450300989584L;
	private int status;
	private boolean operating;
	private boolean jobStarted;
	private boolean jobCompleted;
	private boolean jobFailed;
	private boolean jobAborted;
	private int currentFrame;
	private ArrayList<ProgramEvent> history;
	private boolean paused;
	private int currentStage;
	private String eta;
	// ^Dynamic vStatic (per job)
	private boolean queue;
	private String clientID;
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
	private int startingStage;
	private boolean usingRaw;
	private int totalFrames;
	private Reader reader;
	private String duration;
	private String startTime;
	private String exportExtension;
	private String showName;
	private String command1;
	private String command2;
	private String command3;
	private String command4;
	private String commandm4;
	private int operatingMode;
	private boolean recovered;
	private LocalDateTime firstReportTime = LocalDateTime.now();

	public Report(String id, int status)
	{
		this.clientID = id;
		operating = false;
		recovered = false;
		this.status = status;
		history = new ArrayList<ProgramEvent>();
	}

	public void addEvent(int status, String message)
	{
		history.add(new ProgramEvent(status, message));
		this.status = status;
		if(firstReportTime == null)
		{
			firstReportTime = history.get(history.size()-1).getTime();
		}
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getClientID()
	{
		return clientID;
	}

	public void setClientID(String clientID)
	{
		this.clientID = clientID;
	}

	public boolean isOperating()
	{
		return operating;
	}

	public void setOperating(boolean operating)
	{
		this.operating = operating;
	}

	public boolean isJobStarted()
	{
		return jobStarted;
	}

	public void setJobStarted(boolean jobStarted)
	{
		this.jobStarted = jobStarted;
	}

	public boolean isJobCompleted()
	{
		return jobCompleted;
	}

	public void setJobCompleted(boolean jobCompleted)
	{
		this.jobCompleted = jobCompleted;
	}

	public boolean isJobFailed()
	{
		return jobFailed;
	}

	public void setJobFailed(boolean jobFailed)
	{
		this.jobFailed = jobFailed;
	}

	public boolean isJobAborted()
	{
		return jobAborted;
	}

	public void setJobAborted(boolean jobAborted)
	{
		this.jobAborted = jobAborted;
	}

	public int getCurrentFrame()
	{
		return currentFrame;
	}

	public void setCurrentFrame(int currentFrame)
	{
		this.currentFrame = currentFrame;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void setPaused(boolean paused)
	{
		this.paused = paused;
	}

	public int getCurrentStage()
	{
		return currentStage;
	}

	public void setCurrentStage(int currentStage)
	{
		this.currentStage = currentStage;
	}

	public String getEta()
	{
		return eta;
	}

	public void setEta(String eta)
	{
		this.eta = eta;
	}

	public String getCommand1()
	{
		return command1;
	}

	public void setCommand1(String command1)
	{
		this.command1 = command1;
	}

	public String getCommand2()
	{
		return command2;
	}

	public void setCommand2(String command2)
	{
		this.command2 = command2;
	}

	public String getCommand3()
	{
		return command3;
	}

	public void setCommand3(String command3)
	{
		this.command3 = command3;
	}

	public String getCommand4()
	{
		return command4;
	}

	public void setCommand4(String command4)
	{
		this.command4 = command4;
	}

	public String getCommandm4()
	{
		return commandm4;
	}

	public void setCommandm4(String commandm4)
	{
		this.commandm4 = commandm4;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public ArrayList<ProgramEvent> getHistory()
	{
		return history;
	}

	public boolean isQueue()
	{
		return queue;
	}

	public String getEp()
	{
		return ep;
	}

	public Timing getL1()
	{
		return L1;
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

	public int getStartingStage()
	{
		return startingStage;
	}

	public boolean isUsingRaw()
	{
		return usingRaw;
	}

	public int getTotalFrames()
	{
		return totalFrames;
	}

	public Reader getReader()
	{
		return reader;
	}

	public String getDuration()
	{
		return duration;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public int getOperatingMode()
	{
		return operatingMode;
	}

	public boolean isRecovered()
	{
		return recovered;
	}
	
	public String getExportExtension()
	{
		return exportExtension;
	}

	public void setExportExtension(String exportExtension)
	{
		this.exportExtension = exportExtension;
	}

	public LocalDateTime getFirstReportTime()
	{
		return firstReportTime;
	}
	
	public void setShowName(String show)
	{
		this.showName = show;
	}
	
	public String getShowName()
	{
		return showName;
	}

	@Override
	public int compareTo(Report r)
	{
		if(r.firstReportTime == null && this.firstReportTime == null)
			return clientID.compareTo(r.clientID);
		else if(this.firstReportTime == null)
			return 1;
		else if(r.firstReportTime == null)
			return -1;
		else
			return firstReportTime.compareTo(r.firstReportTime);
	}
	
	@Override
	public String toString()
	{
		return "Status: " + status + " - " + title.substring(17, title.length()-2);
	}
}
