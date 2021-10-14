package com.foxtrotfanatics.ftu;

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu.graphical.MainWindow;

public class DRI
{

	private static Logger logger;
	private static String dir;
	private static FileLock lock;
	public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
	public static String version = new String("3.2.5");

	public static void main(String[] args) throws URISyntaxException
	{
		File temp = new File(DRI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		if (temp.getAbsolutePath().endsWith("jar"))
			dir = temp.getParent();
		else
			dir = temp.getAbsolutePath();
		System.setProperty("directory", dir);
		logger = LogManager.getLogger();
		logger.info("Running Version {} of FoxTrotUpscaler", version);
		logger.info("Directory name found: {}", dir);
		DRI.checkIfSingleInstance();

		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				boolean firstInstall = false;
				boolean incompleteWaifu = false;
				boolean incompleteFFMPEG = false;
				boolean incompleteFFprobe = false;
				boolean incompleteRaw = false;
				Settings settings;
				String settingsPath = File.separator + "Temp" + File.separator + "GlobalData.ser";
				FileInputStream fileIn = null;
				ObjectInputStream in = null;
				try
				{
					fileIn = new FileInputStream(dir + settingsPath);
					in = new ObjectInputStream(fileIn);
					settings = (Settings) in.readObject();
					in.close();
					fileIn.close();
					logger.info("Preferences File Parsed: {}", settingsPath);
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
						logger.error("Preferences File Corrupted: {} - Deleting File", settingsPath);
						logger.fatal("Exception Infomation", b);
						JOptionPane.showMessageDialog(null,
								"Program Failed to parse File:\n" + settingsPath
										+ "\nIs this a actual Preferences File?\nHas this been tampered with?\nNow Deleting...",
								"Corrupted File", JOptionPane.ERROR_MESSAGE);
						Files.deleteIfExists(new File(dir + settingsPath).toPath());
					}
					catch (IOException e)
					{
						logger.fatal("Failed Delete Attempt: {}", settingsPath);
						logger.fatal("Exception Infomation", e);
						JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + settingsPath + "\nCrashing Program", "IO Exception",
								JOptionPane.ERROR_MESSAGE);
						System.exit(20);
					}
					settings = new Settings(dir);
				}
				catch (IOException i)
				{
					settings = new Settings(dir);
					logger.warn("File Not Found: {} - Prompting for Locations", settingsPath);
				}
				settings.setDir(dir);
				incompleteWaifu = !settings.isComplete(0);
				incompleteFFprobe = !settings.isComplete(1);
				incompleteFFMPEG = !settings.isComplete(2);
				incompleteRaw = !settings.isComplete(3);
				while (incompleteWaifu)
				{
					logger.info("Waifu2x-caffe-cui.exe was not found");
					int option = JOptionPane.showOptionDialog(null,
							"Waifu2x-Caffe not detected\nPlease Download the Program or\nis or Select the CUI Program Executable",
							"Incomplete Install", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]
					{ "Open Download Site", "Select Location", "Close" }, "Select Location");
					if (option == 0)
					{
						logger.debug("Opening Website for Waifu2x-caffe");
						settings.openWebsite(0);
					}
					else if (option == 1)
					{
						logger.debug("Selecting Program for Waifu2x-caffe");
						incompleteWaifu = !settings.changePath(0);
					}
					else
					{
						logger.debug("Closing Program");
						System.exit(0);
					}
				}
				while (incompleteFFMPEG)
				{
					logger.info("ffmpeg.exe was not found");
					int option = JOptionPane.showOptionDialog(null,
							"FFMPEG not detected\nDouble Check Path Variable\nOr Please Install to the Path Variable\n or Install to New Location with different permissions\nNote: Make sure you download the release, not the source code",
							"Incomplete Install", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]
					{ "Open Download Site", "Select Location", "Close" }, "Select Location");
					if (option == 0)
					{
						logger.debug("Opening Website for FFMPEG");
						settings.openWebsite(1);
					}
					else if (option == 1)
					{
						logger.debug("Selecting Program for FFMPEG");
						incompleteFFMPEG = !settings.changePath(1);
					}
					else
					{
						logger.debug("Closing Program");
						System.exit(0);
					}
				}
				while (incompleteFFprobe)
				{
					logger.info("ffprobe.exe was not found");
					int option = JOptionPane.showOptionDialog(null,
							"ffprobe not detected\nDouble Check Path Variable\nOr Please Install to the Path Variable\n or Install to New Location with different permissions\nNote: Make sure you download the release, not the source code",
							"Incomplete Install", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]
					{ "Open Download Site", "Select Location", "Close" }, "Select Location");
					if (option == 0)
					{
						logger.debug("Opening Website for FFMPEG");
						settings.openWebsite(2);
					}
					else if (option == 1)
					{
						logger.debug("Selecting Program for FFprobe");
						incompleteFFprobe = !settings.changePath(2);
					}
					else
					{
						logger.debug("Closing Program");
						System.exit(0);
					}
				}
				while (incompleteRaw && settings.isRaw())
				{
					logger.info("rawtherapee-cli.exe was not found, giving option");
					int option = JOptionPane.showOptionDialog(null,
							"RawTherapee not detected, but Optional\nDouble Check Path Variable\nOr Please Install to the Path Variable\n or Install to New Location with different permissions",
							"Incomplete Install", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]
					{ "Open Download Site", "Select Location", "Skip" }, "Skip");
					if (option == 0)
					{
						logger.debug("Opening Website for RawTherapee");
						settings.openWebsite(3);
					}
					if (option == 1)
					{
						logger.debug("Selecting Program for RawTherapee");
						incompleteRaw = !settings.changePath(3);
					}
					else
					{
						logger.debug("Ignoreing RawTherapee");
						settings.setRaw(false);
					}
				}
				File config = new File(dir + File.separator + "config.txt");
				if (!config.exists())
				{
					try
					{
						URL inputUrl = DRI.class.getResource("/resources/config.txt");
						File dest = new File(dir + File.separator + "config.txt");
						FileUtils.copyURLToFile(inputUrl, dest);
						logger.info("Created config.txt");
					}
					catch (IOException e)
					{
						logger.fatal("Unable to create config.txt");
						logger.fatal("Exception Infomation", e);
						System.exit(1);
					}
					File configSample = new File(dir + File.separator + "SampleConfig.txt");
					if (!configSample.exists())
					{
						try
						{
							URL inputUrl = DRI.class.getResource("/resources/SampleConfig.txt");
							File dest = new File(dir + File.separator + "SampleConfig.txt");
							FileUtils.copyURLToFile(inputUrl, dest);
							logger.info("Created Sampleconfig.txt");
						}
						catch (IOException e)
						{
							logger.fatal("Unable to create SampleConfig.txt");
							logger.fatal("Exception Information", e);
							System.exit(1);
						}
					}
				}
				File folder1 = new File(dir + File.separator + "Export");
				if (!folder1.exists())
				{
					folder1.mkdir();
					logger.info("Created {}Export", File.separator);
					firstInstall = true;
				}
				File folder2 = new File(dir + File.separator + "ImportAudio");
				if (!folder2.exists())
				{
					folder2.mkdir();
					logger.info("Created {}ImportAudio", File.separator);
					firstInstall = true;
				}
				File folder3 = new File(dir + File.separator + "ImportSubtitle");
				if (!folder3.exists())
				{
					folder3.mkdir();
					logger.info("Created {}ImportSubtitle", File.separator);
					firstInstall = true;
				}
				File folder4 = new File(dir + File.separator + "ImportVideo");
				if (!folder4.exists())
				{
					folder4.mkdir();
					logger.info("Created {}ImportVideo", File.separator);
					firstInstall = true;
				}
				File folder5 = new File(dir + File.separator + "IntermediateFrames");
				if (!folder5.exists())
				{
					folder5.mkdir();
					logger.info("Created {}Intermediate", File.separator);
					firstInstall = true;
				}
				File folder6 = new File(dir + File.separator + "ProcessedFrames");
				if (!folder6.exists())
				{
					folder6.mkdir();
					logger.info("Created {}ProcessedFrames", File.separator);
					firstInstall = true;
				}
				File folder7 = new File(dir + File.separator + "Queue");
				if (!folder7.exists())
				{
					folder7.mkdir();
					logger.info("Created {}Queue", File.separator);
					firstInstall = true;
				}
				File folder8 = new File(dir + File.separator + "QueuedFrames");
				if (!folder8.exists())
				{
					folder8.mkdir();
					logger.info("Created {}QueuedFrames", File.separator);
					firstInstall = true;
				}
				File folder9 = new File(dir + File.separator + "Temp");
				if (!folder9.exists())
				{
					folder9.mkdir();
					logger.info("Created {}Temp", File.separator);
					firstInstall = true;
				}
				FileOutputStream fileOut;
				try
				{
					fileOut = new FileOutputStream(dir + settingsPath);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					settings.setFilledIn(true);
					out.writeObject(settings);
					out.close();
					fileOut.close();
					logger.info("File Saved: {}", settingsPath);
				}
				catch (FileNotFoundException e)
				{
					logger.fatal("Failed Save Attempt: {}", settingsPath);
					logger.fatal("Exception Infomation", e);
					System.exit(10);
				}
				catch (IOException e)
				{
					logger.fatal("Failed Save Attempt: {}", settingsPath);
					logger.fatal("Exception Infomation", e);
					JOptionPane.showMessageDialog(null, "Unable to Delete File\n" + settingsPath + "\nCrashing Program", "IO Exception",
							JOptionPane.ERROR_MESSAGE);
					System.exit(20);
				}
				if (settings.isReporting())
				{
					String settingsResult = settings.verifyReporting();
					if (settingsResult.equals("0"))
					{

					}
					else if (settingsResult.equals("1"))
					{
						JOptionPane.showMessageDialog(null,
								"Server has no avaliable slots\nConnect as registered Client\n or Restart Server for more Space", "Full Server",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (settingsResult.equals("2"))
					{
						JOptionPane.showMessageDialog(null,
								"Client is already logged into Server under\n" + settings.getClientID() + "\nPick Another Name", "Client Conflict",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (settingsResult.equals("3"))
					{
						JOptionPane.showMessageDialog(null, "Connection Failed\nVerify entered Values or\nVerify that Server is Running",
								"Connection Failed", JOptionPane.ERROR_MESSAGE);
					}
					else if (settingsResult.equals("4"))
					{
						JOptionPane.showMessageDialog(null,
								"Port Number " + " Invalid\nNot in the range of acceptable values\nRange: 1024 < x < 49151", "Port Invalid",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (settingsResult.equals("5"))
					{
						JOptionPane.showMessageDialog(null,
								"Bad Key Generated\nor not saved properly\nRetry recreating the Key", "Encryption Failed",
								JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(null,
								"Server is requiring Version " + settingsResult + "\nYou are running Client Version " + DRI.version
										+ "\nCan not Connect to Server\nThere may be an update to either Server or Client",
								"Version Conflict", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (settings.isUploading())
				{
					boolean uploadResult = settings.verifyUploading();
					if (!uploadResult)
					{
						JOptionPane.showMessageDialog(null, "Connection Failed\nVerify entered Values or\nVerify that Server is Running",
								"Connection Failed", JOptionPane.ERROR_MESSAGE);
					}
				}
				logger.debug(settings);
				logger.debug("Initializing Main Window");
				final SplashScreen splash = SplashScreen.getSplashScreen();
				if (splash != null)
				{
					splash.close();
				}
				MainWindow frame = new MainWindow();
				settings.setWindow(frame);
				frame.execute(dir, settings, firstInstall);
				frame.setVisible(true);
			}
		});
	}

	@SuppressWarnings("resource")
	public static void checkIfSingleInstance()
	{
		FileChannel channel;
		try
		{
			File lockedFile = new File(dir + File.separator + "running");
			channel = new RandomAccessFile(lockedFile, "rw").getChannel();

			try
			{

				try
				{
					lock = channel.tryLock();
				}
				catch (OverlappingFileLockException e)
				{
					// already locked
					JOptionPane.showMessageDialog(null, "Another Instance of FoxTrotUpscaler is Already Running\nClosing this one",
							"Multiple Instances", JOptionPane.ERROR_MESSAGE);
					logger.fatal("Multiple Instances Found");
					System.exit(5);
				}

				if (lock == null)
				{
					// already locked
					JOptionPane.showMessageDialog(null, "Another Instance of FoxTrotUpscaler is Already Running\nClosing this one",
							"Multiple Instances", JOptionPane.ERROR_MESSAGE);
					logger.fatal("Multiple Instances Found");
					System.exit(5);
				}

				Runtime.getRuntime().addShutdownHook(new Thread()
				{

					// destroy the lock when the JVM is closing
					public void run()
					{
						try
						{
							lock.release();
						}
						catch (Exception e5)
						{
						}
						try
						{
							channel.close();
						}
						catch (Exception e6)
						{
						}
						try
						{
							lockedFile.delete();
						}
						catch (Exception e7)
						{
						}
					}
				});
			}
			catch (IOException e8)
			{
				// already locked
				JOptionPane.showMessageDialog(null, "Another Instance of FoxTrotUpscaler is Already Running\nClosing this one", "IO Exeception",
						JOptionPane.ERROR_MESSAGE);
				logger.fatal("Multiple Instances Found");
				System.exit(5);
			}
		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
