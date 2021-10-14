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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FTUServerBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.FTUServerBot.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import structures.Database;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class ClientManager extends Thread
{

	private static Logger logger = LogManager.getLogger();
	private int maxCount;
	private int port;
	private final String name;
	private Database base;
	private ClientThread[] workers;
	private ServerSocket serverSocket;
	private SecretKey key;
	private IDiscordClient api;
	private IChannel adminChannel;
	private int connections;
	private boolean running = true;

	public ClientManager(IDiscordClient api, Database base, IChannel channel, String name, int port, int maxCount, char[] pass)
	{
		this.api = api;
		this.name = name;
		this.port = port;
		this.maxCount = maxCount;
		this.adminChannel = channel;
		this.base = base;
		workers = new ClientThread[maxCount];
		key = generateKey(pass);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ClientManager");
		try
		{
			updateGame();
			serverSocket = new ServerSocket(port);
			while (running)
			{
				Socket place = serverSocket.accept();
				ClientThread thread = new ClientThread(this, place, base);
				//Gets Client name, and sets up streams, decides if Client can speak ok
				if (thread.handshake())
				{
					if (connections < maxCount)//Decides if Server can handle connection
					{
						String id = thread.getClientId();
						if (base.checkIfClientExists(id))//Decides if New or Returning Client
						{
							if (base.getClientStatus(id) == 0)//Decides if Returning, if Client is not already connected
							{
								thread.confirm(true, false);
								for (int x = 0; x < maxCount; x++)
								{
									if (workers[x] == null)//Finds open Thread slot already confirmed to be avaliable somewhere
									{
										workers[x] = thread;
										base.reconnectClient(id, x);
										connections++;
										sendAnnouncement(thread.getClientId(), ":white_check_mark: - has Reconnected", "Returning Client");
										break;
									}
								}
								thread.start();
							}
							else
							{
								thread.confirm(false, false);//Client already Connected, IMPOSTER
							}
						}
						else//New Client, already confirmed to have Thread available
						{
							thread.confirm(true, true);
							for (int x = 0; x < maxCount; x++)
							{
								if (workers[x] == null)//Finds open Thread slot already confirmed to be available somewhere
								{
									workers[x] = thread;
									base.addNewClient(id, x);
									connections++;
									sendAnnouncement(thread.getClientId(), ":eight_spoked_asterisk: - has Connected", "New Client Registered");
									break;
								}
							}
							thread.start();
						}
					}
					else
					{
						thread.confirm(false, true);//No slot open at all
						sendAnnouncement(thread.getClientId(), ":eject:  - attempted connection", "Duplicate Client Name");
					}
					updateGame();
				}
			}
		}
		catch (SocketException i)//Called when Socket is Closed
		{
			if (!i.getMessage().equals("socket closed"))
				logger.catching(i);
			// TODO Server Closing
			// System.exit(0);
		}
		catch (IOException e)
		{
			logger.fatal("Could not listen on port {}", port);
			logger.fatal(e);
			System.exit(19);
		}
	}

	public static SecretKey generateKey(char[] passphrase)
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

	public ClientThread getClient(String clientID)
	{
		int index = base.getClientThreadIndex(clientID);
		if (index != -1)
			return workers[index];
		else
			throw logger.throwing(new IllegalArgumentException("Client Does Not exist"));
	}

	public void disconnectAll()
	{
		for (int x = 0; x < workers.length; x++)
		{
			if (workers[x] != null)
			{
				logger.debug("Removing Client from Index {}", x);
				workers[x].disconnect();
				workers[x] = null;
				connections--;
			}
		}
		updateGame();
	}

	public void sendAnnouncement(String title, String value, String footer)
	{
		EmbedBuilder message = new EmbedBuilder();
		message.withAuthorName(name);
		message.withTitle(title);
		message.appendDesc(value);
		message.withColor(0, 128, 255);
		message.withTimestamp(Instant.now());
		if (footer != null)
			message.withFooterText(footer);
		RequestBuffer.request(() -> {
			adminChannel.sendMessage(message.build());
		});
	}

	public void shutDown()// Called from ActionListener
	{
		try
		{
			serverSocket.close();
			disconnectAll();
		}
		catch (IOException e)
		{
			logger.fatal("Could not Close Server Socket", e);
			System.exit(29);
		}
	}

	/**
	 * Method to remove Clients that already Declared Disconnection, only call from ClientThread
	 * @param id The Client to remove by name
	 */
	public void removeClient(String id)
	{
		int threadIndex = base.disconnectClient(id);
		if (threadIndex > -1)
		{
			logger.debug("Removing Client from Index {}", threadIndex);
			workers[threadIndex] = null;
			connections--;
		}
		else
		{
			logger.warn("Client does not Exist!, Can not Disconnect");
		}
		updateGame();
	}

	/**
	 * Method to remove CONNECTED CLIENTS
	 * Accesses ClientThread to properly sever ties.
	 * @param id The Client name to remove
	 */
	public void disconnectClient(String id)
	{
		int threadIndex = base.getClientThreadIndex(id);
		if (threadIndex > -1)
		{
			logger.info("Removing {} from Index {}", id, threadIndex);
			workers[threadIndex].disconnect();
			workers[threadIndex] = null;
			connections--;
			updateGame();
		}
		else
		{
			logger.warn("Client does not Exist!, Can not Disconnect");
		}
	}

	public void updateGame()
	{
		if (connections == 0)
		{
			api.changePresence(StatusType.IDLE, ActivityType.PLAYING, "with no one");
		}
		else if (connections < 0)
		{
			api.changePresence(StatusType.DND, ActivityType.PLAYING, "Drunk (con < 0)");
		}
		else
		{
			api.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "with " + connections + "/" + maxCount + " Clients");
		}
	}

	public void sendMainBroadcast(String content)
	{
		RequestBuffer.request(() -> {
			adminChannel.sendMessage(content);
		}).get();
	}

	/**
	 * @return the maxCount
	 */
	public int getMaxCount()
	{
		return maxCount;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName()
	{
		return name;
	}

	public SecretKey getKey()
	{
		return key;
	}
}
