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
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataFilter implements FilenameFilter
{
	private static Logger logger = LogManager.getLogger();

	public DataFilter()
	{

	}

	@Override
	public boolean accept(File qDirectory, String filename)
	{
		String quickPath = new String(File.separator + "Queue" + File.separator + filename);
		boolean substring;
		try
		{
			Integer.parseInt(filename.substring(1, 4));
			substring = true;
		}
		catch (NumberFormatException | IndexOutOfBoundsException a)
		{
			substring = false;
		}
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		if (filename.endsWith(".ser") && filename.startsWith("Q") && filename.length() == 8 && substring)
		{
			try
			{
				fileIn = new FileInputStream(qDirectory + File.separator + filename);
				in = new ObjectInputStream(fileIn);
				in.readObject();
				in.close();
				fileIn.close();
				logger.debug("Queue File Parsed: {}", quickPath);
				return true;
			}
			catch (ClassNotFoundException | InvalidClassException | StreamCorruptedException | OptionalDataException b)
			{
				try
				{
					if (in != null)
					{
						in.close();
					}
					if (fileIn != null)
					{
						fileIn.close();
					}
					logger.error("Queue File Corrupted: {} - Deleting File", quickPath);
					logger.catching(b);
					JOptionPane.showMessageDialog(null,
							"Program Failed to interpret File:\n" + quickPath
									+ "\nIs this a actual Queue File?\nHas this been tampered with?\nNow Deleting...",
							"Corrupted File", JOptionPane.ERROR_MESSAGE);
					Files.deleteIfExists(new File(qDirectory.getAbsolutePath() + File.separator + filename).toPath());
				}
				catch (IOException e)
				{
					logger.fatal("Failed Delete Attempt: {}", quickPath);
					logger.catching(e);
					JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program",
							"IO Exception", JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
			catch (IOException f)
			{
				logger.fatal("Failed Parse Attempt: {}", quickPath);
				logger.catching(f);
				JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program",
						"IO Exception", JOptionPane.ERROR_MESSAGE);
				System.exit(20);
			}
		}
		else
		{
			try
			{
				logger.error("Incorrectly Labeled File: {}", quickPath);
				JOptionPane.showMessageDialog(null,
						"Name Formatted incorrectly:\n" + quickPath + "\nDid you place this here?\nNow Deleting...",
						"Corrupted File", JOptionPane.ERROR_MESSAGE);
				Files.deleteIfExists(new File(qDirectory.getAbsolutePath() + File.separator + filename).toPath());
			}
			catch (IOException e)
			{
				logger.fatal("Failed Delete Attempt: {}", quickPath);
				logger.catching(e);
				JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + File.separator + "Queue"
						+ File.separator + filename + "\nCrashing Program", "IO Exception", JOptionPane.ERROR_MESSAGE);
				System.exit(20);
			}
		}
		return false;
	}
}
