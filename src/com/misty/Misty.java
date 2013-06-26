package com.misty;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.kernel.Engine;
import com.kernel.IFramework;
import com.text.Text;

public abstract class Misty extends Activity implements IFramework
{
	private Engine engine;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		init();
	}
	
	public abstract void init();
	
	public void start(int fps, Class<?> soundClass)
	{
		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
		ImageView background = (ImageView)findViewById(R.id.background);
		SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		
		this.engine = new Engine(fps, this, soundClass, surfaceView, background, sensorManager);
		this.engine.start();
	}
	
	@Override
	protected void onDestroy()
	{
		if (this.engine != null)
		{
			this.engine.finishThread();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		if (this.engine != null)
		{
			this.engine.pauseThread();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume()
	{
		if (this.engine != null)
		{
			this.engine.resumeThread();
		}
		super.onResume();
	}
	
	@Override
	public int random(int min, int max)
	{
		return this.engine.random(min, max);
	}
	
	@Override
	public void setBackgroundImage(int resourceId)
	{
		this.engine.setBackgroundImage(resourceId);
	}
	
	@Override
	public void removeBackgroundImage()
	{
		this.engine.removeBackgroundImage();
	}
	
	@Override
	public int getScreenWidth()
	{
		return this.engine.getScreenWidth();
	}
	
	@Override
	public int getScreenHeight()
	{
		return this.engine.getScreenHeight();
	}
	
	@Override
	public void playSound(int soundId)
	{
		this.engine.playSound(soundId);
	}
	
	@Override
	public void playMusic(int musicId)
	{
		this.engine.playMusic(musicId);
	}
	
	@Override
	public void stopMusic()
	{
		this.engine.stopMusic();
	}
	
	@Override
	public void addText(Text text)
	{
		this.engine.addText(text);
	}
	
	@Override
	public void removeText(Text text)
	{
		this.engine.removeText(text);
	}
}