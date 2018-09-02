/**
 * Copyright 2018 Christian Devile
 * 
 * This file is part of FTUServerBot.
 * 
 * FTUServerBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FTUServerBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FTUServerBot.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.FTUServerBot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.darichey.discord.CommandListener;
import structures.Database;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;

/**
 * @author Christian
 *
 */
public class DRI implements IListener<ReadyEvent>
{

	private static Logger logger;
	private static String dir;
	private static FileLock lock;
	public static String version = new String("3.2.5");
	private static String token = new String("fake");
	private static int port = 9626;
	private static String serverName = new String("JoeBlow's Server");
	private static int clientMax = 20;
	private static String adminChannelName = new String("bot-spam");
	private static String adminRoleName = new String("ADMIN");
	private static char[] passphrase;
	private IDiscordClient api;

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException
	{
		File temp = new File(DRI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		if (temp.getAbsolutePath().endsWith("jar"))
			dir = temp.getParent();
		else
			dir = temp.getAbsolutePath();
		System.setProperty("directory", dir);
		logger = LogManager.getLogger();
		logger.info("Directory name found: {}", dir);

		DRI.checkIfSingleInstance();

		try (FileReader file = new FileReader(dir + File.separator + "values.txt"); BufferedReader in = new BufferedReader(file);)
		{
			token = in.readLine().substring(6);
			port = Integer.parseInt(in.readLine().substring(5));
			serverName = in.readLine().substring(5);
			serverName = serverName.substring(0, Math.min(40, serverName.length()));
			ArrayList<String> commandWords = new ArrayList<String>();
			//Command words black listed only for first connection where server name is sent
			commandWords.add("alreadyConnectedClient_terminatingThisClientsConnectionImmediately");
			commandWords.add("clientIsRunningAIncompatibleVersionOfFoxTrotUpscaler");
			if (commandWords.contains(serverName))
			{
				serverName = new String("Nice Try, but Input Sanitized");
			}
			else if (serverName.equals(""))
			{
				serverName = new String("Blank Server name");
			}
			clientMax = Integer.parseInt(in.readLine().substring(19));
			adminChannelName = in.readLine().substring(17);
			adminRoleName = in.readLine().substring(14);
			passphrase = in.readLine().substring(21).toCharArray();
			logger.info("\n\tToken (masked Ending): {}\n\tPort: {}\n\tServer Name: {}\n\tMaximum Client Count: {}\n\tAdmin Channel Name: {}\n\tAdmin Role Name: {}\n\tEncryption Key: {}",
					token.substring(token.length() - 6, token.length() - 1), port, serverName, clientMax, adminChannelName, adminRoleName, new String(passphrase));
		}
		catch (StringIndexOutOfBoundsException| NullPointerException e)
		{
			logger.fatal("Could not read values.txt\n Please edit before rerunning Program\nOr Delete to generate a clean values.txt");
			System.exit(20);
		}
		catch (FileNotFoundException o)
		{
			try
			{
				SecureRandom random = new SecureRandom();
				PrintWriter printer = new PrintWriter(dir + File.separator + "values.txt");
				printer.println("Token=reallyLongWord");
				printer.println("Port=9626");
				printer.println("Name=JoeBlow's Server");
				printer.println("MaximumClientCount=20");
				printer.println("AdminChannelName=bot-spam");
				printer.println("AdminRoleName=ADMIN");
				byte[] b = new byte[20];
				random.nextBytes(b);
				String encryption = new String("EncryptionPassPhrase=<YouCouldPickADifferentPassword>");
				for(int x = 0; x < 20; x++)
				{
					encryption += b[x];
				}
				printer.println(encryption);
				printer.flush();
				printer.close();
				logger.warn("Created values.txt\nPlease Edit before Using Program");
			}
			catch (IOException e)
			{
				logger.fatal("Failed to Generate values.txt: ", e);
			}
			System.exit(20);
		}
		catch (IOException o)
		{
			logger.fatal("IO Exception While Reading Parameters", o);
			System.exit(20);
		}
		connectToDiscord();
	}

	@Override
	public void handle(ReadyEvent event)
	{
		Thread.currentThread().setName("DiscordConnectionThread");
		this.api = event.getClient();
		logger.info("Connected to Discord");
		Database base = new Database(dir);
		IChannel adminChannel = null;
		IRole adminRole = null;
		for (IChannel ch : api.getChannels())
		{
			if (ch.getName().equals(adminChannelName))
			{
				adminChannel = ch;
				logger.info("Found Admin Channel: {}",adminChannel.getLongID());
				break;
			}
		}
		for (IRole r : api.getRoles())
		{
			if(r.getName().equals(adminRoleName))
			{
				adminRole = r;
				logger.info("Found Admin Role: {}",adminRole.getLongID());
			}
		}
		if (adminChannel != null && adminRole != null)
		{
			ClientManager manager = new ClientManager(api, base, adminChannel, serverName, port, clientMax, passphrase);
			UserActivity actions = new UserActivity(base, manager, adminChannel, adminRole, serverName);
			api.getDispatcher().registerListener(new CommandListener(actions.getRegistry()));
			manager.start();
			ConsoleUI ui = new ConsoleUI(base, manager, api, adminChannel);
			ui.start();
		}
		else
		{
			if(adminChannel == null)
			logger.fatal("Admin Channel Not Found!");
			if(adminRole == null)
				logger.fatal("Admin Role Not Found!");
		}
	}

	private DRI(IDiscordClient api)
	{
		this.api = api;
		api.getDispatcher().registerListener(this);
	}

	public static DRI connectToDiscord()
	{
		DRI d = null;
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try
		{
			d = new DRI(clientBuilder.login()); // Creates and logs in the client instance
		}
		catch (DiscordException e)
		{ // This is thrown if there was a problem building the client
			logger.fatal("Failed To Connect: Probably a bad Discord Token");
			logger.catching(e);
			System.exit(21);
		}
		return d;
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
					logger.fatal("Instance Already Running!");
					System.exit(5);
				}

				if (lock == null)
				{
					// already locked
					logger.fatal("Instance Already Running!");
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
				logger.fatal("Instance Already Running!");
				System.exit(5);
			}
		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static URL getLocation(final Class<?> c)
	{
		if (c == null)
			return null; // could not load the class

		// try the easy way first
		try
		{
			final URL codeSourceLocation = c.getProtectionDomain().getCodeSource().getLocation();
			if (codeSourceLocation != null)
				return codeSourceLocation;
		}
		catch (final SecurityException e)
		{
			// NB: Cannot access protection domain.
		}
		catch (final NullPointerException e)
		{
			// NB: Protection domain or code source is null.
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL
		// string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null)
			return null; // cannot find class resource

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix))
			return null; // weird URL

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:"))
			path = path.substring(4, path.length() - 2);

		try
		{
			return new URL(path);
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except
	 * that it also handles "jar:file:" URLs, returning the path to the JAR
	 * file.
	 * </p>
	 * 
	 * @param url
	 * The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException
	 * if the URL does not correspond to a file.
	 */
	public static File urlToFile(final URL url)
	{
		return url == null ? null : urlToFile(url.toString());
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * 
	 * @param url
	 * The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException
	 * if the URL does not correspond to a file.
	 */
	public static File urlToFile(final String url)
	{
		String path = url;
		if (path.startsWith("jar:"))
		{
			// remove "jar:" prefix and "!/" suffix
			final int index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try
		{
			String os = System.getProperty("os.name");
			if (os.equals("Window 10") && path.matches("file:[A-Za-z]:.*"))
			{
				path = "file:/" + path.substring(5);
			}
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException e)
		{
			// NB: URL is not completely well-formed.
		}
		catch (final URISyntaxException e)
		{
			// NB: URL is not completely well-formed.
		}
		if (path.startsWith("file:"))
		{
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}
}
