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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import graphical.MainWindow;
import structures.Report;

/**
 * @author Christian77777
 *
 */
public class StageM2 extends SwingWorker<Void, ProgressTime>
{
	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private Settings settings;
	private String dir;
	private String showName;
	private int ep;
	private Process m2A;
	private Process m2B;
	private JTextArea output;
	private JButton button;
	private JProgressBar bar;
	private int status = 0;
	private String sourceLocation;
	private String chosenLang;
	private String exportCodec;
	private String codecParameters;
	private String extension;
	private boolean extractAll;

	public StageM2(MainWindow window,
			Settings settings,
			String dir,
			String showName,
			String sourceLocation,
			int ep,
			String exportCodec,
			String codecParameters,
			boolean extractAll,
			JButton button,
			JProgressBar bar,
			JTextArea output)
	{
		this.window = window;
		this.settings = settings;
		this.dir = dir;
		this.showName = showName;
		this.ep = ep;
		this.button = button;
		this.bar = bar;
		this.output = output;
		this.exportCodec = exportCodec;
		this.codecParameters = codecParameters;
		this.sourceLocation = sourceLocation;
		this.extractAll = extractAll;
	}

	@Override
	protected Void doInBackground() throws Exception
	{
		Thread.currentThread().setName("AudioExtraction");
		try
		{
			String commandx = new String(settings.getFfprobe() + " -v panic \"" + dir + sourceLocation
					+ "\" -show_entries stream=index:stream=codec_name:stream_tags=language:stream_tags=title -select_streams a -of default=nk=0:nw=0");
			logger.info("Stage M2A: {}", commandx);
			m2A = Runtime.getRuntime().exec(commandx);
			StreamGobbler eater = new StreamGobbler(m2A.getErrorStream());
			eater.start();
			if (!(m2A.waitFor() == 0))
			{
				logger.error("Audio Analysis Failed");
				JOptionPane.showMessageDialog(window, "Audio Analysis Failed", "Process Error", JOptionPane.ERROR_MESSAGE);
				status = 1;
				return null;
			}
			logger.info("Audio Stream Analysis Complete");
			BufferedReader scanner = new BufferedReader(new InputStreamReader(m2A.getInputStream()));
			ArrayList<Integer> mapCount = new ArrayList<Integer>();
			ArrayList<String> codecs = new ArrayList<String>();
			ArrayList<String> languages = new ArrayList<String>();
			ArrayList<String> titles = new ArrayList<String>();
			while (scanner.readLine() != null)
			{
				/*
				 * [STREAM]
				 * index=1
				 * codec_name=ac3
				 * TAG:language=fre
				 * [/STREAM]
				 * [STREAM]
				 * index=2
				 * codec_name=aac
				 * TAG:language=eng
				 * [/STREAM]
				 */
				mapCount.add(Integer.valueOf(scanner.readLine().substring(6)));
				codecs.add(scanner.readLine().substring(11));
				String nextLine = scanner.readLine();
				if (nextLine.substring(0, Math.min(13, nextLine.length())).equals("TAG:language="))
				{
					languages.add(nextLine.substring(13));
					String maybeLine = scanner.readLine();
					if (maybeLine.substring(0, Math.min(10, maybeLine.length())).equals("TAG:title="))
					{
						titles.add(maybeLine.substring(10));
						scanner.readLine();
					}
					else
					{
						titles.add("No Title");
					}
				}
				else if (nextLine.substring(0, Math.min(10, nextLine.length())).equals("TAG:title="))
				{
					languages.add("nul");
					titles.add(nextLine.substring(10));
					scanner.readLine();
				}
				else
				{

				}
			}
			scanner.close();
			if (!extractAll)
			{
				String[] possibleStreams = new String[mapCount.size()];
				for (int x = 0; x < mapCount.size(); x++)
				{
					possibleStreams[x] = mapCount.get(x) + ". " + codecs.get(x) + " - " + languages.get(x) + " - " + titles.get(x);
				}
				if (possibleStreams.length == 0)
				{
					logger.error("No Audio Streams found");
					JOptionPane.showMessageDialog(window, "No Audio Streams found", "No Audio", JOptionPane.ERROR_MESSAGE);
					status = 9;
					return null;
				}
				String option = (String) JOptionPane.showInputDialog(window, "Multiple Audio Streams found\nPlease Select which to Extract",
						"Select Audio Stream", JOptionPane.QUESTION_MESSAGE, null, possibleStreams, possibleStreams[0]);
				if (option == null)
				{
					logger.trace("Audio Extraction Canceled");
					status = 10;
					return null;
				}
				int streamNumber = 0;
				for (int z = 0; z < possibleStreams.length; z++)
				{
					if (option.equals(possibleStreams[z]))
					{
						streamNumber = z;
						chosenLang = languages.get(z);
						if (exportCodec.equals("copy"))
						{
							extension = codecs.get(z);
							codecParameters = "";
						}
						else
						{
							extension = exportCodec;
							codecParameters = (" " + codecParameters.trim());
						}
						break;
					}
				}
				if (!window.canContinue(File.separator + "ImportAudio" + File.separator + languages.get(streamNumber) + "_"
						+ String.format("%03d", ep) + "_" + showName + "." + exportCodec + "\""))
				{
					status = 8;
					return null;
				}
				String commandy = new String(settings.getFfmpeg() + " -y -i \"" + dir + sourceLocation + "\" -map 0:" + mapCount.get(streamNumber)
						+ " -vn -movflags +faststart -acodec " + exportCodec + codecParameters + " \"" + dir + File.separator + "ImportAudio"
						+ File.separator + languages.get(streamNumber) + "_" + String.format("%03d", ep) + "_" + showName + "." + extension + "\"");
				logger.info("Stage M2B: {}", commandy);
				Report r1 = new Report(settings.getClientID(), 12);
				r1.addEvent(12, "Extracting Audio");
				settings.submit(r1);
				m2B = Runtime.getRuntime().exec(commandy);
				StreamGobbler reader = new StreamGobbler(m2B.getInputStream(), output, false);
				eater = new StreamGobbler(m2B.getErrorStream(), output, false);
				reader.start();
				eater.start();
				if (!(m2B.waitFor() == 0))
				{
					Report r2 = settings.getStoredReport();
					r2.setJobFailed(true);
					r2.addEvent(17, "Audio Extraction Failed");
					settings.submit(r2);
					logger.error("Audio Extraction Failed on Stream {}", mapCount.get(streamNumber));
					JOptionPane.showMessageDialog(window, "Audio Extraction Failed on Audio Stream " + mapCount.get(streamNumber), "Process Error", JOptionPane.ERROR_MESSAGE);
					status = 2;
					return null;
				}
				else
				{
					Report r2 = settings.getStoredReport();
					r2.setJobCompleted(true);
					r2.addEvent(16, "Audio Extraction Successful");
					settings.submit(r2);
					logger.info("Audio Extracted");
				}
			}
			else
			{
				for (int z = 0; z < mapCount.size(); z++)
				{
					chosenLang = languages.get(z);
					if (exportCodec.equals("copy"))
					{
						extension = codecs.get(z);
						codecParameters = "";
					}
					else
					{
						extension = exportCodec;
						codecParameters = (" " + codecParameters.trim());
					}
					if (!window.canContinue(File.separator + "ImportAudio" + File.separator + languages.get(z) + "_"
							+ String.format("%03d", ep) + "_" + showName + "." + exportCodec + "\""))
					{
						status = 8;
						return null;
					}
					String commandy = new String(settings.getFfmpeg() + " -y -i \"" + dir + sourceLocation + "\" -map 0:" + mapCount.get(z)
							+ " -vn -movflags +faststart -acodec " + exportCodec + codecParameters + " \"" + dir + File.separator + "ImportAudio"
							+ File.separator + languages.get(z) + "_" + String.format("%03d", ep) + "_" + showName + "." + extension + "\"");
					logger.info("Execution {} of Stage M2B: {}", (z+1), commandy);
					Report r1 = new Report(settings.getClientID(), 12);
					r1.addEvent(12, "Extracting Audio");
					settings.submit(r1);
					m2B = Runtime.getRuntime().exec(commandy);
					StreamGobbler reader = new StreamGobbler(m2B.getInputStream(), output, false);
					eater = new StreamGobbler(m2B.getErrorStream(), output, false);
					reader.start();
					eater.start();
					if (!(m2B.waitFor() == 0))
					{
						Report r2 = settings.getStoredReport();
						r2.setJobFailed(true);
						r2.addEvent(17, "Audio Extraction Failed");
						settings.submit(r2);
						logger.error("Audio Extraction Failed on Stream {}", mapCount.get(z));
						JOptionPane.showMessageDialog(window, "Audio Extraction Failed on Audio Stream " + mapCount.get(z), "Process Error", JOptionPane.ERROR_MESSAGE);
						status = 2;
						return null;
					}
					else
					{
						Report r2 = settings.getStoredReport();
						r2.setJobCompleted(true);
						r2.addEvent(16, "Audio Extraction Successful");
						settings.submit(r2);
						logger.info("{}/{} Audio Extracted",(z+1),mapCount.size());
					}
				}
			}
		}
		catch (InterruptedException | IOException e)
		{
			logger.fatal("Failed Parse Attempt: {}", sourceLocation);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(window, "Unable to Access File\n" + sourceLocation + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		return null;
	}

	@Override
	protected void process(List<ProgressTime> chunks)
	{
		//TODO put Stopwatch in StringText of JProgressBar
	}

	/**
	 * 0 = ok 1 = m2a failed 2 = m2b failed 5 = Video does not Exist 8 = Can not
	 * Overwrite Audio File 9 = No Audio Streams found 10 = Aborted audio Stream
	 * selection
	 */
	@Override
	protected void done()
	{
		window.toggleMenu(1, true);
		button.setEnabled(true);
		button.setText("Execute Audio Extraction");
		if (status == 0)
		{
			bar.setIndeterminate(false);
			bar.setValue(100);
			JOptionPane.showMessageDialog(window,
					"Successfully Extracted Audio\nOutput Folder: " + File.separator + "ImportAudio" + File.separator + chosenLang + "_"
							+ String.format("%03d", ep) + "_" + showName + "." + exportCodec + "\"",
					"Process Complete", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			bar.setIndeterminate(true);
			bar.setForeground(Color.BLACK);
		}

	}
}
