package com.text;

import android.graphics.Paint;
import android.graphics.Typeface;

public class Font extends Paint
{
	public Font(int size, int color, Align align, boolean bold)
	{
		setColor(color);
		setAntiAlias(true);
		setDither(true);
		setTextSize(size);
		setTextAlign(align);
		
		if (bold)
		{
			setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		}
	}
}