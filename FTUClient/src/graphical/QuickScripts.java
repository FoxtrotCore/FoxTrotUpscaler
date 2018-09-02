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
package graphical;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import main.LimitLinesDocumentListener;
import main.Settings;
import main.Stage0;
import main.StageM2;
import main.StageM3;
import main.StageM4;
import main.StreamGobbler;
import net.miginfocom.swing.MigLayout;
import structures.DataBlock;
import structures.Reader;
import structures.Report;
import structures.Timing;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class QuickScripts extends JPanel
{
	private static Logger logger = LogManager.getLogger();
	private JSpinner spinner_M1;
	private JSpinner spinner_M2;
	private JButton btnExecuteRemux;
	private JProgressBar barM4;
	private JProgressBar barM2;
	private JProgressBar barM1;
	private JButton btnDeinterlace;
	private JButton btnAudioExtraction;
	private JTextArea reportArea;
	private Stage0 stage0;
	private StageM3 stageM3;
	private JButton btnManual;
	private JProgressBar barM3A;
	private JProgressBar barM3B;
	private JRadioButton radioBoth;
	private JLabel lblManualImageRetouching;
	private JSeparator separator_4;
	private JTextField txtTimeEstimate;
	private JRadioButton radioRaw;
	private JRadioButton radioWaifu;
	private JButton btnAbort;
	private JComboBox<String> comboBoxShowName_M1;
	private JComboBox<String> comboBoxShowName_M2;
	private JLabel lblShowName;
	private JLabel lblShowName_1;
	private TrackSelection trackSelection;
	private JLabel lblExportEncoding;
	private JComboBox<String> m2codec;
	private JLabel lblEncoding;
	private JComboBox<String> m2cParameters;
	private JButton btnExtractAll;

	public QuickScripts(MainWindow window, String dir, Settings settings)
	{
		setLayout(new MigLayout("", "[][grow][][][][grow][grow][grow]", "[][][][][][fill][][][][][][][][][][][][][][][grow]"));

		trackSelection = new TrackSelection(dir);
		add(trackSelection, "cell 0 15 8 1,grow");

		JLabel lblEpNumber = new JLabel("Episode Number:");
		add(lblEpNumber, "cell 0 1,alignx trailing,growy");

		btnDeinterlace = new JButton("Deinterlace Script");
		btnDeinterlace.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Deinterlace Process Requested");
				String source = new String(File.separator + "ImportVideo" + File.separator + "Interlaced_"
						+ new String(String.format("%03d", (Integer) spinner_M1.getValue())) + "_" + (String) comboBoxShowName_M1.getSelectedItem());
				String extension = TrackSelection.findExtension(window, dir + source);
				if (extension == null)
				{
					logger.error("Input Not Found or Invalid Extension: {}", source);
					barM1.setValue(100);
					barM1.setIndeterminate(true);
					JOptionPane.showMessageDialog(window,
							"Video Source not found, or not Labeled as " + source + ".<extension>\nEntered EP may be different then intended",
							"Input Not Found", JOptionPane.ERROR_MESSAGE);
					barM1.setIndeterminate(false);
					barM1.setForeground(Color.BLACK);
					return;
				}
				String sourceLocation = source + extension;
				String product = new String(File.separator + "ImportVideo" + File.separator + "Input_"
						+ new String(String.format("%03d", (Integer) spinner_M1.getValue())) + "_" + (String) comboBoxShowName_M1.getSelectedItem()
						+ extension);
				if (window.canContinue(product))
				{
					File export = new File(dir + sourceLocation);
					if (export.exists())
					{
						logger.debug("Source File Found: {}", sourceLocation);
						btnDeinterlace.setEnabled(false);
						btnDeinterlace.setText("Executing Deinterlacing");
						barM1.setIndeterminate(true);
						barM1.setForeground(Color.BLUE);
						window.toggleMenu(1, false);
						Thread thread = new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								int status = 0;
								try
								{
									Report r = new Report(settings.getClientID(), 12);
									r.addEvent(12, "DeInterlacing Video");
									settings.submit(r);
									Reader scan = new Reader(dir);
									String commandx = new String(settings.getFfmpeg() + " -y -i \"" + dir + File.separator + "ImportVideo"
											+ File.separator + "Interlaced_" + String.format("%03d", (int) spinner_M1.getValue()) + "_"
											+ (String) comboBoxShowName_M1.getSelectedItem() + extension + "\" " + scan.getDeinterlacing()
											+ "-acodec copy \"" + dir + File.separator + "ImportVideo" + File.separator + "Input_"
											+ String.format("%03d", (int) spinner_M1.getValue()) + "_"
											+ (String) comboBoxShowName_M1.getSelectedItem() + extension + "\"");
									logger.info("Stage M1: {}", commandx);
									Process x = Runtime.getRuntime().exec(commandx);
									StreamGobbler reader = new StreamGobbler(x.getInputStream(), reportArea, false);
									StreamGobbler eater = new StreamGobbler(x.getErrorStream(), reportArea, false);
									reader.start();
									eater.start();
									if (x.waitFor() == 0)
									{
										Report r2 = settings.getStoredReport();
										r2.setJobCompleted(true);
										r2.addEvent(16, "DeInterlacing Complete");
										settings.submit(r2);
										logger.info("Deinterlaced Video Created");
									}
									else
									{
										Report r2 = settings.getStoredReport();
										r2.setJobFailed(true);
										r2.addEvent(17, "DeInterlacing Failed");
										settings.submit(r2);
										logger.error("Deinterlacing Failed");
										JOptionPane.showMessageDialog(window, "Deinterlaced Video Extraction Failed", "Process Error",
												JOptionPane.ERROR_MESSAGE);
										status = 1;
									}
								}
								catch (InterruptedException | IOException e)
								{
									logger.fatal("Failed Parse Attempt: {}", sourceLocation);
									logger.fatal("Exception Infomation", e);
									JOptionPane.showMessageDialog(window, "Unable to Access File\n" + sourceLocation + "\nCrashing Program",
											"IO Exception", JOptionPane.ERROR_MESSAGE);
									System.exit(20);
								}
								if (status == 0)
								{
									EventQueue.invokeLater(new Runnable()
									{
										public void run()
										{
											window.toggleMenu(1, true);
											btnDeinterlace.setEnabled(true);
											btnDeinterlace.setText("Execute Deinterlace");
											barM1.setIndeterminate(false);
											barM1.setValue(100);
										}
									});
								}
								else
								{
									EventQueue.invokeLater(new Runnable()
									{
										public void run()
										{
											window.toggleMenu(1, true);
											btnDeinterlace.setEnabled(true);
											btnDeinterlace.setText("Execute Deinterlace");
											barM1.setIndeterminate(false);
											barM1.setForeground(Color.BLACK);
											barM1.setValue(100);
										}
									});
								}
							}
						});
						thread.start();
					}
					else
					{
						logger.error("Input Not Found: {}", sourceLocation);
						barM1.setValue(100);
						barM1.setIndeterminate(true);
						JOptionPane.showMessageDialog(window,
								"Video Source not found, or not Labeled as " + sourceLocation + "\nEntered EP may be different then intended",
								"Input Not Found", JOptionPane.ERROR_MESSAGE);
						barM1.setIndeterminate(false);
						barM1.setForeground(Color.BLACK);
					}
				}
			}
		});

		spinner_M1 = new JSpinner();
		spinner_M1.setModel(new SpinnerNumberModel(0, 0, 999, 1));
		add(spinner_M1, "cell 1 1 5 1,grow");
		add(btnDeinterlace, "cell 6 1 2 2,grow");

		lblShowName = new JLabel("Show Name:");
		add(lblShowName, "cell 0 2,alignx trailing");

		comboBoxShowName_M1 = new JComboBox<String>();
		comboBoxShowName_M1.setToolTipText("Show name used in File Path");
		comboBoxShowName_M1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				comboBoxShowName_M1.setSelectedItem(comboBoxShowName_M1.getSelectedItem().toString().replaceAll(" ", "_"));
				for (int x = 0; x < comboBoxShowName_M1.getModel().getSize(); x++)
				{
					if (comboBoxShowName_M1.getModel().getElementAt(x).equals(comboBoxShowName_M1.getSelectedItem()))
					{
						return;
					}
				}
				comboBoxShowName_M1.addItem((String) comboBoxShowName_M1.getSelectedItem());
			}
		});
		comboBoxShowName_M1.setModel(new DefaultComboBoxModel<String>(trackSelection.updateInterlacedList()));
		comboBoxShowName_M1.setSelectedIndex(0);
		comboBoxShowName_M1.setEditable(true);
		add(comboBoxShowName_M1, "cell 1 2 5 1,growx");

		barM1 = new JProgressBar();
		barM1.setForeground(Color.decode("0x0000FF"));
		add(barM1, "cell 0 3 8 1,grow");

		JSeparator separator = new JSeparator();
		add(separator, "cell 0 4 8 1,growx,aligny center");

		btnAudioExtraction = new JButton("Single Stream Extraction Script");
		btnAudioExtraction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logger.info("Single Audio Stream Extraction Requested");
				window.toggleMenu(1, false);
				btnAudioExtraction.setEnabled(false);
				btnAudioExtraction.setText("Extracting Audio");
				barM2.setIndeterminate(true);
				barM2.setForeground(Color.BLUE);
				String source = new String(File.separator + "ImportVideo" + File.separator + "Input_"
						+ String.format("%03d", (int) spinner_M2.getValue()) + "_" + (String) comboBoxShowName_M2.getSelectedItem());
				String extension = TrackSelection.findExtension(window, dir + source);
				if (extension == null)
				{
					logger.error("Input Not Found or Invalid Extension: {}", source);
					JOptionPane.showMessageDialog(window,
							"Video Source not found, or not Labeled as " + source + ".<extension>\nEntered EP may be different then intended",
							"Input Not Found", JOptionPane.ERROR_MESSAGE);

					window.toggleMenu(1, true);
					btnAudioExtraction.setEnabled(true);
					btnAudioExtraction.setText("Execute Audio Extraction Process");
					barM2.setIndeterminate(false);
					barM2.setValue(100);
					barM2.setForeground(Color.BLACK);
					return;
				}
				StageM2 stageM2 = new StageM2(window, settings, dir, (String) comboBoxShowName_M2.getSelectedItem(), (source + extension),
						(int) spinner_M2.getValue(), (String) m2codec.getSelectedItem(), (String) m2cParameters.getSelectedItem(), false, btnAudioExtraction, barM2, reportArea);
				stageM2.execute();
			}
		});
		
		btnExtractAll = new JButton("Extract All");
		btnExtractAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("All Audio Stream Extraction Requested");
				window.toggleMenu(1, false);
				btnAudioExtraction.setEnabled(false);
				btnAudioExtraction.setText("Extracting Audio");
				barM2.setIndeterminate(true);
				barM2.setForeground(Color.BLUE);
				String source = new String(File.separator + "ImportVideo" + File.separator + "Input_"
						+ String.format("%03d", (int) spinner_M2.getValue()) + "_" + (String) comboBoxShowName_M2.getSelectedItem());
				String extension = TrackSelection.findExtension(window, dir + source);
				if (extension == null)
				{
					logger.error("Input Not Found or Invalid Extension: {}", source);
					JOptionPane.showMessageDialog(window,
							"Video Source not found, or not Labeled as " + source + ".<extension>\nEntered EP may be different then intended",
							"Input Not Found", JOptionPane.ERROR_MESSAGE);

					window.toggleMenu(1, true);
					btnAudioExtraction.setEnabled(true);
					btnAudioExtraction.setText("Execute Audio Extraction Process");
					barM2.setIndeterminate(false);
					barM2.setValue(100);
					barM2.setForeground(Color.BLACK);
					return;
				}
				StageM2 stageM2 = new StageM2(window, settings, dir, (String) comboBoxShowName_M2.getSelectedItem(), (source + extension),
						(int) spinner_M2.getValue(), (String) m2codec.getSelectedItem(), (String) m2cParameters.getSelectedItem(), true, btnAudioExtraction, barM2, reportArea);
				stageM2.execute();
			}
		});
		add(btnExtractAll, "cell 7 5 1 2,grow");

		JLabel lblEpisodeNumber = new JLabel("Episode Number:");
		add(lblEpisodeNumber, "cell 0 5,alignx trailing,growy");

		spinner_M2 = new JSpinner();
		spinner_M2.setModel(new SpinnerNumberModel(0, 0, 999, 1));
		add(spinner_M2, "cell 1 5,grow");
		add(btnAudioExtraction, "cell 6 5 1 2,grow");

		m2codec = new JComboBox<String>();
		m2codec.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (((String) m2codec.getSelectedItem()).equals("copy"))
				{
					m2cParameters.setEnabled(false);
				}
				else
				{
					m2cParameters.setEnabled(true);
				}
			}
		});
		m2codec.setModel(new DefaultComboBoxModel<String>(new String[]
		{ "copy", "aac", "a3c", "flac", "ogg" }));
		add(m2codec, "cell 3 5,growx");

		lblExportEncoding = new JLabel("Codec:");
		add(lblExportEncoding, "cell 2 5,alignx trailing");

		lblEncoding = new JLabel("Parameters:");
		add(lblEncoding, "cell 4 5,alignx trailing");

		m2cParameters = new JComboBox<String>();
		m2cParameters.setModel(new DefaultComboBoxModel<String>(new String[] {"-b:a 96k "}));
		m2cParameters.setEnabled(false);
		m2cParameters.setEditable(true);
		add(m2cParameters, "cell 5 5,growx");

		lblShowName_1 = new JLabel("Show Name:");
		add(lblShowName_1, "cell 0 6,alignx trailing");

		comboBoxShowName_M2 = new JComboBox<String>();
		comboBoxShowName_M2.setToolTipText("Show name used in File Path");
		comboBoxShowName_M2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				comboBoxShowName_M2.setSelectedItem(comboBoxShowName_M2.getSelectedItem().toString().replaceAll(" ", "_"));
				for (int x = 0; x < comboBoxShowName_M2.getModel().getSize(); x++)
				{
					if (comboBoxShowName_M2.getModel().getElementAt(x).equals(comboBoxShowName_M2.getSelectedItem()))
					{
						return;
					}
				}
				comboBoxShowName_M2.addItem((String) comboBoxShowName_M2.getSelectedItem());
			}
		});
		comboBoxShowName_M2.setEditable(true);
		comboBoxShowName_M2.setModel(new DefaultComboBoxModel<String>(trackSelection.updateShowList()));
		comboBoxShowName_M2.setSelectedIndex(0);
		add(comboBoxShowName_M2, "cell 1 6 5 1,growx");

		barM2 = new JProgressBar();
		barM2.setForeground(Color.decode("0x0000FF"));
		add(barM2, "cell 0 7 8 1,grow");

		separator_4 = new JSeparator();
		add(separator_4, "cell 0 8 8 1,growx");

		lblManualImageRetouching = new JLabel("Manual Image Retouching");
		add(lblManualImageRetouching, "cell 0 9 4 1,alignx left");

		radioBoth = new JRadioButton("Wafiu2x + RawTherapee");
		radioBoth.setHorizontalAlignment(SwingConstants.LEFT);
		radioBoth.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (radioBoth.isSelected())
				{
					if (!new File(dir + File.separator + "rawTherapee.pp3").exists())
					{
						logger.debug("File Not Found: {}rawTherapee.pp3", File.separator);
						JOptionPane.showMessageDialog(window,
								"Can not find RawTherapee\nConversion Parameter File\nIn: " + File.separator + "rawTherapee.pp3", "Input Not Found",
								JOptionPane.WARNING_MESSAGE);
						radioWaifu.setSelected(true);
						radioBoth.setSelected(false);
					}
				}
			}
		});

		radioWaifu = new JRadioButton("Waifu2x Only");
		radioWaifu.setHorizontalAlignment(SwingConstants.CENTER);

		radioRaw = new JRadioButton("RawTherapee Only");
		radioRaw.setHorizontalAlignment(SwingConstants.RIGHT);
		radioRaw.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (radioRaw.isSelected())
				{
					if (!new File(dir + File.separator + "rawTherapee.pp3").exists())
					{
						logger.debug("File Not Found: {}rawTherapee.pp3", File.separator);
						JOptionPane.showMessageDialog(window,
								"Can not find RawTherapee\nConversion Parameter File\nIn: " + File.separator + "rawTherapee.pp3", "Input Error",
								JOptionPane.WARNING_MESSAGE);
						radioWaifu.setSelected(true);
						radioRaw.setSelected(false);
					}
				}
			}
		});

		JPanel manualUpscaleSelectionPanel = new JPanel();
		manualUpscaleSelectionPanel.add(radioBoth);
		manualUpscaleSelectionPanel.add(radioWaifu);
		manualUpscaleSelectionPanel.add(radioRaw);
		add(manualUpscaleSelectionPanel, "cell 0 10 6 1,grow");

		ButtonGroup manualGroup = new ButtonGroup();
		manualGroup.add(radioWaifu);
		manualGroup.add(radioBoth);
		manualGroup.add(radioRaw);
		radioWaifu.setSelected(true);

		if (settings.isRaw())
		{
			radioBoth.setEnabled(true);
			radioRaw.setEnabled(true);
		}
		else
		{
			radioBoth.setEnabled(true);
			radioRaw.setEnabled(true);
		}
		btnManual = new JButton("Manual Image Upscaling Script\r\n");
		btnManual.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				logger.info("Manual Image Retouching Requested");
				boolean folder1 = new File(dir + File.separator + "QueuedFrames").list().length != 0;
				boolean folder2 = new File(dir + File.separator + "IntermediateFrames").list().length != 0;
				String location = "QueuedFrames";
				if (folder2)
					location = "IntermediateFrames";
				if (folder1 || folder2)
				{

					logger.debug("Frames Found: {}{}", File.separator, location);
					window.toggleMenu(1, false);
					barM3A.setValue(0);
					barM3B.setValue(0);
					barM3A.setString("0%");
					barM3B.setString("0%");
					barM3A.setForeground(Color.YELLOW);
					barM3B.setForeground(Color.GREEN);
					barM3A.setIndeterminate(false);
					barM3B.setIndeterminate(false);
					btnManual.setEnabled(false);
					btnManual.setText("Manually Retouching");
					int mode;
					if (radioRaw.isSelected())
					{
						mode = 2;
					}
					else if (radioBoth.isSelected())
					{
						mode = 1;
					}
					else
					{
						mode = 0;
					}
					stageM3 = new StageM3(window, settings, dir, mode, btnManual, barM3A, barM3B, txtTimeEstimate, reportArea, btnAbort);
					stageM3.execute();

				}
				else
				{
					logger.error("No Frames Found: {}{}", File.separator, location);
					barM3A.setValue(0);
					barM3B.setValue(0);
					btnAbort.setEnabled(false);
					barM3A.setIndeterminate(true);
					barM3B.setIndeterminate(true);
					barM3A.setForeground(Color.BLACK);
					barM3B.setForeground(Color.BLACK);
					JOptionPane.showMessageDialog(window, "No Frames were Found in: \n" + File.separator + location, "Input Not Found",
							JOptionPane.ERROR_MESSAGE);
					barM3A.setIndeterminate(false);
					barM3B.setIndeterminate(false);
					barM3A.setForeground(Color.YELLOW);
					barM3B.setForeground(Color.GREEN);
				}
			}
		});
		add(btnManual, "cell 6 9 2 2,grow");

		txtTimeEstimate = new JTextField();
		txtTimeEstimate.setEditable(false);
		txtTimeEstimate.setText("Time Estimate");
		add(txtTimeEstimate, "cell 0 11 6 1,growx");
		txtTimeEstimate.setColumns(10);

		btnAbort = new JButton("Abort Retouching Process")
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				if (this.isEnabled())
				{
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setPaint(new GradientPaint(new Point(0, 0), getBackground(), new Point(0, getHeight() / 3), Color.WHITE));
					g2.fillRect(0, 0, getWidth(), getHeight() / 3);
					g2.setPaint(new GradientPaint(new Point(0, getHeight() / 3), Color.WHITE, new Point(0, getHeight()), getBackground()));
					g2.fillRect(0, getHeight() / 3, getWidth(), getHeight());
					g2.dispose();
				}
				super.paintComponent(g);
			}
		};
		btnAbort.setContentAreaFilled(false);
		btnAbort.setBackground(new Color(255, 150, 150));
		btnAbort.setEnabled(false);
		btnAbort.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				logger.info("Manual Retouching Process Termination Requested");
				stageM3.destroy();
				barM3A.setForeground(Color.BLACK);
				barM3A.setIndeterminate(false);
				barM3B.setForeground(Color.BLACK);
				barM3B.setIndeterminate(false);
				JOptionPane.showMessageDialog(window, "Manual Retouch Terminated\nClosing Program", "Aborted", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		add(btnAbort, "cell 6 11 2 1,growx");

		barM3A = new JProgressBar();
		barM3A.setStringPainted(true);
		barM3A.setForeground(Color.YELLOW);
		barM3A.setUI(new BasicProgressBarUI()
		{
			protected Color getSelectionBackground()
			{
				return Color.BLACK;
			}

			protected Color getSelectionForeground()
			{
				return Color.BLACK;
			}
		});
		add(barM3A, "cell 0 12 8 1,growx");

		barM3B = new JProgressBar();
		barM3B.setStringPainted(true);
		barM3B.setForeground(Color.GREEN);
		barM3B.setUI(new BasicProgressBarUI()
		{
			protected Color getSelectionBackground()
			{
				return Color.BLACK;
			}

			protected Color getSelectionForeground()
			{
				return Color.BLACK;
			}
		});
		add(barM3B, "cell 0 13 8 1,growx");

		JSeparator separator_2 = new JSeparator();
		add(separator_2, "cell 0 14 8 1,growx,aligny center");

		btnExecuteRemux = new JButton("Execute Remux Process");
		btnExecuteRemux.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logger.info("Remux Execution Requested");
				String sourceAudioName = new String("");
				ArrayList<String> possibleRemuxes = new ArrayList<String>();
				for (int x = 0; x < Timing.getLangs().length; x++)//Test which Audio/Video combinations exist
				{
					for (int y = 0; y < TrackSelection.videoExtensions.length; y++)
					{
						if (!possibleRemuxes.contains(Timing.getLangs()[x]) && new File(dir + File.separator + "Export" + File.separator + "Upscaled_"
								+ Timing.getLangs()[x] + "_" + new String(String.format("%03d", trackSelection.getEpisode()) + "_"
										+ trackSelection.getShowName() + "." + TrackSelection.videoExtensions[y])).exists())
						{
							possibleRemuxes.add(Timing.getLanguages()[x]);
						}
					}
				}
				for (int y = 0; y < TrackSelection.videoExtensions.length; y++)//Test which nul video combinations
				{
					if (new File(dir + File.separator + "Export" + File.separator + "Upscaled_nul_"
							+ new String(String.format("%03d", trackSelection.getEpisode()) + "_" + trackSelection.getShowName() + "."
									+ TrackSelection.videoExtensions[y])).exists())
					{
						possibleRemuxes.add("No Audio");
						break;
					}
				}
				if (possibleRemuxes.isEmpty())//Shortcut if no combinations found
				{
					sourceAudioName = null;
				}
				else if (possibleRemuxes.size() == 1)//Shortcut if one found
				{
					if (possibleRemuxes.get(0).equals("No Audio"))
					{
						sourceAudioName = "nul";
					}
					else
					{
						for (int y = 0; y < Timing.getLanguages().length; y++)
						{
							if (possibleRemuxes.get(0).equals(Timing.getLanguages()[y]))
							{
								sourceAudioName = Timing.getLangs()[y];
								break;
							}
						}
					}
				}
				else//Multiple Combinations found
				{
					String option = (String) JOptionPane.showInputDialog(window,
							"Multiple Episodes found with Different Languages\nPlease Select which to Remux", "Select Input Video",
							JOptionPane.QUESTION_MESSAGE, null, possibleRemuxes.toArray(), possibleRemuxes.get(0));
					if (option != null)
					{
						if (option.equals("No Audio"))
						{
							sourceAudioName = "nul";
						}
						else
						{
							for (int y = 0; y < Timing.getLanguages().length; y++)
							{
								if (option.equals(Timing.getLanguages()[y]))
								{
									sourceAudioName = Timing.getLangs()[y];
									break;
								}
							}
						}
					}
				}
				if (sourceAudioName == null)
				{
					JOptionPane.showMessageDialog(window,
							"No Video Source found at all\nShow: " + trackSelection.getShowName() + " Ep: " + trackSelection.getEpisode()
									+ "\nLooked for: " + File.separator + "Export" + File.separator + "Upscaled_<aud>_"
									+ new String(String.format("%03d", trackSelection.getEpisode())) + "_" + trackSelection.getShowName() + "."
									+ trackSelection.getExtension(),
							"Input Not Found", JOptionPane.ERROR_MESSAGE);
				}
				else if (!sourceAudioName.equals(""))
				{
					String finalAudioName;
					if(trackSelection.getChosen()[0])
					{
						finalAudioName = trackSelection.getLangs()[0];
					}
					else
					{
						finalAudioName = "nul";
					}
					if (window.canContinue(File.separator + "Export" + File.separator + "Remuxed_" + finalAudioName + "_"
							+ new String(String.format("%03d", trackSelection.getEpisode())) + "_" + trackSelection.getShowName() + "."
							+ trackSelection.getExtension()))
					{
						window.toggleMenu(1, false);
						btnExecuteRemux.setText("Process Executing");
						barM4.setValue(0);
						barM4.setForeground(Color.decode("0x0000FF"));
						barM4.setIndeterminate(true);
						btnExecuteRemux.setEnabled(false);
						String tempSourceAudioName = sourceAudioName;//Why is this necessary
						Thread thread = new Thread()
						{
							public void run()
							{
								stage0 = new Stage0(window, null, settings, dir, trackSelection.getShowName(),
										Integer.toString(trackSelection.getEpisode()), trackSelection.getChosen(), trackSelection.getLangs(),
										trackSelection.getHandles(), trackSelection.getOffsets(), trackSelection.getTitle(),
										trackSelection.getDescription(), false, false, false, false, false, null, null, null, null, null, barM4,
										btnExecuteRemux, null, null, null, null, reportArea, reportArea, trackSelection.getDuration(),
										trackSelection.getMasterOffset(), tempSourceAudioName, trackSelection.getExtension(), 2, true);
								DataBlock remuxBlock = stage0.checkEverything();
								if (remuxBlock.getStatus() == 0)
								{
									StageM4 stageM4 = new StageM4(window, remuxBlock,finalAudioName);
									stageM4.execute();
								}
								else
								{
									SwingUtilities.invokeLater(new Runnable()
									{
										public void run()
										{
											window.toggleMenu(1, true);
											btnExecuteRemux.setText("Select Remux Process");
											btnExecuteRemux.setEnabled(true);
											barM4.setIndeterminate(false);
											barM4.setValue(100);
											barM4.setForeground(Color.BLACK);
										}
									});
								}
							}
						};
						thread.start();
					}
				}
				else
				{
					logger.debug("Remux Canceled");
				}
			}
		});
		add(btnExecuteRemux, "cell 0 16 8 2,grow");

		barM4 = new JProgressBar();
		barM4.setForeground(Color.BLUE);
		add(barM4, "cell 0 18 8 1,grow");

		JSeparator separator_3 = new JSeparator();
		add(separator_3, "cell 0 19 8 1,growx,aligny center");

		reportArea = new JTextArea(200, 30);
		reportArea.setText("Console Info Output\r\n");
		reportArea.setFont(new Font("Lucida Console", Font.PLAIN, 13));
		reportArea.getDocument().addDocumentListener(new LimitLinesDocumentListener(200));
		reportArea.setLineWrap(true);
		reportArea.setEditable(false);
		JScrollPane outputVisible = new JScrollPane(reportArea);
		add(outputVisible, "cell 0 20 8 1,grow");

	}

	public void updateSettings(Settings settings)
	{
		if (settings.isRaw())
		{
			radioBoth.setEnabled(true);
			radioRaw.setEnabled(true);
		}
		else
		{
			radioWaifu.setSelected(true);
			radioBoth.setEnabled(false);
			radioRaw.setEnabled(false);
		}
	}

	public void lockGUI(boolean value)
	{
		if (value)
		{
			trackSelection.disablePanel();
		}
		else
		{
			trackSelection.enablePanel();
		}
		spinner_M1.setEnabled(!value);
		spinner_M2.setEnabled(!value);
		btnExecuteRemux.setEnabled(!value);
		btnDeinterlace.setEnabled(!value);
		btnAudioExtraction.setEnabled(!value);
		btnManual.setEnabled(!value);
		comboBoxShowName_M1.setEnabled(!value);
		comboBoxShowName_M2.setEnabled(!value);
	}
}
