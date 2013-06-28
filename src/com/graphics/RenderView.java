package com.graphics;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.misty.Misty;

public class RenderView extends SurfaceView implements SurfaceHolder.Callback
{
	private final Misty misty;
	
	public RenderView(Context context)
	{
		super(context);
		this.misty = (Misty)context;
		getHolder().addCallback(this);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		this.misty.startEngine();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}
}