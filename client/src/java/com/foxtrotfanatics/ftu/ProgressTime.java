package com.foxtrotfanatics.ftu;

public class ProgressTime
{
	private int time;
	private String progress;
	
	public ProgressTime(int time, String progress)
	{
		this.time = time;
		this.progress = progress;
	}

	public int getTime()
	{
		return time;
	}

	public void setTime(int time)
	{
		this.time = time;
	}

	public String getProgress()
	{
		return progress;
	}

	public void setProgress(String progress)
	{
		this.progress = progress;
	}

	
}
