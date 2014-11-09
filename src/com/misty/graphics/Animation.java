package com.misty.graphics;

public class Animation
{
	private float totalTime = 0;
	private final String[] sprites;
	private final float frameDuration;
	private final boolean loop;
	
	public Animation(boolean loop, float frameDuration, String... sprites)
	{
		this.loop = loop;
		this.frameDuration = frameDuration;
		this.sprites = sprites;
	}
	
	public Animation(float frameDuration, String... sprites)
	{
		this(true, frameDuration, sprites);
	}
	
	public void reset()
	{
		this.totalTime = 0;
	}
	
	public String getSprite(float delta)
	{
		this.totalTime += delta;
		
		int index = (int)(this.totalTime / this.frameDuration);
		
		if ((!this.loop) && (index >= this.sprites.length))
		{
			return this.sprites[this.sprites.length - 1];
		}
		else
		{
			return this.sprites[index % this.sprites.length];
		}
	}
}