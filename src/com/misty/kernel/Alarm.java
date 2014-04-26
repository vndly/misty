package com.misty.kernel;

public class Alarm
{
	public final int id;
	private final OnAlarmRing listener;
	private final long time;
	private float total = 0;
	
	public Alarm(int id, OnAlarmRing listener, long time)
	{
		this.id = id;
		this.listener = listener;
		this.time = time;
		this.total = 0;
	}
	
	public boolean step(float delta)
	{
		boolean remove = false;
		this.total += (delta * 1000f);
		
		if (this.total >= this.time)
		{
			remove = (!this.listener.onAlarmRing());
			this.total -= this.time;
		}
		
		return remove;
	}
	
	public interface OnAlarmRing
	{
		public boolean onAlarmRing();
	}
}