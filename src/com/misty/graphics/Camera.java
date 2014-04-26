package com.misty.graphics;

public class Camera
{
	public float x = 0;
	public float y = 0;
	public final int width;
	public final int height;
	
	public Camera(ScreenResolution resolution)
	{
		this.width = resolution.horizontal;
		this.height = resolution.vertical;
	}
}