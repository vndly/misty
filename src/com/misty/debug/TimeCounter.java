package com.misty.debug;


public class TimeCounter
{
	private long totalTime = 0;
	private int times = 0;
	private long start = 0;
	private final String name;

	public TimeCounter(String name)
	{
		this.name = name;
	}

	public void start()
	{
		this.start = System.nanoTime();
	}

	public void stop()
	{
		this.totalTime += (System.nanoTime() - this.start) / 1000;
		this.times++;
		// Log.e("DEBUG", this.name + (this.totalTime / this.times) + " us");
	}
}