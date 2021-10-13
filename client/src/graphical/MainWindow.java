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

import java.awt.CardLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import main.Settings;

@SuppressWarnings("serial")
public class MainWindow extends JFrame
{

	private static Logger logger = LogManager.getLogger();
	private String dir;
	private Settings settings;
	private DVDupscale dupscale;
	private QuickScripts scripts;
	private Help rupscale;
	private PreferencesPane preferences;
	private JMenuItem menuUpscale;
	private JMenuItem menuQuickScripts;
	private JMenuItem menuOpenFolder;
	private JMenuItem menuPreferences;
	private JMenuItem menuHelp;

	public MainWindow()
	{

	}

	public void execute(String dir, Settings s, boolean first)
	{
		this.dir = dir;
		this.settings = s;
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/resources/logo.png")));
		setTitle("FoxTrotUpscaler");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent arg0)
			{
				logger.info("Program Quit Requested");
				int confirm = JOptionPane.showConfirmDialog(null,
						"Closing this Program \"should\" Terminate all Processes! \n Process Likely still recoverable on Restart",
						"Exit FoxTrotUpscaler?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirm == JOptionPane.OK_OPTION)
				{
					logger.info("Program Quitting");
					preferences.shutDownStreams();
					dupscale.quit();
					dispose();
					System.exit(0);
				}
				logger.info("Program left Alive");
			}
		});
		setBounds(100, 100, 1000, 900);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		getContentPane().setLayout(new CardLayout(0, 0));

		dupscale = new DVDupscale(this, dir, s);
		getContentPane().add(dupscale);
		rupscale = new Help();
		getContentPane().add(rupscale);
		scripts = new QuickScripts(this, dir, s);
		getContentPane().add(scripts);
		preferences = new PreferencesPane(this, dir, s);
		getContentPane().add(preferences);

		menuHelp = new JMenuItem("Help");
		menuHelp.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (!rupscale.isShowing())
				{
					logger.debug("Help View Requested");
					rupscale.setVisible(true);
					dupscale.setVisible(false);
					scripts.setVisible(false);
					preferences.setVisible(false);
				}
			}
		});

		menuUpscale = new JMenuItem("Setup");
		menuUpscale.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (!dupscale.isShowing())
				{
					logger.debug("Setup View Requested");
					dupscale.setVisible(true);
					rupscale.setVisible(false);
					scripts.setVisible(false);
					preferences.setVisible(false);
				}
			}
		});
		menuBar.add(menuUpscale);

		menuOpenFolder = new JMenuItem("Open Folder");
		menuOpenFolder.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				logger.debug("Parent Directory Folder View Requested");
				try
				{
					Runtime.getRuntime().exec("explorer " + dir);
				}
				catch (IOException e)
				{
					logger.fatal("Failed Open Attempt: {}", dir);
					logger.fatal("Exception Infomation", e);
					JOptionPane.showMessageDialog(null, "Unable to Open File\n" + dir + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
			}
		});

		menuQuickScripts = new JMenuItem("Quick Scripts");
		menuQuickScripts.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if (!scripts.isShowing())
				{
					logger.debug("Quick Scripts View Requested");
					dupscale.setVisible(false);
					rupscale.setVisible(false);
					scripts.setVisible(true);
					preferences.setVisible(false);
				}
			}
		});
		menuBar.add(menuQuickScripts);
		menuBar.add(menuOpenFolder);

		menuPreferences = new JMenuItem("Preferences");
		menuPreferences.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				if (!preferences.isShowing())
				{
					logger.debug("Preferences View Requested");
					dupscale.setVisible(false);
					rupscale.setVisible(false);
					scripts.setVisible(false);
					preferences.setVisible(true);
				}
			}
		});
		menuBar.add(menuPreferences);

		menuBar.add(menuHelp);
		if (first)
		{
			rupscale.setVisible(true);
			dupscale.setVisible(false);
			scripts.setVisible(false);
		}
	}

	/**
	 * Attempts to change the visible panel if possible. Locked Panels will always return false.
	 * @param The Panel number to change to, in order left to right starting from 1
	 * @return if successful
	 */
	public boolean changePanel(int panel)
	{
		//Check if unlocked
		if (menuUpscale.isEnabled() && menuQuickScripts.isEnabled() && menuPreferences.isEnabled())
		{
			switch (panel)
			{
				case 1:
					menuUpscale.doClick();
					break;
				case 2:
					menuQuickScripts.doClick();
					break;
				case 4:
					menuPreferences.doClick();
					break;
				case 5:
					menuHelp.doClick();
					break;
				default:
					throw logger.throwing(new IllegalArgumentException("Invalid Panel Number! Must be either 1,2,4,5"));
			}
			return true;
		}
		else
			return false;
	}

	public boolean checkPanel(boolean shouldBeLocked, int panel)
	{
		boolean visible = false;
		boolean locked = true;
		if (menuUpscale.isEnabled() && menuQuickScripts.isEnabled() && menuPreferences.isEnabled())
			locked = false;
		switch (panel)
		{
			case 0:
				return !locked;
			case 1:
				visible = dupscale.isVisible();
				break;
			case 2:
				visible = scripts.isVisible();
				break;
			case 4:
				visible = preferences.isVisible();
				break;
			case 5:
				visible = rupscale.isVisible();
				break;
			default:
				throw logger.throwing(new IllegalArgumentException("Invalid Panel Number! Must be either 1,2,4,5"));
		}
		if (shouldBeLocked)
			return (locked && visible);
		else
			return (!locked && visible);
	}

	/**
	 * Method Decides if it is necessary to stop running program before overwriting files
	 * Already uses UI to let User know if it is attempting to Overwrite. Calling thread should just begin to terminate if
	 * true, instead of doing UI stuff
	 * @param product String with that starts with File.seperator and rest of Path to File
	 * @return If the Program should continue, or exit before overwriting
	 */
	public boolean canContinue(String product)
	{
		if (settings.isOverwrite())
		{
			logger.trace("Overwriting Acceptable, Ignoring Search");
			return true;
		}
		else
		{
			String productLocation = new String(dir + product);
			File export = new File(productLocation);
			if (export.exists())
			{
				logger.error("Can Not Overwrite File: {}", product);
				JOptionPane.showMessageDialog(this,
						"Can Not Overwrite File:\n" + product + "\nEither Remove File or change Preferences to Allow Overwriting",
						"Overwriting Possible", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else
			{
				logger.debug("File Not Found : {} - Continuing", product);
				return true;
			}
		}
	}

	public DVDupscale getUpscalePanel()
	{
		return dupscale;
	}

	public void toggleMenu(int avaliable, boolean value)
	{
		if (value)
		{
			logger.debug("Enableing Menu Bar");
			menuUpscale.setEnabled(value);
			menuQuickScripts.setEnabled(value);
			menuPreferences.setEnabled(value);
			dupscale.lockGUI(false);
			scripts.lockGUI(false);
		}
		else
		{
			if (avaliable == 0)
			{
				logger.debug("Disabling Menu Bar Minus Setup Panel");
				menuQuickScripts.setEnabled(value);
				menuPreferences.setEnabled(value);
				dupscale.lockGUI(true);
			}
			else if (avaliable == 1)
			{
				logger.debug("Disabling Menu Bar Minus Quick Scripts Panel");
				menuUpscale.setEnabled(value);
				menuPreferences.setEnabled(value);
				scripts.lockGUI(true);
			}
			else if (avaliable == 3)
			{
				logger.debug("Disabling Menu Bar Minus Settings Panel");
				menuUpscale.setEnabled(value);
				menuQuickScripts.setEnabled(value);
			}
		}
	}

	public void updatePreferencesGUI()
	{
		preferences.refreshSettings();
		preferences.updatePanel();
	}

	public void updateSettings(Settings settings)
	{
		this.settings = settings;
		dupscale.updateSettings(settings);
		scripts.updateSettings(settings);
	}

	public void shutdown() throws RuntimeException
	{
		String shutdownCommand;
		String operatingSystem = System.getProperty("os.name").toLowerCase();

		if (operatingSystem.contains("linux") || operatingSystem.contains("mac"))
		{
			shutdownCommand = "shutdown -h now FoxTrotUpscaler Completed Work, Shutting Down";
		}
		else if (operatingSystem.contains("win"))
		{
			shutdownCommand = "shutdown /p /f /d p:0:0";
		}
		else
		{
			throw new RuntimeException("Unsupported operating system.");
		}
		Timer timer = new Timer(60000, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Runtime.getRuntime().exec(shutdownCommand);
				}
				catch (IOException ex)
				{
					logger.fatal("Unable to Execute Command!");
				}
				System.exit(0);
			}
		});
		timer.setRepeats(false);
		timer.start();
		int option = JOptionPane.showOptionDialog(this,
				"You requested this Computer would shutdown after a flawless completion\nIn 60 seconds this Computer will shutdown\nDo you want to Shutdown Now? or Cancel?",
				"Shutdown Computer?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]
				{ "Shutdown NOW", "Cancel" }, "Shutdown NOW");
		if (option == JOptionPane.OK_OPTION)
		{
			timer.getActionListeners()[0].actionPerformed(null);
		}
		else
		{
			timer.stop();
		}
	}
}
