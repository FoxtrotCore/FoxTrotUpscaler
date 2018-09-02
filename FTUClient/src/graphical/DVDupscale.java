/**
 * /**
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FoxTrotUpscaler. If not, see <http://www.gnu.org/licenses/>.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import main.DataFilter;
import main.LimitLinesDocumentListener;
import main.QueueMaker;
import main.QueueStarter;
import main.Settings;
import main.Stage0;
import main.Stage1;
import main.Stage2;
import main.Stage3;
import main.Stage4;
import net.miginfocom.swing.MigLayout;
import structures.DataBlock;

@SuppressWarnings("serial")
public class DVDupscale extends JPanel
{

	private static Logger logger = LogManager.getLogger();
	private MainWindow window;
	private JProgressBar progressBar_1;
	private JProgressBar progressBar_2;
	private JProgressBar progressBar_3;
	private JProgressBar progressBar_4;
	private JButton btnExecute;
	private JTextArea outputArea;
	private JRadioButton rdbtnP_1;
	private JRadioButton rdbtnP_2;
	private JRadioButton rdbtnP_3;
	private Stage0 stage0;
	private JButton btnAbort;
	private JTextField txtTimeEstimate;
	private JTextArea errorArea;
	private JSeparator separator_4;
	private JButton btnSaveForQueue;
	private JButton btnExecuteQueue;
	private QueueStarter starter;
	private boolean queueMode;
	private JButton btnEditQueue;
	private JButton btnFullAbort;
	private JButton btnPause;
	private boolean isPaused;
	private JTextField txtTimeRunning;
	private JToggleButton btnRawtherapee;
	private String dir;
	private JRadioButton rdbtnP_4;
	private TrackSelection trackSelection;
	private DataFilter filter;
	private Settings settings;
	private JProgressBar progressBar_5;
	private JPanel stageSelectionPanel;
	private Timer stopwatchTimer;
	private Instant startTime;

	/**
	 * Creates the Main Upscaling Panel
	 */
	public DVDupscale(MainWindow window, String directory, Settings s)
	{
		this.window = window;
		this.dir = directory;
		this.settings = s;
		filter = new DataFilter();
		stopwatchTimer = new Timer(1000, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				long duration = ChronoUnit.SECONDS.between(startTime, Instant.now());
				String hours = String.format("%02d", (duration / 3600)).substring(0, 2);
				duration -= Integer.valueOf(hours) * 3600;
				String minutes = String.format("%02d", (duration / 60)).substring(0, 2);
				duration -= Integer.valueOf(minutes) * 60;
				String seconds = String.format("%02d", (duration)).substring(0, 2);
				txtTimeRunning.setText(hours + ":" + minutes + ":" + seconds);
			}

		})
		{

			@Override
			public void start()
			{
				super.start();
				startTime = Instant.now();
			}

			@Override
			public void stop()
			{
				super.stop();
				txtTimeRunning.setText("-=" + txtTimeRunning.getText() + "=-");
			}

			@Override
			public void restart()
			{
				super.restart();
				startTime = Instant.now();
			}
		};

		setLayout(new MigLayout("", "[][][grow][][grow][195px]", "[][-2.00][][-10.00][][][][][][][][][500px,grow][][150px:150.00]"));

		trackSelection = new TrackSelection(dir);
		add(trackSelection, "cell 0 0 6 1,grow");

		JSeparator separator_1 = new JSeparator();
		add(separator_1, "cell 0 1 6 1,growx");

		btnRawtherapee = new JToggleButton("RawTherapee Disabled")
		{

			@Override
			protected void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setPaint(new GradientPaint(new Point(0, 0), getBackground(), new Point(0, getHeight() / 3), Color.WHITE));
				g2.fillRect(0, 0, getWidth(), getHeight() / 3);
				g2.setPaint(new GradientPaint(new Point(0, getHeight() / 3), Color.WHITE, new Point(0, getHeight()), getBackground()));
				g2.fillRect(0, getHeight() / 3, getWidth(), getHeight());
				g2.dispose();

				super.paintComponent(g);
			}
		};
		btnRawtherapee.setContentAreaFilled(false);
		btnRawtherapee.setBackground(new Color(255, 255, 100));
		if (!settings.isRaw())
		{
			btnRawtherapee.setEnabled(false);
			btnRawtherapee.setSelected(false);
			btnRawtherapee.setBackground(new Color(150, 150, 150));
			btnRawtherapee.setText("RawTherapee Unavaliable");
		}
		btnRawtherapee.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if (btnRawtherapee.isSelected())
				{
					logger.debug("RawTherapee Button Toggled ON");
					if (new File(dir + File.separator + "rawTherapee.pp3").exists())
					{
						rdbtnP_3.setEnabled(true);
						progressBar_3.setEnabled(true);
						btnRawtherapee.setBackground(new Color(150, 255, 150));
						btnRawtherapee.setText("RawTherapee Enabled");
						logger.debug("File Found: {}rawTherapee.pp3", File.separator);
					}
					else
					{
						logger.debug("File Not Found: {}rawTherapee.pp3", File.separator);
						JOptionPane.showMessageDialog(window,
								"Can not find RawTherapee\nConversion Parameter File\nIn: " + File.separator + "rawTherapee.pp3", "Input Not Found",
								JOptionPane.WARNING_MESSAGE);
						btnRawtherapee.setSelected(false);
						btnRawtherapee.setText("RawTherapee Disabled");
					}
				}
				else
				{
					logger.debug("RawTherapee Button Toggled OFF");
					if (rdbtnP_3.isSelected())
					{
						rdbtnP_2.setSelected(true);
					}
					rdbtnP_3.setEnabled(false);
					progressBar_3.setEnabled(false);
					btnRawtherapee.setBackground(new Color(255, 255, 100));
					btnRawtherapee.setText("RawTherapee Disabled");
				}
			}
		});
		add(btnRawtherapee, "cell 0 2 6 1,growx");

		JSeparator separator_2 = new JSeparator();
		add(separator_2, "cell 0 3 6 1,growx");

		// MARK Execute
		btnExecute = new JButton("Execute");
		btnExecute.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				logger.info("Single Process Execution Requested");
				String audioName;
				if (trackSelection.getChosen()[0])
				{
					audioName = new String(trackSelection.getLangs()[0]);
				}
				else
				{
					audioName = new String("nul");
				}
				if (window.canContinue(File.separator + "Export" + File.separator + "Upscaled_" + audioName + "_"
						+ new String(String.format("%03d", trackSelection.getEpisode())) + "_" + trackSelection.getShowName() + "."
						+ trackSelection.getExtension()))
				{
					queueMode = false;
					btnExecute.setText("Process Executed");
					window.toggleMenu(0, false);
					progressBar_1.setIndeterminate(true);
					progressBar_2.setIndeterminate(true);
					progressBar_3.setIndeterminate(true);
					progressBar_4.setIndeterminate(true);
					progressBar_5.setIndeterminate(false);
					progressBar_1.setValue(0);
					progressBar_2.setValue(0);
					progressBar_3.setValue(0);
					progressBar_4.setValue(0);
					progressBar_5.setValue(0);
					progressBar_1.setForeground(Color.ORANGE);
					progressBar_2.setForeground(Color.YELLOW);
					progressBar_3.setForeground(Color.GREEN);
					progressBar_4.setForeground(Color.CYAN);
					progressBar_5.setForeground(Color.MAGENTA);
					progressBar_1.setString(null);
					progressBar_2.setString(null);
					progressBar_3.setString(null);
					progressBar_4.setString(null);
					progressBar_5.setString("N/A");
					btnExecute.setEnabled(false);
					btnSaveForQueue.setEnabled(false);
					btnEditQueue.setEnabled(false);
					btnExecuteQueue.setEnabled(false);
					stage0 = new Stage0(window, stopwatchTimer, settings, dir, trackSelection.getShowName(),
							Integer.toString(trackSelection.getEpisode()), trackSelection.getChosen(), trackSelection.getLangs(),
							trackSelection.getHandles(), trackSelection.getOffsets(), trackSelection.getTitle(), trackSelection.getDescription(),
							rdbtnP_1.isSelected(), rdbtnP_2.isSelected(), rdbtnP_3.isSelected(), rdbtnP_4.isSelected(), btnRawtherapee.isSelected(),
							progressBar_1, progressBar_2, progressBar_3, progressBar_4, progressBar_5, null, btnExecute, btnPause, btnAbort,
							btnFullAbort, txtTimeEstimate, outputArea, errorArea, trackSelection.getDuration(), trackSelection.getMasterOffset(),
							audioName, trackSelection.getExtension(), 0, true);
					stage0.execute();
				}
			}

		});
		btnExecute.addMouseListener(new MouseAdapter()
		{

			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1)
				{
					logger.info("Process Command Preivew Requested");
					String audioName;
					if (trackSelection.getChosen()[0])
					{
						audioName = new String(trackSelection.getLangs()[0]);
					}
					else
					{
						audioName = new String("nul");
					}
					stage0 = new Stage0(window, stopwatchTimer, settings, dir, trackSelection.getShowName(),
							Integer.toString(trackSelection.getEpisode()), trackSelection.getChosen(), trackSelection.getLangs(),
							trackSelection.getHandles(), trackSelection.getOffsets(), trackSelection.getTitle(), trackSelection.getDescription(),
							rdbtnP_1.isSelected(), rdbtnP_2.isSelected(), rdbtnP_3.isSelected(), rdbtnP_4.isSelected(), true, progressBar_1,
							progressBar_2, progressBar_3, progressBar_4, progressBar_5, null, btnExecute, btnPause, btnAbort, btnFullAbort,
							txtTimeEstimate, outputArea, errorArea, trackSelection.getDuration(), trackSelection.getMasterOffset(), audioName,
							trackSelection.getExtension(), 0, false);
					DataBlock block = stage0.checkEverything();
					StringBuilder b = new StringBuilder("Stage A:\n");
					b.append(block.getCommand1());
					b.append("\n\nStage B:\n");
					b.append(block.getCommand2());
					b.append("\n\nStage C:\n");
					b.append(block.getCommand3());
					b.append("\n\nStage D:\n");
					b.append(block.getCommand4());
					JTextArea smallArea = new JTextArea(b.toString());
					smallArea.setEditable(false);
					smallArea.setLineWrap(true);
					smallArea.setWrapStyleWord(true);
					smallArea.setColumns(60);
					smallArea.setRows(15);
					if (block.getStatus() == 0)
					{
						JOptionPane.showMessageDialog(window, new JScrollPane(smallArea), "Process Command Preview", JOptionPane.PLAIN_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(window, "Can not Generate a Preview", "Process Command Preview", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		btnSaveForQueue = new JButton("Save for Queue");
		btnSaveForQueue.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				updateQueue();
				logger.info("Store Queue File Requested");
				String audioName;
				if (trackSelection.getChosen()[0])// Should not be null as
													// getL1() must not be null,
													// but blank, and real
													// variable is boolean
				{
					audioName = new String(trackSelection.getLangs()[0] + "");
				}
				else
				{
					audioName = new String("nul");
				}
				if (window.canContinue(File.separator + "Export" + File.separator + "Upscaled_" + audioName
						+ new String(String.format("%03d", trackSelection.getEpisode())) + "_" + trackSelection.getShowName() + "."
						+ trackSelection.getExtension()))
				{
					progressBar_1.setValue(0);
					progressBar_2.setValue(0);
					progressBar_3.setValue(0);
					progressBar_4.setValue(0);
					progressBar_5.setValue(0);
					progressBar_1.setString(null);
					progressBar_2.setString(null);
					progressBar_3.setString(null);
					progressBar_4.setString(null);
					progressBar_5.setString(null);
					progressBar_1.setIndeterminate(true);
					progressBar_2.setIndeterminate(true);
					progressBar_3.setIndeterminate(true);
					progressBar_4.setIndeterminate(true);
					progressBar_5.setIndeterminate(true);
					progressBar_1.setForeground(Color.ORANGE);
					progressBar_2.setForeground(Color.YELLOW);
					progressBar_3.setForeground(Color.GREEN);
					progressBar_4.setForeground(Color.CYAN);
					progressBar_5.setForeground(Color.MAGENTA);
					window.toggleMenu(0, false);
					QueueMaker maker = new QueueMaker(window, settings, dir, trackSelection.getShowName(),
							Integer.toString(trackSelection.getEpisode()), trackSelection.getChosen(), trackSelection.getLangs(),
							trackSelection.getHandles(), trackSelection.getOffsets(), trackSelection.getTitle(), trackSelection.getDescription(),
							btnRawtherapee.isSelected(), progressBar_1, progressBar_2, progressBar_3, progressBar_4, progressBar_5, btnExecute,
							btnPause, btnAbort, btnFullAbort, txtTimeEstimate, outputArea, errorArea, trackSelection.getDuration(),
							trackSelection.getMasterOffset(), btnSaveForQueue, btnExecuteQueue, btnEditQueue, audioName,
							trackSelection.getExtension());
					maker.start();
				}

			}
		});

		txtTimeRunning = new JTextField();
		txtTimeRunning.setHorizontalAlignment(SwingConstants.CENTER);
		txtTimeRunning.setEditable(false);
		txtTimeRunning.setText("Time Running");
		add(txtTimeRunning, "cell 0 4 2 1,grow");
		txtTimeRunning.setColumns(10);
		add(btnSaveForQueue, "cell 2 4,grow");
		add(btnExecute, "flowx,cell 3 4 2 3,grow");

		rdbtnP_1 = new JRadioButton("S1");
		rdbtnP_2 = new JRadioButton("S2");
		rdbtnP_3 = new JRadioButton("Raw");
		rdbtnP_3.setEnabled(false);
		rdbtnP_4 = new JRadioButton("S4");

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnP_1);
		group.add(rdbtnP_2);
		group.add(rdbtnP_3);
		group.add(rdbtnP_4);
		rdbtnP_1.setSelected(true);

		stageSelectionPanel = new JPanel();
		stageSelectionPanel.add(rdbtnP_1);
		stageSelectionPanel.add(rdbtnP_2);
		stageSelectionPanel.add(rdbtnP_3);
		stageSelectionPanel.add(rdbtnP_4);
		add(stageSelectionPanel, "cell 0 5 2 1,alignx center,aligny center");

		btnPause = new JButton("Pause")
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
		btnPause.setContentAreaFilled(false);
		btnPause.setBackground(new Color(255, 200, 100));
		btnPause.setEnabled(false);
		btnPause.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if (isPaused)
				{
					if (queueMode)
					{
						starter.resumeProcess();
					}
					else
					{
						stage0.resumeProcess();
					}
					isPaused = false;
				}
				else
				{
					if (queueMode)
					{
						starter.pauseProcess(false);
					}
					else
					{
						stage0.pauseProcess(false);
					}
					isPaused = true;
				}
			}
		});
		add(btnPause, "cell 5 4,grow");

		btnEditQueue = new JButton("Delete Queue");
		btnEditQueue.addActionListener(new ActionListener() // TODO Edit Box,
															// possible Warning
															// for Wipe
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Queue Editor Visual Requested");
				int confirm = JOptionPane.showConfirmDialog(window, "Are you sure you wish the clear the Queue?", "Clear Queue?",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm == JOptionPane.OK_OPTION)
				{
					try
					{
						logger.info("Files Deleted: {}Queue", File.separator);
						FileUtils.cleanDirectory(new File(dir + File.separator + "Queue"));
						updateQueue();
					}
					catch (IOException e1)
					{
						logger.fatal("Failed Delete Attempt: {}Queue", File.separator);
						logger.fatal("Exception Infomation", e1);
						JOptionPane.showMessageDialog(null, "Unable to Access File\n" + File.separator + "Queue" + "\nCrashing Program",
								"IO Exception", JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
				}
			}
		});
		add(btnEditQueue, "cell 2 5,grow");

		btnExecuteQueue = new JButton("Start Queue");
		btnExecuteQueue.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.info("Multi Process Execution Requested");
				updateQueue();
				queueMode = true;
				btnExecute.setText("Process Executed");
				progressBar_1.setIndeterminate(true);
				progressBar_2.setIndeterminate(true);
				progressBar_3.setIndeterminate(true);
				progressBar_4.setIndeterminate(true);
				progressBar_5.setIndeterminate(true);
				progressBar_5.setString(null);
				progressBar_1.setForeground(Color.ORANGE);
				progressBar_2.setForeground(Color.YELLOW);
				progressBar_3.setForeground(Color.GREEN);
				progressBar_4.setForeground(Color.CYAN);
				progressBar_5.setForeground(Color.MAGENTA);
				progressBar_1.setValue(0);
				progressBar_2.setValue(0);
				progressBar_3.setValue(0);
				progressBar_4.setValue(0);
				progressBar_5.setValue(0);
				progressBar_1.setString(null);
				progressBar_2.setString(null);
				progressBar_3.setString(null);
				progressBar_4.setString(null);
				progressBar_5.setString(null);
				progressBar_5.setEnabled(true);
				btnExecute.setEnabled(false);
				btnSaveForQueue.setEnabled(false);
				btnEditQueue.setEnabled(false);
				btnExecuteQueue.setEnabled(false);
				btnPause.setEnabled(true);
				window.toggleMenu(0, false);
				starter = new QueueStarter(window, stopwatchTimer, settings, dir, progressBar_1, progressBar_2, progressBar_3, progressBar_4,
						progressBar_5, btnExecute, btnPause, btnAbort, btnFullAbort, txtTimeEstimate, outputArea, errorArea);
				starter.start();
			}
		});

		// MARK Abort
		btnAbort = new JButton("Abort")
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
				btnAbort.setEnabled(false);
				if (queueMode)
				{
					logger.info("Single Process Termination from Queue Requested");
					starter.destroySingle();
					JOptionPane.showMessageDialog(window, "Current Upscale Process Terminated\nStarting next Process in Queue", "Aborted",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					logger.info("Single Process Termination Requested");
					stage0.destroy();
					progressBar_1.setForeground(Color.BLACK);
					progressBar_1.setIndeterminate(false);
					progressBar_2.setForeground(Color.BLACK);
					progressBar_2.setIndeterminate(false);
					progressBar_3.setForeground(Color.BLACK);
					progressBar_3.setIndeterminate(false);
					progressBar_4.setForeground(Color.BLACK);
					progressBar_4.setIndeterminate(false);
					btnExecute.setEnabled(true);
					btnSaveForQueue.setEnabled(true);
					btnPause.setEnabled(false);
					updateQueue();
					JOptionPane.showMessageDialog(window, "Process Terminated", "Aborted", JOptionPane.INFORMATION_MESSAGE);
				}
				String quickPath = new String(File.separator + "Temp" + File.separator + "currentProcess.ser");
				try
				{
					Files.deleteIfExists(Paths.get(dir + quickPath));
					logger.info("File Deleted: {}", quickPath);
				}
				catch (IOException p)
				{
					logger.fatal("Failed Delete Attempt: {}", quickPath);
					logger.fatal("Exception Infomation", p);
					JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}

		});
		add(btnAbort, "cell 5 5,grow");

		txtTimeEstimate = new JTextField();
		txtTimeEstimate.setEditable(false);
		txtTimeEstimate.setHorizontalAlignment(SwingConstants.CENTER);
		txtTimeEstimate.setText("Time Estimate");
		add(txtTimeEstimate, "cell 0 6 2 1,grow");
		txtTimeEstimate.setColumns(10);
		add(btnExecuteQueue, "cell 2 6,grow");

		btnFullAbort = new JButton("Full Abort")
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
		btnFullAbort.setContentAreaFilled(false);
		btnFullAbort.setBackground(new Color(255, 150, 150));
		btnFullAbort.setEnabled(false);
		btnFullAbort.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (queueMode)
				{
					btnAbort.setEnabled(false);
					btnFullAbort.setEnabled(false);
					logger.info("Full Queue Termination Requested");
					starter.killAll();
					String quickPath = new String(File.separator + "Temp" + File.separator + "currentProcess.ser");
					try
					{
						Files.deleteIfExists(Paths.get(dir + quickPath));
						logger.info("File Deleted: {}", quickPath);
					}
					catch (IOException p)
					{
						logger.fatal("Failed Delete Attempt: {}", quickPath);
						logger.fatal("Exception Infomation", p);
						JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
				}
			}
		});
		add(btnFullAbort, "cell 5 6,grow");

		progressBar_1 = new JProgressBar();
		progressBar_1.setToolTipText("Stage of Decompressing Video into Individual Frames");
		progressBar_1.setStringPainted(true);
		progressBar_1.setForeground(Color.ORANGE);
		progressBar_1.setUI(new BasicProgressBarUI()
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
		add(progressBar_1, "cell 0 7 6 1,grow");

		progressBar_2 = new JProgressBar();
		progressBar_2.setToolTipText("Stage of Upscaleing all of the Frames through Waifu2x-Caffe");
		progressBar_2.setStringPainted(true);
		progressBar_2.setForeground(Color.YELLOW);
		progressBar_2.setUI(new BasicProgressBarUI()
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
		add(progressBar_2, "cell 0 8 6 1,grow");

		progressBar_3 = new JProgressBar();
		progressBar_3.setToolTipText("Optional Stage of Touching Up all of the Frames with RawTherapee");
		progressBar_3.setForeground(Color.GREEN);
		progressBar_3.setStringPainted(true);
		progressBar_3.setEnabled(false);
		progressBar_3.setUI(new BasicProgressBarUI()
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
		add(progressBar_3, "cell 0 9 6 1,grow");

		progressBar_4 = new JProgressBar();
		progressBar_4.setToolTipText("Stage of Remuxing all of the Frames, Audio, and Subtitiles Back into a Video");
		progressBar_4.setForeground(Color.CYAN);
		progressBar_4.setStringPainted(true);
		progressBar_4.setUI(new BasicProgressBarUI()
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
		add(progressBar_4, "cell 0 10 6 1,grow");

		outputArea = new JTextArea(200, 30);
		outputArea.setText("Console Info Output\r\n");
		outputArea.setLineWrap(true);
		outputArea.setEditable(false);
		outputArea.setFont(new Font("Lucida Console", Font.PLAIN, 13));
		// Limits to appended lines is at 200
		outputArea.getDocument().addDocumentListener(new LimitLinesDocumentListener(200));

		progressBar_5 = new JProgressBar();
		progressBar_5.setToolTipText("Percentage of Queue Completed\r\n");
		progressBar_5.setStringPainted(true);
		progressBar_5.setEnabled(false);
		progressBar_5.setForeground(Color.MAGENTA);
		progressBar_5.setUI(new BasicProgressBarUI()
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
		add(progressBar_5, "cell 0 11 6 1,growx");
		JScrollPane outputVisible = new JScrollPane(outputArea);
		add(outputVisible, "cell 0 12 6 1,grow");

		separator_4 = new JSeparator();
		add(separator_4, "cell 0 13 6 1,growx,aligny center");

		errorArea = new JTextArea();
		errorArea.setFont(new Font("Lucida Console", Font.PLAIN, 13));
		errorArea.setLineWrap(true);
		errorArea.setForeground(Color.RED);
		errorArea.setEditable(false);
		errorArea.setText("Console Error Output for either Troubleshooting or Debugging\r\n");
		JScrollPane errorVisible = new JScrollPane(errorArea);
		add(errorVisible, "cell 0 14 6 1,grow");
		updateQueue();
		checkIfComplete();
		logger.debug("Main Window Initialization Complete");
	}
	
	public void remoteDeleteQueue()
	{
		EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				if (btnEditQueue.isEnabled())
				{
					try
					{
						logger.info("Files Deleted: {}Queue", File.separator);
						FileUtils.cleanDirectory(new File(dir + File.separator + "Queue"));
						updateQueue();
					}
					catch (IOException e1)
					{
						logger.fatal("Failed Delete Attempt: {}Queue", File.separator);
						logger.fatal("Exception Infomation", e1);
						JOptionPane.showMessageDialog(null, "Unable to Access File\n" + File.separator + "Queue" + "\nCrashing Program",
								"IO Exception", JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
				}
			}
		});
	}

	public void remoteFullAbortUpscale()
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (btnFullAbort.isEnabled())
				{
					btnFullAbort.doClick();
				}
			}
		});
	}

	public void remoteSingleAbortUpscale()
	{
		EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				if (btnAbort.isEnabled())
				{
					btnAbort.doClick();
				}
			}
		});
	}

	public void remoteResumeUpscale()
	{
		EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				if (isPaused && btnPause.isEnabled())
				{
					if (queueMode)
					{
						starter.resumeProcess();
					}
					else
					{
						stage0.resumeProcess();
					}
					isPaused = false;
				}
			}
		});
	}

	public void remotePauseUpscale()
	{
		EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				if (!isPaused && btnPause.isEnabled())
				{
					if (queueMode)
					{
						starter.pauseProcess(true);
					}
					else
					{
						stage0.pauseProcess(true);
					}
					isPaused = true;
				}
			}
		});
	}

	public void remoteExecuteQueue()
	{
		EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				btnExecuteQueue.doClick();
			}
		});
	}

	public void checkIfComplete()
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				logger.debug("Verifying Previous Completion");
				String quickPath = new String(File.separator + "Temp" + File.separator + "currentProcess.ser");
				String currentProcessPath = new String(dir + quickPath);
				if (new File(currentProcessPath).exists())
				{
					logger.debug("File Found: {}", quickPath);
					int confirm = JOptionPane.showConfirmDialog(window,
							"The Last Time FoxTrotUpscaler ran\nIt was interrupted\nWould you like to continue?", "Incomplete Process",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (confirm == JOptionPane.OK_OPTION)
					{
						window.toggleMenu(0, false);
						logger.info("Attempting Recovery of Crashed Process");
						FileInputStream fileIn = null;
						ObjectInputStream in = null;
						try
						{
							window.toggleMenu(0, false);
							fileIn = new FileInputStream(currentProcessPath);
							in = new ObjectInputStream(fileIn);
							DataBlock block = (DataBlock) in.readObject();
							in.close();
							fileIn.close();
							logger.info("Process File Parsed");
							block.setDir(dir);
							block.setSettings(settings);
							block.setBar1(progressBar_1);
							block.setBar2(progressBar_2);
							block.setBar3(progressBar_3);
							block.setBar4(progressBar_4);
							block.setExecute(btnExecute);
							block.setPause(btnPause);
							block.setAbort(btnAbort);
							block.setFullAbort(btnFullAbort);
							block.setTime(txtTimeEstimate);
							block.setOutput(outputArea);
							block.setError(errorArea);
							block.setPaused(false);
							block.setRecovered(true);
							block.init();
							if (block.getStatus() == 0)
							{
								block.getBar1().setMaximum(block.getTotalFrames());
								block.getBar2().setMaximum(block.getTotalFrames());
								block.getBar3().setMaximum(block.getTotalFrames());
								block.getBar4().setMaximum(block.getTotalFrames());
								try
								{
									Files.delete(Paths.get(dir + quickPath));
									logger.info("File Deleted:");
								}
								catch (IOException p)
								{
									logger.fatal("Failed Delete Attempt: {}", quickPath);
									logger.fatal("Exception Infomation", p);
									JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
											JOptionPane.ERROR_MESSAGE);
									System.exit(20);
								}
								if (block.isQueue())
								{
									logger.info("Restarting as a Queue");
									btnFullAbort.setEnabled(true);
									if (block.getProgress() == 1)
									{
										logger.info("Restarting Process at Stage 1");
										if (!block.isPaused())
										{
											try
											{
												FileUtils.cleanDirectory(new File(block.getDir() + File.separator + "QueuedFrames"));
												logger.info("Files Deleted: {}QueuedFrames", File.separator);
											}
											catch (IOException e)
											{
												logger.fatal("Failed Delete Attempt: {}QueuedFrames", File.separator);
												logger.fatal("Exception Infomation", e);
												JOptionPane.showMessageDialog(null,
														"Unable to Delete Files\n" + File.separator + "QueuedFrames\nCrashing Program",
														"IO Exception", JOptionPane.ERROR_MESSAGE);
												System.exit(20);
											}
										}
										block.setRadio1(true);
										block.setRadio2(false);
										block.setRadio3(false);
										block.setRadio4(false);
									}
									else if (block.getProgress() == 2)
									{
										logger.info("Restarting Process at Stage 2");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											String folderLabel;
											if (block.isUseRaw())
											{
												folderLabel = new String("IntermediateFrames");
											}
											else
											{
												folderLabel = new String("ProcessedFrames");
											}
											File resultFolder = new File(block.getDir() + File.separator + folderLabel);
											int finalframe = resultFolder.list().length - 3;
											if (finalframe < 0)
											{
												finalframe = 0;
											}
											for (int x = 1; x <= finalframe; x++)
											{
												String quickPart = new String(File.separator + "QueuedFrames" + File.separator + "OEP"
														+ String.format("%06d", x) + "." + settings.getImage());
												String framePath = new String(block.getDir() + quickPart);
												try
												{
													Files.deleteIfExists(Paths.get(framePath));
													logger.info("Frames Deleted: {}", quickPart);
												}
												catch (IOException e)
												{
													logger.fatal("Failed Delete Attempt: {}", quickPart);
													logger.fatal("Exception Infomation", e);
													JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
															"IO Exception", JOptionPane.ERROR_MESSAGE);
													System.exit(20);
												}
											}
										}
										block.setRadio1(false);
										block.setRadio2(true);
										block.setRadio3(false);
										block.setRadio4(false);
									}
									else if (block.getProgress() == 3)
									{
										logger.info("Restarting Process at Stage 3");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio2())
										{
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											File resultFolder = new File(block.getDir() + File.separator + "ProcessedFrames");
											int finalframe = resultFolder.list().length;
											int startframe = finalframe - 5;
											if (startframe < 0)
											{
												startframe = 0;
											}
											for (int x = startframe; x <= finalframe; x++)
											{
												String quickPart = new String(File.separator + "ProcessedFrames" + File.separator + "OEP"
														+ String.format("%06d", x) + "." + settings.getImage());
												String framePath = new String(block.getDir() + quickPart);
												try
												{
													Files.deleteIfExists(Paths.get(framePath));
													logger.info("Frames Deleted: {}", quickPart);
												}
												catch (IOException e)
												{
													logger.fatal("Failed Delete Attempt: {}", quickPart);
													logger.fatal("Exception Infomation", e);
													JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
															"IO Exception", JOptionPane.ERROR_MESSAGE);
													System.exit(20);
												}
											}
										}
										btnExecute.setText("Executing Stage 3");
										block.setRadio1(false);
										block.setRadio2(false);
										block.setRadio3(true);
										block.setRadio4(false);
									}
									else if (block.getProgress() == 4)
									{
										logger.info("Restarting Process at Stage 4");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio2())
										{
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio3())
										{
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											String quickPart = new String(File.separator + "Export" + File.separator + block.getEep() + "_"
													+ block.getShowName() + "." + block.getExportExtension());
											try
											{
												Files.deleteIfExists(Paths.get(block.getDir() + quickPart));
												logger.info("File Deleted: {}", quickPart);
											}
											catch (IOException e)
											{
												logger.fatal("Failed Delete Attempt: {}", quickPart);
												logger.fatal("Exception Infomation", e);
												JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
														"IO Exception", JOptionPane.ERROR_MESSAGE);
												System.exit(20);
											}
										}
										block.setRadio1(false);
										block.setRadio2(false);
										block.setRadio3(false);
										block.setRadio4(true);
									}
									String quickPart = new String(File.separator + "Queue" + File.separator + "Q001.ser");
									try
									{
										Files.deleteIfExists(Paths.get(block.getDir() + quickPart));
										logger.info("File Deleted: {}", quickPart);
										String filePath = new String(dir + quickPart);
										FileOutputStream fileOut = new FileOutputStream(filePath);
										ObjectOutputStream out = new ObjectOutputStream(fileOut);
										out.writeObject(block);
										out.close();
										fileOut.close();
										logger.info("File Saved: {}", quickPart);
										updateQueue();
										logger.info("Multi Process Execution Starting");
										queueMode = true;
										btnExecute.setText("Process Executed");
										progressBar_1.setIndeterminate(true);
										progressBar_2.setIndeterminate(true);
										progressBar_3.setIndeterminate(true);
										progressBar_4.setIndeterminate(true);
										btnExecute.setEnabled(false);
										btnSaveForQueue.setEnabled(false);
										btnEditQueue.setEnabled(false);
										btnExecuteQueue.setEnabled(false);
										starter = new QueueStarter(window, stopwatchTimer, settings, dir, progressBar_1, progressBar_2, progressBar_3,
												progressBar_4, progressBar_5, btnExecute, btnPause, btnAbort, btnFullAbort, txtTimeEstimate,
												outputArea, errorArea);
										starter.start();
									}
									catch (IOException i)
									{
										logger.fatal("Failed Save Attempt: {}", quickPart);
										logger.fatal("Exception Infomation", i);
										JOptionPane.showMessageDialog(null, "Unable to Access Folders\n" + quickPath + "\nCrashing Program",
												"IO Exception", JOptionPane.ERROR_MESSAGE);
										System.exit(20);
									}
								}
								else
								{
									logger.info("Restarting as a Single Process");
									txtTimeRunning.setEnabled(true);
									progressBar_1.setIndeterminate(false);
									progressBar_2.setIndeterminate(false);
									progressBar_3.setIndeterminate(false);
									progressBar_4.setIndeterminate(false);
									txtTimeRunning.setText("Calculating");
									if (block.getProgress() == 1)
									{
										logger.info("Restarting Process at Stage 1");
										if (!block.isPaused())
										{
											try
											{
												FileUtils.cleanDirectory(new File(block.getDir() + File.separator + "QueuedFrames"));
												logger.info("Files Deleted: {}QueuedFrames", File.separator);
											}
											catch (IOException e)
											{
												logger.fatal("Failed Delete Attempt: {}QueuedFrames", File.separator);
												logger.fatal("Exception Infomation", e);
												JOptionPane.showMessageDialog(null,
														"Unable to Delete File\n" + File.separator + "QueuedFrames\nCrashing Program", "IO Exception",
														JOptionPane.ERROR_MESSAGE);
												System.exit(20);
											}
										}
										btnPause.setEnabled(true);
										btnAbort.setEnabled(true);
										btnExecute.setText("Executing Stage 1");
										Stage1 stage1 = new Stage1(window, block, stopwatchTimer);
										stage1.execute();
									}
									else if (block.getProgress() == 2)
									{
										logger.info("Restarting Process at Stage 2");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											String folderLabel;
											if (block.isUseRaw())
											{
												folderLabel = new String("IntermediateFrames");
											}
											else
											{
												folderLabel = new String("ProcessedFrames");
											}
											File resultFolder = new File(block.getDir() + File.separator + folderLabel);
											int finalframe = resultFolder.list().length - 3;
											if (finalframe < 0)
											{
												finalframe = 0;
											}
											for (int x = 1; x <= finalframe; x++)
											{
												String quickPart = new String(File.separator + "QueuedFrames" + File.separator + "OEP"
														+ String.format("%06d", x) + "." + settings.getImage());
												String framePath = new String(block.getDir() + quickPart);
												try
												{
													Files.deleteIfExists(Paths.get(framePath));
													logger.info("Frames Deleted: {}", quickPart);
												}
												catch (IOException e)
												{
													logger.fatal("Failed Delete Attempt: {}", quickPart);
													logger.fatal("Exception Infomation", e);
													JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
															"IO Exception", JOptionPane.ERROR_MESSAGE);
													System.exit(20);
												}
											}
										}
										btnPause.setEnabled(true);
										btnAbort.setEnabled(true);
										btnExecute.setText("Executing Stage 2");
										Stage2 stage2 = new Stage2(window, block, stopwatchTimer);
										stage2.execute();
									}
									else if (block.getProgress() == 3)
									{
										logger.info("Restarting Process at Stage 3");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio2())
										{
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											File resultFolder = new File(block.getDir() + File.separator + "ProcessedFrames");
											int finalframe = resultFolder.list().length;
											int startframe = finalframe - 5;
											if (startframe < 0)
											{
												startframe = 0;
											}
											for (int x = startframe; x <= finalframe; x++)
											{
												String quickPart = new String(File.separator + "ProcessedFrames" + File.separator + "OEP"
														+ String.format("%06d", x) + "." + settings.getImage());
												String framePath = new String(block.getDir() + quickPart);
												try
												{
													Files.deleteIfExists(Paths.get(framePath));
													logger.info("Frame Deleted: {}", quickPart);
												}
												catch (IOException e)
												{
													logger.fatal("Failed Delete Attempt: {}", quickPart);
													logger.fatal("Exception Infomation", e);
													JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
															"IO Exception", JOptionPane.ERROR_MESSAGE);
													System.exit(20);
												}
											}
										}
										btnPause.setEnabled(true);
										btnAbort.setEnabled(true);
										btnExecute.setText("Executing Stage 3");
										Stage3 stage3 = new Stage3(window, block, stopwatchTimer);
										stage3.execute();
									}
									else if (block.getProgress() == 4)
									{
										logger.info("Restarting Process at Stage 4");
										if (block.isRadio1())
										{
											progressBar_1.setValue(block.getTotalFrames());
											progressBar_1.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio2())
										{
											progressBar_2.setValue(block.getTotalFrames());
											progressBar_2.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										else if (block.isRadio3())
										{
											progressBar_3.setValue(block.getTotalFrames());
											progressBar_3.setString("(" + block.getTotalFrames() + "/" + block.getTotalFrames() + ") - 100%");
										}
										if (!block.isPaused())
										{
											String quickPart = new String(File.separator + "Export" + File.separator + block.getEep() + "_"
													+ block.getShowName() + "." + block.getExportExtension());
											try
											{
												Files.deleteIfExists(Paths.get(block.getDir() + quickPart));
												logger.info("File Deleted: {}", quickPart);
											}
											catch (IOException e)
											{
												logger.fatal("Failed Delete Attempt: {}", quickPart);
												logger.fatal("Exception Infomation", e);
												JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPart + "\nCrashing Program",
														"IO Exception", JOptionPane.ERROR_MESSAGE);
												System.exit(20);
											}
										}
										btnPause.setEnabled(true);
										btnAbort.setEnabled(true);
										btnExecute.setText("Executing Stage 4");
										Stage4 stage4 = new Stage4(window, block, stopwatchTimer);
										stage4.execute();
									}
								}
							}
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
								window.toggleMenu(0, true);
								logger.error("Process File Corrupted: {} - Deleting File", quickPath);
								logger.fatal("Exception Infomation", b);
								JOptionPane.showMessageDialog(window,
										"Program Failed to interpret File:\n" + quickPath
												+ "\nIs this a actual Queue File?\nHas this been tampered with?\nNow Deleting...",
										"IO Exception", JOptionPane.ERROR_MESSAGE);
								Files.deleteIfExists(new File(dir + quickPath).toPath());
							}
							catch (IOException e)
							{
								logger.fatal("Failed Delete Attempt: {}", quickPath);
								logger.fatal("Exception Infomation", e);
								JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program", "IO Exception",
										JOptionPane.ERROR_MESSAGE);
								System.exit(20);
							}
						}
						catch (IOException e1)
						{
							logger.fatal("Failed Parse Attempt: {}", quickPath);
							logger.fatal("Exception Infomation", e1);
							JOptionPane.showMessageDialog(window, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
									JOptionPane.ERROR_MESSAGE);
							System.exit(20);
						}
					}
					else
					{
						try
						{
							Files.deleteIfExists(new File(dir + quickPath).toPath());
						}
						catch (IOException e)
						{
							logger.fatal("Failed Delete Attempt: {}", quickPath);
							logger.fatal("Exception Infomation", e);
							JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + quickPath + "\nCrashing Program", "IO Exception",
									JOptionPane.ERROR_MESSAGE);
							System.exit(20);
						}

					}
				}
			}
		});
		thread.start();
	}

	public void updateQueue()
	{
		logger.info("Verifying Queue Files");
		btnEditQueue.setEnabled(false);
		btnSaveForQueue.setEnabled(false);
		btnExecuteQueue.setEnabled(false);
		btnEditQueue.setText("updating...");
		btnSaveForQueue.setText("updating...");
		btnExecuteQueue.setText("updating...");
		File parentDir = new File(dir + File.separator + "Queue");
		File[] list = parentDir.listFiles(filter);
		int totalProcessCount = list.length;
		Arrays.sort(list);
		for (int x = 1; x <= totalProcessCount; x++)
		{
			String queueName = new String("Q" + String.format("%03d", x) + ".ser");
			if (!list[x - 1].getAbsolutePath().equals(queueName))
			{
				list[x - 1].renameTo(new File(dir + File.separator + "Queue" + File.separator + queueName));
			}
		}
		if (totalProcessCount == 0)
		{
			logger.info("Verification Complete with 0 Queue Files");
			btnExecuteQueue.setEnabled(false);
			btnEditQueue.setEnabled(false);
			btnSaveForQueue.setEnabled(true);
			btnEditQueue.setText("Delete Queue");
			btnSaveForQueue.setText("Save for Queue");
			btnExecuteQueue.setText("Start Queue");
		}
		else
		{
			logger.info("Verification Complete with {} Queue Files", totalProcessCount);
			btnExecuteQueue.setEnabled(true);
			btnEditQueue.setEnabled(true);
			btnSaveForQueue.setEnabled(true);
			btnEditQueue.setText("Delete Queue");
			btnSaveForQueue.setText("Save For Queue");
			btnExecuteQueue.setText("Start Queue");
		}
	}

	public void quit()
	{
		logger.debug("External Stage Termination Requested");
		if (stage0 != null)
		{
			stage0.kill();
		}
		if (starter != null)
		{
			starter.kill();
		}
	}

	public void updateSettings(Settings settings)
	{
		this.settings = settings;
		if (settings.isRaw())
		{
			logger.debug("Allowing RawTherapee Usage");
			btnRawtherapee.setEnabled(true);
			btnRawtherapee.setBackground(new Color(255, 255, 100));
			btnRawtherapee.setSelected(false);
			btnRawtherapee.setText("RawTherapee Disabled");
		}
		else
		{
			logger.debug("Disabling RawTherapee Usage");
			btnRawtherapee.setEnabled(false);
			btnRawtherapee.setBackground(new Color(150, 150, 150));
			btnRawtherapee.setSelected(false);
			btnRawtherapee.setText("RawTherapee Unavaliable");
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
		rdbtnP_1.setEnabled(!value);
		rdbtnP_2.setEnabled(!value);
		rdbtnP_3.setEnabled(!value);
		rdbtnP_4.setEnabled(!value);
		btnRawtherapee.setEnabled(!value);
	}
}
