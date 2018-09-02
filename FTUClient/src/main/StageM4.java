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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import graphical.MainWindow;
import structures.DataBlock;
import structures.Report;

public class StageM4 extends SwingWorker<Void, ProgressTime>
{

	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private DataBlock block;
	private Process m;
	private String finalAudioName;
	private int totalFrames;

	public StageM4(MainWindow window, DataBlock block, String finalAudioName)
	{
		this.window = window;
		this.block = block;
		totalFrames = block.getTotalFrames();
		this.finalAudioName = finalAudioName;
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		block.getOutput().append(
				"\nStarting Remux Process" + "--------------------------------------------------------------------------------------------\n");
		logger.info("Duration of Video: " + Integer.valueOf(((int)(totalFrames / Double.valueOf(block.getScan().getR2())))));
		String quickPath = new String(
				File.separator + "Export" + File.separator + block.getEep() + "_" + block.getShowName() + "." + block.getExportExtension());
		try
		{
			Files.deleteIfExists(Paths.get(block.getDir() + quickPath));
			logger.info("File Deleted: {}", quickPath);
		}
		catch (IOException p)
		{
			logger.fatal("Failed Delete Attempt: {}", quickPath);
			logger.fatal("Exception Infomation", p);
			JOptionPane.showMessageDialog(null, "Unable to Delete Files\n" + quickPath + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		logger.info("Stage M4: ({})", block.getCommandM4());
		m = Runtime.getRuntime().exec(block.getCommandM4());
		block.setM(m);
		StreamGobbler reader = new StreamGobbler(m.getInputStream(), block.getError(), false);
		StreamGobbler eater = new StreamGobbler(m.getErrorStream(), block.getOutput(), false);
		reader.start();
		eater.start();
		Report r = new Report(block.getSettings().getClientID(), 12);
		r.addEvent(12, "Remuxing Video");
		r.setCommandm4(block.getCommandM4());
		r.setJobStarted(true);
		block.getSettings().submit(r);
		int exitValue = m.waitFor();
		logger.info("Exit Value for Stage M4: {}", exitValue);
		if (exitValue != 0 && block.getStatus() != 1)
		{
			Report r2 = block.getSettings().getStoredReport();
			r2.setJobFailed(true);
			r2.addEvent(17, "Video Remuxing Failed");
			block.getSettings().submit(r2);
			logger.error("Stage M4 Crashed");
			window.toggleMenu(1, true);
			block.setStatus(1);
			JOptionPane.showMessageDialog(window, "Fatal Crash of Remux Process\nPlease look at the Error for assistance\nThen Restart the Program",
					"Critical Error", JOptionPane.ERROR_MESSAGE);
		}
		if (block.getStatus() == 0)
		{
			Report r2 = block.getSettings().getStoredReport();
			r2.setJobCompleted(true);
			r2.addEvent(16, "Video Remuxing Completed");
			block.getSettings().submit(r2);
			File product = new File(block.getDir() + File.separator + "Export" + File.separator + block.getEep() + "_" + block.getShowName() + "."
					+ block.getExportExtension());
			File fullName = new File(block.getDir() + File.separator + "Export" + File.separator + "Remuxed_" + finalAudioName + "_"
					+ block.getEep() + "_" + block.getShowName() + "." + block.getExportExtension());
			if (!fullName.exists())
			{
				product.renameTo(fullName);
			}
			else
			{
				//TODO
			}
		}
		return null;
	}

	@Override
	protected void done()
	{
		if (block.getStatus() == 0)
		{
			logger.info("Stage M4 Completed");
			window.toggleMenu(1, true);
			block.getExecute().setText("Select Remux Process");
			block.getExecute().setEnabled(true);
			block.getBarM().setIndeterminate(false);
			block.getBarM().setValue(100);
			JOptionPane.showMessageDialog(
					window, "Program Successfully Remuxed Video\nOutput Folder: " + File.separator + "Export" + File.separator + "Upscaled_" +
							finalAudioName + "_" + block.getEep() + "_" + block.getShowName() + "." + block.getExportExtension(),
					"Process Complete", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			window.toggleMenu(1, true);
			block.getExecute().setText("Select Remux Process");
			block.getExecute().setEnabled(true);
			block.getBarM().setIndeterminate(false);
			block.getBarM().setValue(100);
			block.getBarM().setForeground(Color.BLACK);
		}
	}
}