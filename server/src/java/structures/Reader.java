/**
 * Copyright 2018 Christian Devile
 * 
 * This file is part of FTUServerBot.
 * 
 * FTUServerBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FTUServerBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FTUServerBot.  If not, see <http://www.gnu.org/licenses/>.
 */
package structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is the File manager of the Program. It is the Only Class to Read
 * and Write to the File. While providing other Classes with Data to Function.
 * This is also where The list of Recipes is filtered to Show 3 Recommendations
 */
public class Reader implements java.io.Serializable
{

	/**
	 * Version 1.0.0
	 */
	private static Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -1095699862453934587L;
	private String m;
	private String h;
	private String n;
	private String p;
	private String c;
	private String d;
	private String b;
	private String gpu;
	private String tta;
	private String y;
	private String r1;
	private String r2;
	private String deinterlacing;
	private String AInputString;
	private String AExportString;
	private String DInputString;
	private String DExportString;

	public Reader(String m,
			String h,
			String n,
			String p,
			String c,
			String d,
			String b,
			String gpu,
			String tta,
			String y,
			String r1,
			String r2,
			String deinterlacing,
			String AInputString,
			String AExportString,
			String DInputString,
			String DExportString)
	{
		this.m = m;
		this.h = h;
		this.n = n;
		this.p = p;
		this.c = c;
		this.d = d;
		this.b = b;
		this.gpu = gpu;
		this.tta = tta;
		this.y = y;
		this.r1 = r1;
		this.r2 = r2;
		this.deinterlacing = deinterlacing;
		this.AInputString = AInputString;
		this.AExportString = AExportString;
		this.DInputString = DInputString;
		this.DExportString = DExportString;
	}

	/** Sole Constructor used to get Location of Save Data */
	public Reader(String directory)
	{
		File file = new File(directory + File.separator + "config.txt");
		logger.debug("Attempting Parse: {}config.txt", File.separator);
		try(Scanner i = new Scanner(file);)
		{
			String result = i.nextLine();
			if (result.equals("[Video Upscaler Config Settings]"))
			{
				//TODO Notify User or Something
				readOld(i);
				if (!file.renameTo(new File(directory + File.separator + "outdated_config.txt")))
					logger.warn("Could not backup old Config");
				if (!file.exists())
				{
					if (!file.delete())
					{
						logger.fatal("Failed Delete Attempt: {}config.txt", File.separator);
						JOptionPane.showMessageDialog(null, "Unable to Access File\n" + File.separator + "config.txt\nCrashing Program",
								"IO Exception", JOptionPane.ERROR_MESSAGE);
						System.exit(20);

					}
				}
				writeNew(directory);
			}
			else if (result.equals("[FoxTrotUpscaler Extra Settings]"))
			{
				i.nextLine();
				i.nextLine();
				i.nextLine();
				i.nextLine();
				i.nextLine();
				i.nextLine();
				m = i.nextLine().substring(30).trim();
				h = i.nextLine().substring(24).trim();
				n = i.nextLine().substring(23).trim();
				p = i.nextLine().substring(26).trim();
				c = i.nextLine().substring(27).trim();
				d = i.nextLine().substring(36).trim();
				b = i.nextLine().substring(32).trim();
				gpu = i.nextLine().substring(34).trim();
				tta = i.nextLine().substring(36).trim();
				y = i.nextLine().substring(25).trim();
				r1 = i.nextLine().substring(33).trim();
				r2 = i.nextLine().substring(33).trim();
				deinterlacing = i.nextLine().substring(56).trim();
				if (deinterlacing.length() != 0)
				{
					deinterlacing = (deinterlacing + " ");
				}
				AInputString = i.nextLine().substring(50).trim();
				if (AInputString.length() != 0)
				{
					AInputString = (AInputString + " ");
				}
				AExportString = i.nextLine().substring(44).trim();
				if (AExportString.length() != 0)
				{
					AExportString = (AExportString + " ");
				}
				DInputString = i.nextLine().substring(50).trim();
				if (DInputString.length() != 0)
				{
					DInputString = (DInputString + " ");
				}
				DExportString = i.nextLine().substring(44).trim();
				if (DExportString.length() != 0)
				{
					DExportString = (DExportString + " ");
				}
				logger.info("Parsed Confg: {}config.txt", File.separator);
			}
			else
			{
				logger.error("Unexpected Text, Unable to Parse: {}config.txt",File.separator);
				JOptionPane.showMessageDialog(null,
						"\"config.txt\" Improperly Formatted\nPlease Restore Sample Config file\n(Delete config.txt and rename Sample.txt to config.txt)",
						"Critical Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (FileNotFoundException e)
		{
			logger.error("Failed Parse Attempt: {}config.txt", File.separator);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "config.txt not found", "File Input Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (NoSuchElementException | IllegalStateException | StringIndexOutOfBoundsException e)
		{
			logger.error("File Not Found: {}config.txt", File.separator);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null,
					"\"config.txt\" Improperly Formatted\nPlease Restore Sample Config file\n(Delete config.txt and rename Sample.txt to config.txt)",
					"Critical Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void writeNew(String directory)
	{
		try (PrintWriter w = new PrintWriter(directory + File.separator + "config.txt");
				BufferedReader r = new BufferedReader(new InputStreamReader(Reader.class.getResourceAsStream("/resources/config.txt")));)
		{
			String line;
			w.println(r.readLine());
			w.println(r.readLine());
			w.println(r.readLine());
			w.println(r.readLine());
			w.println(r.readLine());
			w.println(r.readLine());
			w.println(r.readLine().substring(0, 30) + m);
			w.println(r.readLine().substring(0, 24) + h);
			w.println(r.readLine().substring(0, 23) + n);
			w.println(r.readLine().substring(0, 26) + p);
			w.println(r.readLine().substring(0, 27) + c);
			w.println(r.readLine().substring(0, 36) + d);
			w.println(r.readLine().substring(0, 32) + b);
			w.println(r.readLine().substring(0, 34) + gpu);
			w.println(r.readLine().substring(0, 36) + tta);
			w.println(r.readLine().substring(0, 25) + y);
			w.println(r.readLine().substring(0, 33) + r1);
			w.println(r.readLine().substring(0, 33) + r1);
			w.println(r.readLine().substring(0, 56) + deinterlacing);
			w.println(r.readLine().substring(0, 50) + AInputString);
			w.println(r.readLine().substring(0, 44) + AExportString);
			w.println(r.readLine().substring(0, 50) + DInputString);
			w.println(r.readLine().substring(0, 44) + DExportString);
			while ((line = r.readLine()) != null)
			{
				w.println(line);
			}
			w.flush();
		}
		catch (FileNotFoundException e)
		{

		}
		catch (IOException e)
		{

		}
	}

	private void readOld(Scanner i)
	{
		i.nextLine();
		i.nextLine();
		i.nextLine();
		i.nextLine();
		m = i.nextLine().substring(30).trim();
		h = i.nextLine().substring(24).trim();
		n = i.nextLine().substring(23).trim();
		p = i.nextLine().substring(26).trim();
		c = i.nextLine().substring(27).trim();
		d = i.nextLine().substring(36).trim();
		b = i.nextLine().substring(32).trim();
		gpu = i.nextLine().substring(34).trim();
		tta = i.nextLine().substring(36).trim();
		y = i.nextLine().substring(25).trim();
		r1 = i.nextLine().substring(33).trim();
		r2 = i.nextLine().substring(33).trim();
		deinterlacing = i.nextLine().substring(62).trim();
		if (deinterlacing.length() != 0)
		{
			deinterlacing = (deinterlacing + " ");
		}
		DExportString = i.nextLine().substring(43).trim();
		if (DExportString.length() != 0)
		{
			DExportString = (DExportString + " ");
		}
		AInputString = i.nextLine().substring(48).trim();
		if (AInputString.length() != 0)
		{
			AInputString = (AInputString + " ");
		}
		AExportString = "";
		DInputString = "";
		i.close();
		logger.info("Parsed Old Confg: {}config.txt", File.separator);
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
		builder.append("Reader Parameters\n\t[Waifu2x_Applied_Operations = ");
		builder.append(m);
		builder.append("\n\t Waifu2x_Scale_Height = ");
		builder.append(h);
		builder.append("\n\t Waifu2x_Noise_Level = ");
		builder.append(n);
		builder.append("\n\t Waifu2x_Processor_Type = ");
		builder.append(p);
		builder.append("\n\t Waifu2x_Image_Crop_Size = ");
		builder.append(c);
		builder.append("\n\t Waifu2x_Image_Output_Data_Output = ");
		builder.append(d);
		builder.append("\n\t Waifu2x_Simultaneous_Threads = ");
		builder.append(b);
		builder.append("\n\t Waifu2x_Multi_GPU_Selection = ");
		builder.append(gpu);
		builder.append("\n\t Waifu2x_Test_Time_Augmentation = ");
		builder.append(tta);
		builder.append("\n\t Waifu2x_Upscale_Model = ");
		builder.append(y);
		builder.append("\n\t Import r = ");
		builder.append(r1);
		builder.append("\n\t Export r = ");
		builder.append(r2);
		builder.append("\n\t Deinterlacing parameters = ");
		builder.append(deinterlacing);
		builder.append("\n\t Stage A Input Source Parameters = ");
		builder.append(AInputString);
		builder.append("\n\t Stage A Export Parameters = ");
		builder.append(AExportString);
		builder.append("\n\t Stage D Input Source Parameters = ");
		builder.append(DInputString);
		builder.append("\n\t Stage D Export Parameters = ");
		builder.append(DExportString);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * public String getExportFrames()
	 * {
	 * try
	 * {
	 * String submodel = new String();
	 * if(y.equals("upconv_7_anime_style_art_rgb"))
	 * {
	 * submodel = "UpRGB";
	 * }
	 * else if(y.equals("upconv_7_photo"))
	 * {
	 * submodel = "UpPhoto";
	 * }
	 * else if(y.equals("anime_style_art_rgb"))
	 * {
	 * submodel = "RGB";
	 * }
	 * else if (y.equals("photo"))
	 * {
	 * submodel = "Photo";
	 * }
	 * else if(y.equals("anime_style_art_y"))
	 * {
	 * submodel = "Y";
	 * }
	 * else
	 * {
	 * throw new IllegalArgumentException();
	 * }
	 * String outputPath = new String(File.separator + "ProcessedFrames" + File.separator + "QueuedFrames multi(" + submodel
	 * + ")(" + m + ")(Level" + n + ")(height " + h + ")" + File.separator + "QueuedFrames\" ");
	 * System.out.println("GUI Guessed Output Path: " + outputPath);
	 * return (dir + outputPath);
	 * }
	 * catch(IllegalArgumentException i)
	 * {
	 * System.out.println("Model Label in Config, incorrect, restore sample config");
	 * System.exit(6);
	 * }
	 * return null;
	 * }
	 */
	public String getM()
	{
		return m;
	}

	public void setM(String m)
	{
		this.m = m;
	}

	public String getH()
	{
		return h;
	}

	public void setH(String h)
	{
		this.h = h;
	}

	public String getN()
	{
		return n;
	}

	public void setN(String n)
	{
		this.n = n;
	}

	public String getP()
	{
		return p;
	}

	public void setP(String p)
	{
		this.p = p;
	}

	public String getC()
	{
		return c;
	}

	public void setC(String c)
	{
		this.c = c;
	}

	public String getD()
	{
		return d;
	}

	public void setD(String d)
	{
		this.d = d;
	}

	public String getB()
	{
		return b;
	}

	public void setB(String b)
	{
		this.b = b;
	}

	public String getGpu()
	{
		return gpu;
	}

	public void setGpu(String gpu)
	{
		this.gpu = gpu;
	}

	public String getTta()
	{
		return tta;
	}

	public void setTta(String tta)
	{
		this.tta = tta;
	}

	public String getY()
	{
		return y;
	}

	public void setY(String y)
	{
		this.y = y;
	}

	public String getR1()
	{
		return r1;
	}

	public void setR1(String r)
	{
		this.r1 = r;
	}

	public String getR2()
	{
		return r2;
	}

	public void setR2(String r)
	{
		this.r2 = r;
	}

	public String getDeinterlacing()
	{
		return deinterlacing;
	}

	public void setDeinterlacing(String deinterlacing)
	{
		this.deinterlacing = deinterlacing;
	}

	public String getAInputString()
	{
		return AInputString;
	}

	public void setAInputString(String aInputString)
	{
		AInputString = aInputString;
	}

	public String getAExportString()
	{
		return AExportString;
	}

	public void setAExportString(String aExportString)
	{
		AExportString = aExportString;
	}

	public String getDInputString()
	{
		return DInputString;
	}

	public void setDInputString(String dInputString)
	{
		DInputString = dInputString;
	}

	public String getDExportString()
	{
		return DExportString;
	}

	public void setDExportString(String dExportString)
	{
		DExportString = dExportString;
	}
}
