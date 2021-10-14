package com.foxtrotfanatics.ftu;

import java.awt.EventQueue;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.foxtrotfanatics.ftu.graphical.FileTransferProgressDialog;
import com.foxtrotfanatics.ftu.graphical.MainWindow;
import com.foxtrotfanatics.ftu.structures.Report;

public class Settings implements java.io.Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6666803814192736272L;
	private static Logger logger = LogManager.getLogger();
	private transient MainWindow window;
	private transient String dir;
	private boolean filledIn;
	private String waifu;
	private String ffmpeg;
	private String ffprobe;
	private String rawTherapee;
	private boolean isRaw;// Is Rawtherapee Installed
	private boolean image; // true = Tiff, False = PNG
	private boolean overwrite;// True = Overwrite, False = Stall
	//Reporting MetaData
	private String clientID;
	private String hostName;
	private String serverName;
	private boolean reporting;
	private int port;
	private SecretKey key;//This makes Serialized Setting Objects single handedly sensitive
	//Reporting Functions
	private Report storedReport;
	private transient DataOutputStream out;
	private transient Socket socket;
	private transient DataInputStream in;
	//Uploading MetaData
	private String filePath;
	private String uploadingUserName;
	private char[] uploadingPassword;
	private boolean uploading;
	//Uploading Functions
	private transient ServerCommunicator communicator;
	private boolean shutdown;
	//Constant Links and predetermined directories
	private String[] fileNames = new String[]
	{ "Select waifu2x-caffe-cui.exe", "Select FFMPEG.exe", "Select FFprobe.exe", "Select rawtherapee-cli.exe" };
	private String[] presetPaths = new String[]
	{ "C:" + File.separator + "Program Files", "C:" + File.separator + "Program Files", "C:" + File.separator + "Program Files",
			"C:" + File.separator + "Program Files" + File.separator + "RawTherapee" };
	private String[] websitePaths = new String[]
	{ "https://github.com/lltcggie/waifu2x-caffe/releases", "https://ffmpeg.org/download.html", "https://ffmpeg.org/download.html",
			"http://rawtherapee.com/downloads" };

	public Settings(String dir)
	{
		this.dir = dir;
		waifu = null;
		ffmpeg = null;
		ffprobe = null;
		rawTherapee = null;
		isRaw = true;
		image = false;
		overwrite = false;
		filledIn = false;
		reporting = false;
		uploading = false;
		filePath = new String("https://www.google.com/");
		uploadingUserName = new String("admin");
		uploadingPassword = new char[]
		{ ' ' };
		clientID = new String("JoeBlow's_Computer");
		hostName = new String("localhost");
		serverName = null;
		port = 9626;
		storedReport = null;
		out = null;
		socket = null;
		in = null;
		shutdown = false;
	}

	public Settings(Settings s)
	{

		filledIn = true;
		this.dir = s.getDir();
		this.waifu = s.getWaifu();
		this.ffmpeg = s.getFfmpeg();
		this.ffprobe = s.getFfprobe();
		this.rawTherapee = s.getRawTherapee();
		this.isRaw = s.isRaw();
		this.image = s.isImage();
		this.overwrite = s.isOverwrite();
		this.reporting = s.isReporting();
		this.clientID = s.getClientID();
		this.hostName = s.getHostName();
		this.serverName = s.getServerName();
		this.uploading = s.isUploading();
		this.filePath = s.getFilePath();
		this.uploadingUserName = s.getUserName();
		this.uploadingPassword = s.getPassword();
		this.port = s.getPort();
		this.storedReport = null;
		this.shutdown = s.isShutdown();
		this.out = null;
		socket = null;
		in = null;
		window = s.window;
	}

	public boolean isComplete(int program)
	{
		logger.debug("Verifying Given Program Path");
		String process;
		if (program == 0)
		{
			process = waifu;
		}
		else if (program == 1)
		{
			process = ffmpeg;
		}
		else if (program == 2)
		{
			process = ffprobe;
		}
		else if (program == 3)
		{
			process = rawTherapee;
		}
		else
		{
			process = "";
		}
		if (process == null)
		{
			return false;
		}
		try
		{
			Process x = Runtime.getRuntime().exec(process);
			StreamGobbler reader = new StreamGobbler(x.getInputStream());
			StreamGobbler eater = new StreamGobbler(x.getErrorStream());
			reader.start();
			eater.start();
			if (x.waitFor(100, TimeUnit.MILLISECONDS))
			{
				logger.info("{} recognized", process);
				return true;
			}
			logger.info("{} accepted", process);
			return true;
		}
		catch (InterruptedException | IOException e)
		{
			logger.error("{} rejected", process);
			return false;
		}
	}

	public void submit(Report r)
	{
		storedReport = r;
		if (reporting)
		{
			logger.trace("Sending Report with Status: {}", r.getStatus());
			try
			{
				sendReport(r);
			}
			catch (IllegalStateException e)
			{

			}
		}
	}

	public boolean changePath(int x)
	{
		logger.debug("Initializing File Chooser");
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(fileNames[x]);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
		chooser.setCurrentDirectory(new File(presetPaths[x]));
		chooser.setApproveButtonText("Select");
		chooser.setMultiSelectionEnabled(false);
		chooser.showOpenDialog(null);
		try
		{
			if (x == 0)
			{
				waifu = ("\"" + chooser.getSelectedFile().getAbsolutePath() + "\"");
			}
			else if (x == 1)
			{
				ffmpeg = ("\"" + chooser.getSelectedFile().getAbsolutePath() + "\"");
			}
			else if (x == 2)
			{
				ffprobe = ("\"" + chooser.getSelectedFile().getAbsolutePath() + "\"");
			}
			else if (x == 3)
			{
				rawTherapee = ("\"" + chooser.getSelectedFile().getAbsolutePath() + "\"");
			}
			else
			{
				logger.fatal("No Program Specified to Change");
				System.exit(1);
			}
		}
		catch (NullPointerException p)
		{
			if (filledIn)
			{
				logger.error("Bad File Path");
				return false;
			}
			else
			{
				logger.fatal("User did not Select any File, Quitting Program...");
				System.exit(0);
			}
		}
		if (isComplete(x))
		{
			return true;
		}
		return false;
	}

	public void openWebsite(int x)
	{
		try
		{
			java.awt.Desktop.getDesktop().browse(new URI(websitePaths[x]));
		}
		catch (IOException | URISyntaxException e)
		{
			logger.fatal("Failed Open Attempt: {}", File.separator, websitePaths[x]);
			logger.fatal("Exception Infomation", e);
			JOptionPane.showMessageDialog(null, "Unable to Open File\n" + websitePaths[x] + "\nCrashing Program", "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			System.exit(20);
		}
		if (filledIn)
		{

		}
		else
		{
			System.exit(0);
		}
	}

	/**
	 * HandShakes with Server to either setup Communication protocal, or exits gracefully
	 * 0 = Connection Established
	 * 1 = Server is already at connection limit
	 * 2 = Server already has a Client connected under that name
	 * 3 = IO Exception, unable to communicate
	 * 4 = Decryption Failed, invalid password
	 * 5 = Key generation failed <problem with code
	 * version number = Client is running an incompatible version, Server sent the expected version
	 */
	public String verifyReporting()// Use to self Verify, and edit Values if needed
	{
		Socket s = null;
		if (!(port >= 1024 && port <= 49151))
		{
			logger.warn("Port out of Range");
			port = 9626;
		}
		try
		{
			s = new Socket();
			s.connect(new InetSocketAddress(hostName, port), 5000);
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			if (null == key)
			{
				logger.error("Bad Key Generated");
				this.reporting = false;
				s.close();
				out = null;
				in = null;
				return "5";
			}
			sendReport(new Report(clientID, 1));
			//TODO Do this in a separate Callable Thread, since this needs to be able to time out if the Encryption fails
			String result = readLine();
			// Server has no more room for Clients
			if (result.equals(""))
			{
				logger.error("Server Is Full");
				this.reporting = false;
				out = null;
				key = null;
				in = null;
				return "1";
			}
			// Server Already has similar Client
			else if (result.equals("alreadyConnectedClient_terminatingThisClientsConnectionImmediately"))
			{
				logger.error("Client Already Connected");
				this.reporting = false;
				out = null;
				key = null;
				in = null;
				return "2";
			}
			else if (result.equals("clientIsRunningAIncompatibleVersionOfFoxTrotUpscaler"))
			{
				String version = readLine();
				logger.error("Server is Running Version {}, can not Connect", version);
				this.reporting = false;
				out = null;
				key = null;
				in = null;
				return version;
			}
			else
			{
				logger.info("Connection Established");
				reporting = true;
				serverName = result;
				socket = s;
				communicator = new ServerCommunicator();
				communicator.start();
				return "0";
			}
		}
		catch (IllegalStateException | IOException e)
		{
			disableReporting(false, false);
			logger.error("Connection to Server: ({}) On Socket: {} Failed\n\tIO Exception Details: {}", hostName, port, e.getMessage());
			return "3";
		}
		catch (InvalidKeyException e)
		{
			disableReporting(false, false);
			return "4";
		}

	}

	private static SecretKey generateKey(char[] passphrase)
	{
		try
		{
			//Prepares Secret Key Factory
			//PBKDF2WithHmacSHA1 / Password based Key Derivation Function 2 with HmacSHA1 Signature
			SecretKeyFactory kgen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			//Sets up a hashing method that takes a passphrase and a NONEMPTY SALT, which would do 65536 iterations of the algorithm, to generate a 256 bit key
			//NOTE: Most likely I will have to hard code in a salt, as it must be the same across Client and Server, and its not fair to ask the User for one since they already supplied a password
			KeySpec spec = new PBEKeySpec(passphrase, new byte[]
			{ (byte) 0xba, (byte) 0x8a, 0x0d, 0x45, 0x25, (byte) 0xad, (byte) 0xd0, 0x11, (byte) 0x98, (byte) 0xa8, 0x08, 0x00, 0x36, 0x1b, 0x11,
					0x03 }, 65536, 128);
			//finally generates a Key with PBKDF2
			SecretKey tmp = kgen.generateSecret(spec);
			//designates the Key to AES, for use in Encryption and Decryption
			return new SecretKeySpec(tmp.getEncoded(), "AES");
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new IllegalStateException(e.toString());
		}
	}

	private void sendReport(Report r) throws IllegalStateException
	{
		byte[] sealedArray = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos);)
		{
			//Convert Report to byte[]
			oos.writeObject(r);
			oos.flush();
			byte[] encoded = bos.toByteArray();
			//Set Up Initial Cipher
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			//Generates Random IV
			byte[] initVector = new byte[12];
			(new SecureRandom()).nextBytes(initVector);
			//Creates necessary MetaData from IV
			GCMParameterSpec spec = new GCMParameterSpec(16 * 8, initVector);
			//Initializes Cipher with Key and MetaData
			//ONLY GOOD for 1 encryption, Cipher must always be reinitalized with a new IV
			cipher.init(Cipher.ENCRYPT_MODE, key, spec);
			//Create array of size of what the cipher will create + the bytes needed for the Initialization Vector
			sealedArray = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
			//Puts the Initialization Vector in at the beginning
			for (int i = 0; i < initVector.length; i++)
			{
				sealedArray[i] = initVector[i];
			}
			// Perform encryption, but places bytes in ciphertext just after the included IV
			cipher.doFinal(encoded, 0, encoded.length, sealedArray, initVector.length);
			//Quick check if Streams have already been shut down
			if (out == null)
			{
				throw new NullPointerException("OutputStream closed already");
			}
			//write expected file length
			out.writeInt(sealedArray.length);
			//write actual array
			out.write(sealedArray, 0, sealedArray.length);
			out.flush();
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException e)
		{
			/* None of these exceptions should be possible if key is valid and GCM mode is available in the JRE */
			logger.fatal("Impossible Decryption Failure, check implementation");
			logger.catching(new IllegalStateException(e.toString()));
			System.exit(20);
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			logger.error("Invalid Key Used: ", e);
			disableReporting(false, true);
			throw new IllegalStateException(e.getMessage());
		}
		catch (IOException e)
		{
			if (e.getMessage().equals("socket closed"))
				logger.error("IO Exception: Socket Closed");
			else
			{
				logger.catching(e);
				disableReporting(false, true);
			}
		}
	}

	private String readLine() throws SocketException, IOException, InvalidKeyException
	{
		try
		{
			//Quick check if Streams have already been shut down
			if (in == null)
			{
				throw new NullPointerException("InputStream closed already");
			}
			//read the actual length
			int value = in.readInt();
			byte[] sealedArray = new byte[value];
			in.readFully(sealedArray);
			//Set up initial cipher
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			//Gets the IV of the sent sealed object
			byte[] initVector = Arrays.copyOfRange(sealedArray, 0, 12);
			//Converts the IV to the MetaData of the object
			GCMParameterSpec spec = new GCMParameterSpec(16 * 8, initVector);
			//completes the initialization of the decipher
			//Again, only good for one decryption, must reinit with the initVector supplied in the first couple bytes
			cipher.init(Cipher.DECRYPT_MODE, key, spec);
			//actually deciphers the Encrypted object, making sure not to reread the IV
			byte[] plaintext = cipher.doFinal(sealedArray, 12, sealedArray.length - 12);
			//Converts the array to a String
			return new String(plaintext);
		}
		catch (NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException e)
		{
			/* None of these exceptions should be possible if key is valid and GCM mode is available in the JRE */
			logger.fatal("Impossible Decryption Failure, check implementation");
			logger.catching(new IllegalStateException(e.toString()));
			System.exit(20);
			return null;
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			logger.error("Invalid Decryption Key");
			throw new InvalidKeyException(e.toString());
		}
		catch (NegativeArraySizeException e)
		{
			throw new IOException("End Of Stream Reached, read -1 for Stream Input");
		}
	}

	/**
	 * HandShakes with Server to either setup Communication protocal, or exits gracefully
	 * 0 = Connection Established
	 * 1 = Server is already at connection limit
	 * 2 = Server already has a Client connected under that name
	 * 3 = IO Exception, unable to communicate
	 * 4 = Decryption Failed, invalid password
	 * 5 = Key generation failed <problem with code
	 * version number = Client is running an incompatible version, Server sent the expected version
	 * @param id ClientID User chooses to call this Client
	 * @param host Hostname or IP address of Server
	 * @param p Port Number, default is 9626
	 * @param pass Passphrase to use as Encryption Key seed
	 * @return A string with either a code, or version number, use legend above
	 */
	public String verifyReporting(String id, String host, int p, char[] pass)
	{
		Socket s = null;
		if (!(p >= 1024 && p <= 49151))//GUI should prevent, but just incase
		{
			logger.warn("Port out of Range");
			port = 9626;
		}
		try
		{
			s = new Socket();
			s.connect(new InetSocketAddress(host, p), 5000);
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			key = generateKey(pass);
			if (null == key)
			{
				logger.error("Bad Key Generated");
				this.reporting = false;
				socket.close();
				out = null;
				in = null;
				return "5";
			}

			sendReport(new Report(id, 1));
			//TODO Do this in a separate Callable Thread, since this needs to be able to time out if the Encryption fails
			String result = readLine();
			if (result.equals(""))// Means Client Already Connected
			{
				logger.error("Server Is Full");
				this.reporting = false;
				key = null;
				out = null;
				in = null;
				return "1";
			}
			// Means Server Reached Client Limit
			else if (result.equals("alreadyConnectedClient_terminatingThisClientsConnectionImmediately"))
			{
				logger.error("Client Already Connected");
				this.reporting = false;
				out = null;
				key = null;
				in = null;
				return "2";
			}
			else if (result.equals("clientIsRunningAIncompatibleVersionOfFoxTrotUpscaler"))
			{
				String version = readLine();
				logger.error("Server is Running Version {}, can not Connect", version);
				this.reporting = false;
				out = null;
				key = null;
				in = null;
				return version;
			}
			else
			{
				logger.info("Connection Established");
				clientID = id;
				hostName = host;
				port = p;
				serverName = result;
				socket = s;
				reporting = true;
				communicator = new ServerCommunicator();
				communicator.start();
				return "0";
			}
		}
		catch (IOException ex)
		{
			logger.error("Connection to Server: ({}) On Socket: {} Failed\n\tIO Exception Details: {}", hostName, port, ex.getMessage());
			disableReporting(false, false);
			return "3";
		}
		catch (InvalidKeyException e)
		{
			disableReporting(false, false);
			return "4";
		}
	}

	public void disableReporting(boolean tellServer, boolean reportingOff)
	{
		boolean something = false;
		if (reportingOff)
		{
			reporting = false;
		}
		if (out != null)
		{
			if (tellServer)
			{
				sendReport(new Report(clientID, 100));
			}
			try
			{
				out.close();
				logger.debug("Stream Shut Down");
			}
			catch (IOException e)
			{
				logger.error("Stream Failed to Close", e);
			}
			something = true;
			out = null;
		}
		if (socket != null)
		{
			try
			{
				socket.close();
				logger.debug("Socket Shut Down");
			}
			catch (IOException e)
			{
				logger.error("Socket Failed to Close", e);
			}
			something = true;
			socket = null;
		}
		if (in != null)
		{
			try
			{
				in.close();
				logger.debug("Reader Shut Down");
			}
			catch (IOException e)
			{
				logger.error("Reader Failed to Close", e);
			}
			something = true;
			in = null;
		}
		key = null;
		if (!something)
		{
			logger.debug("Streams Closed Already");
		}
	}

	public boolean verifyUploading()
	{
		if (uploading)
		{
			try
			{
				URL locator = new URL(filePath);
				Sardine uploader = SardineFactory.begin(uploadingUserName, new String(uploadingPassword));
				uploader.enablePreemptiveAuthentication(locator);
				String networkPath = filePath + "/" + "botConnectionLog.txt";
				String localPath = dir + File.separator + "Temp" + File.separator + "botConnectionLog.txt";
				File localFile = new File(localPath);
				localFile.delete();
				if (uploader.exists(networkPath))
				{
					FileUtils.copyToFile(uploader.get(networkPath), localFile);
				}
				FileWriter write = new FileWriter(localFile, true);
				write.write("FTU AutoUploading Enabled: " + uploadingUserName + " " + DRI.dateFormat.format(LocalDateTime.now()) + "\n");
				write.close();
				if (uploader.exists(networkPath))
				{
					uploader.delete(networkPath);
				}
				FileInputStream token = new FileInputStream(localFile);
				uploader.put(networkPath, token);
				uploader.shutdown();
				logger.info("Connection to WebDAV Server Established");
				return true;
			}
			catch (MalformedURLException e)
			{
				disableUploading();
				logger.error("Bad URL: {}", filePath);
				return false;
			}
			catch (IOException e)
			{
				disableUploading();
				logger.error("Connection to {} Failed: {}", filePath, e.getMessage());
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean verifyUploading(String path, String name, char[] pass)
	{
		try
		{
			URL locator = new URL(path);
			Sardine uploader = SardineFactory.begin(name, new String(pass));
			uploader.enablePreemptiveAuthentication(locator);
			String networkPath = path + "/" + "botConnectionLog.txt";
			String localPath = dir + File.separator + "Temp" + File.separator + "botConnectionLog.txt";
			File localFile = new File(localPath);
			localFile.delete();
			if (uploader.exists(networkPath))
			{
				FileUtils.copyToFile(uploader.get(networkPath), localFile);
			}
			FileWriter write = new FileWriter(localFile, true);
			write.write("FTU AutoUploading Enabled: " + name + " " + DRI.dateFormat.format(LocalDateTime.now()) + "\n");
			write.close();
			if (uploader.exists(networkPath))
			{
				uploader.delete(networkPath);
			}
			FileInputStream token = new FileInputStream(localFile);
			uploader.put(networkPath, token);
			this.filePath = path;
			this.uploadingUserName = name;
			this.uploadingPassword = pass;
			uploader.shutdown();
			this.uploading = true;
			logger.info("Connection to WebDAV Server Established");
			return true;
		}
		catch (MalformedURLException e)
		{
			disableUploading();
			logger.error("Bad URL: {}", path);
			return false;
		}
		catch (IOException e)
		{
			disableUploading();
			logger.error("Connection to {} Failed: {}", path, e.getMessage());
			return false;
		}
	}

	public void disableUploading()
	{
		uploading = false;
		try
		{
			URL locator = new URL(filePath);
			Sardine uploader = SardineFactory.begin(uploadingUserName, new String(uploadingPassword));
			uploader.enablePreemptiveAuthentication(locator);
			String networkPath = filePath + "/" + "botConnectionLog.txt";
			String localPath = dir + File.separator + "Temp" + File.separator + "botConnectionLog.txt";
			File localFile = new File(localPath);
			localFile.delete();
			if (uploader.exists(networkPath))
			{
				FileUtils.copyToFile(uploader.get(networkPath), localFile);
			}
			FileWriter write = new FileWriter(localFile, true);
			write.write("FTU AutoUploading Disabled: " + uploadingUserName + " " + DRI.dateFormat.format(LocalDateTime.now()) + "\n");
			write.close();
			if (uploader.exists(networkPath))
			{
				uploader.delete(networkPath);
			}
			FileInputStream token = new FileInputStream(localFile);
			uploader.put(networkPath, token);
			uploader.shutdown();
			logger.info("Disconnected from WebDAV Server");
		}
		catch (IOException e)
		{
			logger.catching(e);
		}
		logger.info("Uploading Disabled");
	}

	public void upload(JFrame window, String path, String name, JTextArea area)
	{
		if (uploading)
		{

			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{

					/**
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
						FileTransferProgressDialog subWindow = new FileTransferProgressDialog(true, filePath, uploadingUserName, uploadingPassword,
								name, path, overwrite, window, area);
						subWindow.executeTransfer();
					}

				});
			}
			catch (InvocationTargetException | InterruptedException e)
			{
				e.printStackTrace();
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		/*
		 * builder.append("Settings [");
		 * builder.append("Waifu2x-caffe Program Path="); builder.append(waifu);
		 * builder.append(", FFMPEG Program Path="); builder.append(ffmpeg);
		 * builder.append(", FFprobe Program Path="); builder.append(ffprobe);
		 * builder.append(", RawTherapee Program Path=");
		 * builder.append(rawTherapee);
		 * builder.append(", RawTherapee Installed="); builder.append(isRaw);
		 * builder.append(", Intermediary Image Format=");
		 * builder.append(image); builder.append(", Overwrite Final Products=");
		 * builder.append(overwrite); builder.append("]");
		 */
		builder.append("Settings Parameters \n\t[");
		builder.append("Waifu2x-caffe Program Path = ");
		builder.append(waifu);
		builder.append("\n\t FFMPEG Program Path = ");
		builder.append(ffmpeg);
		builder.append("\n\t FFprobe Program Path = ");
		builder.append(ffprobe);
		builder.append("\n\t RawTherapee Program Path = ");
		builder.append(rawTherapee);
		builder.append("\n\t RawTherapee Installed = ");
		builder.append(isRaw);
		builder.append("\n\t Intermediary Image Format = ");
		builder.append(image);
		builder.append("\n\t Overwrite Final Products = ");
		builder.append(overwrite);
		builder.append("\n\t Report to Discord = ");
		builder.append(reporting);
		if (true/* reporting */)
		{
			builder.append("\n\t\t Client ID = ");
			builder.append(clientID);
			builder.append("\n\t\t Server HostName = ");
			builder.append(hostName);
			builder.append("\n\t\t Server Port = ");
			builder.append(port);
			builder.append("\n\t\t Server Name = ");
			builder.append(serverName);
		}
		builder.append("\n\t Upload to Server = ");
		builder.append(uploading);
		if (true/* uploading */)
		{
			builder.append("\n\t\t File Path to Server = ");
			builder.append(filePath);
			builder.append("\n\t\t Server Username = ");
			builder.append(uploadingUserName);
			builder.append("\n\t\t Password = ");
			int count = uploadingPassword.length;
			builder.append(count + "Chars long");
		}
		builder.append("]\n");
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj2)
	{
		Settings set2 = (Settings) obj2;
		int x = 0;
		if (waifu.equals(set2.getWaifu()))
		{
			x++;
		}
		if (ffmpeg.equals(set2.getFfmpeg()))
		{
			x++;
		}
		if (ffprobe.equals(set2.getFfprobe()))
		{
			x++;
		}
		if (rawTherapee.equals(set2.getRawTherapee()))
		{
			x++;
		}
		if (isRaw == set2.isRaw())
		{
			x++;
		}
		if (image == set2.isImage())
		{
			x++;
		}
		if (overwrite == set2.isOverwrite())
		{
			x++;
		}
		if (filledIn == set2.isFilledIn())
		{
			x++;
		}
		if (x == 8)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private class ServerCommunicator extends Thread
	{

		private boolean running = true;

		@Override
		public void run()
		{
			setName("ServerCommunicator");
			while (running)
			{
				try
				{
					logger.trace("ServerCommunicator ready");
					String line = readLine();//blocks indefinitely
					switch (line)
					{

						case "d":// Server Shutdown
							logger.info("Server is Shutting Down, Disabling Reporting");
							disableReporting(false, true);
							window.updatePreferencesGUI();
							running = false;
							break;
						case "addSingleProcessToQueue":
							logger.info("Queue File Download Declared");
							String urlString = readLine();
							logger.info("Downloading Queue Object at request of {}: {}", serverName, urlString);
							clearDialogs();
							if (window.changePanel(1))
							{
								window.getUpscalePanel().updateQueue();
								int count = new File(dir + File.separator + "Queue").listFiles().length;
								String queueName = dir + File.separator + "Queue" + File.separator + "Q" + String.format("%03d", count + 1) + ".ser";
								URLConnection openConnection = new URL(urlString).openConnection();
								try
								{
									openConnection.setRequestProperty("User-Agent",
											"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
									openConnection.connect();
									FileUtils.copyInputStreamToFile(openConnection.getInputStream(), new File(queueName));
								}
								catch (IOException x)
								{
									logger.catching(x);
								}
								window.getUpscalePanel().updateQueue();
							}
							else
							{
								//TODO Write back to reporter that Process Addition failed by Stuck on another window, busy either doing something, or in settings
								logger.error("Unable to change window, ignoring command.");
							}
							break;
						case "remoteExecuteTheQueueRightNow":
							clearDialogs();
							logger.info("Queue Remotely Executed");
							if (window.changePanel(1))
								window.getUpscalePanel().remoteExecuteQueue();
							else
								logger.error("Unable to change window, ignoring command.");
							break;
						case "pauseUpscale":
							logger.info("Remote Command to Pause Queue");
							clearDialogs();
							if (window.checkPanel(true, 1))
								window.getUpscalePanel().remotePauseUpscale();
							else
								logger.error("Not in the correct operating mode, ignoring command.");
							break;
						case "resumeUpscale":
							logger.info("Remote Command to Resume Queue");
							clearDialogs();
							if (window.checkPanel(true, 1))
								window.getUpscalePanel().remoteResumeUpscale();
							else
								logger.error("Not in the correct operating mode, ignoring command.");
							break;
						case "singleAbortUpscale":
							logger.info("Remote Command to Abort Single Upscale");
							clearDialogs();
							if (window.checkPanel(true, 1))
								window.getUpscalePanel().remoteSingleAbortUpscale();
							else
								logger.error("Not in the correct operating mode, ignoring command.");
							break;
						case "fullAbortUpscale":
							logger.info("Remote Command to Abort Full Queue");
							clearDialogs();
							if (window.checkPanel(true, 1))
								window.getUpscalePanel().remoteFullAbortUpscale();
							else
								logger.error("Not in the correct operating mode, ignoring command.");
							break;
						case "deleteQueue":
							logger.info("Remote Command to Delete Queue");
							clearDialogs();
							if (window.changePanel(1))
								window.getUpscalePanel().remoteDeleteQueue();
							else
								logger.error("Unable to change window, ignoring command.");
							break;
						case "download":
							logger.info("Remote Command for WebDAV Download");
							clearDialogs();
							if (window.checkPanel(false, 0))//Just Check if unlocked at all
							{
								String nP = readLine();
								String uN = readLine();
								char[] pW = readLine().toCharArray();
								String fN = readLine();
								String lP = dir + File.separator + readLine();
								logger.info(
										"Download Parameters:\n\tNetwork Path: {}\n\tUsername: {}\n\tPassword: {}\n\tFile Name: {}\n\tLocal Path: {}",
										nP, uN, new String(pW), fN, lP);
								FileTransferProgressDialog dialog = new FileTransferProgressDialog(false, nP, uN, pW, fN, lP, true, window, null);
								int result = dialog.executeTransfer();
								switch (result)
								{
									case 0://Completed
										sendReport(new Report(clientID, 9, "Download Completed"));
										break;
									case 1://Error in the Transfer
										String message = dialog.getError().getMessage();
										Report r = new Report(clientID, 9, "Error happened mid transfer");
										r.setCommand1(message);
										sendReport(r);
										break;
									case 2://Failed to Init the Download
										String message2 = dialog.getError().getMessage();
										Report r2 = new Report(clientID, 9, "Bad Download Preferences");
										r2.setCommand1(message2);
										sendReport(r2);
										break;
									case 3://Cancelled manually
										sendReport(new Report(clientID, 9, "Download Manually Cancelled"));
										break;
									case 4://Weird things
										sendReport(new Report(clientID, 9, "Programming Issue, by Interruption Exception in Client"));
										break;
								}
							}
						default:
							logger.error("Recieved Unknown Command: {}", line);
							break;
					}
				}
				catch (IllegalStateException e)//Bad Key, somehow
				{
					logger.error("Can Not Accept Commands Right now", e);
					//TODO Tell Server ^
				}
				catch (SocketException e)
				{
					if (e.getMessage().equalsIgnoreCase("Socket closed"))
					{
						//logger.warn("Socket Closed Already, Ignoring Command");
						//Always Happens on Shutdown if Reporting, so annoying to be logging every time
					}
					else
					{
						logger.error("Network Connection Interrupted - Disableing Reporting", e);
						disableReporting(false, true);
					}
					running = false;
				}
				catch (IOException e)
				{
					logger.error("IO Exception", e);
					disableReporting(false, true);
					running = false;
				}
				catch (InvalidKeyException e)
				{
					disableReporting(false, true);
					running = false;
				}
			}
		}

		private void clearDialogs()
		{
			for (Window w : JDialog.getWindows())
			{
				if (w instanceof JDialog && ((JDialog) w).isVisible())
				{
					if (!((JDialog) w).getTitle().equals("WebDAV File Transfer"))
					{
						logger.debug("Forced to close \"{}\" JDialog to execute Remote Commands", ((JDialog) w).getTitle());
						w.dispose();
					}
					else
					{
						throw new IllegalStateException("WebDAV File Transfer happening, can not interfer");
					}
				}
			}
			try
			{
				Thread.sleep(500);//MUST WAIT, as changePanel() will always return false if not enough time is given for the GUI to update if Process completed, but JDialog not clicked
			}
			catch (InterruptedException e)
			{
				logger.catching(e);
			}
		}
	}

	/**
	 * @return the storedReport
	 */
	public Report getStoredReport()
	{
		if (storedReport == null)
		{
			logger.throwing(new IllegalAccessException("No Stored report Exists Yet, Check the calling Method!"));
		}
		return storedReport;
	}

	/**
	 * @param storedReport
	 * the storedReport to set
	 */
	public void setStoredReport(Report storedReport)
	{
		this.storedReport = storedReport;
	}

	public DataOutputStream getOut()
	{
		return out;
	}

	public void setOut(DataOutputStream out)
	{
		this.out = out;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket()
	{
		return socket;
	}

	/**
	 * @param socket
	 * the socket to set
	 */
	public void setSocket(Socket socket)
	{
		this.socket = socket;
	}

	/**
	 * @return the in
	 */
	public DataInputStream getIn()
	{
		return in;
	}

	/**
	 * @param in
	 * the in to set
	 */
	public void setIn(DataInputStream in)
	{
		this.in = in;
	}

	public boolean isFilledIn()
	{
		return filledIn;
	}

	public void setFilledIn(boolean filledIn)
	{
		this.filledIn = filledIn;
	}

	public String getWaifu()
	{
		return waifu;
	}

	public void setWaifu(String waifu)
	{
		this.waifu = waifu;
	}

	public String getFfmpeg()
	{
		return ffmpeg;
	}

	public void setFfmpeg(String ffmpeg)
	{
		this.ffmpeg = ffmpeg;
	}

	public String getFfprobe()
	{
		return ffprobe;
	}

	public void setFfprobe(String ffprobe)
	{
		this.ffprobe = ffprobe;
	}

	public String getRawTherapee()
	{
		return rawTherapee;
	}

	public void setRawTherapee(String rawTherapee)
	{
		this.rawTherapee = rawTherapee;
	}

	public boolean isRaw()
	{
		return isRaw;
	}

	public void setRaw(boolean isRaw)
	{
		this.isRaw = isRaw;
	}

	public boolean isImage()
	{
		return image;
	}

	public void setImage(boolean image)
	{
		this.image = image;
	}

	public boolean isOverwrite()
	{
		return overwrite;
	}

	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}

	public boolean isReporting()
	{
		return reporting;
	}

	public String getClientID()
	{
		return clientID;
	}

	public String getHostName()
	{
		return hostName;
	}

	public int getPort()
	{
		return port;
	}

	public String getServerName()
	{
		return serverName;
	}

	public String getImage()
	{
		if (image)
		{
			return new String("tif");
		}
		else
		{
			return new String("png");
		}
	}

	/**
	 * @return the userName
	 */
	public String getUserName()
	{
		return uploadingUserName;
	}

	/**
	 * @return the password
	 */
	public char[] getPassword()
	{
		return uploadingPassword;
	}

	/**
	 * @return the fileNames
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * @return if uploading to WebDav Server
	 */
	public boolean isUploading()
	{
		return uploading;
	}

	/**
	 * @return The Current Directory
	 */
	public String getDir()
	{
		return dir;
	}

	/**
	 * Sets the Parent Directory
	 * 
	 * @param dir
	 * New Directory Path to be Set
	 */
	public void setDir(String dir)
	{
		this.dir = dir;
	}

	/**
	 * @return the shutdown
	 */
	public boolean isShutdown()
	{
		return shutdown;
	}

	/**
	 * @param shutdown the shutdown to set
	 */
	public void setShutdown(boolean shutdown)
	{
		this.shutdown = shutdown;
	}

	/**
	 * @param window the MainWindow to set
	 */
	public void setWindow(MainWindow window)
	{
		this.window = window;
	}
}
