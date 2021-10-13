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
package structures;

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
 * @author Christian
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
		this.status = status;
		history = new ArrayList<ProgramEvent>();
	}
	
	public Report(String id, int status, String message)
	{
		this.clientID = id;
		this.status = status;
		this.title = message;
		history = new ArrayList<ProgramEvent>();
	}

	/**
	 * Constructor to create report with Process information
	 * 
	 * @param id The Client Name of this instance of FTU
	 * @param status Integer code, summary of what this action is
	 * @param block The Process to represent to the server
	 * @param startedProcess Mark if this is currently being run, or queued
	 */
	public Report(String id, int status, DataBlock block, boolean startedProcess)
	{
		if (block.getStatus() == 1)
		{
			this.status = 50;
			operating = true;
			jobStarted = true;
			jobCompleted = false;
			jobFailed = true;
			jobAborted = false;
			currentFrame = 0;
			history = new ArrayList<ProgramEvent>();
			history.add(new ProgramEvent(50, "Process Failed to Start"));
		}
		else
		{
			this.status = status;
			clientID = id;
			operating = !block.isPaused();
			jobStarted = startedProcess;
			jobCompleted = false;
			jobFailed = false;
			jobAborted = false;
			queue = block.isQueue();
			ep = block.getEp();
			L1 = block.getL1();
			L2 = block.getL2();
			L3 = block.getL3();
			L4 = block.getL4();
			L5 = block.getL5();
			S1 = block.getS1();
			S2 = block.getS2();
			S3 = block.getS3();
			S4 = block.getS4();
			S5 = block.getS5();
			title = block.getTitle();
			desc = block.getDesc();
			if (block.isRadio1())
			{
				startingStage = 1;
			}
			else if (block.isRadio2())
			{
				startingStage = 2;
			}
			else if (block.isRadio3())
			{
				startingStage = 3;
			}
			else if (block.isRadio4())
			{
				startingStage = 4;
			}
			else
			{
				throw new IllegalArgumentException("Starting Stage Not Selected");
			}
			usingRaw = block.isUseRaw();
			totalFrames = block.getTotalFrames();
			currentFrame = 0;
			history = new ArrayList<ProgramEvent>();
			if (startedProcess)
			{
				history.add(new ProgramEvent(status, "Process Started"));
			}
			else
			{
				firstReportTime = null;
			}
			reader = block.getScan();
			duration = block.getDuration();
			startTime = block.getStartTime();
			command1 = block.getCommand1();
			command2 = block.getCommand2();
			command3 = block.getCommand3();
			command4 = block.getCommand4();
			commandm4 = block.getCommandM4();
			exportExtension = block.getExportExtension();
			operatingMode = block.getMode();
			paused = block.isPaused();
			recovered = block.isRecovered();
			currentStage = block.getProgress();
			eta = "";
			showName = block.getShowName();
		}
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
