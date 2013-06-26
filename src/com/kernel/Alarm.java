package com.kernel;

public class Alarm
{
	private final int id;
	private final OnAlarmRing listener;
	private final long delay;
	private long limit;
	private final boolean loop;
	
	public Alarm(int id, OnAlarmRing listener, long delay, boolean loop)
	{
		this.id = id;
		this.listener = listener;
		this.delay = delay;
		this.limit = System.currentTimeMillis() + delay;
		this.loop = loop;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public boolean step()
	{
		boolean ring = false;
		long current = System.currentTimeMillis();
		
		if (current >= this.limit)
		{
			this.listener.onAlarmRing();
			ring = true;
		}
		
		if (ring && (this.loop))
		{
			this.limit = current + this.delay;
		}
		
		return (ring && (!this.loop));
	}
	
	public interface OnAlarmRing
	{
		public void onAlarmRing();
	}
}