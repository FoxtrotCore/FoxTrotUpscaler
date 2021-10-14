package com.foxtrotfanatics.ftu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JTextArea;

public class StreamGobbler extends Thread
{
	private final InputStream is;
	private final JTextArea text;
	private boolean type;
	private int rawFrameSkip;
	private boolean justSkipped;

	public StreamGobbler(InputStream is, JTextArea text, boolean japanese)
	{
		this.is = is;
		this.text = text;
		this.type = japanese;
		rawFrameSkip = 0;
		justSkipped = false;
	}

	public StreamGobbler(InputStream is)
	{
		this.is = is;
		text = null;
	}

	public void run()
	{
		try
		{
			InputStreamReader isr;
			if (type)
			{
				isr = new InputStreamReader(is, "SHIFT-JIS");
			}
			else
			{
				isr = new InputStreamReader(is);
			}
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
			{
				line.trim();
				if (text != null && !line.equals(""))
				{
					if (line.equals(" already exists: use -Y option to overwrite. This image has been skipped."))
					{
						rawFrameSkip++;
						justSkipped = true;
					}
					else if(!line.equals(" already exists: use -Y option to overwrite. This image has been skipped.") && justSkipped)
					{
						text.append(rawFrameSkip + " Frames Skipped from Step 3, likely from a Pause/Resume or Recovery\n");
						text.append(line + "\n");
						justSkipped = false;
						rawFrameSkip = 0;
					}
					else
					{
						text.append(line + "\n");
					}
					// TODO JScrollBar vertical = scrollPane.getVerticalScrollBar();
					// TODO vertical.setValue( vertical.getMaximum() );
				}
				else
				{

				}
			}
		}
		catch (IOException ioe)
		{
			if (text != null)
			{
				text.append(ioe.toString());
				ioe.printStackTrace();
				System.exit(20);
			}
			else
			{
				ioe.printStackTrace();
				System.exit(20);
			}
		}
	}

	public boolean isType()
	{
		return type;
	}

	public void setType(boolean type)
	{
		this.type = type;
	}
}