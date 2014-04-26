package com.misty.graphics;

import javax.microedition.khronos.opengles.GL10;

public class Sprite
{
	public final int id;
	public final Texture texture;
	
	public Sprite(int resourceId)
	{
		this.id = resourceId;
		this.texture = Texture.getTexture(resourceId);
	}
	
	public void render(GL10 screen, float x, float y)
	{
		this.texture.render(screen, x, y);
	}
}