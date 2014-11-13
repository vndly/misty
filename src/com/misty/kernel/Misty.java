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
	private GLSurfaceView screen;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		super.onCreate(savedInstanceState);
		
		this.engine = new Engine(this);
		
		this.screen = new GLSurfaceView(this);
		this.screen.setEGLContextClientVersion(2);
		Renderer renderer = new Renderer(this, this.engine, getResolution());
		this.screen.setRenderer(renderer);
		this.screen.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		this.screen.setOnTouchListener(this);
		setContentView(this.screen);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	public abstract ScreenResolution getResolution();
	
	public abstract void start();
	
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		if (this.engine != null)
		{
			this.engine.onTouch(event);
		}
		
		return true;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (this.engine != null)
		{
			this.engine.resume();
		}
		
		if (this.screen != null)
		{
			this.screen.onResume();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if (this.engine != null)
		{
			this.engine.pause(isFinishing());
		}
		
		if (this.screen != null)
		{
			this.screen.onPause();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		if (this.engine != null)
		{
			this.engine.stop();
		}
		
		super.onDestroy();
	}
}