package com.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import com.kernel.Process;

public class Collision
{
	public static boolean hit(Process processA, Process processB)
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
					
					int colorA = bitmapA.getPixel(realX - xA, realY - yA);
					int colorB = bitmapB.getPixel(realX - xB, realY - yB);
					
					result = (Color.red(colorA) != 0) || (Color.green(colorA) != 0) || ((Color.blue(colorA) != 0) && (Color.red(colorB) != 0)) || (Color.green(colorB) != 0) || (Color.blue(colorB) != 0);
				}
			}
		}
		
		return result;
	}
}