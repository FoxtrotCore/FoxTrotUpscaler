package com.foxtrotfanatics.ftu.graphical;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * @author Christian
 * Should Sardine Class ever allow Upload Cancellation, or Progress Tracking, structure to implement exists
 */
public class FileTransferProgressDialog extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6933656023802246417L;
	private static Logger logger = LogManager.getLogger();
	private JProgressBar bar;
	private JOptionPane optionPane;
	private JButton cancelButton = new JButton("Cancel");
	private Callable<Boolean> call;
	private FutureTask<Boolean> result;
	private String localPath;
	private String fileName;
	private JTextArea area;
	private Timer timer;
	private Instant startTime;
	private Sardine serverConnection;
	private String networkPath;
	private boolean overwrite;
	private SizeInputStream s;
	private long fileByteSize;
	private boolean ready = true;
	private Throwable error;

	/** Creates the reusable dialog. */
	public FileTransferProgressDialog(boolean upload,
			String networkPath,
			String userName,
			char[] password,
			String fileName,
			String localPath,
			boolean overwrite,
			JFrame window,
			JTextArea area)
	{
		//Init Fields and Data
		super(window, "WebDAV File Transfer", true);
		this.networkPath = networkPath;
		this.overwrite = overwrite;
		this.localPath = localPath;
		this.fileName = fileName;
		this.area = area;
		//Init JProgressBar
		bar = new JProgressBar();
		bar.setStringPainted(true);
		if (upload)
		{
			bar.setString("Uploading...");
			bar.setForeground(Color.CYAN);
			bar.setToolTipText("Upload Progress");
		}
		else
		{
			bar.setString("Downloading...");
			bar.setForeground(Color.ORANGE);
			bar.setToolTipText("Download Progress");
		}
		bar.setValue(0);
		bar.setMaximum(100);
		bar.setUI(new BasicProgressBarUI()
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
		//Test URL syntax and Server Authentication and Communication
		try
		{
			URL locator = new URL(networkPath);
			serverConnection = SardineFactory.begin(userName, new String(password));
			serverConnection.enablePreemptiveAuthentication(locator);
			if (upload)
			{
				serverConnection.exists(networkPath);
				//Gets Size of File from Disk
				fileByteSize = (int) new File(localPath + File.separator + fileName).length();
			}
			else
			{
				//Gets Size of File from Server
				String fullNetworkPath = networkPath + "/" + fileName;
				List<DavResource> list = serverConnection.list(fullNetworkPath,0);
				DavResource r = list.get(0);
				fileByteSize = r.getContentLength();
				//fileByteSize = serverConnection.list(networkPath + "/" + fileName, 0).get(0).getContentLength();
			}
			logger.debug("Expected File Size: {}", fileByteSize);
		}
		catch (MalformedURLException e2)
		{
			logger.error("Bad URL: {}", networkPath, e2.getMessage());
			if (area != null)
				area.append("Bad URL Recieved: " + networkPath);
			bar.setString("Bad URL");
			bar.setIndeterminate(true);
			bar.setForeground(Color.RED);
			//Sardine didn't even get to Init
			fileByteSize = -1;
			ready = false;
		}
		catch (IOException e)
		{
			logger.error("Failed to read from Server", e);
			if (area != null)
				area.append("Failed WebDAVConnection: " + e.getMessage());
			bar.setString("Connection Failed");
			bar.setIndeterminate(true);
			bar.setForeground(Color.RED);
			error = e;
			try
			{
				serverConnection.shutdown();
			}
			catch (IOException e1)
			{
				//Attempted Shutdown
			}
			fileByteSize = -1;
			ready = false;
		}
		// Create an array of the text and components to be displayed.
		String msgString1;
		if (upload)
			msgString1 = "<html><b>Uploading</b> File to Server</html>";
		else
			msgString1 = "<html><b>Downloading</b> File from Server</html>";
		String msgString2 = "<html>Network Path: <u>" + networkPath + "/" + fileName + "</u></html>";
		String msgString3 = "<html>Local Path: <i>" + localPath + File.separator + fileName + "</i></html>";
		String msgString4 = "<html>User: " + userName + "</html>";
		String msgString5 = "<html>Pass: ";//Partially Obfuscates Password
		if (password.length > 8)
		{
			for (int x = 0; x < password.length - 4; x++)
			{
				msgString5 += "*";
			}
			for (int x = password.length - 4; x < password.length; x++)
			{
				msgString5 += password[x];
			}
		}
		else
		{
			for (int x = 0; x < password.length; x++)
			{
				msgString5 += "*";
			}
		}
		msgString5 += "</html>";
		String msgString6 = "<html>File Size: <b>";
		if (fileByteSize != -1)
			msgString6 += humanReadableByteCount(fileByteSize, true) + "</b></html>";
		else
			msgString6 += "?</b></html>";
		Object[] Array =
		{ msgString1, msgString2, msgString3, msgString4, msgString5, msgString6, bar };
		logger.info("File Transfer Parameters:\n{}\n{}\n{}", msgString1, msgString2, msgString3);
		cancelButton.setEnabled(false);
		//Specify how to Close Program
		cancelButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				cancelButton.setEnabled(false);
				result.cancel(false);//Do NOT Interrupt, or will skip the 3 second grace window
				try
				{
					serverConnection.shutdown();
				}
				catch (IOException e1)
				{
					logger.catching(e1);
				}
			}
		});
		// Create an array of just the Options avaliable
		Object[] options =
		{ cancelButton };

		timer = new Timer(1000, this);
		timer.setInitialDelay(1000);
		if (upload)
			prepareUpload();
		else
			prepareDownload();
		// Create the JOptionPane
		optionPane = new JOptionPane(Array, JOptionPane.INFORMATION_MESSAGE, JOptionPane.CANCEL_OPTION, null, options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		//Can not setVisible() Here, as the method blocks for Modal Dialogs
	}

	public int executeTransfer()
	{
		result = new FutureTask<Boolean>(call);
		Thread worker = new Thread(result);
		worker.start();
		cancelButton.setEnabled(true);
		startTime = Instant.now();
		timer.start();
		setVisible(true);//BLOCKS until setVisible(false); or dispose();
		try
		{
			if (result.get())
				return 0;//Completed
			else
				return 2; //Didn't even get to Start, failed in init

		}
		catch (CancellationException e)//Canceled before the IOException could be thrown
		{
			logger.info("File Transfer Cancelled");
			return 3;
		}
		catch (InterruptedException e)//Should Not Happen
		{
			logger.info("File Transfer Interrupted");
			return 4;//Strange case
		}
		catch (ExecutionException e)// Larger Error
		{
			logger.error("File Transfer Failed", e);
			error = e.getCause();
			return 1;//Program ran into error MidDownload
		}
	}
	
	public Throwable getError()
	{
		return error;
	}

	private void prepareUpload()
	{
		call = new Callable<Boolean>()
		{

			@Override
			public Boolean call() throws Exception
			{
				Thread.currentThread().setName("UploaderThread");
				if (!ready)
				{
					logger.trace("Uploader Found Not Ready");
					endDialog(false, "");
					return false;
				}
				if (area != null)
					area.append("\n\nAttempting Upload of Video to " + localPath);
				if (serverConnection.exists(networkPath + "/" + fileName))
				{
					if (!overwrite)
					{
						logger.error("Upload Failed: Not Allowed To Overwrite File");
						if (area != null)
							area.append("\nUpload Failed: Not Allowed To Overwrite File-------------");
						serverConnection.shutdown();
						endDialog(false, "Error: Can Not Overwrite File");
						return false;
					}
					else
					{
						if (area != null)
							area.append("\nFound Source File in Network Directory Already-------------");
						logger.info("Overwriting File in Network Directory: {}", fileName);
						serverConnection.delete(networkPath + "/" + fileName);
					}
				}
				else
				{
					logger.info("Copying {} to Network Directory: {}", fileName, networkPath);
				}
				try (FileInputStream str = new FileInputStream(localPath + File.separator + fileName);
						SizeInputStream stream = new SizeInputStream(str, fileByteSize))
				{
					s = stream;
					serverConnection.put(networkPath + "/" + fileName, stream);// Blocks until completion or Error
					if (area != null)
						area.append("\nUpload Complete");
					bar.setValue(100);
					logger.info("Upload Complete");
					serverConnection.shutdown();
					endDialog(true, "Complete");
					return true;
				}
				catch (IOException i)
				{
					endDialog(false, "Download Stopped");
					throw new IOException(i);
				}
			}

		};
	}

	private void prepareDownload()
	{
		call = new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Thread.currentThread().setName("DownloaderThread");
				if (!ready)
				{
					logger.trace("Downloader Found Not Ready");
					endDialog(false, "");
					return false;
				}
				if (area != null)
					area.append("\n\nAttempting Download of Video to " + localPath);
				String fullLocalPath = localPath + File.separator + fileName;
				File destination = new File(fullLocalPath);
				if (destination.exists())
				{
					if (!overwrite)
					{
						logger.error("Download Failed: Not Allowed To Overwrite File");
						serverConnection.shutdown();
						endDialog(false, "Error: Can't Overwrite");
						return false;
					}
					else
					{
						if (area != null)
							area.append("\nFound Source File in Local Directory Already-------------");
						logger.info("Overwriting File in Local Directory: {}", fileName);
						destination.delete();
					}
				}
				else
				{
					logger.info("Copying {} to Local Directory: {}", fileName, localPath);
				}
				try (SizeInputStream stream = new SizeInputStream((serverConnection.get(networkPath + "/" + fileName)), fileByteSize);)
				{
					//Continue with Download
					s = stream;
					FileUtils.copyToFile(s, destination);//Blocks until Completion or Error
					if (area != null)
						area.append("\nDownload Complete");
					bar.setValue(100);
					logger.info("Download Complete");
					serverConnection.shutdown();
					endDialog(true, "Complete");
					return true;
				}
				catch (IOException i)
				{
					endDialog(false, "Download Stopped");
					throw new IOException(i);
				}
			}
		};
	}

	private void endDialog(boolean successful, String resultFeedback)
	{
		try
		{
			timer.stop();
			if (successful)
			{

			}
			else
			{
				bar.setIndeterminate(true);
			}
			if (!resultFeedback.equals(""))
				bar.setString(resultFeedback);
			Thread.sleep(3000);
			dispose();
		}
		catch (InterruptedException e)
		{
			logger.fatal("InterruptedException", e);
			dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		long duration = ChronoUnit.SECONDS.between(startTime, Instant.now());
		bar.setString(String.format("Time Elapsed %02d:%02d ", TimeUnit.SECONDS.toMinutes(duration),
				duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration))));
		if (s != null)
			bar.setValue(s.percentComplete());
	}

	class SizeInputStream extends InputStream
	{

		// The InputStream to read bytes from
		private InputStream in = null;

		// The number of expected Total Bytes from InputStream
		private long size = 0;

		// The number of bytes that have been read from the InputStream
		private long bytesRead = 0;

		public SizeInputStream(InputStream in, long size)
		{
			this.in = in;
			this.size = size;
		}

		public long availableInLong()
		{
			return (size - bytesRead);
		}

		public int percentComplete()
		{
			return ((int) (bytesRead * 100 / size));
		}

		@Override
		public int read() throws IOException
		{
			int b = in.read();
			if (b != -1)
			{
				bytesRead++;
			}
			return b;
		}

		@Override
		public int read(byte[] b) throws IOException
		{
			int read = in.read(b);
			bytesRead += read;
			return read;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			int read = in.read(b, off, len);
			bytesRead += read;
			return read;
		}
	}

	/**
	 * Credits to aioobe from StackExchange
	 * https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * @param bytes Size of Data in bytes
	 * @param si If True, will use Proper 1024 notation, otherwise will use local 1000 notation
	 * @return String representing the Bytes in a normal standard
	 */
	public static String humanReadableByteCount(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}