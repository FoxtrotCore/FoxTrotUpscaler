package com.foxtrotfanatics.ftu.structures;

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
