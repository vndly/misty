package com.misty.graphics;

public class Animation
{
	private float totalTime = 0;
	private final int[] sprites;
	private final float frameDuration;
	
	public Animation(float frameDuration, int... sprites)
	{
		this.frameDuration = frameDuration;
		this.sprites = sprites;
	}
	
	public int getSprite(float delta)
	{
		this.totalTime += delta;
		
		int id = (int)(this.totalTime / this.frameDuration);
		
		return this.sprites[id % this.sprites.length];
	}
}