package com.misty.graphics;

import com.misty.kernel.Process;

public class Camera
{
	public float x = 0;
	public float y = 0;
	public int width = 0;
	public int height = 0;
	
	public Camera()
	{
	}
	
	public void setSize(ScreenResolution screenResolution)
	{
		this.width = screenResolution.horizontal;
		this.height = screenResolution.vertical;
	}
	
	public boolean isInside(Process process)
	{
		float right = this.x + this.width;
		float top = this.y + this.height;

		return (!((right < process.x) || (top < process.y) || ((process.x + process.width) < this.x) || ((process.y + process.height) < this.y)));
	}
}