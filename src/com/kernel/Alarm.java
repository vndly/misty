package com.kernel;

public class Alarm
{
	private final int id;
	private final OnAlarmRing listener;
	private final long delay;
	private long limit;
	
	public Alarm(int id, OnAlarmRing listener, long delay)
	{
		this.id = id;
		this.listener = listener;
		this.delay = delay;
		this.limit = System.currentTimeMillis() + delay;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public boolean step()
	{
		boolean remove = false;
		long current = System.currentTimeMillis();
		
		if (current >= this.limit)
		{
			remove = (!this.listener.onAlarmRing());
			this.limit = current + this.delay;
		}
		
		return remove;
	}
	
	public interface OnAlarmRing
	{
		public boolean onAlarmRing();
	}
}