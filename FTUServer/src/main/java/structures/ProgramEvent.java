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
package structures;

import java.time.LocalDateTime;

/**
 * @author Christian77777
 *
 */
public class ProgramEvent implements java.io.Serializable, Comparable<ProgramEvent>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3189250528254253648L;
	private LocalDateTime time;
	private int type;
	private String message;
	
	public ProgramEvent(int t, String m)
	{
		type = t;
		message = m;
		time = LocalDateTime.now();
	}

	/**
	 * @return the time
	 */
	public LocalDateTime getTime() {
		return time;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public int compareTo(ProgramEvent r)
	{
		return time.compareTo(r.getTime());
	}
	
	
}
