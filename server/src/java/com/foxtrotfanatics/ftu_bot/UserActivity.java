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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.darichey.discord.Command;
import com.darichey.discord.Command.Builder;
import com.darichey.discord.CommandContext;
import com.darichey.discord.CommandRegistry;
import com.darichey.discord.limiter.ChannelLimiter;
import com.darichey.discord.limiter.RoleLimiter;
import com.darichey.discord.limiter.UserLimiter;
import com.foxtrotfanatics.ftu_bot.structures.Database;
import com.foxtrotfanatics.ftu_bot.structures.Report;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

/**
 * @author Christian
 * All Embed Messages in this Class must be restricted to these Char limits
 * Author: 40 (Hard limit on Server Name)
 * Description: 100
 * Footer: 100
 * Title: 100
 * Field Titles: 50
 * Field Values: 175
 * 
 * Separate Limits:
 * Process MetaTitle: 50
 * Client Name: 20 (hard limit, must be enforced)
 */
public class UserActivity
{

	private static Logger logger = LogManager.getLogger();
	private Database base;
	private ClientManager manager;
	private ChannelLimiter channelLimiter;
	private RoleLimiter roleLimiter;
	private String serverName;
	public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
	private CommandRegistry registry = new CommandRegistry("f!");
	private static ReactionEmoji confirm = ReactionEmoji.of(new String(Character.toChars(9989)));
	private static ReactionEmoji deny = ReactionEmoji.of(new String(Character.toChars(10062)));

	/**
	 *
	 */
	public UserActivity(Database base, ClientManager manager, IChannel c, IRole r, String s)
	{
		this.base = base;
		this.manager = manager;
		this.serverName = s;
		channelLimiter = new ChannelLimiter(c);
		roleLimiter = new RoleLimiter(r);
		//Debug Commands
		addDebugCommand();
		//Admin Commands
		addUpdateDBCommand();
		addRestartDBCommand();
		addClearCommand();
		addInstallCredentialsCommand();
		addDeleteCredentialsCommand();
		addViewCredentialsCommand();
		//Upscaler Control Commands
		addAddQueueCommand();
		addExecuteQueueCommand();
		addDisconnectCommand();
		addPauseCommand();
		addResumeCommand();
		addSingleAbortCommand();
		addFullAbortCommand();
		addDeleteQueueCommand();
		addDownloadCommand();
		//Query Commands
		addListCommand();
		addProcessesCommand();
		addProcessCommand();
		addConnectionsCommand();
		//Generic Commands
		addInfoCommand();
		addHelpCommand();
	}
	
	private void addHelpCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command help = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
			}
			if(args.size() == 0)
			{
				ArrayList<String> textTitle = new ArrayList<String>();
				ArrayList<String> textValue = new ArrayList<String>();
				ArrayList<Boolean> inline = new ArrayList<Boolean>();
				//Field Titles: 50
				//Field Values: 175
				textTitle.add("f!debugDB");
				textValue.add("Freeze code in IDE to see fields");
				inline.add(false);
				textTitle.add("f!updateDB [Statement]");
				textValue.add("Runs command on SQLite database, no return ");
				inline.add(false);
				textTitle.add("f!restartDB");
				textValue.add("Disconnects all clients and reenforces expected outcomes on SQLite tables");
				inline.add(false);
				textTitle.add("f!clear");
				textValue.add("Wipes all Data from SQLite tables");
				inline.add(false);
				textTitle.add("> addCredentials");
				textValue.add("Console Command Only, follow prompts to add secure login to WebDAV Server");
				inline.add(false);
				textTitle.add("f!deleteCredentials [Alias]");
				textValue.add("Deletes the specified set of WebDAV Server Credentials");
				inline.add(false);
				textTitle.add("f!viewCredentials");
				textValue.add("Lists all saved Credentials, passwords are unretrieveable");
				inline.add(false);
				textTitle.add("f!addQueue [ClientID]");
				textValue.add("Downloads a properly labeled Process Instruction to specified Client. Called Q###.ser");
				inline.add(false);
				textTitle.add("f!executeQueue [ClientID]");
				textValue.add("Executes Client's current Queue, includes processes added by f!addQueue");
				inline.add(false);
				textTitle.add("f!disconnect <ClientID>");
				textValue.add("Disconnect single or all Clients from Server. Use while specificed clients are idle");
				inline.add(false);
				textTitle.add("f!pause [ClientID]");
				textValue.add("Remotely Pauses Client, resume with f!resume");
				inline.add(false);
				textTitle.add("f!resume [ClientID]");
				textValue.add("Remotely Resumes Client");
				inline.add(false);
				textTitle.add("f!abort [ClientID]");
				textValue.add("Remotely Aborts a single Process of a Client");
				inline.add(false);
				textTitle.add("f!fullAbort [ClientID]");
				textValue.add("Terminates all Processes in Queue for Client, sets Client Idle");
				inline.add(false);
				textTitle.add("f!deleteQueue [ClientID]");
				textValue.add("Deletes all Processes in the Queue");
				inline.add(false);
				textTitle.add("f!download [ClientID] [CredentialSet] [Destination Folder] [File Name]");
				textValue.add("Download Source File from WebDAV Server using installed Credentials to Client");
				inline.add(false);
				textTitle.add("f!list <ClientID>");
				textValue.add("Lists all Clients with their operating status");
				inline.add(false);
				textTitle.add("f!pros <-r> <-f> <-q> <-c> <-p> <e> <-{#}> <ClientID>");
				textValue.add("Lists all Processes in memory filtered by provided arguments");
				inline.add(false);
				textTitle.add("~~f!process [ProcessID]~~ Broken");
				textValue.add("Print out Details of the specific Process, find the specific ID with f!pros in brackets");
				inline.add(false);
				textTitle.add("f!conn <ClientID>");
				textValue.add("List out events where a Client dis/connects to Server");
				inline.add(false);
				textTitle.add("f!info");
				textValue.add("Display Info about the bot");
				inline.add(false);
				textTitle.add("f!help <Command>");
				textValue.add("Display Command list with descriptions, or command details");
				inline.add(false);
				int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
				for (int z = 0; z < pageCount; z++)
				{
					EmbedBuilder message = new EmbedBuilder();
					message.withAuthorName(serverName);
					message.appendDesc("All Commands FTU-Bot respects, might require special permission set in values.txt");
					message.withColor(255, 165, 0);
					message.withTitle("Command List");
					int fieldCount = 0;
					for (int x = 0; x < 25; x++)
					{
						if ((x + (z * 25)) < textTitle.size())
						{
							message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
							fieldCount++;
						}
					}
					message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage(message.build());
					});
				}
			}
			else if(args.size() == 1)
			{
				switch(args.get(0))
				{
					case "debugDB":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Debug command, please ignore");
						}).get();
						break;
					case "updateDB":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Execute SQL command on Database\nUsage: **f!updateDB** [Command String]");
						}).get();
						break;
					case "restartDB":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Disconnect Clients, and revalidates Database into an expected state.");
						}).get();
						break;
					case "clear":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Drops all Tables, and Recreates Tables to latest specification");
						}).get();
						break;
					case "addCredentials":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("__Console Command Only__: Adds Credential Set to Database, follow in console prompts");
						}).get();
						break;
					case "deleteCredentials":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Deletes Credential Set from Database" + "\nUsage: **f!deleteCredentials** [alias]"
									+ "\n[alias] = Shortcut to Credentials, use in place of [Credential Set]");
						}).get();
						break;
					case "viewCredentials":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Displays List of all Credential Sets in Database" + "\nUsage: **f!viewCredentials**");
						}).get();
						break;
					case "addQueue":
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Displays List of all Credential Sets in Database" + "\nUsage: **f!viewCredentials**");
						}).get();
						break;
					case "executeQueue":
						break;
					case "disconnect":
						break;
					case "pause":
						break;
					case "resume":
						break;
					case "abort":
						break;
					case "fullAbort":
						break;
					case "deleteQueue":
						break;
					case "download":
						break;
					case "list":
						break;
					case "pros":
						break;
					case "process":
						break;
					case "conn":
						break;
					case "info":
						break;
					case "help":
						break;
					default:
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Unknown Command!\nUse f!help for Command List\nUsage: **f!help** <Command Name>");
						}).get();
						break;
				}
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Too Many Arguments");
				}).get();
			}
		}).build();
		registry.register(help, "help");
	}

	private boolean confirmAction(CommandContext ctx, String message)
	{
		IMessage m = RequestBuffer.request(() -> {
			return ctx.getMessage().reply(message.substring(0, Math.min(message.length(), 1900))
					+ "\n:white_check_mark: to Confirm\n:negative_squared_cross_mark: to Deny\n5 seconds to Accept");
		}).get();
		final IMessage m2 = m;//I dont get why this is necessary
		RequestBuffer.request(() -> {
			m2.addReaction(confirm);
		}).get();
		RequestBuffer.request(() -> {
			m2.addReaction(deny);
		}).get();
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		IMessage m3 = ctx.getClient().getMessageByID(m.getLongID());
		if (m3.getReactionByEmoji(deny).getUserReacted(ctx.getMessage().getAuthor()))
		{
			RequestBuffer.request(() -> {
				m3.removeReaction(m3.getClient().getOurUser(), confirm);
			}).get();
			RequestBuffer.request(() -> {
				m3.addReaction(ReactionEmoji.of(new String(Character.toChars(127383))));
			}).get();
			return false;
		}
		else if (m3.getReactionByEmoji(confirm).getUserReacted(ctx.getMessage().getAuthor()))
		{
			RequestBuffer.request(() -> {
				m3.removeReaction(m3.getClient().getOurUser(), deny);
			}).get();
			RequestBuffer.request(() -> {
				m3.addReaction(ReactionEmoji.of(new String(Character.toChars(127383))));
			}).get();
			return true;
		}
		else
		{
			RequestBuffer.request(() -> {
				m3.removeAllReactions();
			}).get();
			RequestBuffer.request(() -> {
				m3.addReaction(ReactionEmoji.of(new String(Character.toChars(128162))));
			}).get();
			return false;
		}
	}

	private void addViewCredentialsCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		b.limiter(roleLimiter);
		Command viewCredentials = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))//None Specified, Disconnect ALL
			{
				args.remove(0);
			}
			if (args.size() != 0)
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Incorrect Number of Arguments!" + "\nUsage: **f!viewCredentials**");
				}).get();
				return;
			}
			ArrayList<String[]> credentials = base.viewCredentials();
			boolean empty = true;
			String subTitle;
			ArrayList<String> textTitle = new ArrayList<String>();
			ArrayList<String> textValue = new ArrayList<String>();
			ArrayList<Boolean> inline = new ArrayList<Boolean>();
			int y = 1;
			for (int x = 0; x < credentials.size(); x++)
			{
				subTitle = (y++) + ". " + credentials.get(x)[0];
				textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
				String text = "Base Directory: " + credentials.get(x)[1] + "\nUsername: " + credentials.get(x)[2];
				textValue.add(text.substring(0, Math.min(175, text.length())));
				inline.add(false);
				empty = false;
			}
			if (empty)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Credentials Stored");
				message.withColor(255, 255, 0);
				message.withTimestamp(Instant.now());
				message.withTitle("Credential List");
				message.setLenient(false);
				//message.appendField("No Credentials", "- No Computer has Connected Yet", false);
				message.withFooterText("No Credentials");
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
				return;
			}
			int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
			for (int z = 0; z < pageCount; z++)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Credentials Stored");
				message.withColor(255, 255, 0);
				message.withTimestamp(Instant.now());
				message.withTitle("Credential List");
				int fieldCount = 0;
				for (int x = 0; x < 25; x++)
				{
					if ((x + (z * 25)) < textTitle.size())
					{
						message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
						fieldCount++;
					}
				}
				message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
			}
		}).build();
		registry.register(viewCredentials, "viewCredentials");
	}

	private void addDeleteCredentialsCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		b.limiter(roleLimiter);
		Command deleteCredentials = b.onCalled(ctx -> {

			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))//None Specified, Disconnect ALL
			{
				args.remove(0);
			}
			if (args.size() == 1)
			{
				if (base.retrieveCredentials(args.get(0)) != null)
				{
					if (confirmAction(ctx, "Are you sure you want to **Delete** the \"" + args.get(0) + "\"f Credential Set?"))
					{
						if (base.deleteCredentials(args.get(0)))
						{
							RequestBuffer.request(() -> {
								ctx.getChannel().sendMessage("Credential Deleted");
							}).get();
						}
						else
						{
							RequestBuffer.request(() -> {
								ctx.getChannel().sendMessage("Failed to Remove Credential!");
							}).get();
						}
					}
					else
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Command Cancelled!");
						}).get();
					}
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Alias does not Exist!");
					}).get();
				}
			}
			else if (args.size() == 0)
			{
				if (confirmAction(ctx, "Are you sure you want to **Delete** ALL Credentials?"))
				{
					if (base.deleteCredentials(null))
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("All Credentials Deleted");
						}).get();
					}
					else
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Failed to Remove Credentials!");
						}).get();
					}
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Command Cancelled!");
					}).get();
				}
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Incorrect Number of Arguments!" + "\nUsage: **f!deleteCredentials** [alias]"
							+ "\n[alias] = Shortcut to Credentials, use in place of [Credential Set]");
				}).get();
				return;
			}
		}).build();
		registry.register(deleteCredentials, "deleteCredentials", "removeCredentials");
	}

	private void addInstallCredentialsCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		b.limiter(roleLimiter);
		Command addCredentials = b.onCalled(ctx -> {
			RequestBuffer.request(() -> {
				ctx.getChannel()
						.sendMessage("Can not create Credentials in Discord.\nEnter \"addCredentials\" in the Console to start the procedure.");
			}).get();
		}).build();
		registry.register(addCredentials, "addCredentials");
	}

	private void addDownloadCommand()
	{
		Builder b = Command.builder();
		b.limiter(roleLimiter);
		b.limiter(channelLimiter);
		Command download = b.onCalled(ctx -> {

			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			ArrayList<String> folders = new ArrayList<String>();
			folders.add("ImportVideo");
			folders.add("ImportAudio");
			folders.add("ImportSubtitles");
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			if (args.size() != 4)
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Incorrect Number of Arguments!"
							+ "\nUsage: **f!download** [ClientID] [CredentialSet] [Destination Folder] [File Name]"
							+ "\n[ClientID] = ClientID of the Client to control" + "\n[CredentialSet Name] = See command f!installCredentials"
							+ "\n[Destination Folder] = SubDirectory in FTUClient Directory to store files, choices are \"ImportVideo\",\"ImportAudio\", or \"ImportSubtitles\""
							+ "\n[File Name] = Name of file in both WebDAV Server and Destination");
				}).get();
				return;
			}
			if (base.checkIfClientExists(args.get(0)) && base.getClientStatus(args.get(0)) == 1)
			{
				String[] credentials = null;
				if ((credentials = base.retrieveCredentials(args.get(1))) != null)
				{
					if (folders.contains(args.get(2)))
					{
						try
						{
							@SuppressWarnings("unused")
							URL syntaxTest = new URL(credentials[0] + "/" + args.get(3));
							ClientThread client = manager.getClient(args.get(0));
							client.remoteCommand("download");
							client.remoteCommand(credentials[0]);
							client.remoteCommand(credentials[1]);
							client.remoteCommand(credentials[2]);
							client.remoteCommand(args.get(3));
							client.remoteCommand(args.get(2));
							RequestBuffer.request(() -> {
								ctx.getChannel()
										.sendMessage("Submitted Command, waiting for Results...\n__Ignoring Further Commands to this Client__");
							}).get();
							Report r = client.getMessage();
							String result = r.getTitle();
							if (r.getCommand1() != null)
							{
								result += "\n" + r.getCommand1();
							}
							final String result2 = result;
							RequestBuffer.request(() -> {
								ctx.getChannel().sendMessage(result2);
							}).get();
						}
						catch (MalformedURLException e)
						{
							RequestBuffer.request(() -> {
								ctx.getChannel().sendMessage("Complete WebDAV Directory has bad Syntax!");
							}).get();
						}
					}
					else
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Destination Folder Does Not Exist!");
						}).get();
					}
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Credentials Alias Does Not Exist!");
					}).get();
				}
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
			}
		}).build();
		registry.register(download, "download");
	}

	private void addDisconnectCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command disconnect = b.onCalled(ctx -> {

			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))//None Specified, Disconnect ALL
			{
				args.remove(0);
				if (confirmAction(ctx, "Are you sure you want to Disconnect ALL Clients?"))
				{
					manager.disconnectAll();
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Disconnection Completed!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Command Cancelled!");
					}).get();
				}
			}
			if (base.checkIfClientExists(args.get(0)) && base.getClientStatus(args.get(0)) == 1)
			{
				if (confirmAction(ctx, "Are you sure you want to Disconnect " + args.get(0) + "?"))
				{
					manager.disconnectClient(args.get(0));
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Disconnection Completed!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Command Cancelled!");
					}).get();
				}
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
			}
		}).build();
		registry.register(disconnect, "disconnect");
	}

	private void addAddQueueCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command addQueue = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Adding Queue File to {}", args.get(0));
			if (ctx.getMessage().getAttachments().size() == 1)
			{
				if (base.checkIfClientExists(args.get(0)) && base.getClientStatus(args.get(0)) == 1)
				{
					Attachment att = ctx.getMessage().getAttachments().get(0);
					if (att.getFilename().endsWith(".ser") && att.getFilename().startsWith("Q"))
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage(
									"Attempting Queue Submission to " + args.get(0) + "\nAfter completion, please reverify Queue before Executing.");
						}).get();
						manager.getClient(args.get(0)).remoteCommand("addSingleProcessToQueue");
						manager.getClient(args.get(0)).remoteCommand(att.getUrl());
					}
					else
					{
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Improper Filename! Specify as `Q###.ser`");
						}).get();
					}
				}
				else
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client Does Not Exist!");
					}).get();
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Queue File Attached!");
				}).get();
			}

		}).build();
		registry.register(addQueue, "addQueue");
	}

	private void addResumeCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command resume = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Resumeing Upscale for: {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)))
			{
				int status = base.getClientStatus(args.get(0));
				if (status == 1)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Attempting Remote Resume on Upscale for " + args.get(0)
								+ "\nAfter completion, please do `f!pros` to verify it was successful.");
					}).get();
					manager.getClient(args.get(0)).remoteCommand("resumeUpscale");
				}
				else if (status == 2)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is already Running!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is Offline!");
					}).get();
				}
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(resume, "resume");
	}

	private void addFullAbortCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command fAbort = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Aborting Upscale for: {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)))
			{
				int status = base.getClientStatus(args.get(0));
				if (status == 2)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Attempting Remote Abort full Queue for " + args.get(0)
								+ "\nAfter completion, please do `f!pros` to verify it was successful.");
					}).get();
					manager.getClient(args.get(0)).remoteCommand("fullAbortUpscale");
				}
				else if (status == 1)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is already Idle!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is Offline!");
					}).get();
				}
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(fAbort, "fullAbort");
	}

	private void addSingleAbortCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command sAbort = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Aborting Upscale for: {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)))
			{
				int status = base.getClientStatus(args.get(0));
				if (status == 2)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Attempting Remote Single Abort on Upscale for " + args.get(0)
								+ "\nAfter completion, please do `f!pros` to verify it was successful.");
					}).get();
					manager.getClient(args.get(0)).remoteCommand("singleAbortUpscale");
				}
				else if (status == 1)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is already Idle!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is Offline!");
					}).get();
				}
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(sAbort, "abort");
	}

	private void addPauseCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command pause = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Pauseing Upscale for: {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)))
			{
				int status = base.getClientStatus(args.get(0));
				if (status == 2)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Attempting Remote Pause on Upscale for " + args.get(0)
								+ "\nAfter completion, please do `f!pros` to verify it was successful.");
					}).get();
					manager.getClient(args.get(0)).remoteCommand("pauseUpscale");
				}
				else if (status == 1)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is already Idle!");
					}).get();
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is Offline!");
					}).get();
				}
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(pause, "pause");
	}

	private void addDeleteQueueCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command deleteQueue = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Deleting Queue {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)))
			{
				if (base.getClientStatus(args.get(0)) == 1)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Attempting Remote Queue Deletion for: " + args.get(0));
					}).get();
					manager.getClient(args.get(0)).remoteCommand("deleteQueue");
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client is not in a position to Delete the Queue!");
					}).get();
				}
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(deleteQueue, "deleteQueue");
	}

	private void addExecuteQueueCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command executeQueue = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
			{
				args.remove(0);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No Client Specified!");
				}).get();
				return;
			}
			logger.info("Remotely Executing Queue {}", args.get(0));
			if (base.checkIfClientExists(args.get(0)) && base.getClientStatus(args.get(0)) == 1)
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(
							"Attempting Remote Queue Execution to {}\nAfter completion, please do `f!pros` to verify it was successful.");
				}).get();
				manager.getClient(args.get(0)).remoteCommand("remoteExecuteTheQueueRightNow");
			}
			else
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Client Does Not Exist!");
				}).get();
		}).build();
		registry.register(executeQueue, "executeQueue");
	}

	private void addDebugCommand()
	{
		Builder b = Command.builder();
		b.limiter(new UserLimiter(163810952905490432L));
		Command debug = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			RequestBuffer.request(() -> {
				ctx.getChannel().sendMessage("No");
			});
		}).build();
		registry.register(debug, "debug");
	}

	private void addUpdateDBCommand()
	{
		Builder b = Command.builder();
		b.limiter(roleLimiter);
		b.limiter(channelLimiter);
		Command updateDB = b.onCalled(ctx -> {
			logger.debug("SQL Command: {}", ctx.getMessage().getFormattedContent().substring(11, ctx.getMessage().getFormattedContent().length()));
			boolean result = base.executeDebugUpdate(ctx.getMessage().getFormattedContent().substring(11, ctx.getMessage().getFormattedContent().length()));
			String content;
			if (result)
				content = "Command Executed Successfully";
			else
				content = "SQL Failed, check Console for Errors";
			RequestBuffer.request(() -> {
				ctx.getChannel().sendMessage(content);
			});
		}).build();
		registry.register(updateDB, "updateDB");
	}

	private void addClearCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		b.limiter(roleLimiter);
		Command clearDB = b.onCalled(ctx -> {
			if (confirmAction(ctx, "Are you Sure you Want to Clear the database?\nAll Clients must be Disconnected first."))
			{
				logger.debug("Kicking Clients, and Wiping Database");
				manager.disconnectAll();
				boolean result = base.wipeAllRecords();
				String content;
				if (result)
					content = "Database Successfully Cleared";
				else
					content = "Something went Wrong, check Console for Errors";
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(content);
				});
				manager.updateGame();
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Command Cancelled");
				});
			}
		}).build();
		registry.register(clearDB, "clearDB");
	}

	/**
	 * Performs Cleanup and Maintenance work on the Database, so that all Data should be validated.
	 * Useful for when a Client can not connect because the Database forgot to register its disconnection.
	 * Does not clear rows, unless the row can not be salvaged
	 * All Clients will be disconnected to execute this properly
	 */
	private void addRestartDBCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		b.limiter(roleLimiter);
		Command restartDB = b.onCalled(ctx -> {
			if (confirmAction(ctx, "Are you Sure you Want to Restart the database?\nAll Clients must be Disconnected first."))
			{
				logger.debug("Kicking Clients, and validating Database");
				manager.disconnectAll();
				boolean result = base.restartDatabase();
				String content;
				if (result)
					content = "Database Successfully Validated";
				else
					content = "Something went Wrong, check Console for Errors";
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(content);
				});
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Command Cancelled");
				});
			}
		}).build();
		registry.register(restartDB, "restartDB");
	}

	/**
	 * Procedure for Executing List command
	 */
	private void addListCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command list = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			String subTitle;
			ResultSet r;
			boolean empty = true;
			ArrayList<String> textTitle = new ArrayList<String>();
			ArrayList<String> textValue = new ArrayList<String>();
			ArrayList<Boolean> inline = new ArrayList<Boolean>();
			int y = 1;
			if (args.size() == 0)
			{
				r = base.queryClientList();
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Too Many Arguements");
				});
				return;
			}
			try
			{
				while (r.next())
				{
					empty = false;
					subTitle = (y++) + ". " + r.getString(1);
					textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
					switch (r.getInt(2))//Shorter then 175 chars
					{
						case 0:
							textValue.add(" - :red_circle: Offline");
							break;
						case 1:
							textValue.add(" - :large_orange_diamond:  Idle");
							break;
						case 2:
							textValue.add(" - :large_blue_diamond: Running");
							break;
						default:
							logger.error("Unexpected Valued Given for Client Status: {}", r.getInt(3));
					}
					inline.add(false);
					empty = false;
				}
				r.close();
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
			base.releaseLock();
			if (empty)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Clients that have Connected");
				message.withColor(0, 255, 255);
				message.withTimestamp(Instant.now());
				message.withTitle("Client List");
				message.setLenient(false);
				//message.appendField("No Clients", "- No Computer has Connected Yet", false);
				message.withFooterText("No Clients");
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
				return;
			}
			int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
			for (int z = 0; z < pageCount; z++)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Clients that have Connected");
				message.withColor(0, 255, 255);
				message.withTimestamp(Instant.now());
				message.withTitle("Client List");
				int fieldCount = 0;
				for (int x = 0; x < 25; x++)
				{
					if ((x + (z * 25)) < textTitle.size())
					{
						message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
						fieldCount++;
					}
				}
				message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
			}
		}).build();
		registry.register(list, "list", "clientList", "clients");
	}

	/**
	 * Shows some information about the bot.
	 * usage = info [author|time]
	 */
	private void addInfoCommand()
	{
		Builder b = Command.builder();
		Command info = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			String message;
			if (args.size() > 0)
			{ // more than 1 argument
				message = "Too many arguments!";
			}
			else if (args.size() == 0)
			{ // !info
				message = "- **Author:** Christian77777\n" + "- **Programming Language:** Java\n" + "- **Discord Connection Library:** Discord4J\n"
						+ "- **Discord (Command) Library:** Commands4J";
			}
			else
				throw new IllegalArgumentException("Negative number of Arguments!");
			RequestBuffer.request(() -> {
				ctx.getChannel().sendMessage(message);
			});
		}).build();
		registry.register(info, "info");
	}

	private void addConnectionsCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command conn = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			String title;//Restricted to 100 chars
			String subTitle;
			ResultSet r;
			boolean empty = true;
			ArrayList<String> textTitle = new ArrayList<String>();
			ArrayList<String> textValue = new ArrayList<String>();
			ArrayList<Boolean> inline = new ArrayList<Boolean>();
			int y = 0;
			if (args.size() == 1)
			{
				title = ("" + args.get(0) + " Connection History");
				if (base.checkIfClientExists(args.get(0)))
				{
					r = base.queryConnectionsTable(args.get(0));
				}//gets index of ClientName
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client not Found!");
					});
					return;
				}
			}
			else if (args.size() == 0)
			{
				title = ("Global Connection History");
				r = base.queryConnectionsTable(null);
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Too Many Arguements");
				});
				return;
			}
			try
			{
				while (r.next())
				{
					subTitle = printTitle(y++, r.getString(1), r.getString(2), 0);
					textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
					if (r.getBoolean(3))
						textValue.add(" - :white_check_mark: Connected");//Shorter then 175 chars
					else
						textValue.add(" - :no_entry: Disconnected");//Shorter then 175 chars
					inline.add(false);
					empty = false;
				}
				r.close();
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
			base.releaseLock();
			if (empty)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.setLenient(false);
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Connections made to the Server");
				message.withColor(200, 0, 0);
				message.withTimestamp(Instant.now());
				//message.appendField("No Activity", "- 0 records fit criteria", false);
				message.withFooterText("No Connections");
				message.withTitle(title);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
				return;
			}
			int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
			for (int z = 0; z < pageCount; z++)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.setLenient(false);
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Connections made to the Server");
				message.withColor(200, 0, 0);
				message.withTimestamp(Instant.now());
				message.withTitle(title);
				int fieldCount = 0;
				for (int x = 0; x < 25; x++)
				{
					if ((x + (z * 25)) < textTitle.size())
					{
						message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
						fieldCount++;
					}
				}
				message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
			}
		}).build();
		registry.register(conn, "conn", "connection");
	}

	/**
	 * Shows all processes of a Client, or the whole Roster
	 * usage = processes [clientID]
	 */
	private void addProcessesCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command pros = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			String title;//Embed Title
			String subTitle;//Temp variable
			ResultSet r;
			boolean empty = true;
			ArrayList<String> textTitle = new ArrayList<String>();
			ArrayList<String> textValue = new ArrayList<String>();
			ArrayList<Boolean> inline = new ArrayList<Boolean>();
			int y = 0;
			ArrayList<Integer> acceptStatus = new ArrayList<Integer>();//List of Status to include in report
			String name = null;//Client Name
			for (int a = 0; a < args.size(); a++)
			{
				if (args.get(a).equals("-r"))//Running processes
				{
					acceptStatus.add(12);
					acceptStatus.add(11);
					acceptStatus.add(0);
					acceptStatus.add(10);
					acceptStatus.add(14);
					acceptStatus.add(32);
					acceptStatus.add(30);
				}
				else if (args.get(a).equals("-f"))//failed processes
				{
					acceptStatus.add(17);
					acceptStatus.add(18);
					acceptStatus.add(21);
					acceptStatus.add(50);
					acceptStatus.add(20);
				}
				else if (args.get(a).equals("-q"))//Queued Processes
				{
					acceptStatus.add(57);
				}
				else if (args.get(a).equals("-c"))//Completed Processes
				{
					acceptStatus.add(16);
					acceptStatus.add(13);
				}
				else if (args.get(a).equals("-p"))//Paused Processes
				{
					acceptStatus.add(30);
				}
				else if (args.get(a).equals("-e"))//QuickScripts executed
				{
					acceptStatus.add(12);
					acceptStatus.add(16);
					acceptStatus.add(17);
					acceptStatus.add(18);
				}
				else if (args.get(a).startsWith("-") && args.get(a).length() >= 2 && args.get(a).substring(1, args.get(a).length()).matches("-?\\d+"))//Advanced command, Select by Status Code
				{
					acceptStatus.add(Integer.parseInt(args.get(a).substring(1, args.get(a).length())));
				}
				else//ClientID or bust
				{
					if(args.get(a).startsWith("-"))
					{
						final String arg = args.get(a);
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Invalid Argument: \"" + arg + "\"");
						});
						return;
					}
					else if(name == null)
					{
						name = args.get(a);
					}
					else
					{
						final String arg = args.get(a);
						RequestBuffer.request(() -> {
							ctx.getChannel().sendMessage("Duplicate Client Name: \"" + arg + "\"");
						});
						return;
					}
				}
			}
			if(!acceptStatus.isEmpty())
			{
				//Remove Duplicate Statuses
				LinkedHashSet<Integer> hashSet = new LinkedHashSet<Integer>(acceptStatus);
				acceptStatus.clear();
				acceptStatus.addAll(hashSet);
				//Prepare Numbers in Array to print to log
				String statuses = new String("Whitelisted Status Numbers for f!pros call: ");
				for(int i : acceptStatus)
				{
					statuses += i + ", ";
				}
				statuses = statuses.substring(0, statuses.length()-2);
				logger.debug(statuses);
			}
			else
			{
				logger.debug("All Statuses whitelisted for f!pros call");
			}
			if (name != null)
			{
				title = ("" + name + " Process History");
				if (base.checkIfClientExists(name))//VERIFY NAME FIRST
				{
					r = base.queryProcessesTable(name, acceptStatus);
				}
				else
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client not Found!");
					});
					return;
				}
			}
			else
			{ // !info
				title = ("Global Process History");
				r = base.queryProcessesTable(null, acceptStatus);
			}
			try
			{
				while (r.next())
				{
					int s = r.getInt(3);
					subTitle = printTitle(y++, r.getString(1), r.getString(2), r.getInt(10));
					switch (s)
					{
						case 0:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Running\n" + printProgress(r.getInt(4), r.getInt(5), r.getString(6), printStage(r.getInt(7)),
									r.getString(8), r.getString(9)));
							inline.add(false);
							break;
						case 10:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Started");
							inline.add(false);
							break;
						case 16:
						case 17:
						case 18:
						case 12:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							ResultSet r2 = base.queryLastEvent(r.getInt(10));
							r2.next();
							textValue.add(" - Quick Script " + r2.getString(3));
							r2.close();
							inline.add(false);
							break;
						case 13:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Completed");
							inline.add(false);
							break;
						case 14:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Started");
							inline.add(false);
							break;
						case 57:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Queued");
							inline.add(false);
							break;
						case 50:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Failed");
							inline.add(false);
							break;
						case 30:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Paused");
							inline.add(false);
							break;
						case 32:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Resumed\n\tStage: " + printStage(r.getInt(7)));
							inline.add(false);
							break;
						case 20:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Aborted\n" + printProgress(r.getInt(4), r.getInt(5), r.getString(6), printStage(r.getInt(7)),
									r.getString(8), r.getString(9)));
							inline.add(false);
							break;
						case 21:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Process Cancelled\n");
							inline.add(false);
							break;
						case 5:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Stage " + printStage(r.getInt(7)) + " Starting");
							inline.add(false);
							break;
						case 6:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Stage " + printStage(r.getInt(7)) + " Finished");
							inline.add(false);
							break;
						case 8:
							textTitle.add(subTitle.substring(0, Math.min(50, subTitle.length())));
							textValue.add(" - Waifu2x took a dump\n\tPls Wait ");
							inline.add(false);
							break;
						default:
							logger.error("Unknown Status Found for Client: {} - Status: {}", r.getString(2), s);
					}
					empty = false;
				}
				r.close();
				base.releaseLock();
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
			if (empty)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Process History Server has recorded since Startup");
				message.withColor(128, 0, 255);
				message.withTimestamp(Instant.now());
				message.withTitle(title);
				message.withFooterText("No Processes");
				//message.appendField("No Processes", " - 0 records fit criteria", false);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
				return;
			}
			int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
			for (int z = 0; z < pageCount; z++)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Record of all Process History Server has recorded since Startup");
				message.withColor(128, 0, 255);
				message.withTimestamp(Instant.now());
				message.withTitle(title);
				int fieldCount = 0;
				for (int x = 0; x < 25; x++)
				{
					if ((x + (z * 25)) < textTitle.size())
					{
						message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
						fieldCount++;
					}
				}
				message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
			}
		}).build();
		registry.register(pros, "pros", "jobs", "processes");
	}

	/**
	 * Shows all processes of a Client, or the whole Roster
	 * usage = processes [clientID]
	 */
	private void addProcessCommand()
	{
		Builder b = Command.builder();
		b.limiter(channelLimiter);
		Command process = b.onCalled(ctx -> {
			ArrayList<String> args = new ArrayList<String>(ctx.getArgs());
			if (args.size() == 1 && args.get(0).equals(""))
				args.remove(0);
			String title;
			String tempSubTitle;
			ResultSet r;
			boolean empty = true;
			ArrayList<String> textTitle = new ArrayList<String>();
			ArrayList<String> textValue = new ArrayList<String>();
			ArrayList<Boolean> inline = new ArrayList<Boolean>();
			int y = 0;
			if (args.size() == 1)
			{
				title = ("Process Details - [" + args.get(0) + "]");
				r = base.queryProcess(args.get(0));
				if (r == null)
				{
					RequestBuffer.request(() -> {
						ctx.getChannel().sendMessage("Client not Found!");
					});
					return;
				}
			}
			else if (args.size() == 0)
			{ // !info
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("No ProcessID Argument" + "\nUsage: **f!process [ProcessID]"
							+ "\n[ProcessID] = ID of Process to view in detail, find it with f!pros");
				});
				return;
			}
			else
			{
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage("Too Many Arguements" + "\nUsage: **f!process [ProcessID]"
							+ "\n[ProcessID] = ID of Process to view in detail, find it with f!pros");
				});
				return;
			}
			try
			{
				if (r.next())
				{
					int s = r.getInt(3);
					tempSubTitle = printTitle(y++, r.getString(1), r.getString(2), r.getInt(10));
					switch (s)
					{
						case 0:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Running\n" + printProgress(r.getInt(4), r.getInt(5), r.getString(6), printStage(r.getInt(7)),
									r.getString(8), r.getString(9)));
							inline.add(false);
							break;
						case 10:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Started");
							inline.add(false);
							break;
						case 16:
						case 17:
						case 18:
						case 12:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							ResultSet r2 = base.queryLastEvent(r.getInt(10));
							r2.next();
							textValue.add(" - Quick Script " + r2.getString(3));
							r2.close();
							inline.add(false);
							break;
						case 13:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Completed");
							inline.add(false);
							break;
						case 14:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Started");
							inline.add(false);
							break;
						case 57:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Queued");
							inline.add(false);
							break;
						case 50:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Failed");
							inline.add(false);
							break;
						case 30:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Paused");
							inline.add(false);
							break;
						case 32:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Resumed\n\tStage: " + printStage(r.getInt(7)));
							inline.add(false);
							break;
						case 20:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Aborted\n" + printProgress(r.getInt(4), r.getInt(5), r.getString(6), printStage(r.getInt(7)),
									r.getString(8), r.getString(9)));
							inline.add(false);
							break;
						case 21:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Process Cancelled\n");
							inline.add(false);
							break;
						case 5:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Stage " + printStage(r.getInt(7)) + " Starting");
							inline.add(false);
							break;
						case 6:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Stage " + printStage(r.getInt(7)) + " Finished");
							inline.add(false);
							break;
						case 8:
							textTitle.add(tempSubTitle.substring(0, Math.min(50, tempSubTitle.length())));
							textValue.add(" - Waifu2x took a dump\n\tPls Wait ");
							inline.add(false);
							break;
						default:
							logger.error("Unknown Status Found for Client: {} - Status: {}", r.getString(2), s);
					}
					empty = false;
				}
				r.close();
				base.releaseLock();
			}
			catch (SQLException e)
			{
				logger.catching(e);
			}
			if (empty)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Detailed information on this Process");
				message.withColor(57, 255, 20);
				message.withTimestamp(Instant.now());
				message.withTitle(title);
				message.withFooterText("Does Not Exist");
				//message.appendField("No Processes", " - 0 records fit criteria", false);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
				return;
			}
			int pageCount = ((int) Math.ceil(textTitle.size() / 25.0));
			for (int z = 0; z < pageCount; z++)
			{
				EmbedBuilder message = new EmbedBuilder();
				message.withAuthorName(serverName);
				message.appendDesc("Detailed information on this Process");
				message.withColor(57, 255, 20);
				message.withTimestamp(Instant.now());
				message.withTitle(title);
				int fieldCount = 0;
				for (int x = 0; x < 25; x++)
				{
					if ((x + (z * 25)) < textTitle.size())
					{
						message.appendField(textTitle.get(x + (z * 25)), textValue.get(x + (z * 25)), inline.get(x + (z * 25)));
						fieldCount++;
					}
				}
				message.withFooterText("Showing " + fieldCount + " Entries - Page " + (z + 1) + "/" + pageCount);
				RequestBuffer.request(() -> {
					ctx.getChannel().sendMessage(message.build());
				});
			}
		}).build();
		registry.register(process, "process", "");
	}

	public CommandRegistry getRegistry()
	{
		return registry;
	}

	private String printProgress(int currentFrame, int totalFrames, String ep, String currentStage, String eta, String title)
	{
		int percent = (currentFrame * 100) / totalFrames;
		StringBuilder sb = new StringBuilder("\tEP: ");
		sb.append(ep);
		sb.append("\n\tStage: ");
		sb.append(currentStage);
		sb.append("\n\tProgress: (");
		sb.append(currentFrame);
		sb.append("/");
		sb.append(totalFrames);
		sb.append(") - ");
		sb.append(percent);
		sb.append("%\n\t");
		if (!eta.equals(""))
		{
			sb.append("ETA: ");

			if (!eta.equals("Calculating"))
			{
				sb.append(eta.substring(5, 13));
				sb.append(eta.substring(14));
			}
			else
				sb.append("Calculating");
			sb.append("\n\t");
		}
		else
		{
			sb.append("**ETA was blank**\n\t");
		}
		if (!title.equals(""))
		{
			sb.append("Title: ");
			sb.append(title.substring(17, Math.min(title.length() - 2, 50)));
		}
		return sb.toString();
	}

	private String printTitle(int index, String time, String clientID, int processID)
	{
		String date;
		String id;
		if (time == null)
			date = "";
		else
			date = time;
		if (processID == 0)
			id = "";
		else
			id = " - [" + processID + "]";
		return ((index + 1) + ". " + date + " " + clientID + id);
	}

	private String printStage(int stage)
	{
		switch (stage)
		{
			case 1:
				return "A";
			case 2:
				return "B";
			case 3:
				return "C";
			case 4:
				return "D";
			default:
				throw new IllegalArgumentException("Integer entered is not a value of 1-4, instead returns: " + stage);
		}
	}
}