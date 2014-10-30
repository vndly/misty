package com.misty.kernel;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import com.misty.audio.AudioManager;
import com.misty.debug.TimeCounter;
import com.misty.graphics.Camera;
import com.misty.graphics.CollisionGrid;
import com.misty.graphics.Renderer;
import com.misty.graphics.ScreenResolution;
import com.misty.graphics.textures.TextureManager;
import com.misty.input.TouchEvent;

public class Engine
{
	private static Engine instance;
	
	// processes
	private int nextProcessId = 1;
	private final SparseArray<Process> processesStatic = new SparseArray<Process>();
	private final SparseArray<Process> processesDynamic = new SparseArray<Process>();
	private final SparseArray<Process> processesCollisionable = new SparseArray<Process>();
	private final List<Process> newProcesses = new ArrayList<Process>();
	private final List<Process> removedProcesses = new ArrayList<Process>();
	
	// collision
	private final CollisionGrid collisionGrid = new CollisionGrid();
	
	// video
	public final Camera camera;
	private ScreenResolution resolution;
	private Renderer renderer;
	
	// audio
	private final AudioManager audioManager;
	
	// input
	private static final int TOUCH_EVENT_SIZE = 5;
	private final Object touchLock = new Object();
	private final TouchEvent[] touchEvents = new TouchEvent[Engine.TOUCH_EVENT_SIZE];
	
	public Engine(Context context)
	{
		Engine.instance = this;
		
		this.camera = new Camera();
		this.audioManager = new AudioManager(context);
		
		for (int i = 0; i < Engine.TOUCH_EVENT_SIZE; i++)
		{
			this.touchEvents[i] = new TouchEvent();
		}
		
		TextureManager.initialize(context);
	}
	
	public void setRenderer(Renderer renderer, ScreenResolution resolution)
	{
		this.renderer = renderer;
		this.resolution = resolution;
	}
	
	public static Engine getInstance()
	{
		return Engine.instance;
	}
	
	// ============================ INPUT =========================== \\
	
	public void onTouch(MotionEvent event)
	{
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		
		if (pointerId < Engine.TOUCH_EVENT_SIZE)
		{
			switch (event.getActionMasked())
			{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					updateTouchEvent(pointerId, event.getX(pointerIndex), event.getY(pointerIndex));
					break;
				case MotionEvent.ACTION_MOVE:
					updateTouchEvent(pointerId, event.getX(pointerIndex), event.getY(pointerIndex));
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					updateTouchEvent(pointerId);
					break;
			}
		}
	}
	
	private void updateTouchEvent(int index, float x, float y)
	{
		int screenWidth = this.renderer.width;
		int screenHeight = this.renderer.height;
		
		synchronized (this.touchLock)
		{
			TouchEvent touchEvent = this.touchEvents[index];
			touchEvent.pressed = true;
			touchEvent.x = (int)(x * this.resolution.horizontal) / screenWidth;
			touchEvent.y = (int)((screenHeight - y) * this.resolution.vertical) / screenHeight;
		}
	}
	
	private void updateTouchEvent(int index)
	{
		synchronized (this.touchLock)
		{
			TouchEvent touchEvent = this.touchEvents[index];
			touchEvent.pressed = false;
		}
	}
	
	public List<TouchEvent> getTouchEvents()
	{
		List<TouchEvent> result = new ArrayList<TouchEvent>();
		
		synchronized (this.touchLock)
		{
			for (int i = 0; i < Engine.TOUCH_EVENT_SIZE; i++)
			{
				TouchEvent touchEvent = this.touchEvents[i];
				
				if (touchEvent.pressed)
				{
					result.add(touchEvent);
				}
			}
		}
		
		return result;
	}
	
	public boolean isPressed(int left, int right, int bottom, int top)
	{
		boolean result = false;
		
		synchronized (this.touchLock)
		{
			for (int i = 0; i < Engine.TOUCH_EVENT_SIZE; i++)
			{
				TouchEvent touchEvent = this.touchEvents[i];
				
				if ((touchEvent.pressed) && (touchEvent.x >= left) && (touchEvent.x <= right) && (touchEvent.y >= bottom) && (touchEvent.y <= top))
				{
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	// ========================== AUDIO ========================== \\
	
	public void playSound(String soundPath)
	{
		this.audioManager.playSound(soundPath);
	}
	
	public void playMusic(String musicPath)
	{
		this.audioManager.playAudio(musicPath);
	}
	
	public void stopMusic()
	{
		this.audioManager.stopAudio();
	}
	
	public void pauseAudio()
	{
		this.audioManager.pauseAudio();
	}
	
	public void resumeAudio()
	{
		this.audioManager.resumeAudio();
	}
	
	public void stopAudio()
	{
		this.audioManager.stopAudio();
	}
	
	// ============================= RESOLUTION =========================== \\

	public int getResolutionX()
	{
		return this.renderer.getResolutionX();
	}

	public int getResolutionY()
	{
		return this.renderer.getResolutionY();
	}
	
	// ======================== LIFE CYCLE ====================== \\
	
	public void pause(boolean finishing)
	{
		pauseAudio();
		this.renderer.pause(finishing);
	}
	
	public void resume()
	{
		resumeAudio();
	}
	
	public void stop()
	{
		stopAudio();
	}
	
	// ======================== PROCESSES ======================= \\
	
	public int addProcess(Process process)
	{
		this.newProcesses.add(process);
		
		return this.nextProcessId++;
	}
	
	public void removeProcess(Process process)
	{
		this.removedProcesses.add(process);
	}
	
	private final boolean timersEnabled = false;
	private final TimeCounter logCollisions = new TimeCounter("COLLISIONS: ", this.timersEnabled);
	private final TimeCounter logProcesses = new TimeCounter("PROCESSES:  ", this.timersEnabled);
	private final TimeCounter logRender = new TimeCounter("RENDER:     ", this.timersEnabled);
	private final TimeCounter logUpdateTotal = new TimeCounter("UPDATE:     ", this.timersEnabled);
	
	public void update(float delta, Renderer renderer)
	{
		this.logUpdateTotal.start();
		
		this.logCollisions.start();
		updateCollisions();
		this.logCollisions.stop();
		
		this.logProcesses.start();
		updateProcesses(delta);
		updateProcessesLists();
		this.logProcesses.stop();
		
		render(renderer);
		
		this.logUpdateTotal.stop();

		if (this.timersEnabled)
		{
			Log.e("DEBUG", "=====================================");
		}
	}
	
	private void updateProcessesLists()
	{
		addNewProcesses();
		removeProcesses();
	}
	
	private void addNewProcesses()
	{
		int numberNewProcesses = this.newProcesses.size();
		
		if (numberNewProcesses > 0)
		{
			for (int i = 0; i < numberNewProcesses; i++)
			{
				Process process = this.newProcesses.get(i);
				
				if (process.isDynamic)
				{
					this.processesDynamic.put(process.id, process);
				}
				else
				{
					this.processesStatic.put(process.id, process);
				}
				
				if (process.isCollisionable)
				{
					this.processesCollisionable.put(process.id, process);
				}
			}
			
			this.newProcesses.clear();
		}
	}
	
	private void removeProcesses()
	{
		int numberRemovedProcesses = this.removedProcesses.size();
		
		if (numberRemovedProcesses > 0)
		{
			for (int i = 0; i < numberRemovedProcesses; i++)
			{
				Process process = this.removedProcesses.get(i);
				
				if (process.isDynamic)
				{
					this.processesDynamic.remove(process.id);
				}
				else
				{
					this.processesStatic.remove(process.id);
				}
				
				if (process.isCollisionable)
				{
					this.processesCollisionable.remove(process.id);
				}
			}
			
			this.removedProcesses.clear();
		}
	}
	
	private void updateProcesses(float delta)
	{
		int size = this.processesDynamic.size();
		
		for (int i = 0; i < size; i++)
		{
			this.processesDynamic.valueAt(i).process(delta);
		}
	}
	
	private void updateCollisions()
	{
		this.collisionGrid.clear();
		
		int size = this.processesCollisionable.size();
		
		for (int i = 0; i < size; i++)
		{
			this.collisionGrid.addProcess(this.processesCollisionable.valueAt(i));
		}
	}
	
	public List<Process> getCollisions(Process process, Class<?>... classes)
	{
		return this.collisionGrid.getCollisions(process, classes);
	}
	
	private void render(Renderer renderer)
	{
		renderer.clearScreen(this.camera);
		
		this.logRender.start();
		SparseArray<List<Process>> processesToRender = new SparseArray<List<Process>>();
		int numberOfProcess = 0;
		
		numberOfProcess += addProcessesToRender(this.processesDynamic, processesToRender);
		numberOfProcess += addProcessesToRender(this.processesStatic, processesToRender);
		
		renderProcesses(processesToRender, numberOfProcess, renderer);
		this.logRender.stop();
	}
	
	private int addProcessesToRender(SparseArray<Process> list, SparseArray<List<Process>> processesToRender)
	{
		int result = 0;
		
		int size = list.size();
		
		float cameraRight = this.camera.x + this.resolution.horizontal;
		float cameraTop = this.camera.y + this.resolution.vertical;
		
		for (int i = 0; i < size; i++)
		{
			Process process = list.valueAt(i);
			
			if ((!(cameraRight < process.x)) && (!(cameraTop < process.y)) && (!((process.x + process.width) < this.camera.x)) && (!((process.y + process.height) < this.camera.y)))
			{
				List<Process> listOfProcesses = processesToRender.get(process.z);
				
				if (listOfProcesses == null)
				{
					listOfProcesses = new ArrayList<Process>();
					listOfProcesses.add(process);
					processesToRender.put(process.z, listOfProcesses);
				}
				else
				{
					listOfProcesses.add(process);
				}
				
				result++;
			}
		}
		
		return result;
	}
	
	private void renderProcesses(SparseArray<List<Process>> processesToRender, int numberOfProcess, Renderer renderer)
	{
		int zValue = 0;
		int rendered = 0;
		
		while (rendered < numberOfProcess)
		{
			List<Process> listOfProcesses = processesToRender.get(zValue);
			
			if (listOfProcesses != null)
			{
				int listSize = listOfProcesses.size();
				rendered += listSize;
				
				for (int i = 0; i < listSize; i++)
				{
					listOfProcesses.get(i).render(renderer);
				}
			}
			
			zValue++;
		}
	}
}