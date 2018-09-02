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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FoxTrotUpscaler. If not, see <http://www.gnu.org/licenses/>.
 */

package graphical;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
/*
 * import java.awt.GradientPaint;
 * import java.awt.Graphics;
 * import java.awt.Graphics2D;
 * import java.awt.Point;
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import main.DRI;
import main.Settings;
import net.miginfocom.swing.MigLayout;

public class PreferencesPane extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1175137204764269275L;
	private static Logger logger = LogManager.getLogger();
	private boolean changeMade;
	private JTextField program1Field;
	private JTextField program2Field;
	private JTextField program3Field;
	private JTextField program4Field;
	private Settings intermediateSettings;
	private Settings currentSettings;
	private String dir;
	private MainWindow window;
	private JButton btnFileChooser_1;
	private JButton btnFileChooser_2;
	private JButton btnFileChooser_3;
	private JButton btnFileChooser_4;
	private JButton btnSavePreferences;
	private JButton btnRevertPreferences;
	private JButton btnResetPrefernces;
	private JRadioButton radioPNG;
	private JRadioButton radioTiff;
	private JCheckBox chckbxRawtherapee;
	private JToggleButton btnOverwrite;
	private JTextField txtHostName;
	private JButton tglbtnReport;
	private JTextField txtClient;
	private JLabel lblClientId;
	private JTextField txtPort;
	private boolean streamChanged;
	private JLabel lblReporting;
	private JLabel lblWebdavUpload;
	private JButton tglbtnUpload;
	private JTextField txtFilePath;
	private JLabel lblFilePath;
	private JTextField txtUsername;
	private JPasswordField passwordField;
	private JLabel lblPassword;
	private JLabel lblUsername;
	private JLabel lblUploading;
	private JSeparator separator_5;
	private JToggleButton tglbtnShutdown;
	private JLabel lblShutdown;

	public static void main(String[] args)
	{
		File temp = new File("");
		try
		{
			temp = new File(PreferencesPane.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dir;
		if (temp.getAbsolutePath().endsWith("jar"))
			dir = temp.getParent();
		else
			dir = temp.getAbsolutePath();
		System.setProperty("directory", dir);
		logger = LogManager.getLogger();
		logger.info("Directory name found: {}", dir);
		try
		{
			FileInputStream fileIn = new FileInputStream(dir + File.separator + "Temp" + File.separator + "GlobalData.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Settings s = (Settings) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Reporting2: " + s.isReporting());
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * Create the panel.
	 */
	@SuppressWarnings("serial")
	public PreferencesPane(MainWindow window, String dir, Settings sett)
	{
		this.window = window;
		currentSettings = sett;
		intermediateSettings = new Settings(sett);// copy with Reporting off
													// until live
		streamChanged = false;
		changeMade = false;
		this.dir = dir;
		setLayout(new MigLayout("", "[][grow][][]",
				"[-14.00][][][][][][-4.00][][][-5.00,center][][18.00][-7.00][][][][-14.00][][][][1.00][][][][grow]"));

		JSeparator separator = new JSeparator();
		add(separator, "cell 0 0 4 1,growx");

		JLabel lblDependancys = new JLabel("Dependancy File Paths:");
		add(lblDependancys, "cell 0 1 4 1");

		JLabel lblWaifuxcaffecui = new JLabel("Waifu2x-caffe-cui:");
		add(lblWaifuxcaffecui, "cell 0 2,alignx trailing");

		program1Field = new JTextField();
		program1Field.setEditable(false);
		add(program1Field, "cell 1 2,growx");
		program1Field.setColumns(10);

		btnFileChooser_1 = new JButton("File Chooser");
		program1Field.setText(intermediateSettings.getWaifu());
		btnFileChooser_1.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				logger.debug("Waifu2x-caffe File Selection Requested");
				if (intermediateSettings.changePath(0))
				{
					program1Field.setText(intermediateSettings.getWaifu());
					changeMade();
				}
			}
		});
		add(btnFileChooser_1, "cell 2 2");

		JButton btnOpenWebsite_1 = new JButton("Open Website");
		btnOpenWebsite_1.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Opening Website for Waifu2x-caffe");
				intermediateSettings.openWebsite(0);
			}
		});
		add(btnOpenWebsite_1, "cell 3 2");

		JLabel lblFfmpeg = new JLabel("FFMPEG:");
		add(lblFfmpeg, "cell 0 3,alignx trailing");

		program2Field = new JTextField();
		program2Field.setEditable(false);
		program2Field.setText(intermediateSettings.getFfmpeg());
		add(program2Field, "cell 1 3,growx");
		program2Field.setColumns(10);

		btnFileChooser_2 = new JButton("File Chooser");
		program2Field.setText(intermediateSettings.getFfmpeg());
		btnFileChooser_2.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("FFMPEG File Selection Requested");
				if (intermediateSettings.changePath(1))
				{
					program2Field.setText(intermediateSettings.getFfmpeg());
					changeMade();
				}
			}
		});
		add(btnFileChooser_2, "cell 2 3");

		JButton btnOpenWebsite_2 = new JButton("Open Website");
		btnOpenWebsite_2.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Opening Website for FFMPEG");
				intermediateSettings.openWebsite(1);
			}
		});
		add(btnOpenWebsite_2, "cell 3 3");

		JLabel lblFfprobe = new JLabel("Ffprobe:");
		add(lblFfprobe, "cell 0 4,alignx trailing");

		program3Field = new JTextField();
		program3Field.setEditable(false);
		program3Field.setText(intermediateSettings.getFfprobe());
		add(program3Field, "cell 1 4,growx");
		program3Field.setColumns(10);

		btnFileChooser_3 = new JButton("File Chooser");
		btnFileChooser_3.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("FFProbe File Selection Requested");
				if (intermediateSettings.changePath(2))
				{
					program3Field.setText(intermediateSettings.getFfprobe());
					changeMade();
				}
			}
		});
		add(btnFileChooser_3, "cell 2 4");

		JButton btnOpenWebsite_3 = new JButton("Open Website");
		btnOpenWebsite_3.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Opening Website for FFMPEG");
				intermediateSettings.openWebsite(2);
			}
		});
		add(btnOpenWebsite_3, "cell 3 4");

		chckbxRawtherapee = new JCheckBox("RawTherapee:");
		chckbxRawtherapee.setSelected(intermediateSettings.isRaw());
		chckbxRawtherapee.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (chckbxRawtherapee.isSelected())
				{
					if (intermediateSettings.isComplete(3))
					{
						logger.debug("INTERIM CHANGE: Allowing RawTherapee Use");
						program4Field.setEnabled(true);
						program4Field.setText(intermediateSettings.getRawTherapee());
						btnFileChooser_4.setEnabled(true);
						intermediateSettings.setRaw(true);
						changeMade();
					}
					else
					{
						if (intermediateSettings.changePath(3))
						{
							logger.debug("INTERIM CHANGE: Allowing RawTherapee Use after Path Request");
							program4Field.setEnabled(true);
							program4Field.setText(intermediateSettings.getRawTherapee());
							btnFileChooser_4.setEnabled(true);
							intermediateSettings.setRaw(true);
							changeMade();
						}
						else
						{
							logger.error("INTERIM CHANGE: Unlock Failed, Restricting RawTherapee Use");
							JOptionPane.showMessageDialog(window, "Program Not Recognized\nNot Enabling RawTherapee Use", "Input Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				else
				{
					logger.debug("INTERIM CHANGE: Restricted RawTherapee Use");
					program4Field.setEnabled(false);
					btnFileChooser_4.setEnabled(false);
					intermediateSettings.setRaw(false);
					changeMade();
				}
			}
		});
		add(chckbxRawtherapee, "cell 0 5,alignx trailing");

		program4Field = new JTextField();
		program4Field.setEnabled(false);
		program4Field.setEditable(false);
		program4Field.setText(intermediateSettings.getRawTherapee());
		add(program4Field, "cell 1 5,growx");
		program4Field.setColumns(10);

		btnFileChooser_4 = new JButton("File Chooser");
		btnFileChooser_4.setEnabled(false);
		btnFileChooser_4.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("RawTherapee File Selection Requested");
				if (intermediateSettings.changePath(3))
				{
					program4Field.setText(intermediateSettings.getRawTherapee());
					changeMade();
				}
			}
		});
		add(btnFileChooser_4, "cell 2 5");
		if (chckbxRawtherapee.isSelected())
		{
			program4Field.setEnabled(true);
			btnFileChooser_4.setEnabled(true);
		}

		JButton btnOpenWebsite_4 = new JButton("Open Website");
		btnOpenWebsite_4.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Opening Website for FFMPEG");
				intermediateSettings.openWebsite(3);
			}
		});
		add(btnOpenWebsite_4, "cell 3 5");

		JSeparator separator_1 = new JSeparator();
		add(separator_1, "cell 0 6 4 1,growx");

		JLabel lblIntermediaryImageFile = new JLabel("Intermediary Image File Format:");
		add(lblIntermediaryImageFile, "cell 0 7 2 1");

		radioPNG = new JRadioButton("PNG");
		radioPNG.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("INTERIM CHANGE: Intermediary Image File Format set to PNG");
				intermediateSettings.setImage(false);
				changeMade();
			}
		});

		lblShutdown = new JLabel("Shutdown on Process Completion?");
		add(lblShutdown, "cell 2 7 2 1,alignx right");
		radioPNG.setToolTipText("Execution is Slower then Uncompressed TIFF, but does not use as much Disk Space");
		add(radioPNG, "cell 0 8");

		radioTiff = new JRadioButton("Uncompressed TIFF");
		radioTiff.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("INTERIM CHANGE: Intermediary Image File Format set to Uncompressed TIFF");
				intermediateSettings.setImage(true);
				changeMade();
			}
		});
		radioTiff.setToolTipText("Theoretically makes Stage 2 and 3 Faster, but takes more Disk Space.");
		add(radioTiff, "cell 1 8");

		ButtonGroup imageGroup = new ButtonGroup();
		imageGroup.add(radioPNG);
		imageGroup.add(radioTiff);

		if (intermediateSettings.isImage())
		{
			radioTiff.setSelected(true);
		}
		else
		{
			radioPNG.setSelected(true);
		}

		tglbtnShutdown = new JToggleButton("");
		if (intermediateSettings.isShutdown())
		{
			tglbtnShutdown.setSelected(true);
			tglbtnShutdown.setText("Shutdown");
		}
		else
		{
			tglbtnShutdown.setSelected(false);
			tglbtnShutdown.setText("Stay On");
		}
		tglbtnShutdown.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (tglbtnShutdown.isSelected())
				{
					logger.debug("INTERIM CHANGE: Will ShutDown Computer on completion");
					intermediateSettings.setShutdown(true);
					tglbtnShutdown.setText("Shutdown");
					changeMade();
				}
				else
				{
					logger.debug("INTERIM CHANGE: Will Stay On Computer on completion");
					intermediateSettings.setShutdown(false);
					tglbtnShutdown.setText("Stay On");
					changeMade();
				}
			}
		});
		add(tglbtnShutdown, "cell 3 8,growx");

		JSeparator separator_2 = new JSeparator();
		add(separator_2, "cell 0 9 4 1,growx,aligny center");

		JLabel lblOverwriteAnyData = new JLabel("Overwrite ANY Data if Required:");
		add(lblOverwriteAnyData, "cell 0 10 3 1");

		btnOverwrite = new JToggleButton("")
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
		btnOverwrite.setContentAreaFilled(false);
		if (intermediateSettings.isOverwrite())
		{
			btnOverwrite.setText("Will Overwrite Data");
			btnOverwrite.setSelected(true);
			btnOverwrite.setBackground(new Color(255, 200, 100));
		}
		else
		{
			btnOverwrite.setText("Will Stall on finding Existing Files");
			btnOverwrite.setSelected(false);
			btnOverwrite.setBackground(new Color(150, 255, 150));
		}
		btnOverwrite.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (btnOverwrite.isSelected())
				{
					logger.debug("INTERIM CHANGE: Will Overwrite Final Files Produced");
					btnOverwrite.setText("Will Overwrite Data");
					btnOverwrite.setBackground(new Color(255, 200, 100));
					intermediateSettings.setOverwrite(true);
					changeMade();
				}
				else
				{
					logger.debug("INTERIM CHANGE: Will Stall when Existing Files are Found");
					btnOverwrite.setText("Will Stall on finding Existing Files");
					btnOverwrite.setBackground(new Color(150, 255, 150));
					intermediateSettings.setOverwrite(false);
					changeMade();
				}
			}
		});
		add(btnOverwrite, "cell 0 11 4 1,grow");

		JSeparator separator_3 = new JSeparator();
		add(separator_3, "cell 0 12 4 1,growx");

		JLabel lblFtuserverbotSettings = new JLabel("FTUServerBot Settings");
		add(lblFtuserverbotSettings, "cell 0 13");

		JLabel lblHostName = new JLabel("Host Name:");
		add(lblHostName, "cell 1 13,alignx center");

		tglbtnReport = new JButton();
		if (currentSettings.isReporting())
		{
			tglbtnReport.setText("Unlink");
		}
		else
		{
			tglbtnReport.setText("Link");
		}
		/*
		 * On Check> 1. Destroy all Streams but store 3 Old Parameters in old
		 * settings 2. Verify and Save 3. No Matter what Result, the new
		 * settings good or bad, are live 4. On save, serialize the 3 Paramters,
		 * and give the reference to the stream to the current settings, and
		 * reset Intermediate 5. On Revert, destroy all streams, and use the 3
		 * Old Paramters to To Create the new One, and pass the reference, and
		 * Reset Intermediate
		 */
		tglbtnReport.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				currentSettings.disableReporting(true,false);
				if (!intermediateSettings.isReporting())
				{
					ReporterQueryDialog subwindow = new ReporterQueryDialog(window, intermediateSettings);
					subwindow.pack();
					subwindow.setLocationRelativeTo(window);
					subwindow.setVisible(true);
					if (subwindow.wasVerified())
					{
						logger.info("Report Function Unlocked");
						txtClient.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						txtHostName.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						txtPort.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						txtClient.setText(intermediateSettings.getClientID());
						txtHostName.setText(intermediateSettings.getHostName());
						txtPort.setText(Integer.toString(intermediateSettings.getPort()));
						StringBuilder builder = new StringBuilder();
						builder.append("Connected To: ");
						builder.append(intermediateSettings.getServerName());
						lblReporting.setText(builder.toString());
						tglbtnReport.setText("Unlink");
					}
					else
					{
						tglbtnReport.setText("Link");
						lblReporting.setText("Disconnected");
					}
				}
				else
				{
					logger.info("Report Function Disabled and Locked");
					intermediateSettings.disableReporting(true, true);
					txtHostName.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					txtClient.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					txtPort.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					lblReporting.setText("Unlink");
					tglbtnReport.setText("Connect");
				}
				changeMade();
				streamChanged = true;
			}
		});
		add(tglbtnReport, "cell 0 14,growx");

		lblClientId = new JLabel("Client ID:");
		add(lblClientId, "cell 2 13,alignx center");

		JLabel lblPortNumber = new JLabel("Port Number:");
		add(lblPortNumber, "cell 3 13,alignx center");

		txtHostName = new JTextField();
		txtHostName.setEditable(false);
		txtHostName.setText(currentSettings.getHostName());
		add(txtHostName, "cell 1 14,growx");
		txtHostName.setColumns(10);

		txtClient = new JTextField();
		txtClient.setEditable(false);
		txtClient.setText(currentSettings.getClientID());
		add(txtClient, "cell 2 14,growx");
		txtClient.setColumns(10);

		txtPort = new JTextField();
		txtPort.setEditable(false);
		txtPort.setText(Integer.toString(currentSettings.getPort()));
		add(txtPort, "cell 3 14,growx");
		txtPort.setColumns(10);

		if (currentSettings.isReporting())
		{
			txtClient.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtHostName.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtPort.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		}
		else
		{
			txtHostName.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtClient.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtPort.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		}

		lblReporting = new JLabel("Dynamic1");
		lblReporting.setFont(Font.getFont("Segoe UI Emoji"));
		if (currentSettings.getServerName() != null)
		{
			lblReporting.setText("Connected To: " + intermediateSettings.getServerName());
		}
		else
		{
			lblReporting.setText("Disconnected");
		}
		add(lblReporting, "cell 0 15 4 1,growy");

		JSeparator separator_4 = new JSeparator();
		add(separator_4, "cell 0 16 4 1,growx,aligny center");

		lblWebdavUpload = new JLabel("WebDAV Upload");
		add(lblWebdavUpload, "cell 0 17");

		lblFilePath = new JLabel("File Path:");
		add(lblFilePath, "cell 1 17,alignx center");

		lblUsername = new JLabel("Username:");
		add(lblUsername, "cell 2 17");

		lblPassword = new JLabel("Password:");
		add(lblPassword, "cell 3 17");

		tglbtnUpload = new JButton();
		if (currentSettings.isUploading())
		{
			tglbtnUpload.setText("Disable Uploads");
		}
		else
		{
			tglbtnUpload.setText("Enable Uploads");
		}

		tglbtnUpload.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if (!intermediateSettings.isUploading())
				{
					UploaderQueryDialog subwindow = new UploaderQueryDialog(window, intermediateSettings);
					subwindow.pack();
					subwindow.setLocationRelativeTo(window);
					subwindow.setVisible(true);
					if (subwindow.wasVerified())
					{
						logger.info("Upload Function Unlocked");
						txtUsername.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						txtFilePath.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						passwordField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
						txtUsername.setText(intermediateSettings.getUserName());
						txtFilePath.setText(intermediateSettings.getFilePath());
						passwordField.setText(new String(intermediateSettings.getPassword()));
						tglbtnUpload.setText("Disable Uploads");
						lblUploading.setText("Connected");

					}
					else
					{
						tglbtnUpload.setText("Enable Uploads");
						lblUploading.setText("Disconnected");
					}
					changeMade();
				}
				else
				{
					logger.info("Upload Function Disabled and Locked");
					intermediateSettings.disableUploading();
					txtUsername.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					txtFilePath.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					passwordField.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
					tglbtnUpload.setText("Enable Uploads");
					lblUploading.setText("Disconnected");
					changeMade();
				}
			}
		});
		add(tglbtnUpload, "cell 0 18,growx");

		txtFilePath = new JTextField();
		txtFilePath.setEditable(false);
		txtFilePath.setText(currentSettings.getFilePath());
		add(txtFilePath, "cell 1 18,growx");
		txtFilePath.setColumns(10);

		txtUsername = new JTextField();
		txtUsername.setEditable(false);
		txtUsername.setText(currentSettings.getUserName());
		add(txtUsername, "cell 2 18,growx");
		txtUsername.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setText(new String(currentSettings.getPassword()));
		passwordField.setEditable(false);
		add(passwordField, "cell 3 18,growx");

		if (currentSettings.isUploading())
		{
			txtUsername.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtFilePath.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			passwordField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		}
		else
		{
			txtUsername.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtFilePath.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			passwordField.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		}

		lblUploading = new JLabel("Dynamic2");
		lblUploading.setFont(Font.getFont("Segoe UI Emoji"));
		if (currentSettings.isUploading())
		{
			lblUploading.setText("Connected");
		}
		else
		{
			lblUploading.setText("Disconnected");
		}
		add(lblUploading, "cell 0 19 4 1,growy");

		btnSavePreferences = new JButton("Save Preferences");
		btnSavePreferences.setEnabled(false);
		btnSavePreferences.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Preferences Save Requested");
				if (saveToFile())
				{
					JOptionPane.showMessageDialog(window, "Preferences Saved", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
					DataOutputStream temp = null;
					Socket sock = null;
					DataInputStream reader = null;
					if (!streamChanged)
					{
						// Holds on to Streams to avoid Double Activation while
						// switching
						temp = currentSettings.getOut();
						sock = currentSettings.getSocket();
						reader = currentSettings.getIn();
					}
					// Actually Switches the Settings after all Activity Safety
					// is Done
					// current settings who may have been ACTIVE points to
					// intended New settings data
					currentSettings = intermediateSettings;
					// intermediate is made with fresh copys of the new Current
					// Settings
					intermediateSettings = new Settings(currentSettings);
					if (!streamChanged)
					{
						// The Current live Settings take whatever "Active"
						// Streams existed, either Null or Real
						currentSettings.setOut(temp);
						currentSettings.setSocket(sock);
						currentSettings.setIn(reader);
					}
					logger.info("New {}", intermediateSettings);
					streamChanged = false;
					btnSavePreferences.setEnabled(false);
					btnRevertPreferences.setEnabled(false);
					// Finally updates across the Program
					window.updateSettings(currentSettings);
					window.toggleMenu(3, true);
					changeMade = false;
				}
				else
				{
					JOptionPane.showMessageDialog(window, "Save Errored", "Save Errored", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		separator_5 = new JSeparator();
		add(separator_5, "cell 0 20 4 1,growx,aligny center");
		add(btnSavePreferences, "cell 1 21 2 1,growx");

		btnRevertPreferences = new JButton("Revert Preferences");
		btnRevertPreferences.setEnabled(false);
		btnRevertPreferences.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Reverting Preferences to Previous State");
				logger.debug("Previous {}", intermediateSettings);
				if (streamChanged)
				{
					// Deactivates intermediate settings streams
					intermediateSettings.disableReporting(true, false);
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e1)
					{
						logger.fatal("Settings Stream Update Pause Interupted");
					}
					// Renabled the streams that were correct before, if
					// existing
					currentSettings.verifyReporting();
				}
				intermediateSettings = new Settings(currentSettings);
				updatePanel();
				btnSavePreferences.setEnabled(false);
				btnRevertPreferences.setEnabled(false);
				streamChanged = false;
				window.toggleMenu(3, true);
				changeMade = false;
				logger.info(intermediateSettings);
			}
		});
		add(btnRevertPreferences, "cell 1 22 2 1,growx");

		btnResetPrefernces = new JButton("Reset Prefernces");
		btnResetPrefernces.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				logger.debug("Complete Program Reset Requested");
				int confirm = JOptionPane.showConfirmDialog(window, "Wipe all userdata and restart?", "Reset FoxTrotUpscaler?",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm == JOptionPane.OK_OPTION)
				{
					logger.info("Executing Program Reset");
					String quickPath = new String(File.separator + "Temp" + File.separator + "GlobalData.ser");
					try
					{
						Files.deleteIfExists(new File(dir + quickPath).toPath());
					}
					catch (IOException e1)
					{
						logger.fatal("Failed Delete Attempt: {}", quickPath);
						logger.fatal("Exception Infomation", e1);
						JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
					logger.info("Reset Complete");
					System.exit(0);
				}
			}
		});
		add(btnResetPrefernces, "cell 1 23 2 1,growx");

		JLabel lblVersion = new JLabel("BETA Version " + DRI.version);
		lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblVersion, "cell 0 24 4 1,alignx center");
	}

	private boolean saveToFile()
	{
		FileOutputStream fileOut;
		String quickPath = new String(File.separator + "Temp" + File.separator + "GlobalData.ser");
		logger.info("Attempting Preferences Save: {}", quickPath);
		try
		{
			fileOut = new FileOutputStream(dir + quickPath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(intermediateSettings);
			out.close();
			fileOut.close();
			logger.info("File Saved: {}", quickPath);
			//return true;
		}
		catch (FileNotFoundException e1)
		{
			logger.fatal("Failed Delete Attempt: File Not Found Exception: {}", quickPath);
			logger.fatal("Exception Infomation", e1);
			JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "File Not Found Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		catch (IOException e1)
		{
			logger.fatal("Failed Delete Attempt: IO Exception: {}", quickPath);
			logger.fatal("Exception Infomation", e1);
			JOptionPane.showMessageDialog(null, "Unable to Access File\n" + quickPath + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		//return false;
		try
		{
			FileInputStream fileIn = new FileInputStream(dir + quickPath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Settings s = (Settings) in.readObject();
			in.close();
			fileIn.close();
			logger.debug("Read File: {}", quickPath);
			if (intermediateSettings.equals(s))
			{
				logger.info("Preferences Saved Successfully");
				return true;
			}
			else
			{
				System.out.println("Reread GlobalData.ser Incorrectly");
				return false;
			}
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
			return false;
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			return false;
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
			return false;
		}

	}

	public void updatePanel()
	{
		logger.debug("Updating GUI with New Values");
		program1Field.setText(intermediateSettings.getWaifu());
		program2Field.setText(intermediateSettings.getFfmpeg());
		program3Field.setText(intermediateSettings.getFfprobe());
		program4Field.setText(intermediateSettings.getRawTherapee());
		if (intermediateSettings.isRaw())
		{
			chckbxRawtherapee.setSelected(true);
			program4Field.setEnabled(true);
			btnFileChooser_4.setEnabled(true);
		}
		else
		{
			chckbxRawtherapee.setSelected(false);
			program4Field.setEnabled(false);
			btnFileChooser_4.setEnabled(false);
		}
		if (intermediateSettings.isImage())
		{
			radioTiff.setSelected(true);
			radioPNG.setSelected(false);
		}
		else
		{
			radioTiff.setSelected(false);
			radioPNG.setSelected(true);
		}
		if (intermediateSettings.isOverwrite())
		{
			btnOverwrite.setText("Will Overwrite Data");
			btnOverwrite.setSelected(true);
			btnOverwrite.setBackground(new Color(255, 200, 100));
		}
		else
		{
			btnOverwrite.setText("Will Stall on finding Existing Files");
			btnOverwrite.setSelected(false);
			btnOverwrite.setBackground(new Color(150, 255, 150));
		}
		if (intermediateSettings.isReporting())
		{
			txtClient.setEnabled(true);
			txtHostName.setEnabled(true);
			txtPort.setEnabled(true);
			tglbtnReport.setText("Unlink");
			txtClient.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtHostName.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtPort.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		}
		else
		{
			txtClient.setEnabled(false);
			txtHostName.setEnabled(false);
			txtPort.setEnabled(false);
			tglbtnReport.setText("Link");
			txtHostName.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtClient.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtPort.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		}
		txtClient.setText(intermediateSettings.getClientID());
		txtHostName.setText(intermediateSettings.getHostName());
		txtPort.setText(Integer.toString(intermediateSettings.getPort()));
		if (currentSettings.getServerName() != null)
		{
			lblReporting.setText("Connected To: " + intermediateSettings.getServerName());
		}
		else
		{
			lblReporting.setText("Disconnected");
		}
		if (intermediateSettings.isUploading())
		{
			txtFilePath.setEnabled(true);
			txtUsername.setEnabled(true);
			passwordField.setEnabled(true);
			tglbtnUpload.setText("Disable Uploads");
			lblUploading.setText("Connected");
			txtUsername.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			txtFilePath.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
			passwordField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		}
		else
		{
			txtFilePath.setEnabled(false);
			txtUsername.setEnabled(false);
			passwordField.setEnabled(false);
			tglbtnUpload.setText("Enable Uploads");
			lblUploading.setText("Disconnected");
			txtUsername.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			txtFilePath.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			passwordField.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		}
		txtFilePath.setText(intermediateSettings.getFilePath());
		txtUsername.setText(intermediateSettings.getUserName());
		passwordField.setText(new String(intermediateSettings.getPassword()));
		if (intermediateSettings.isShutdown())
		{
			tglbtnShutdown.setText("Shutdown");
			tglbtnShutdown.setSelected(true);
		}
		else
		{
			tglbtnShutdown.setText("Stay On");
			tglbtnShutdown.setSelected(false);
		}
	}

	private void changeMade()
	{
		if (changeMade)
		{

		}
		else
		{
			logger.debug("Locking GUI to Preferences until Changes Saved or Reverted");
			changeMade = true;
			window.toggleMenu(3, false);
			btnSavePreferences.setEnabled(true);
			btnRevertPreferences.setEnabled(true);
		}
	}

	public void refreshSettings()
	{
		System.out.println(currentSettings);
		System.out.println(intermediateSettings);
		intermediateSettings = new Settings(currentSettings);
	}

	public void shutDownStreams()
	{
		currentSettings.disableReporting(true, false);
		intermediateSettings.disableReporting(true, false);
	}
}
