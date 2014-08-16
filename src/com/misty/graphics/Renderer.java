package com.misty.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import com.misty.kernel.Engine;

public class Renderer implements android.opengl.GLSurfaceView.Renderer
{
	public int width = 0;
	public int height = 0;

	private long startTime;
	private final Engine engine;
	private final GLSurfaceView screen;
	private final ScreenResolution resolution;

	// state
	private RendererStatus state = null;
	private final Object stateChangedLock = new Object();

	// engine status
	private enum RendererStatus
	{
		RUNNING, IDLE, PAUSED, FINISHED
	}

	public Renderer(Engine engine, GLSurfaceView screen, ScreenResolution resolution)
	{
		this.engine = engine;
		this.screen = screen;
		this.resolution = resolution;
		this.startTime = System.nanoTime();

		engine.setRenderer(this, resolution);
	}

	@Override
	public void onDrawFrame(GL10 screen)
	{
		RendererStatus status = null;

		synchronized (this.stateChangedLock)
		{
			status = this.state;
		}

		if (status == RendererStatus.RUNNING)
		{
			long currentTime = System.nanoTime();
			float delta = (currentTime - this.startTime) / 1000000000f;
			this.startTime = currentTime;

			// FPS.log(currentTime);

			this.engine.update(delta, screen);
		}
		else if ((status == RendererStatus.PAUSED) || (status == RendererStatus.FINISHED))
		{
			synchronized (this.stateChangedLock)
			{
				this.state = RendererStatus.IDLE;
				this.stateChangedLock.notifyAll();
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 screen, int width, int height)
	{
		this.width = width;
		this.height = height;

		screen.glViewport(0, 0, width, height);
		screen.glMatrixMode(GL10.GL_PROJECTION);
		screen.glLoadIdentity();
		screen.glOrthof(0, this.resolution.horizontal, 0, this.resolution.vertical, 1, -1);
	}

	@Override
	public void onSurfaceCreated(GL10 screen, EGLConfig config)
	{
		screen.glEnable(GL10.GL_TEXTURE_2D);
		screen.glEnable(GL10.GL_BLEND);
		screen.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		screen.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		screen.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		synchronized (this.stateChangedLock)
		{
			this.state = RendererStatus.RUNNING;
		}

		Texture.reloadTextures(screen);
	}

	public void pause(boolean finishing)
	{
		synchronized (this.stateChangedLock)
		{
			if (this.state == RendererStatus.RUNNING)
			{
				if (finishing)
				{
					this.state = RendererStatus.FINISHED;
				}
				else
				{
					this.state = RendererStatus.PAUSED;
				}
				
				while (true)
				{
					try
					{
						this.stateChangedLock.wait();
						break;
					}
					catch (Exception e)
					{
					}
				}
			}

			if (this.screen != null)
			{
				this.screen.onPause();
			}
		}
	}

	public void resume()
	{
		if (this.screen != null)
		{
			this.screen.onResume();
		}
	}
}