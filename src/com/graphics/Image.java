package com.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Image
{
	private final int imageId;
	private Bitmap image;
	private int width = 0;
	private int height = 0;
	
	// TODO: ADD METHODS TO SCALE THE IMAGE
	
	public Image(Resources resources, int imageId)
	{
		this.imageId = imageId;
		this.image = BitmapFactory.decodeResource(resources, imageId);
		updateDimension();
	}
	
	public Bitmap get()
	{
		return this.image;
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	private void updateDimension()
	{
		this.width = this.image.getWidth();
		this.height = this.image.getHeight();
	}
	
	public int getResourceId()
	{
		return this.imageId;
	}
	
	public void rotate(float angle)
	{
		Matrix matrix = new Matrix();
		matrix.setRotate(angle, this.image.getWidth() / 2, this.image.getHeight() / 2);
		this.image = Bitmap.createBitmap(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), matrix, true);
		updateDimension();
	}
}