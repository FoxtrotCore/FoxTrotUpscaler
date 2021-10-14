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

package com.foxtrotfanatics.ftu_bot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu_bot.structures.Database;
import com.foxtrotfanatics.ftu_bot.structures.ProgramEvent;
import com.foxtrotfanatics.ftu_bot.structures.Report;

/**
 * @author Christian
 *
 */
public class ClientThread extends Thread
{

	private static Logger logger = LogManager.getLogger();
	private String clientId;
	private ClientManager manager;
	private Socket socket;
	private ArrayList<Integer> queue;
	private int eventCount = 0;
	private DataInputStream in;
	private DataOutputStream out;
	private int currentID;
	private Report first;
	private Database base;
	/**
	 * Locks access for external processes until the server actually has a response.
	 */
	private ReentrantLock serverResponse = new ReentrantLock();
	private ExecutorService reportReader = Executors.newSingleThreadExecutor();
	private ReadReport instructions = new ReadReport();
	/**
	 * Locks Access to This Threads Streams to Delay external action until it completes a reporting cycle
	 */
	private ReentrantLock functionLock = new ReentrantLock();
	private boolean running = true;

	public ClientThread(ClientManager manager, Socket socket, Database b)
	{
		super("New Connection");
		this.manager = manager;
		this.socket = socket;
		this.base = b;
	}

	public boolean handshake()
	{
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			first = readReport();
			clientId = first.getClientID();
			return true;
		}
		catch (InvalidClassException e)
		{
			logger.warn("Client Using Incompatible Version of FoxTrotUpscaler");
			try
			{
				sendCommand("clientIsRunningAIncompatibleVersionOfFoxTrotUpscaler");
				sendCommand(DRI.version);
			}
			catch (InvalidKeyException | IOException e2)
			{
				//If Class read, should not happen
			}
			shutDownStreams();
			return false;
		}
		catch (InvalidKeyException e)
		{
			try
			{
				sendCommand("SendingStringJustSoClientUnderstandsInvalidKeyException");
			}
			catch (InvalidKeyException | IOException e1)
			{
				//Already know that Key is bad
			}
			logger.error("Invalid Key, Disconnecting Client", e);
			shutDownStreams();
			return false;
		}
		catch (SocketException e)
		{
			logger.error("Socket Exception Occured", e);
			shutDownStreams();
			return false;
		}
		catch (IOException e)
		{
			logger.fatal("Network Bad: ", e);
			shutDownStreams();
			return false;
		}
	}

	public void run()
	{
		try
		{
			serverResponse.lock();
			while (running)
			{
				Report latest;
				try
				{
					Future<Report> summoner = reportReader.submit(instructions);
					try
					{
						latest = summoner.get();
					}
					catch (ExecutionException e)//Should be for actual exceptions
					{
						logger.catching(e);
						base.abortAllActiveReports(clientId);
						reportReader.shutdown();
						//Client Marked as Disconnected in database from Manager
						shutDownStreams();
						manager.removeClient(clientId);
						return;
					}
				}
				catch (RejectedExecutionException | InterruptedException e)//Marked for Disconnection
				{
					logger.debug("Thread now terminating");
					return;
				}
				try
				{
					if (functionLock.tryLock(5000, TimeUnit.SECONDS))//CONTENTS Can NOT block for more then 1 sec, or disconnect uncertain
					{
						int s = latest.getStatus();
						switch (s)
						{
							case 100://Disconnect
								logger.info("Client: {} - Reported Shutdown", clientId);
								base.abortAllActiveReports(clientId);
								reportReader.shutdown();
								//Client Marked as Disconnected in database from Manager
								shutDownStreams();
								manager.removeClient(clientId);
								manager.sendAnnouncement(clientId, ":no_entry: - has Disconnected", "Self Disconnected");
								return;
							case 1://Connection Made
								logger.error("Received Report with Status 1 when Handshake already completed, for Client {}", clientId);
								break;
							case 11://Multi Queue Execution Started
								boolean expecting = true;
								logger.info("Client Signaling Queue Execution, receiving all Queue Objects");
								int counter = 0;
								queue = new ArrayList<Integer>();
								while (expecting)
								{
									//Should be in rapid succession
									Report queuedReport = readReport();
									if (queuedReport.getStatus() == 57)
									{
										logger.info("Queue Process {} Recieved", ++counter);
										queue.add(base.addReport(queuedReport));
									}
									else if (queuedReport.getStatus() == 15)
									{
										logger.debug("Recieved All Queue Objects, resuming to normal scanning");
										expecting = false;
									}
									else
										throw logger
												.throwing(new IllegalArgumentException("Report Status Code Unexpected: " + queuedReport.getStatus()));
								}
								manager.sendAnnouncement(clientId, ":alarm_clock: - started Queue", counter + " Processes");
								base.setClientStatus(clientId, 2);
								break;
							case 9://Annoucement for UserActivity Discord Command Thread
								String result = latest.getTitle();
								if(latest.getCommand1() != null)
								{
									result += "\n" + latest.getCommand1();
								}
								final String result2 = result;
								manager.sendMainBroadcast(result2);
								break;
							case 10://Single Process Execution Started
								currentID = base.addReport(latest);
								base.setClientStatus(clientId, 2);
								eventCount = latest.getHistory().size();
								manager.sendAnnouncement(clientId, ":cd: - Starting Upscale", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 12://QuickScript Executed
								currentID = base.addReport(latest);
								base.setClientStatus(clientId, 2);
								eventCount = latest.getHistory().size();
								manager.sendAnnouncement(clientId, ":scroll: - Starting QuickScript", latest.getHistory().get(latest.getHistory().size()-1).getMessage());
								break;
							case 16://QuickScript Completed
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":ok: - QuickScript Completed", latest.getHistory().get(latest.getHistory().size()-1).getMessage());
								break;
							case 17://QuickScript Failed
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":radioactive: - QuickScript Failed", latest.getHistory().get(latest.getHistory().size()-1).getMessage());
								break;
							case 18://QuickScript Aborted
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":sos: - QuickScript Aborted", latest.getHistory().get(latest.getHistory().size()-1).getMessage());
								break;
							case 32://Process Resumed
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 2);
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":arrow_forward: - Process Resumed", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 50://Process Failed
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 1);
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":radioactive: - Process Failed", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 30://Process Paused
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 1);
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":pause_button: - Process Paused", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 20://Process Aborted
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 1);
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":sos: - Process Aborted", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 13://Process Completed
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 1);
								handleEvent(latest);
								manager.sendAnnouncement(clientId, ":dvd: - Process Completed", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 6://Stage Completed
								switch(latest.getCurrentStage())
								{
									case 1:
										manager.sendAnnouncement(clientId, ":closed_book: - Stage A Completed", "EP: " + latest.getEp() + " - " + latest.getShowName());
										break;
									case 2:
										manager.sendAnnouncement(clientId, ":orange_book: - Stage B Completed", "EP: " + latest.getEp() + " - " + latest.getShowName());
										break;
									case 3:
										manager.sendAnnouncement(clientId, ":green_book: - Stage C Completed", "EP: " + latest.getEp() + " - " + latest.getShowName());
										break;
									case 4:
										manager.sendAnnouncement(clientId, ":blue_book:- Stage D Completed", "EP: " + latest.getEp() + " - " + latest.getShowName());
										break;
									default:
										logger.error("Unexpected Stage Number Recieved: {}",latest.getCurrentStage());
								}
							case 5://Stage Started
							case 8://Waifu2x reinitalized
								base.setReport(currentID, latest);//Just in case
								handleEvent(latest);//Connected!, Do not move around
							case 0://Normal Progress Update
								base.setReport(currentID, latest);
								break;
							case 14://Single Process from Queue Starting again
								if (queue.isEmpty())
								{
									logger.error("Unexpected Process in Queue Started, End of Local Queue History");
									break;
								}
								currentID = queue.get(0);
								queue.remove(0);
								base.setReport(currentID, latest);
								base.setClientStatus(clientId, 2);
								eventCount = 0;
								handleEvent(latest);//Client does create Event 14, keep this
								manager.sendAnnouncement(clientId, ":cd: - Starting new Process", "EP: " + latest.getEp() + " - " + latest.getShowName());
								break;
							case 21://Full Abort - Signal only
								base.addEvent(false, currentID, LocalDateTime.now().format(UserActivity.dateFormat), 20, "Process Aborted");
								currentID = -1;
								queue = null;
								eventCount = 0;
								base.abortAllActiveReports(clientId);
								manager.sendAnnouncement(clientId, ":octagonal_sign: - Full Abort", "Client Now Idle");
								break;
							default:
								throw logger
										.throwing(new IllegalArgumentException("Unknown Status Found for Client: " + clientId + " - Status: " + s));
						}
						functionLock.unlock();
					}
					else
					{
						running = false;
					}
				}
				catch (InterruptedException e)
				{
					//Should not really happen
					logger.error("ClientThread Interrupted WHILE in decision block, should not happen!",e);
				}
			}
			serverResponse.unlock();
		}
		catch (SocketException e)
		{
			logger.error("Socket Exception Occured", e);
			//manager.disconnectClient(clientId);
		}
		catch (InvalidKeyException e)
		{
			logger.error("Encryption Failed", e);
			base.disconnectClient(clientId);
			shutDownStreams();
		}
		catch (IOException e)
		{
			logger.fatal("Client Disconnected: ", e);
			base.disconnectClient(clientId);
			shutDownStreams();
		}
	}

	public Report getMessage()
	{
		serverResponse.lock();
		serverResponse.unlock();
		return first;
	}

	public String remoteCommand(String command)
	{
		functionLock.lock();
		try
		{
			sendCommand(command);
		}
		catch (InvalidKeyException | IOException e)
		{
			logger.catching(e);
			return "Communication Failed, Disconnecting Client";
		}
		functionLock.unlock();
		return "";
	}

	private void sendCommand(String plainText) throws IOException, InvalidKeyException
	{
		try
		{
			//Convert String to byte[]
			byte[] encoded = plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			//Set Up Initial Cipher
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			//Generates Random IV
			byte[] initVector = new byte[12];
			(new SecureRandom()).nextBytes(initVector);
			//Creates necessary MetaData from IV
			GCMParameterSpec spec = new GCMParameterSpec(16 * 8, initVector);
			//Initializes Cipher with Key and MetaData
			//ONLY GOOD for 1 encryption, Cipher must always be reinitalized with a new IV
			cipher.init(Cipher.ENCRYPT_MODE, manager.getKey(), spec);
			//Create array of size of what the cipher will create + the bytes needed for the Initialization Vector
			byte[] sealedArray = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
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
		catch (IllegalBlockSizeException | BadPaddingException e2)
		{
			throw new InvalidKeyException(e2.toString());
		}
	}

	public Report readReport() throws IOException, InvalidKeyException
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
			cipher.init(Cipher.DECRYPT_MODE, manager.getKey(), spec);
			//actually deciphers the Encrypted object, making sure not to reread the IV
			byte[] byteObject = cipher.doFinal(sealedArray, 12, sealedArray.length - 12);
			//Convert this array to a Report
			Report result = null;
			try (ByteArrayInputStream bis = new ByteArrayInputStream(byteObject); ObjectInputStream ois = new ObjectInputStream(bis);)
			{
				result = ((Report) ois.readObject());
			}
			catch (ClassNotFoundException e2)
			{
				logger.catching(e2);
				System.exit(30);
				return null;
			}
			return result;
		}
		catch (SocketException e)
		{
			if(e.getMessage().equalsIgnoreCase("Socket closed"))
			{
				logger.warn("Socket Closed Already, Ignoring Command");
			}
			throw e;
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

	private void handleEvent(Report r)
	{
		while (r.getHistory().size() > eventCount)
		{
			ProgramEvent e = r.getHistory().get(eventCount);
			base.addEvent(false, currentID, e.getTime().format(UserActivity.dateFormat), e.getType(), e.getMessage());
			eventCount++;
		}
	}

	public void confirm(boolean accepted, boolean newClient)
	{
		if (accepted)
		{
			if (newClient)
			{
				logger.info("New Client Added: {}", clientId);
			}
			else
			{
				logger.info("Disconnected Client Added: {}", clientId);
			}
			super.setName(clientId);
			//Manager Already marks Clients connection in Database
			try
			{
				sendCommand(manager.getName());
			}
			catch (InvalidKeyException | IOException e)
			{
				//Should not happen as Key already accepted
				logger.catching(e);
			}
		}
		else
		{
			if (newClient)
			{
				logger.warn("Too Many Clients, Rejecting: {}", clientId);
				if (out != null)
				{
					try
					{
						sendCommand("");
						out.close();
					}
					catch (IOException | InvalidKeyException e)
					{
						logger.catching(e);
					}
					out = null;
				}
			}
			else
			{
				logger.warn("Client: {} - Already Connected, Rejecting", clientId);
				if (out != null)
				{
					try
					{
						sendCommand("alreadyConnectedClient_terminatingThisClientsConnectionImmediately");
						out.close();
					}
					catch (IOException | InvalidKeyException e)
					{
						logger.catching(e);
					}
					out = null;
				}
			}
			boolean something = false;
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
			if (!something)
			{
				logger.info("Streams Closed Already");
			}
		}
	}

	/**
	 * Waits For any remaining readObject()s to complete,
	 * If this returns true, then thread is not blocked waiting for a report
	 * So functionLock to either prevent waiting for insertion into Database
	 * Or Wait until the the insertion completes to continue
	 * reportReader.shutdown() will prevent another attempt to read, and drop to an Exception
	 * If this returns false, then the thread is blocked waiting for a report.
	 * Interrupt the waiting of the Client thread (not the subthread that reads reports)
	 * Which will be caught by a interrupt exception
	 * 
	 * From either two Exceptions, exit the run method, while this thread continues
	 * With the Fields still intact, disconnects the clients after reading exits.
	 * 
	 * After the reader is dealt with, Writer informs Client it has been disconnected.
	 * Tells Database that Client is disconnected.
	 * Releases Lock, even though pointless.
	 * Manager should then after the thread completes, remove this Thread from workers[] for garbade collection
	 */
	public void disconnect()
	{
		reportReader.shutdown();//Prevents any additional Readings
		try
		{
			if (!reportReader.awaitTermination(1000, TimeUnit.MILLISECONDS))
			{
				this.interrupt();
			}
		}
		catch (InterruptedException e2)
		{
			logger.catching(e2);
		}
		functionLock.lock();//After this, run method is guaranteed to not read anymore, and exiting if not already
		reportReader.shutdownNow();
		//Tells Client to Disconnect. 
		//Client should not announce Disconnection since Server already knows
		try
		{
			sendCommand("d");
		}
		catch (InvalidKeyException | IOException e)
		{
			//Attempt Made, any error should be irrelevant as the connection is severed
		}
		base.disconnectClient(clientId);
		shutDownStreams();
		manager.sendAnnouncement(clientId, ":eject:  - dropping Client", "Server removing Client");
		functionLock.unlock();
	}

	public void shutDownStreams()
	{
		boolean something = false;
		if (out != null)
		{
			try
			{
				out.close();
			}
			catch (IOException e)
			{
				logger.catching(e);
			}
			logger.debug("Writer Shut Down");
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
		if (!something)
		{
			logger.info("Streams Closed Already");
		}
	}

	/**
	 * @return the id
	 */
	public String getClientId()
	{
		return clientId;
	}

	/**
	 * @param id
	 * the id to set
	 */
	public void setClientId(String id)
	{
		this.clientId = id;
	}

	class ReadReport implements Callable<Report>
	{

		@Override
		public Report call() throws Exception
		{
			return readReport();
		}

	}
}
