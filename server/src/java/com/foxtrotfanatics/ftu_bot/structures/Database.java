package com.foxtrotfanatics.ftu_bot.structures;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;
import com.foxtrotfanatics.ftu_bot.UserActivity;

/**
 * @author Christian
 * Class that interfaces with the database, and the rest of the Classes
 * No other class should import the java.sql libraries except this one
 * Also, all methods should open and close Connection objects as quickly as possible, to enforce concurrency
 * 
 */
public class Database
{
	private static String directory;
	private static Logger logger = LogManager.getLogger();
	private String connectionPath;
	private Connection c;
	private ReentrantLock lock = new ReentrantLock();
	private PreparedStatement psCheckIfClientExists;
	private PreparedStatement psGetClientStatus;
	private PreparedStatement psAddNewClient1;
	private PreparedStatement psSelectThread;
	private PreparedStatement gUpdateClients;
	private PreparedStatement psAbortAllReports1;
	private PreparedStatement psAbortAllReports2;
	private PreparedStatement psAbortAllReports3;
	private PreparedStatement gInsertNewEvent;
	private PreparedStatement gUpdateClientStatus;
	private PreparedStatement psStatus0Update;
	private PreparedStatement gFullProcessUpdate;
	private PreparedStatement psFindConfigID;
	private PreparedStatement gInsertConfig;
	private PreparedStatement gInsertProcess;
	private PreparedStatement psInsertRemux;
	private PreparedStatement gInsertTiming;
	private PreparedStatement psQueryClientTable;
	private PreparedStatement psQueryConnectionTable;
	private PreparedStatement gQueryConnectionTable;
	private PreparedStatement psQueryLastEvent;
	private PreparedStatement psQueryProcess;
	private PreparedStatement gSelectThreadIndex;
	private PreparedStatement psCreateCredentials;
	private PreparedStatement psRetrieveCredentials;
	private PreparedStatement psDeleteCredentials;

	public static void main(String[] args) throws SQLException
	{
		File temp;
		try
		{
			temp = new File(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if (temp.getAbsolutePath().endsWith("jar"))
				directory = temp.getParent();
			else
				directory = temp.getAbsolutePath();
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setProperty("directory", directory);
		logger = LogManager.getLogger();
		logger.info("Directory name found: {}", directory);
		String path = "jdbc:sqlite:" + directory + File.separator + "ftuData.db";
		Connection conn = DriverManager.getConnection(path);
		DatabaseMetaData dbMetaData = conn.getMetaData();
		System.out.println("Support RESULT_SET_TYPE: TYPE_FORWARD_ONLY? " + dbMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
		System.out
				.println("Support RESULT_SET_TYPE: TYPE_SCROLL_INSENSITIVE? " + dbMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
		System.out.println("Support RESULT_SET_TYPE: TYPE_SCROLL_SENSITIVE? " + dbMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_FORWARD_ONLY: CONCUR_READ_ONLY? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_FORWARD_ONLY: CONCUR_UPDATABLE? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_SCROLL_INSENSITIVE: CONCUR_READ_ONLY? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_SCROLL_INSENSITIVE: CONCUR_UPDATABLE? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_SCROLL_SENSITIVE: CONCUR_READ_ONLY? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY));
		System.out.println("Support RESULT_SET_CONCURRENCY: TYPE_SCROLL_SENSITIVE: CONCUR_UPDATABLE? "
				+ dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE));
		System.out.println("ResultSet.HOLD_CURSORS_OVER_COMMIT = " + ResultSet.HOLD_CURSORS_OVER_COMMIT);

		System.out.println("ResultSet.CLOSE_CURSORS_AT_COMMIT = " + ResultSet.CLOSE_CURSORS_AT_COMMIT);

		System.out.println("Default cursor holdability: " + dbMetaData.getResultSetHoldability());

		System.out.println("Supports HOLD_CURSORS_OVER_COMMIT? " + dbMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));

		System.out.println("Supports CLOSE_CURSORS_AT_COMMIT? " + dbMetaData.supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT));
		conn.close();
		@SuppressWarnings("unused")
		Database db = new Database(directory);
	}

	/**
	 * Sees if a ClientID name exists already
	 * @param name The ClientID to verify if exists
	 * @return if the ClientID was already entered
	 */
	public boolean checkIfClientExists(String name)
	{
		boolean result = true;
		lock.lock();
		try
		{
			psCheckIfClientExists.setString(1, name);
			ResultSet rs = psCheckIfClientExists.executeQuery();
			if (rs.next())
			{
				result = (rs.getInt(1) == 1) ? true : false;
			}
			else
			{
				logger.error("Client Existence Query Statement somehow failed to return a value");
			}
			rs.close();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * Return a Clients current operating Status
	 * @param name The ClientID to verify if exists
	 * @return if the Client is running = 2, 1 = idle, 0 = offline
	 */
	public int getClientStatus(String name)
	{
		int result = -1;
		lock.lock();
		try
		{
			psGetClientStatus.setString(1, name);
			ResultSet rs = psGetClientStatus.executeQuery();
			if (rs.next())
			{
				result = rs.getInt(1);
			}
			else
			{
				logger.error("Client Status Query Statement somehow failed to return a value");
			}
			rs.close();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * Return a Clients ThreadIndex
	 * @param name The ClientID to verify if exists
	 * @return the ThreadIndex
	 */
	public int getClientThreadIndex(String name)
	{
		int result = -1;
		lock.lock();
		try
		{
			gSelectThreadIndex.setString(1, name);
			ResultSet rs = gSelectThreadIndex.executeQuery();
			if (rs.next())
			{
				result = rs.getInt(1);
				if (result == 0)
					logger.warn("Thread may be null, since result = 0");
			}
			else
			{
				logger.error("Client ThreadIndex Query Statement somehow failed to return a value");
			}
			rs.close();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * Adds a Client to the roster, does not check if too many clients are already connected
	 * SQL INJECTION ATTACK VULNERABLE
	 * @param name The ClientID
	 * @return if the SQL executed successfully
	 */
	public boolean addNewClient(String name, int thread)
	{
		boolean result = false;
		lock.lock();
		try
		{
			psAddNewClient1.setString(1, name);
			psAddNewClient1.setInt(2, thread);
			psAddNewClient1.executeUpdate();
			/**
			 * gInsertConnection.setString(1, LocalDateTime.now().format(UserActivity.dateFormat));
			 * gInsertConnection.setString(2, name);
			 * gInsertConnection.setInt(3, 1);
			 * gInsertConnection.executeUpdate();
			 */
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);

		}
		lock.unlock();
		return result;
	}

	/**
	 * Sets a Client as connected, assigns Thread, and Adds Connection record of connection
	 * SQL INJECTION ATTACK VULNERABLE
	 * @param name The ClientID
	 * @return if the SQL executed successfully
	 */
	public boolean reconnectClient(String name, int thread)
	{
		boolean result = false;
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		try
		{
			gUpdateClients.setInt(1, 1);
			gUpdateClients.setInt(2, thread);
			gUpdateClients.setString(3, name);
			gUpdateClients.executeUpdate();
			// 1 = Connected
			/**
			 * gInsertConnection.setString(1, LocalDateTime.now().format(UserActivity.dateFormat));
			 * gInsertConnection.setString(2, name);
			 * gInsertConnection.setBoolean(3, true);
			 * gInsertConnection.executeUpdate();
			 */
			c.commit();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return result;
	}

	/**
	 * Marks a Client as disconnected in the Database, and returns the thread index of the associated client, IF AVALIABLE
	 * If client does not exist, -1 is returned
	 * SQL INJECTION ATTACK VULNERABLE
	 * @param name ClientID to disconnect
	 * @return the id of the thread that was previously connected. Or -1 if already disconnected
	 */
	public int disconnectClient(String name)
	{
		int result = -1;
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		try
		{
			psSelectThread.setString(1, name);
			ResultSet rs = psSelectThread.executeQuery();
			if (rs.next())
			{
				result = rs.getInt(1);
			}
			else
			{
				logger.warn("Client does not exist");
			}
			rs.close();
			gUpdateClients.setInt(1, 0);
			gUpdateClients.setNull(2, Types.INTEGER);
			gUpdateClients.setString(3, name);
			gUpdateClients.executeUpdate();
			/**
			 * gInsertConnection.setString(1, LocalDateTime.now().format(UserActivity.dateFormat));
			 * gInsertConnection.setString(2, name);
			 * gInsertConnection.setBoolean(3, false);
			 * gInsertConnection.executeUpdate();
			 */
			c.commit();
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return result;
	}

	/**
	 * Deactivates all ongoing and Upcoming Processes by adding Aborted Event, Also changes Client status to Idle,
	 * and specifies firstReportTime if null
	 * @param name ID of Client to modify
	 * @return if the SQL Transaction was successful
	 */
	public boolean abortAllActiveReports(String name)
	{
		boolean completion = false;
		ArrayList<Integer> results = new ArrayList<Integer>();
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		try
		{
			psAbortAllReports1.setString(1, name);
			ResultSet rs = psAbortAllReports1.executeQuery();
			while (rs.next())
			{
				results.add(rs.getInt(1));
			}
			for (int x = 0; x < results.size(); x++)
			{
				gInsertNewEvent.setInt(1, results.get(x));
				gInsertNewEvent.setString(2, LocalDateTime.now().format(UserActivity.dateFormat));
				gInsertNewEvent.setInt(3, 21);
				gInsertNewEvent.setString(4, "Program Quit, All Processes Aborted");
				gInsertNewEvent.executeUpdate();
				psAbortAllReports2.setInt(1, results.get(x));
				psAbortAllReports2.executeUpdate();
			}
			rs.close();
			psAbortAllReports3.setString(1, name);
			results = new ArrayList<Integer>();
			rs = psAbortAllReports3.executeQuery();
			while (rs.next())
			{
				results.add(rs.getInt(1));
			}
			for (int x = 0; x < results.size(); x++)
			{
				psQueryLastEvent.setInt(1, results.get(x));
				ResultSet rs2 = psQueryLastEvent.executeQuery();//IN loop, but loop should only ever execute once
				if (rs2.next())
				{
					addEvent(true, results.get(x), LocalDateTime.now().format(UserActivity.dateFormat), 18, rs2.getString(3) + " Cancelled");
				}
				else
				{
					logger.error("Process with QuickScript In Progress Status had no Event");
				}
				rs2.close();
			}
			rs.close();
			gUpdateClientStatus.setInt(1, 1);
			gUpdateClientStatus.setString(2, name);
			gUpdateClientStatus.executeUpdate();
			c.commit();
			completion = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return completion;
	}

	/**
	 * Adds a new Event to a Process
	 * @param id The ProcessID to add the Event to
	 * @return if the SQL executed successfully
	 */
	public boolean addEvent(boolean preLocked, int id, String time, int action, String message)
	{
		boolean result = false;
		if (!preLocked)
		{
			lock.lock();
		}
		try
		{
			gInsertNewEvent.setInt(1, id);
			gInsertNewEvent.setString(2, time);
			gInsertNewEvent.setInt(3, action);
			gInsertNewEvent.setString(4, message);
			gInsertNewEvent.executeUpdate();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		if (!preLocked)
		{
			lock.unlock();
		}
		return result;
	}

	/**
	 * Updates a Process in the process Table of the Database
	 * Does not update History
	 * @param id The Process to update
	 * @param r The Report with the updated information
	 * @return if the SQL executed successfully
	 */
	public boolean setReport(int id, Report r)
	{
		boolean result = false;
		lock.lock();
		try
		{
			switch (r.getStatus())
			{
				case 0:
					psStatus0Update.setInt(1, r.getCurrentFrame());
					psStatus0Update.setString(2, r.getEta());
					psStatus0Update.setInt(3, id);
					psStatus0Update.executeUpdate();
					result = true;
					break;
				case 14:
				case 13:
				case 50:
				case 30:
				case 32:
				case 20:
				case 5:
				case 6:
				case 8:
					gFullProcessUpdate.setInt(1, r.getStatus());
					gFullProcessUpdate.setBoolean(2, r.isOperating());
					gFullProcessUpdate.setBoolean(3, r.isJobStarted());
					gFullProcessUpdate.setBoolean(4, r.isJobCompleted());
					gFullProcessUpdate.setBoolean(5, r.isJobFailed());
					gFullProcessUpdate.setBoolean(6, r.isJobAborted());
					gFullProcessUpdate.setInt(7, r.getCurrentFrame());
					gFullProcessUpdate.setBoolean(8, r.isPaused());
					gFullProcessUpdate.setInt(9, r.getCurrentStage());
					gFullProcessUpdate.setString(10, r.getEta());
					gFullProcessUpdate.setInt(11, id);
					gFullProcessUpdate.executeUpdate();
					result = true;
					break;
				case 16:
				case 17:
				case 18:
				default:
					logger.warn(
							"Received Report to Replace, which Status suggests it should NOT be Replaced, Ignoring Command. \nStatus: {}\nName: {}\nProcessID: {}",
							r.getStatus(), r.getClientID(), id);
					break;
			}
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * First, if Reader Exists, return its ConfigID
	 * Otherwise, Insert Reader, and use special method to return its ConfigID
	 * @param r
	 * @return
	 */
	private int addConfig(Reader r, LocalDateTime t)
	{
		int result = -1;
		//CAN NOT LOCK, CALLED FROM ONLY AddProcess() which locks already
		try
		{
			psFindConfigID.setString(1, r.getM());
			psFindConfigID.setString(2, r.getH());
			psFindConfigID.setString(3, r.getN());
			psFindConfigID.setString(4, r.getP());
			psFindConfigID.setString(5, r.getC());
			psFindConfigID.setString(6, r.getD());
			psFindConfigID.setString(7, r.getB());
			psFindConfigID.setString(8, r.getGpu());
			psFindConfigID.setString(9, r.getTta());
			psFindConfigID.setString(10, r.getY());
			psFindConfigID.setString(11, r.getR1());
			psFindConfigID.setString(12, r.getR2());
			psFindConfigID.setString(13, r.getDeinterlacing());
			psFindConfigID.setString(14, r.getAInputString());
			psFindConfigID.setString(15, r.getAExportString());
			psFindConfigID.setString(16, r.getDInputString());
			psFindConfigID.setString(17, r.getDExportString());
			ResultSet rs = psFindConfigID.executeQuery();
			if (rs.next())
			{
				result = rs.getInt(1);
			}
			else
			{
				gInsertConfig.setString(1, r.getM());
				gInsertConfig.setString(2, r.getH());
				gInsertConfig.setString(3, r.getN());
				gInsertConfig.setString(4, r.getP());
				gInsertConfig.setString(5, r.getC());
				gInsertConfig.setString(6, r.getD());
				gInsertConfig.setString(7, r.getB());
				gInsertConfig.setString(8, r.getGpu());
				gInsertConfig.setString(9, r.getTta());
				gInsertConfig.setString(10, r.getY());
				gInsertConfig.setString(11, r.getR1());
				gInsertConfig.setString(12, r.getR2());
				gInsertConfig.setString(13, r.getDeinterlacing());
				gInsertConfig.setString(14, r.getAInputString());
				gInsertConfig.setString(15, r.getAExportString());
				gInsertConfig.setString(16, r.getDInputString());
				gInsertConfig.setString(17, r.getDExportString());
				gInsertConfig.setString(18, LocalDateTime.now().format(UserActivity.dateFormat));
				gInsertConfig.executeUpdate();
				rs = gInsertConfig.getGeneratedKeys();
				if (rs.next())
				{
					result = rs.getInt(1);
				}
				else
				{
					throw new SQLException("Could not get Integer Primary Key of Config");
				}
			}
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		//Can not Unlock
		return result;
	}

	/**
	 * Adds a new Process to the Database, while first inserting a new Config if needed, getting its id
	 * and creating Timing and Event rows in their respective tables.
	 * @param r The Report with the updated information
	 * @return the ProcessID created
	 */
	public int addReport(Report r)
	{
		int result = -1;
		int configID;
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
			return -1;
		}
		try
		{
			switch (r.getStatus())
			{
				case 10:
				case 57:
					configID = addConfig(r.getReader(), r.getFirstReportTime());
					gInsertProcess.setString(1, r.getClientID());
					gInsertProcess.setInt(2, configID);
					gInsertProcess.setInt(3, r.getStatus());
					gInsertProcess.setBoolean(4, r.isOperating());
					gInsertProcess.setBoolean(5, r.isJobStarted());
					gInsertProcess.setBoolean(6, r.isJobCompleted());
					gInsertProcess.setBoolean(7, r.isJobFailed());
					gInsertProcess.setBoolean(8, r.isJobAborted());
					gInsertProcess.setInt(9, r.getCurrentFrame());
					gInsertProcess.setBoolean(10, r.isPaused());
					gInsertProcess.setInt(11, r.getCurrentStage());
					gInsertProcess.setString(12, r.getEta());
					gInsertProcess.setBoolean(13, r.isQueue());
					gInsertProcess.setString(14, r.getEp());
					gInsertProcess.setString(15, r.getTitle());
					gInsertProcess.setString(16, r.getDesc());
					gInsertProcess.setInt(17, r.getStartingStage());
					gInsertProcess.setBoolean(18, r.isUsingRaw());
					gInsertProcess.setInt(19, r.getTotalFrames());
					gInsertProcess.setString(20, r.getDuration());
					gInsertProcess.setString(21, r.getStartTime());
					gInsertProcess.setString(22, r.getExportExtension());
					gInsertProcess.setString(23, r.getCommand1());
					gInsertProcess.setString(24, r.getCommand2());
					gInsertProcess.setString(25, r.getCommand3());
					gInsertProcess.setString(26, r.getCommand4());
					gInsertProcess.setString(27, r.getCommandm4());
					gInsertProcess.setInt(28, r.getOperatingMode());
					gInsertProcess.setBoolean(29, r.isRecovered());
					if (r.getStatus() == 57)
						gInsertProcess.setNull(30, Types.VARCHAR);
					else
						gInsertProcess.setString(30, r.getFirstReportTime().format(UserActivity.dateFormat));
					gInsertProcess.executeUpdate();
					ResultSet rs = gInsertProcess.getGeneratedKeys();
					if (rs.next())
					{
						result = rs.getInt(1);
						if (r.getL1().gR())
							addTiming(result, r.getL1());
						if (r.getL2().gR())
							addTiming(result, r.getL2());
						if (r.getL3().gR())
							addTiming(result, r.getL3());
						if (r.getL4().gR())
							addTiming(result, r.getL4());
						if (r.getL5().gR())
							addTiming(result, r.getL5());
						if (r.getS1().gR())
							addTiming(result, r.getS1());
						if (r.getS2().gR())
							addTiming(result, r.getS2());
						if (r.getS3().gR())
							addTiming(result, r.getS3());
						if (r.getS4().gR())
							addTiming(result, r.getS4());
						if (r.getS5().gR())
							addTiming(result, r.getS5());
						for (int x = 0; x < r.getHistory().size(); x++)//Most likely not used
						{
							ProgramEvent e = r.getHistory().get(x);
							addEvent(true, result, e.getTime().format(UserActivity.dateFormat), e.getType(), e.getMessage());
						}
					}
					else
					{
						throw new SQLException("Unable to get ProcessID of recent Insertion");
					}
					break;
				case 12:
					psInsertRemux.setString(1, r.getClientID());
					psInsertRemux.setInt(2, r.getStatus());
					psInsertRemux.setBoolean(3, r.isOperating());
					psInsertRemux.setBoolean(4, r.isJobStarted());
					psInsertRemux.setBoolean(5, r.isJobCompleted());
					psInsertRemux.setBoolean(6, r.isJobFailed());
					psInsertRemux.setBoolean(7, r.isJobAborted());
					psInsertRemux.setNull(8, Types.INTEGER);
					psInsertRemux.executeUpdate();
					//firstReportTime added through the stored Events with the SQL Trigger
					ResultSet rs2 = psInsertRemux.getGeneratedKeys();
					if (rs2.next())
					{
						result = rs2.getInt(1);
						for (int x = 0; x < r.getHistory().size(); x++)//Most likely not used
						{
							ProgramEvent e = r.getHistory().get(x);
							addEvent(true, result, e.getTime().format(UserActivity.dateFormat), e.getType(), e.getMessage());
						}
					}
					else
					{
						throw new SQLException("Unable to get ProcessID of recent Insertion");
					}
					break;
				default:
					logger.warn("Received Report to Add, which Status suggests it should NOT be Added, Ignoring Command. \nStatus: {}\nName: {}",
							r.getStatus(), r.getClientID());
			}
			c.commit();
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return result;
	}

	/**
	 * For one timing, insert data into row, with specific ProcessID
	 * Can only be called from a method that locks the threads
	 * @param id Process ID to add for Child Key
	 * @param t The Timing to insert
	 * @return if the SQL executed successfully
	 */
	private boolean addTiming(int id, Timing t)
	{
		boolean result = false;
		//CAN NOT LOCK, CALLED FROM ONLY AddProcess() which locks already
		try
		{
			gInsertTiming.setInt(1, id);
			gInsertTiming.setString(2, t.gF());
			gInsertTiming.setString(3, t.gM());
			gInsertTiming.setString(4, t.gL());
			gInsertTiming.setString(5, t.gH());
			gInsertTiming.setString(6, t.gO());
			gInsertTiming.setString(7, t.gT());
			gInsertTiming.executeUpdate();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		//Can not Unlock
		return result;
	}

	public void releaseLock()
	{
		lock.unlock();
	}

	/**
	 * Executes Query, and returns result set, while locking access
	 */
	public ResultSet queryClientList()
	{
		lock.lock();
		try
		{
			return psQueryClientTable.executeQuery();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		return null;
		//Can not Unlock
	}

	/**
	 * Executes Query on Connections table, and returns result set if Successful
	 * Locks Database down, must close the result set, and call the unlock method
	 * Passing in Null, will return all connections
	 * @param id The only Client ID cared about
	 */
	public ResultSet queryConnectionsTable(String id)
	{
		lock.lock();
		try
		{
			if (id == null)
			{
				return psQueryConnectionTable.executeQuery();
			}
			else
			{
				gQueryConnectionTable.setString(1, id);
				return gQueryConnectionTable.executeQuery();
			}
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		return null;
		//Can not Unlock
	}

	/**
	 * Executes Query on Processes table, and returns result set of whitelisted Clients and or Statuses
	 * Locks Database down, must close the result set, and call the unlock method
	 * ONLY returns following values: firstReportTime, ClientID, Status, currentFrame, totalFrames, ep, currentStage, eta,
	 * title
	 * VULNERABLE TO SQL INJECTION: Screen ID for existence in Clients first, and it should be fine. 
	 * Passing in Null for id, will return all connections
	 * An empty numbers array will return all status numbers
	 * @param id The only Client ID cared about
	 * @param numbers List of Statuses to Include in command
	 */
	public ResultSet queryProcessesTable(String id, ArrayList<Integer> numbers)
	{
		lock.lock();
		try
		{
			Statement stmt = c.createStatement();
			String preface = "SELECT firstReportTime, ClientID, Status, currentFrame, totalFrames, ep, currentStage, eta, title, ProcessID FROM Processes WHERE 1 = 1";
			if (id != null)
			{
				preface += " AND ClientID = '" + id + "'";//SQL Injection Vulnerable
			}
			if(!numbers.isEmpty())
			{
				preface += " AND Status IN (";
				for(int x = 0; x < numbers.size(); x++)
				{
					preface += numbers.get(x) + ",";
				}
				preface = preface.substring(0,preface.length()-1);
				preface += ")";
			}
			preface += ";";
			logger.debug("Running SQL Command: {}",preface);
			return stmt.executeQuery(preface);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		return null;
		//Can not Unlock
	}

	/**
	 * Executes Query on Processes table, and returns result set if Successful
	 * Does not lock the Database down, must close the result set, and call the unlock method eventually
	 * ONLY returns following values: firstReportTime, ClientID, Status, currentFrame, totalFrames, ep, currentStage, eta,
	 * title
	 * Passing in Null, will return all connections
	 * @param processId The only Client ID cared about
	 */
	public ResultSet queryLastEvent(int processId)
	{
		//CAN NOT LOCK
		try
		{
			psQueryLastEvent.setInt(1, processId);
			return psQueryLastEvent.executeQuery();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		return null;
		//Can not Unlock
	}

	/**
	 * Sets a Clients current operating Status
	 * @param name The ClientID to verify if exists
	 * @param status New Client Status to set
	 * @return If the SQL Executed Properly
	 */
	public boolean setClientStatus(String name, int status)
	{
		boolean result = false;
		lock.lock();
		try
		{
			gUpdateClientStatus.setInt(1, status);
			gUpdateClientStatus.setString(2, name);
			gUpdateClientStatus.executeUpdate();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * Runs any command given by @author on SQLite Database
	 * Nothing is verified, or sanitized
	 * @param s The Command to run
	 * @return if the SQL Executed successfully or not.
	 */
	public boolean executeDebugUpdate(String s)
	{
		boolean result = false;
		lock.lock();
		try
		{
			Statement stmt = c.createStatement();
			stmt.executeUpdate(s);
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	/**
	 * Deletes all Data in the Database, Leaves Tables intact to continue running.
	 * All Clients must be Disconnected
	 * No warning, calling this method is for a sure deletion with the Exception of an SQLException
	 * @return if the SQL executed successfully
	 */
	public boolean wipeAllRecords()
	{
		boolean result = false;
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		try (Statement stmt = c.createStatement();)
		{
			stmt.executeUpdate("DELETE FROM Clients");
			stmt.executeUpdate("DELETE FROM Configs");
			stmt.executeUpdate("DELETE FROM Connections");
			stmt.executeUpdate("DELETE FROM Processes");
			stmt.executeUpdate("DELETE FROM Timings");
			stmt.executeUpdate("DELETE FROM Events");
			c.commit();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return result;
	}

	public ResultSet queryProcess(String id)
	{
		//CAN NOT LOCK
		try
		{
			psQueryProcess.setString(1, id);
			return psQueryProcess.executeQuery();
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		return null;
		//Can not Unlock
	}

	public boolean restartDatabase()
	{
		boolean completion = false;
		lock.lock();
		try
		{
			c.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		try
		{
			abortAllCurrentProcesses();
			setClientsOffline();
			c.commit();
			completion = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return completion;
	}

	/**
	 * Method to Validate the Processes table, and its Children
	 * Must preLock, and AutoCommit should be disabled
	 * @return
	 */
	private boolean abortAllCurrentProcesses()
	{
		boolean completion = false;
		ArrayList<Integer> results = new ArrayList<Integer>();
		//CAN NOT LOCK
		try (Statement stmt = c.createStatement();)
		{
			ResultSet rs = stmt.executeQuery("SELECT ProcessID FROM Processes WHERE Status IN (10,14,57,30,32,5,6,8);");
			while (rs.next())
			{
				results.add(rs.getInt(1));
			}
			for (int x = 0; x < results.size(); x++)
			{
				gInsertNewEvent.setInt(1, results.get(x));
				gInsertNewEvent.setString(2, LocalDateTime.now().format(UserActivity.dateFormat));
				gInsertNewEvent.setInt(3, 21);
				gInsertNewEvent.setString(4, "Server Restarting, All Processes Aborted");
				gInsertNewEvent.executeUpdate();
				psAbortAllReports2.setInt(1, results.get(x));
				psAbortAllReports2.executeUpdate();
			}
			rs.close();
			results = new ArrayList<Integer>();
			rs = stmt.executeQuery("SELECT ProcessID FROM Processes WHERE Status = 12;");
			while (rs.next())
			{
				results.add(rs.getInt(1));
			}
			for (int x = 0; x < results.size(); x++)
			{
				psQueryLastEvent.setInt(1, results.get(x));
				ResultSet rs2 = psQueryLastEvent.executeQuery();//IN loop, but loop should only ever execute once
				if (rs2.next())
				{
					addEvent(true, results.get(x), LocalDateTime.now().format(UserActivity.dateFormat), 18, rs2.getString(3) + " Cancelled");
				}
				else
				{
					logger.error("Process with QuickScript In Progress Status had no Event");
				}
				rs2.close();
			}
			rs.close();
			completion = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		//CAN NOT UNLOCK
		return completion;
	}

	/**
	 * Validates Clients and Connections Table to be Offline
	 * Must preLock and autoCommit should be disabled
	 * @return if the SQL Executed successfully
	 */
	private boolean setClientsOffline()
	{
		boolean result = false;
		//CAN NOT LOCK
		try(Statement stmt = c.createStatement();)
		{
			/**
			 * ResultSet rs = stmt.executeQuery("SELECT ClientID FROM Clients WHERE Status IN (1,2);");
			 * while(rs.next())
			 * {
			 * gInsertConnection.setString(1, LocalDateTime.now().format(UserActivity.dateFormat));
			 * gInsertConnection.setString(2, rs.getString(1));
			 * gInsertConnection.setInt(3, 0);
			 * gInsertConnection.executeUpdate();
			 * }
			 */
			stmt.executeUpdate("UPDATE Clients SET ThreadIndex = NULL, Status = 0");
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		//CAN NOT UNLOCK
		return result;
	}

	public boolean createCredentials(String alias, String url, String username, char[] pass)
	{
		boolean result = false;
		lock.lock();
		try
		{
			psCreateCredentials.setString(1, alias);
			psCreateCredentials.setString(2, url);
			psCreateCredentials.setString(3, username);
			psCreateCredentials.setString(4, new String(pass));
			psCreateCredentials.executeUpdate();
			result = true;
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	public String[] retrieveCredentials(String alias)
	{
		String[] result = null;
		lock.lock();
		try
		{
			psRetrieveCredentials.setString(1, alias);
			ResultSet rs = psRetrieveCredentials.executeQuery();
			if (rs.next())
			{
				result = new String[3];
				result[0] = rs.getString(1);
				result[1] = rs.getString(2);
				result[2] = rs.getString(3);
			}
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return result;
	}

	public boolean deleteCredentials(String alias)
	{
		boolean result = false;
		lock.lock();
		if (alias == null)
		{
			try(Statement s = c.createStatement();)
			{
				s.executeUpdate("DELETE FROM Credentials");
				s.close();
				result = true;
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		else
		{
			try
			{
				psDeleteCredentials.setString(1, alias);
				psDeleteCredentials.executeUpdate();
				result = true;
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
		return result;
	}

	public ArrayList<String[]> viewCredentials()
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		lock.lock();
		try
		{
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM Credentials;");
			while (rs.next())
			{
				String[] result = new String[4];
				result[0] = rs.getString(1);
				result[1] = rs.getString(2);
				result[2] = rs.getString(3);
				result[3] = rs.getString(4);
				list.add(result);
			}
		}
		catch (SQLException e)
		{
			logger.catching(e);
		}
		lock.unlock();
		return list;
	}

	/**
	 * Creates Database, and prepares tables if not prepared already
	 */
	public Database(String d)
	{
		connectionPath = "jdbc:sqlite:" + d + File.separator + "ftuData.db";
		SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(true);
		config.setDateStringFormat("MM/dd HH:mm:ss");
		config.setOpenMode(SQLiteOpenMode.NOMUTEX);
		try
		{
			c = DriverManager.getConnection(connectionPath, config.toProperties());
			logger.info("Opened Local Database Successfully");
			c.setAutoCommit(false);
		}
		catch (Exception e)
		{
			logger.catching(e);
			System.exit(20);
		}
		lock.lock();
		try (Statement stmt = c.createStatement();)
		{
			//Date Field: (MM/dd HH:mm:ss)
			generateTablesAndTriggers(stmt);
			generateStatements();
			c.commit();
			//String for SQL Time Formatting: strftime('%m/%d %H:%M:%S','now','LocalTime')
			setClientsOffline();
			abortAllCurrentProcesses();
			c.commit();
		}
		catch (SQLException e)
		{
			logger.catching(e);
			try
			{
				c.rollback();
			}
			catch (SQLException e1)
			{
				logger.catching(e1);
			}
		}
		finally
		{
			try
			{
				c.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
		}
		lock.unlock();
	}

	private void generateTablesAndTriggers(Statement stmt) throws SQLException
	{

		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Clients (" + " ClientID TEXT PRIMARY KEY NOT NULL,"
				+ " Status TINYINT CHECK(Status BETWEEN 0 and 2) NOT NULL DEFAULT 1," + " ThreadIndex TINYINT" + ");");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Connections (" + " Time TEXT NOT NULL," + " ClientID TEXT NOT NULL,"
				+ " Action TINYINT CHECK(ACTION BETWEEN 0 and 1) NOT NULL DEFAULT 1," + " FOREIGN KEY (ClientID) REFERENCES Clients(ClientID)"
				+ "  ON DELETE CASCADE" + "  ON UPDATE CASCADE" + ");");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Configs (" + "  ConfigID INTEGER PRIMARY KEY," + "  m TEXT NOT NULL," + "  h TEXT NOT NULL,"
				+ "  n TEXT NOT NULL," + "  p TEXT NOT NULL," + "  c TEXT NOT NULL," + "  d TEXT NOT NULL," + "  b TEXT NOT NULL,"
				+ "  gpu TEXT NOT NULL," + "  tta TEXT NOT NULL," + "  y TEXT NOT NULL," + "  r1 TEXT NOT NULL," + "  r2 TEXT NOT NULL,"
				+ "  deinterlacing TEXT NOT NULL," + "  AInputString TEXT NOT NULL," + "  AExportString TEXT NOT NULL,"
				+ "  DInputString TEXT NOT NULL," + "  DExportString TEXT NOT NULL," + "  time TEXT);");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Processes (" + "  ProcessID INTEGER PRIMARY KEY," + "  ClientID TEXT(40) NOT NULL,"
				+ "  ConfigID INTEGER," + "  Status TINYINT(3) NOT NULL," + "  operating TINYINT NOT NULL," + "  jobStarted TINYINT NOT NULL,"
				+ "  jobCompleted TINYINT NOT NULL," + "  jobFailed TINYINT NOT NULL," + "  jobAborted TINYINT NOT NULL," + "  currentFrame INT,"
				+ "  paused TINYINT," + "  currentStage INT," + "  eta TEXT," + "  queue TINYINT," + "  ep TEXT," + "  title TEXT," + "  desc TEXT,"
				+ "  startingStage TINYINT," + "  usingRaw TINYINT," + "  totalFrames INT," + "  duration TEXT," + "  startTime TEXT,"
				+ "  exportExtension TEXT," + "  command1 TEXT," + "  command2 TEXT," + "  command3 TEXT," + "  command4 TEXT," + "  commandM4 TEXT,"
				+ "  operatingMode INT," + "  recovered TINYINT," + "  firstReportTime TEXT,"
				+ "  FOREIGN KEY (ConfigID) REFERENCES Configs (ConfigID)" + "    ON DELETE CASCADE" + "    ON UPDATE CASCADE,"
				+ "  FOREIGN KEY (ClientID) REFERENCES Clients (ClientID)" + "    ON DELETE CASCADE" + "    ON UPDATE CASCADE" + ");");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Events (" + " ProcessID INTEGER NOT NULL," + " TIME TEXT NOT NULL,"
				+ " Action TINYINT NOT NULL," + " MESSAGE TEXT," + " FOREIGN KEY (ProcessID) REFERENCES Processes(ProcessID)" + "  ON DELETE CASCADE"
				+ "  ON UPDATE CASCADE" + ");");
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS Timings (" + " ProcessID INTEGER NOT NULL," + " RelativeLoc TEXT NOT NULL," + " map TEXT(9) NOT NULL,"
						+ " lang TEXT(3) NOT NULL," + " handle TEXT NOT NULL," + " offset TEXT(12) NOT NULL," + " type TEXT(45) NOT NULL,"
						+ " FOREIGN KEY (ProcessID) REFERENCES Processes (ProcessID)" + "  ON DELETE CASCADE" + "  ON UPDATE CASCADE" + ");");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Credentials (" + " Alias TEXT PRIMARY KEY NOT NULL," + " Url TEXT NOT NULL,"
				+ " Username TEXT NOT NULL," + " Password TEXT NOT NULL" + ");");
		//Triggers
		stmt.executeUpdate("CREATE TRIGGER IF NOT EXISTS AutoStatusUpdate" + " AFTER INSERT" + " ON Events" + " BEGIN"
				+ " UPDATE Processes SET Status = NEW.Action WHERE ProcessID = NEW.ProcessID;"
				+ " UPDATE Processes SET firstReportTime = NEW.Time WHERE ProcessID = NEW.ProcessID AND firstReportTime IS NULL;" + " END;");
		stmt.executeUpdate("CREATE TRIGGER IF NOT EXISTS AutoAddDisconnection " + " AFTER UPDATE OF Status" + " ON Clients WHEN NEW.Status = 0"
				+ " BEGIN" + "  INSERT INTO Connections VALUES(strftime('%m/%d %H:%M:%S','now','LocalTime'),OLD.ClientID,0);" + " END;");
		stmt.executeUpdate(
				"CREATE TRIGGER IF NOT EXISTS AutoAddConnection " + " AFTER UPDATE OF Status" + " ON Clients WHEN NEW.Status = 1 AND OLD.Status = 0"
						+ " BEGIN" + "  INSERT INTO Connections VALUES(strftime('%m/%d %H:%M:%S','now','LocalTime'),OLD.ClientID,1);" + " END;");
		stmt.executeUpdate("CREATE TRIGGER IF NOT EXISTS AutoAddClient " + " AFTER INSERT ON Clients" + " BEGIN"
				+ "  INSERT INTO Connections VALUES(strftime('%m/%d %H:%M:%S','now','LocalTime'),NEW.ClientID,1);" + " END;");
	}

	private void generateStatements() throws SQLException
	{
		psCheckIfClientExists = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM Clients WHERE ClientID = ? LIMIT 1);");
		psGetClientStatus = c.prepareStatement("SELECT Status FROM Clients WHERE ClientID= ? LIMIT 1;");
		psAddNewClient1 = c.prepareStatement("INSERT INTO Clients VALUES (?, 1, ?);");

		psSelectThread = c.prepareStatement("SELECT ThreadIndex FROM CLIENTS WHERE ClientID = ?;");
		gUpdateClients = c.prepareStatement("UPDATE Clients SET Status = ?, ThreadIndex = ? WHERE ClientID=?;");
		psAbortAllReports1 = c.prepareStatement("SELECT ProcessID FROM Processes WHERE ClientID = ? AND Status IN (10,14,57,30,32,5,6,8);");
		psAbortAllReports2 = c.prepareStatement("UPDATE Processes SET operating = 0, jobAborted = 1 WHERE ProcessID = ?;");
		psAbortAllReports3 = c.prepareStatement("SELECT ProcessID FROM Processes WHERE ClientID = ? AND Status = 12;");
		gInsertNewEvent = c.prepareStatement("INSERT INTO Events VALUES (?, ?, ?, ?);");
		gUpdateClientStatus = c.prepareStatement("UPDATE Clients Set Status = ? WHERE ClientID = ?;");
		psStatus0Update = c.prepareStatement("UPDATE Processes SET Status = 0, currentFrame = ?, eta = ? WHERE ProcessID = ?;");

		gFullProcessUpdate = c.prepareStatement(
				"UPDATE Processes SET Status = ?, operating = ?, jobStarted = ?, jobCompleted = ?, jobFailed = ?, jobAborted = ?, currentFrame = ?, paused = ?, currentStage = ?, eta = ? WHERE ProcessID = ?;");
		psFindConfigID = c.prepareStatement(
				"SELECT ConfigID FROM Configs WHERE m = ? AND h = ? AND n = ? AND p = ? AND c = ? AND d = ? AND b = ? AND gpu = ? AND tta = ? AND y = ? AND r1 = ? AND r2 = ? AND deinterlacing = ? AND AInputString = ? AND AExportString = ? AND DInputString = ? AND DExportString = ?;");
		gInsertConfig = c.prepareStatement(
				"INSERT INTO Configs (m, h, n, p, c, d, b, gpu, tta, y, r1, r2, deinterlacing, AInputString, AExportString, DInputString, DExportString, time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		gInsertProcess = c.prepareStatement(
				"INSERT INTO Processes VALUES(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		gInsertTiming = c.prepareStatement("INSERT INTO Timings VALUES (?, ?, ?, ?, ?, ?, ?);");
		psQueryClientTable = c.prepareStatement("SELECT ClientID, Status FROM Clients;");
		psQueryConnectionTable = c.prepareStatement("SELECT Time, ClientID, Action FROM Connections;");
		gQueryConnectionTable = c.prepareStatement("SELECT Time, ClientID, Action FROM Connections WHERE ClientID = ?;");
		psQueryLastEvent = c.prepareStatement("SELECT Time, Action, Message FROM Events WHERE ProcessID = ? ORDER BY Time DESC, rowid DESC LIMIT 1;");
		psInsertRemux = c.prepareStatement(
				"INSERT INTO Processes (ClientID, Status, operating, jobStarted, jobCompleted, jobFailed, jobAborted, ConfigID) VALUES(?, ?, ?, ?, ?, ?, ?, ?);");
		/**
		 * Query Process
		 * ClientID = 1
		 * ConfigID = 2
		 * Status = 3
		 * operating = 4
		 * jobStarted = 5
		 * jobCompleted = 6
		 * jobFailed = 7
		 * jobAborted = 8
		 * currentFrame = 9
		 * paused = 10
		 * currentStage = 11
		 * eta = 12
		 * queue = 13
		 * ep = 14
		 * title = 15
		 * desc = 16
		 * startingStage = 17
		 * usingRaw = 18
		 * totalFrames = 19
		 * duration = 20
		 * startTime = 21
		 * exportExtension = 22
		 * command1 = 23
		 * command2 = 24
		 * command3 = 25
		 * command4 = 26
		 * commandM4 = 27
		 * operatingMode = 28
		 * recovered = 29
		 * firstReportTime = 30
		 */
		psQueryProcess = c.prepareStatement(
				"SELECT ClientID, ConfigID, Status, operating, jobStarted, jobCompleted, jobFailed, jobAborted, currentFrame, paused, currentStage, eta, queue, ep, title, desc, startingStage, usingRaw, totalFrames, duration, startTime, exportExtension, command1, command2, command3, command4, operatingMode, recovered, firstReportTime FROM Processes WHERE ProcessID = ? LIMIT 1;");
		gSelectThreadIndex = c.prepareStatement("SELECT ThreadIndex FROM Clients WHERE ClientID = ?");
		psCreateCredentials = c.prepareStatement("INSERT INTO Credentials VALUES (?, ?, ?, ?);");
		psRetrieveCredentials = c.prepareStatement("SELECT Url, Username, Password FROM Credentials WHERE Alias = ?");
		psDeleteCredentials = c.prepareStatement("DELETE FROM Credentials WHERE Alias = ?;");
	}
}
