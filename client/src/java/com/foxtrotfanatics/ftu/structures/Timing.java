package com.foxtrotfanatics.ftu.structures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timing implements java.io.Serializable
{
	private static Logger logger = LogManager.getLogger();
	/**
	 * Created for Version 2.12.0
	 */
	private static final long serialVersionUID = 6424982509093476236L;
	private static final String[] langs = new String[]
	{ "eng", "fre", "spa", "ita", "pol" };
	private static final String[] languages = new String[]
	{ "English", "French", "Spanish", "Italian", "Polish" };
	/** Path to file starting from "/ImportAudio..." */
	private String relativeLoc; // Lang = true, Sub = false
	/** mapping label, "-map 2:a" or "-map 4:s" */
	private String map;
	/** Language Code Chosen from Dropdown Menu */
	private String lang;
	/** Stream title name, aka handle */
	private String handle;
	/** Offset used, stored as "-itsoffset ##:##:##.### " */
	private String offset;
	/** Index of stream, like "1:a" or "2:s" */
	private String type;
	/** Activated Stream, or Placeholder for null. */
	private boolean real;

	public Timing()
	{
		real = false;
	}

	public Timing(String loc, String num, String langt, String title, String offsett, String t)
	{
		relativeLoc = loc;
		map = num;
		lang = new String(langt);
		this.handle = title;
		offset = new String(offsett);
		type = t;
		real = true;
	}

	public Timing(String substring)
	{
		if (substring.length() == 0)
		{
			real = false;
		}
		else
		{
			real = true;
		}
	}

	public boolean gR()
	{
		return real;
	}

	public String gF()
	{
		return relativeLoc;
	}

	public void sF(String type)
	{
		this.relativeLoc = type;
	}

	public String gM()
	{
		return map;
	}

	public void sM(String number)
	{
		this.map = number;
	}

	public String gL()
	{
		return lang;
	}

	public void sL(String lang)
	{
		this.lang = lang;
	}

	public String gH()
	{
		return handle;
	}

	public void sH(String title)
	{
		this.handle = title;
	}

	public String gO()
	{
		return offset;
	}

	public void sO(String offset)
	{
		this.offset = offset;
	}

	public String gT()
	{
		return type;
	}

	public void sT(String type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return new String("Mapping: " + map + " | Lang: " + lang + " | Title: " + handle + " | Offset: (" + offset + ") | File Path: " + relativeLoc);
	}

	public static String fullLang(String partLang)
	{
		for (int x = 0; x < langs.length; x++)
		{
			if (langs[x].equals(partLang))
			{
				return languages[x];
			}
		}
		logger.fatal("Illegal Language Passed");
		throw new IllegalArgumentException("Invalid Language: " + partLang);
	}

	public String meta()
	{
		if (real)
		{
			return new String("-metadata:s:" + type + " language=" + lang + " -metadata:s:" + type + " title=\"" + handle + "\" ");
		}
		return "";
	}

	public String full(String dir)
	{
		if (real)
		{
			return new String(offset + " -i " + "\"" + dir + relativeLoc + "\" ");
		}
		return "";
	}

	public static String[] getLangs()
	{
		return langs;
	}

	public static String[] getLanguages()
	{
		return languages;
	}
}