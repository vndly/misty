package com.misty.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import com.misty.graphics.Collision;
import com.misty.sound.AudioManger;
import com.misty.text.Font;
import com.misty.text.Text;

@SuppressWarnings("deprecation")
public class Engine extends Thread implements IFramework, SensorEventListener
{
	private final int fps;
	private final int time;
	
	private final static int MAX_FRAME_SKIPS = 5;
	
	// engine status
	private enum EngineStatus
	{
		RUNNING,
		PAUSED,
		FINISHED
	};
	
	private EngineStatus running = EngineStatus.RUNNING;
	
	private static Engine instance;
	
	private final Context context;
	private final Resources resources;
	private final SurfaceHolder surfaceHolder;
	
	// sensors
	private final SensorManager sensorManager;
	
	// rotation input
	private final Sensor sensorRotation;
	private float rotationX = 0;
	private float rotationY = 0;
	private float rotationZ = 0;
	
	// touch screen
	private float screenTouchX = Float.NaN;
	private float screenTouchY = Float.NaN;
	
	// screen
	private final int screenWidth;
	private final int screenHeight;
	private final ImageView background;
	private final Paint paintClear;
	
	// processes
	private int lastProcessId = 0;
	private final SparseArray<Process> processes;
	
	// sound
	private final AudioManger soundManager;
	
	// text
	private final List<Text> texts = new ArrayList<Text>();
	
	// debug
	private int totalTime = 0;
	private int times = 1;
	
	public Engine(int fps, Context context, Class<?> soundClass, SurfaceView surfaceView, ImageView background, SensorManager sensorManager)
	{
		Engine.instance = this;
		
		this.paintClear = new Paint();
		this.paintClear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		
		this.fps = fps;
		this.time = 1000 / this.fps;
		
		this.processes = new SparseArray<Process>();
		
		this.soundManager = new AudioManger(context);
		this.soundManager.loadSounds(soundClass);
		
		this.context = context;
		this.resources = context.getResources();
		this.surfaceHolder = surfaceView.getHolder();
		this.background = background;
		this.sensorManager = sensorManager;
		
		surfaceView.setZOrderOnTop(true); // necessary
		this.surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		
		Point size = getScreenSize();
		this.screenWidth = size.x;
		this.screenHeight = size.y;
		
		this.sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		this.sensorManager.registerListener(this, this.sensorRotation, SensorManager.SENSOR_DELAY_GAME);
		
		surfaceView.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					onScreenTouch(event.getX(), event.getY());
				}
				else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					clearTouchScreen();
				}
				return true;
			}
		});
	}
	
	private void onScreenTouch(float x, float y)
	{
		this.screenTouchX = x;
		this.screenTouchY = y;
	}
	
	private boolean touchScreenAvailable()
	{
		return ((!Float.isNaN(this.screenTouchX)) && (!Float.isNaN(this.screenTouchY)));
	}
	
	private void clearTouchScreen()
	{
		this.screenTouchX = Float.NaN;
		this.screenTouchY = Float.NaN;
	}
	
	public static Engine getInstance()
	{
		return Engine.instance;
	}
	
	public Resources getResources()
	{
		return this.resources;
	}
	
	public String getPackageName()
	{
		return this.context.getPackageName();
	}
	
	@Override
	public int random(int min, int max)
	{
		Random r = new Random();
		return r.nextInt(max - min) + min;
	}
	
	// ============================ SCREEN ========================== \\
	
	@Override
	public void addText(Text text)
	{
		this.texts.add(text);
	}
	
	@Override
	public void removeText(Text text)
	{
		this.texts.remove(text);
	}
	
	// ============================ SCREEN ========================== \\
	
	private Point getScreenSize()
	{
		WindowManager wm = (WindowManager)this.context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		return new Point(width, height);
	}
	
	@Override
	public int getScreenWidth()
	{
		return this.screenWidth;
	}
	
	@Override
	public int getScreenHeight()
	{
		return this.screenHeight;
	}
	
	// ======================== BACKGROUND IMAGE ======================= \\
	
	@Override
	public void setBackgroundImage(int resourceId)
	{
		this.background.setImageResource(resourceId);
		this.background.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void removeBackgroundImage()
	{
		this.background.setVisibility(View.GONE);
	}
	
	// ======================== SOUND & MUSIC ======================= \\
	
	@Override
	public void playSound(int soundId)
	{
		this.soundManager.playSound(soundId);
	}
	
	@Override
	public void playMusic(int musicId)
	{
		this.soundManager.playMusic(musicId);
	}
	
	@Override
	public void stopMusic()
	{
		this.soundManager.stopMusic();
	}
	
	// ======================== PROCESSES ======================= \\
	
	public int addProcess(Process process)
	{
		int id = 0;
		
		synchronized (this.processes)
		{
			id = ++this.lastProcessId;
			this.processes.put(id, process);
		}
		
		return id;
	}
	
	public void removeProcess(Process process)
	{
		synchronized (this.processes)
		{
			this.processes.remove(process.getId());
		}
	}
	
	@Override
	public void run()
	{
		this.running = EngineStatus.RUNNING;
		int consumed = 0;
		int framesSkipped = 0;
		
		while (this.running != EngineStatus.FINISHED)
		{
			if (this.running == EngineStatus.RUNNING)
			{
				framesSkipped = 0;
				
				// ~ 20 us
				Process[] list = getProcessList();
				
				consumed = update(list, this.time);
				
				this.totalTime += consumed;
				this.times++;
				
				int sleepTime = this.time - consumed;
				
				if (sleepTime > 0)
				{
					try
					{
						Thread.sleep(sleepTime);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					while ((sleepTime < 0) && (framesSkipped < Engine.MAX_FRAME_SKIPS))
					{
						runProcesses(list, this.time);
						sleepTime += this.time;
						framesSkipped++;
					}
				}
			}
		}
	}
	
	private int update(Process[] list, int time)
	{
		long start = System.nanoTime();
		
		// ~ 600 us
		runProcesses(list, time);
		
		// ~ 16 ms
		drawProcesses(list);
		
		return (int)(System.nanoTime() - start) / 1000000;
	}
	
	private void runProcesses(Process[] list, int time)
	{
		// ~ 400 us
		updateProcesses(list, time);
		
		// ~ 200 us
		updateCollisions(list);
	}
	
	private Process[] getProcessList()
	{
		int size = this.processes.size();
		Process[] list = new Process[size];
		for (int i = 0; i < size; i++)
		{
			list[i] = this.processes.valueAt(i);
		}
		
		return list;
	}
	
	private void updateProcesses(Process[] list, int time)
	{
		for (Process process : list)
		{
			updateProcessInput(process);
			process.process(time);
		}
	}
	
	private void updateProcessInput(Process process)
	{
		if (process.hasRotationListener() && process.isAwake())
		{
			process.getOnRotationChange().onRotationChange(this.rotationX, this.rotationY, this.rotationZ);
		}
		
		if (process.hasTouchScreenListener() && touchScreenAvailable() && process.isAwake())
		{
			process.getOnTouchScreen().onTouchScreen(this.screenTouchX, this.screenTouchY);
		}
	}
	
	private void updateCollisions(Process[] list)
	{
		for (int i = 0; (i < (list.length - 1)); i++)
		{
			Process processA = list[i];
			
			for (int j = (i + 1); j < list.length; j++)
			{
				Process processB = list[j];
				
				if (processA.isCollisionable() && processB.isCollisionable())
				{
					if (Collision.hit(processA, processB))
					{
						processA.onCollision(processB);
						processB.onCollision(processA);
					}
				}
			}
		}
	}
	
	private void drawProcesses(Process[] list)
	{
		Canvas canvas = null;
		try
		{
			// ~ 11 ms
			canvas = this.surfaceHolder.lockCanvas();
			
			synchronized (this.surfaceHolder)
			{
				if (canvas != null)
				{
					// ~ 2 ms
					canvas.drawPaint(this.paintClear);
					
					for (Process process : list)
					{
						if (process.hasImage() && process.isVisible() && (!process.isSpeeling()))
						{
							canvas.drawBitmap(process.getBitmap(), process.getX(), process.getY(), null);
						}
					}
					
					for (Text text : this.texts)
					{
						canvas.drawText(text.getText(), text.getX(), text.getY(), text.getFont());
					}
					
					canvas.drawText("PROCESSES: " + list.length, getScreenWidth() - 10, 20, new Font(20, Color.GREEN, Align.RIGHT, true));
					canvas.drawText("TIME: " + (this.totalTime / this.times) + " ms", getScreenWidth() - 10, 45, new Font(20, Color.GREEN, Align.RIGHT, true));
				}
			}
		}
		finally
		{
			if (canvas != null)
			{
				// ~ 3 ms
				this.surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	public void pauseThread()
	{
		this.soundManager.pauseMusic();
		this.sensorManager.unregisterListener(this, this.sensorRotation);
		this.running = EngineStatus.PAUSED;
	}
	
	public void resumeThread()
	{
		this.soundManager.resumeMusic();
		this.sensorManager.registerListener(this, this.sensorRotation, SensorManager.SENSOR_DELAY_GAME);
		this.running = EngineStatus.RUNNING;
	}
	
	public void finishThread()
	{
		this.soundManager.stopMusic();
		this.sensorManager.unregisterListener(this, this.sensorRotation);
		this.running = EngineStatus.FINISHED;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int value)
	{
	}
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		this.rotationZ = -event.values[0];
		this.rotationY = -event.values[1];
		this.rotationX = -event.values[2];
	}
}