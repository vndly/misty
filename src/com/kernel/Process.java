package com.kernel;

import android.graphics.Bitmap;
import android.util.SparseArray;
import com.graphics.Image;
import com.input.rotation.OnRotationChange;
import com.input.touch.OnTouchScreen;
import com.kernel.Alarm.OnAlarmRing;
import com.text.Text;

public class Process implements IFramework
{
	private int id;
	private final Engine engine;
	
	// states
	enum State
	{
		AWAKE, FROZEN, SLEEPING
	}
	
	private State state;
	
	// input
	private OnRotationChange rotationListener;
	private OnTouchScreen touchScreenListener;
	
	// image
	private Image image;
	private boolean visible = true;
	
	// position
	protected float x = 0;
	protected float minX = Float.NaN;
	protected float maxX = Float.NaN;
	protected float y = 0;
	protected float minY = Float.NaN;
	protected float maxY = Float.NaN;
	
	// velocity
	protected float velocityX = 0;
	protected float velocityY = 0;
	protected float maxVelocityX = Float.NaN;
	protected float maxVelocityY = Float.NaN;
	
	// alarm
	private int lastAlarmId = 0;
	private final SparseArray<Alarm> alarms = new SparseArray<Alarm>();
	
	public Process()
	{
		this.engine = Engine.getInstance();
		this.state = State.AWAKE;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void create()
	{
		this.id = this.engine.addProcess(this);
	}
	
	public void finish()
	{
		this.engine.removeProcess(this);
		this.alarms.clear();
	}
	
	public final void process(int time)
	{
		if (this.alarms.size() > 0)
		{
			for (int i = 0; i < this.alarms.size(); i++)
			{
				Alarm alarm = this.alarms.get(this.alarms.keyAt(i));
				
				if (alarm.step())
				{
					this.alarms.remove(alarm.getId());
				}
			}
		}
		if (isAwake())
		{
			update(time);
		}
	}
	
	public void update(int time)
	{
	}
	
	@Override
	public int random(int min, int max)
	{
		return this.engine.random(min, max);
	}
	
	// ============================= IMAGE =========================== \\
	
	private int getImageResource(String name)
	{
		return this.engine.getResources().getIdentifier(name, "drawable", this.engine.getPackageName());
	}
	
	protected void setImage(int resourceId)
	{
		this.image = new Image(this.engine.getResources(), resourceId);
	}
	
	protected void setImage(int resourceId, boolean visible)
	{
		setImage(resourceId);
		setVisible(visible);
	}
	
	protected void setImage(String name, boolean visible)
	{
		setImage(getImageResource(name));
		setVisible(visible);
	}
	
	public Image getImage()
	{
		return this.image;
	}
	
	public Bitmap getBitmap()
	{
		return this.image.get();
	}
	
	public void rotate(float angle)
	{
		this.image.rotate(angle);
	}
	
	public int getImageResourceId()
	{
		return this.image.getResourceId();
	}
	
	public int getImageWidth()
	{
		return (hasImage() ? this.image.getWidth() : 0);
	}
	
	public int getImageHeight()
	{
		return (hasImage() ? this.image.getHeight() : 0);
	}
	
	public boolean hasImage()
	{
		return (this.image != null);
	}
	
	public boolean isCollisionable()
	{
		return hasImage() && isVisible();
	}
	
	public boolean isVisible()
	{
		return this.visible;
	}
	
	public void setVisible(boolean visible)
	{
		this.visible = visible;
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
	
	// ============================= SPACE =========================== \\
	
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
		setVisible(true);
		this.state = State.AWAKE;
	}
	
	public void freeze()
	{
		setVisible(true);
		this.state = State.FROZEN;
	}
	
	public void sleep()
	{
		setVisible(false);
		this.state = State.SLEEPING;
	}
	
	// ============================= ALARMS =========================== \\
	
	public int setAlarm(OnAlarmRing listener, int delay, boolean loop)
	{
		this.lastAlarmId++;
		this.alarms.put(this.lastAlarmId, new Alarm(this.lastAlarmId, listener, delay, loop));
		return this.lastAlarmId;
	}
	
	public int setAlarm(OnAlarmRing listener, int delay)
	{
		return setAlarm(listener, delay, false);
	}
	
	// ============================ SCREEN ========================== \\
	
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
	
	// ============================= COLLISION =========================== \\
	
	public void onCollision(Process porcess)
	{
	}
	
	// ============================= POSITION =========================== \\
	
	public int getX()
	{
		return (int)this.x;
	}
	
	public int getY()
	{
		return (int)this.y;
	}
	
	public void addX(float value)
	{
		setX(this.x + value);
	}
	
	public void addY(float value)
	{
		setY(this.y + value);
	}
	
	public void setX(float x)
	{
		this.x = x;
		
		if ((!Float.isNaN(this.minX)) && (this.x < this.minX))
		{
			this.x = this.minX;
		}
		
		if ((!Float.isNaN(this.maxX)) && (this.x > this.maxX))
		{
			this.x = this.maxX;
		}
	}
	
	public void setY(float y)
	{
		this.y = y;
		
		if ((!Float.isNaN(this.minY)) && (this.y < this.minY))
		{
			this.y = this.minY;
		}
		
		if ((!Float.isNaN(this.maxY)) && (this.y > this.maxY))
		{
			this.y = this.maxY;
		}
	}
	
	// ============================= VELOCITY =========================== \\
	
	public void addVelocityX(float value)
	{
		setVelocityX(this.velocityX + value);
	}
	
	public void setVelocityX(float velocityX)
	{
		this.velocityX = velocityX;
		
		if (!Float.isNaN(this.maxVelocityX))
		{
			if (this.velocityX > this.maxVelocityX)
			{
				this.velocityX = this.maxVelocityX;
			}
			
			if (this.velocityX < -this.maxVelocityX)
			{
				this.velocityX = -this.maxVelocityX;
			}
		}
	}
	
	public void addVelocityY(float value)
	{
		setVelocityY(this.velocityY + value);
	}
	
	public void setVelocityY(float velocityY)
	{
		this.velocityY = velocityY;
		
		if (!Float.isNaN(this.maxVelocityY))
		{
			if (this.velocityY > this.maxVelocityY)
			{
				this.velocityY = this.maxVelocityY;
			}
			
			if (this.velocityY < -this.maxVelocityY)
			{
				this.velocityY = -this.maxVelocityY;
			}
		}
	}
	
	// ============================= SCREEN =========================== \\
	
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
	
	// ====================== BACKGROUND IMAGE ======================== \\
	
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
	
	// ======================== SOUND & MUSIC ======================= \\
	
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
	
	// ============================= ROTATION =========================== \\
	
	protected void setOnRotationChange(OnRotationChange listener)
	{
		this.rotationListener = listener;
	}
	
	protected OnRotationChange getOnRotationChange()
	{
		return this.rotationListener;
	}
	
	public boolean hasRotationListener()
	{
		return (this.rotationListener != null);
	}
	
	// ============================= TOUCH SCREEN =========================== \\
	
	protected void setOnTouchScreen(OnTouchScreen listener)
	{
		this.touchScreenListener = listener;
	}
	
	protected OnTouchScreen getOnTouchScreen()
	{
		return this.touchScreenListener;
	}
	
	public boolean hasTouchScreenListener()
	{
		return (this.touchScreenListener != null);
	}
}