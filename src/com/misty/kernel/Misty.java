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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);

		this.engine = new Engine(this);

		GLSurfaceView screen = new GLSurfaceView(this);
		screen.setEGLContextClientVersion(2);
		Renderer renderer = new Renderer(this, this.engine, screen, getResolution());
		screen.setRenderer(renderer);
		screen.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		screen.setOnTouchListener(this);
		setContentView(screen);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		start();
	}

	public abstract ScreenResolution getResolution();

	public abstract void start();

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

		if (this.engine != null)
		{
			this.engine.resume();
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