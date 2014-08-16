package com.misty.kernel;

import java.util.List;
import java.util.Random;
import javax.microedition.khronos.opengles.GL10;
import android.util.SparseArray;
import com.misty.graphics.Camera;
import com.misty.graphics.Texture;
import com.misty.input.TouchEvent;
import com.misty.kernel.Alarm.OnAlarmRing;

public class Process
{
	public int id;
	private final Engine engine;
	private State state = State.AWAKE;
	public final boolean isDynamic;
	public final boolean isCollisionable;
	public final Camera camera;

	// states
	public enum State
	{
		AWAKE, FROZEN, SLEEPING
	}

	// sprite
	public Texture sprite;
	public boolean visible = true;

	// position
	public float x = 0;
	public float y = 0;
	public int z = 0;

	// size
	public int width = 0;
	public int height = 0;

	// alarm
	private int nextAlarmId = 1;
	private final SparseArray<Alarm> alarms = new SparseArray<Alarm>();

	public Process(boolean isDynamic, boolean isCollisionable)
	{
		this.isDynamic = isDynamic;
		this.isCollisionable = isCollisionable;

		this.engine = Engine.getInstance();
		this.camera = this.engine.camera;
	}

	public void start()
	{
		this.id = this.engine.addProcess(this);
	}

	public void finish()
	{
		this.engine.removeProcess(this);
		this.alarms.clear();
	}

	public final void process(float delta)
	{
		int size = this.alarms.size();

		if (size > 0)
		{
			for (int i = 0; i < size; i++)
			{
				Alarm alarm = this.alarms.valueAt(i);

				if (alarm.step(delta))
				{
					this.alarms.remove(alarm.id);
				}
			}
		}

		if (isAwake())
		{
			update(delta);
		}
	}

	@SuppressWarnings("unused")
	public void update(float delta)
	{
	}

	// ============================= SPRITE =========================== \\
	
	public void render(GL10 screen)
	{
		if (hasSprite() && this.visible && (!isSpeeling()))
		{
			this.sprite.render(screen, this.x, this.y);
		}
	}

	public void setSprite(int resourceId)
	{
		if ((this.sprite == null) || (this.sprite.id != resourceId))
		{
			this.sprite = Texture.getTexture(resourceId);
			
			if (this.sprite != null)
			{
				this.width = this.sprite.width;
				this.height = this.sprite.height;
			}
			else
			{
				this.width = 0;
				this.height = 0;
			}
		}
	}

	public void setSprite(String name)
	{
		setSprite(this.engine.getResourceId(name));
	}

	public boolean hasSprite()
	{
		return (this.sprite != null);
	}
	
	// ============================= GEOMETRY =========================== \\

	public float getAngle(Process process)
	{
		return -(float)Math.toDegrees(Math.atan2(process.y - this.y, process.x - this.x));
	}

	public float getDistance(Process process)
	{
		return (float)(Math.sqrt(Math.pow(this.x - process.x, 2) + Math.pow(this.y - process.y, 2)));
	}

	public int random(int min, int max)
	{
		Random random = new Random();

		return random.nextInt(max - min) + min;
	}

	// ============================= STATE =========================== \\

	public boolean isAwake()
	{
		return this.state == State.AWAKE;
	}

	public boolean isFrozen()
	{
		return this.state == State.FROZEN;
	}

	public boolean isSpeeling()
	{
		return this.state == State.SLEEPING;
	}

	public void wakeUp()
	{
		this.visible = true;
		this.state = State.AWAKE;
	}

	public void freeze()
	{
		this.visible = true;
		this.state = State.FROZEN;
	}

	public void sleep()
	{
		this.visible = false;
		this.state = State.SLEEPING;
	}

	// ============================= ALARMS =========================== \\

	public int setAlarm(OnAlarmRing listener, int milliseconds)
	{
		int id = this.nextAlarmId++;

		this.alarms.put(id, new Alarm(id, listener, milliseconds));

		return id;
	}

	// ============================= COLLISION =========================== \\

	public List<Process> getCollisions(Class<?> classes)
	{
		return this.engine.getCollisions(this, classes);
	}

	// ============================= SCREEN =========================== \\

	public int getScreenWidth()
	{
		return this.engine.getScreenWidth();
	}

	public int getScreenHeight()
	{
		return this.engine.getScreenHeight();
	}

	// ============================= AUDIO =========================== \\

	public void playSound(int soundId)
	{
		this.engine.playSound(soundId);
	}

	public void playMusic(int musicId)
	{
		this.engine.playMusic(musicId);
	}

	public void stopMusic()
	{
		this.engine.stopMusic();
	}

	// ============================= INPUT ============================= \\

	public List<TouchEvent> getTouchEvents()
	{
		return this.engine.getTouchEvents();
	}

	public boolean isPressed(int left, int right, int bottom, int top)
	{
		return this.engine.isPressed(left, right, bottom, top);
	}
}