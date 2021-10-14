package com.foxtrotfanatics.ftu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Calculations implements java.io.Serializable
{
	/**
	 * Version 1.0.0
	 */
	private static Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 2867541706582314555L;
	private transient String ffmpeg;
	private int totalCount;
	private String vfilename;
	private int status;
	
	public Calculations(String ffmpeg, String fn)
	{
		this.ffmpeg = ffmpeg;
		vfilename = fn;
	}
	
	public int getTotalFrameCount(String dir)
	{
		status = 0;
		String quickPath = new String(File.separator + "ImportVideo" + File.separator + vfilename);
		File test = new File(dir + quickPath);
		if(!test.exists())
		{
			logger.error("File Not Found: {}",quickPath);
			return -1;
		}
		try
		{
			String command0 = new String(ffmpeg + " -v panic -count_frames -select_streams v:0 -show_entries stream=nb_read_frames -of default=nokey=1:noprint_wrappers=1 \""
							+ dir + quickPath + "\"");
			logger.info("Stage 0: {}", command0);
			Process p =  Runtime.getRuntime().exec(command0);
			StreamGobbler eater = new StreamGobbler(p.getErrorStream());
			eater.start();
			int errCode = p.waitFor();
			if (errCode != 0)
			{
				logger.error("Exit Value for Stage 0: {}", errCode);
				JOptionPane.showMessageDialog(null,
							"Fatal Crash of Initial Frame Counter\nPlease look at the Console for assistance\nProblem might be with Video",
							"FFprobe Crash", JOptionPane.ERROR_MESSAGE);
				status = -2;
			}
			else
			{
				logger.info("Exit Value for Stage 0: {}", errCode);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			String temp1 = new String(builder.toString().trim());
			logger.info("Total Number of Frames: {}", temp1);
			int result = Integer.valueOf(temp1);
			totalCount = result;
			if(status == 0)
			{
				return result;
			}
			else
			{
				return status;
			}
		}
		catch (NumberFormatException e1)
		{
			logger.error("Unparseable Number was returned for Stage 0");
			logger.fatal("Exception Infomation", e1);
			return -1;
		}
		catch (IOException e2)
		{
			logger.fatal("Stage 0 unable to return Output: IO Exception");
			logger.fatal("Exception Infomation", e2);
			JOptionPane.showMessageDialog(null,
					"Unable to Access Output\n" + quickPath + "\nCrashing Program",
					"IO Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(20);
			return -2;
		}
		catch (InterruptedException e3)
		{
			logger.fatal("Stage 0 unable to Complete");
			logger.fatal("Exception Infomation", e3);
			System.exit(20);
			return -3;
		}
	}

	public String getffmpeg()
	{
		return ffmpeg;
	}
	
	public void setffmpeg(String ffmpeg)
	{
		this.ffmpeg = ffmpeg;
	}
	
	public int getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
	}

	public String getVfilename()
	{
		return vfilename;
	}

	public void setVfilename(String vfilename)
	{
		this.vfilename = vfilename;
	}

	public String readLastLines(File file, int lines)
	{
		java.io.RandomAccessFile fileHandler = null;
		try
		{
			fileHandler = new java.io.RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--)
			{
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA)
				{
					if (filePointer < fileLength)
					{
						line = line + 1;
					}
				}
				else if (readByte == 0xD)
				{
					if (filePointer < fileLength - 1)
					{
						line = line + 1;
					}
				}
				if (line >= lines)
				{
					break;
				}
				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		}
		catch (java.io.FileNotFoundException e)
		{
			logger.fatal("Exception Infomation", e);
			return null;
		}
		catch (java.io.IOException e)
		{
			logger.fatal("Exception Infomation", e);
			return null;
		}
		finally
		{
			if (fileHandler != null) try
			{
				fileHandler.close();
			}
			catch (IOException e)
			{
			}
		}
	};

	public int progress(int current)
	{
		int value;
		try
		{
			value = (current * 100) / totalCount;
			return value;
		}
		catch (Exception m)
		{
			logger.fatal("Exception Infomation", m);
		}
		return -1;
	}
}
