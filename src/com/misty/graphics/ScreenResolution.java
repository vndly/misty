package com.misty.graphics;

public class ScreenResolution
{
	public int horizontal = 0;
	public int vertical = 0;
	
	public ScreenResolution(int horizontal, int vertical)
	{
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	public static ScreenResolution fromHorizontal(int horizontal)
	{
		return new ScreenResolution(horizontal, 0);
	}

	public static ScreenResolution fromVertical(int vertical)
	{
		return new ScreenResolution(0, vertical);
	}
	
	public void normalize(int width, int height)
	{
		// TODO
		if (this.horizontal == 0)
		{
			this.horizontal = (int)(this.vertical / ((float)width / (float)height));
		}
		else if (this.vertical == 0)
		{
			this.vertical = (int)(this.horizontal / ((float)width / (float)height));
		}
	}
}