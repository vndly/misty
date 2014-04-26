package com.misty.kernel;

import android.app.Activity;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import com.misty.graphics.Renderer;
import com.misty.graphics.ScreenResolution;

public abstract class Misty extends Activity implements OnTouchListener
{
	private Engine engine;
	private Renderer renderer;
	private GLSurfaceView screen;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		super.onCreate(savedInstanceState);
		
		this.engine = new Engine(this);
		
		this.screen = new GLSurfaceView(this);
		this.renderer = new Renderer(this.engine, getResolution());
		this.screen.setRenderer(this.renderer);
		this.screen.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		this.screen.setOnTouchListener(this);
		setContentView(this.screen);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		init();
	}
	
	public abstract ScreenResolution getResolution();
	
	public abstract void init();
	
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		this.engine.onTouch(event);
		
		return true;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (this.renderer != null)
		{
			this.renderer.onResume();
		}
		
		if (this.screen != null)
		{
			this.screen.onResume();
		}
	}
	
	@Override
	protected void onPause()
	{
		if (this.renderer != null)
		{
			this.renderer.onPause(isFinishing());
		}
		
		if (this.screen != null)
		{
			this.screen.onPause();
		}
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		if (this.renderer != null)
		{
			this.renderer.onDestroy();
		}
	}
}