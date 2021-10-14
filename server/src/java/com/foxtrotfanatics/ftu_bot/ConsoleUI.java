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
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.foxtrotfanatics.ftu_bot.structures.Database;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.RequestBuffer;

/**
 * @author Christian
 */
public class ConsoleUI extends Thread
{

	private static Logger logger = LogManager.getLogger();
	private Scanner i = new Scanner(System.in);
	private ClientManager manager;
	private IDiscordClient api;
	private IChannel adminChannel;
	private Database base;
	private boolean running = true;

	/**
	 * 
	 */
	public ConsoleUI(Database b, ClientManager m, IDiscordClient a, IChannel c)
	{
		base = b;
		manager = m;
		api = a;
		adminChannel = c;
		adminChannel.sendMessage("Connected To Server, Ready for Clients");
	}

	public void run()
	{
		Thread.currentThread().setName("Console");
		logger.info("Server is now Running, Shutdown Server with \"Stop\"");
		while (running)
		{
			String command = i.nextLine();
			if (command.equalsIgnoreCase("Stop"))
			{
				logger.info("Stopping Server");
				manager.shutDown();
				RequestBuffer.request(() -> {
					adminChannel.sendMessage("Shutting Down from Console");
				}).get();
				api.logout();
				logger.info("Server Closed");
				running = false;
			}
			else if (command.equalsIgnoreCase("addCredentials"))
			{
				System.out.print("Alias: ");
				String alias = i.nextLine();
				if (alias.indexOf(" ") == -1)
				{
					if (base.retrieveCredentials(alias) == null)
					{
						System.out.print("WebDAV Base Directory: ");
						String path = i.nextLine().replaceAll("\\s", "%20");
						try
						{
							new URL(path);
							System.out.print("Username: ");
							String username = i.nextLine();
							System.out.print("Password: ");
							char[] pass = i.nextLine().toCharArray();
							if (base.createCredentials(alias, path, username, pass))
							{
								System.out.println("Credentials Creation Completed");
								RequestBuffer.request(() -> {
									adminChannel.sendMessage("New Credentials Created > " + alias);
								}).get();
							}
							else
							{
								System.out.println("Failed to install Credentials!");
							}
						}
						catch (MalformedURLException e)
						{
							System.out.println("Bad URL! Exiting Credential inclusion Procedure");
						}
					}
					else
					{
						System.out.println("Alias already used! Exiting Credential inclusion Procedure");
					}
				}
				else
				{
					System.out.println("Alias contains spaces! Exiting Credential inclusion Procedure");
				}
			}
			else
				logger.error("Not A Valid Command");

		}
	}

}
