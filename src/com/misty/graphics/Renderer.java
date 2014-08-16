package com.misty.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.misty.kernel.Engine;

public class Renderer implements android.opengl.GLSurfaceView.Renderer
{
	public int width = 0;
	public int height = 0;

	private long startTime;
	private final Engine engine;
	private final ScreenResolution resolution;

	// state
	private RendererStatus state = RendererStatus.RUNNING;
	private final Object stateChanged = new Object();

	// engine status
	private enum RendererStatus
	{
		RUNNING, IDLE, PAUSED, FINISHED
	}

	public Renderer(Engine engine, ScreenResolution resolution)
	{
		this.engine = engine;
		this.resolution = resolution;
		this.startTime = System.nanoTime();
		engine.setRenderer(this, resolution);
	}

	@Override
	public void onDrawFrame(GL10 screen)
	{
		RendererStatus status = null;

		synchronized (this.stateChanged)
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

		if ((status == RendererStatus.PAUSED) || (status == RendererStatus.FINISHED))
		{
			synchronized (this.stateChanged)
			{
				this.state = RendererStatus.IDLE;
				this.stateChanged.notifyAll();
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

		synchronized (this.stateChanged)
		{
			this.state = RendererStatus.RUNNING;
			Texture.unloadTextures();
		}
	}

	public void pause(boolean finishing)
	{
		synchronized (this.stateChanged)
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
					this.stateChanged.wait();
					break;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}