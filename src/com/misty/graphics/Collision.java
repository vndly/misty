package com.misty.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

public class Collision
{
	public static boolean hit(com.misty.kernel.Process processA, com.misty.kernel.Process processB)
	{
		boolean result = false;
		
		Image imageA = processA.getImage();
		int xA = processA.getX();
		int yA = processA.getY();
		
		Image imageB = processB.getImage();
		int xB = processB.getX();
		int yB = processB.getY();
		
		Rect rectA = new Rect(xA, yA, imageA.getWidth() + xA, imageA.getHeight() + yA);
		Rect rectB = new Rect(xB, yB, imageB.getWidth() + xB, imageB.getHeight() + yB);
		
		if (rectA.intersect(rectB))
		{
			Bitmap bitmapA = imageA.get();
			Bitmap bitmapB = imageB.get();
			
			int intersectionWidth = rectA.width();
			int intersectionHeight = rectA.height();
			
			for (int x = 0; ((x < intersectionWidth) && (!result)); x++)
			{
				for (int y = 0; (y < intersectionHeight) && (!result); y++)
				{
					int realX = x + rectA.left;
					int realY = y + rectA.top;
					
					result = (Color.alpha(bitmapA.getPixel(realX - xA, realY - yA)) > 0) && (Color.alpha(bitmapB.getPixel(realX - xB, realY - yB)) > 0);
				}
			}
		}
		
		return result;
	}
}