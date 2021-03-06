package com.misty.kernel;

import java.util.List;
import android.util.SparseArray;
import com.misty.graphics.Camera;
import com.misty.graphics.Renderer;
import com.misty.graphics.textures.Texture;
import com.misty.graphics.textures.TextureManager;
import com.misty.input.TouchEvent;
import com.misty.kernel.Alarm.OnAlarmRing;
import com.misty.math.Rectangle;

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
	
	// texture
	public Texture texture;
	public boolean visible = true;
	public boolean fixedPosition = false;
	
	// position
	public float x = 0;
	public float y = 0;
	public int z = 0;
	
	// angle
	public float angle = 0;
	
	// orientation
	public int orientationHorizontal = 1;
	public int orientationVertical = 1;
	
	// scale
	public float scaleX = 1;
	public float scaleY = 1;
	
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
	
	public final void start()
	{
		this.id = this.engine.addProcess(this);
	}
	
	public final void finish()
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
	
	// ============================= TEXTURE =========================== \\
	
	public final void render(Renderer renderer)
	{
		if (hasImage() && this.visible && (!isSpeeling()))
		{
			if (this.fixedPosition)
			{
				renderer.render(this.texture, this.x + this.camera.x, this.y + this.camera.y, this.scaleX, this.scaleY, this.angle, this.orientationHorizontal, this.orientationVertical);
			}
			else
			{
				renderer.render(this.texture, this.x, this.y, this.scaleX, this.scaleY, this.angle, this.orientationHorizontal, this.orientationVertical);
			}
		}
	}
	
	public final void setImage(String texturePath)
	{
		if (texturePath != null)
		{
			if ((this.texture == null) || (!texturePath.equals(this.texture.path)))
			{
				this.texture = TextureManager.getTexture(texturePath);
				
				if (this.texture != null)
				{
					this.width = this.texture.width;
					this.height = this.texture.height;
				}
				else
				{
					this.width = 0;
					this.height = 0;
				}
			}
		}
	}
	
	public final boolean hasImage()
	{
		return (this.texture != null);
	}
	
	public final Rectangle getInternalBounds()
	{
		float deltaX = (this.texture == null) ? 0 : this.texture.bounds.x;
		float deltaY = (this.texture == null) ? 0 : this.texture.bounds.y;
		float boundWidth = (this.texture == null) ? this.width : this.texture.bounds.width;
		float boundHeight = (this.texture == null) ? this.width : this.texture.bounds.height;
		
		return new Rectangle(this.x + deltaX, this.y + deltaY, boundWidth, boundHeight);
	}
	
	public final Rectangle getExternalBounds()
	{
		return new Rectangle(this.x, this.y, this.width, this.height);
	}
	
	// ============================= GEOMETRY =========================== \\
	
	public final float getAngle(Process process)
	{
		return -(float)Math.toDegrees(Math.atan2(process.y - this.y, process.x - this.x));
	}
	
	public final float getDistance(Process process)
	{
		return (float)(Math.sqrt(Math.pow(this.x - process.x, 2) + Math.pow(this.y - process.y, 2)));
	}
	
	// ============================= RESOLUTION =========================== \\
	
	public final int getResolutionX()
	{
		return this.engine.getResolutionX();
	}
	
	public final int getResolutionY()
	{
		return this.engine.getResolutionY();
	}
	
	// ============================= STATE =========================== \\
	
	public final boolean isAwake()
	{
		return this.state == State.AWAKE;
	}
	
	public final boolean isFrozen()
	{
		return this.state == State.FROZEN;
	}
	
	public final boolean isSpeeling()
	{
		return this.state == State.SLEEPING;
	}
	
	public final void wakeUp()
	{
		this.visible = true;
		this.state = State.AWAKE;
	}
	
	public final void freeze()
	{
		this.visible = true;
		this.state = State.FROZEN;
	}
	
	public final void sleep()
	{
		this.visible = false;
		this.state = State.SLEEPING;
	}
	
	// ============================= ALARMS =========================== \\
	
	public final int setAlarm(OnAlarmRing listener, int milliseconds)
	{
		int id = this.nextAlarmId++;
		
		this.alarms.put(id, new Alarm(id, listener, milliseconds));
		
		return id;
	}
	
	public final void removeAlarms()
	{
		this.alarms.clear();
	}
	
	// ============================= COLLISION =========================== \\
	
	public final List<Process> getCollisions(Class<?>... classes)
	{
		return this.engine.getCollisions(this, classes);
	}
	
	// ============================= AUDIO =========================== \\
	
	public final void playSound(String soundPath)
	{
		this.engine.playSound(soundPath);
	}
	
	public final void playMusic(String musicPath)
	{
		this.engine.playMusic(musicPath);
	}
	
	public final void stopMusic()
	{
		this.engine.stopMusic();
	}
	
	// ============================= INPUT ============================= \\
	
	public final List<TouchEvent> getTouchEvents()
	{
		return this.engine.getTouchEvents();
	}
	
	public final boolean isPressed(float left, float right, float bottom, float top)
	{
		return this.engine.isPressed(left, right, bottom, top);
	}
	
	public final boolean isPressed()
	{
		return this.engine.isPressed(this.x, this.x + this.width, this.y, this.y + this.height);
	}
}