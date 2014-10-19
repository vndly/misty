package com.misty.graphics;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.misty.R;
import com.misty.graphics.textures.Texture;
import com.misty.graphics.textures.TextureManager;
import com.misty.kernel.Engine;

public class Renderer implements android.opengl.GLSurfaceView.Renderer
{
	private long startTime;
	
	private final Context context;
	private final Engine engine;
	private final ScreenResolution resolution;
	
	// state
	private RendererStatus state = null;
	private final Object stateChangedLock = new Object();
	
	// shader
	private int uMatrixLocation;
	private int uTextureUnitLocation;
	private int aPositionLocation;
	private int aTextureCoordinatesLocation;
	private final float[] projectionMatrix = new float[16];
	
	// engine status
	private enum RendererStatus
	{
		RUNNING, IDLE, PAUSED, FINISHED
	}

	public Renderer(Context context, Engine engine, ScreenResolution resolution)
	{
		this.context = context;
		this.engine = engine;
		this.resolution = resolution;
		this.startTime = System.nanoTime();
		
		engine.setRenderer(this, resolution);
	}
	
	public int getResolutionX()
	{
		return this.resolution.horizontal;
	}

	public int getResolutionY()
	{
		return this.resolution.vertical;
	}
	
	public void clearScreen(Camera camera)
	{
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		Matrix.orthoM(this.projectionMatrix, 0, camera.x, camera.x + this.resolution.horizontal, camera.y, camera.y + this.resolution.vertical, -1f, 1f);
	}
	
	public void render(Texture texture, float x, float y)
	{
		texture.render(this.projectionMatrix, x, y, this.uMatrixLocation, this.uTextureUnitLocation, this.aPositionLocation, this.aTextureCoordinatesLocation);
	}
	
	@Override
	public void onDrawFrame(GL10 unused)
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
			
			this.engine.update(delta, this);
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
	public void onSurfaceCreated(GL10 unused, EGLConfig config)
	{
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		
		String vertexShader = readTextFile(this.context, R.raw.vertex_shader);
		String fragmentShader = readTextFile(this.context, R.raw.fragment_shader);
		int program = buildProgram(vertexShader, fragmentShader);
		GLES20.glUseProgram(program);

		this.uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");
		this.uTextureUnitLocation = GLES20.glGetUniformLocation(program, "u_TextureUnit");
		this.aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
		this.aTextureCoordinatesLocation = GLES20.glGetAttribLocation(program, "a_TextureCoordinates");
		
		synchronized (this.stateChangedLock)
		{
			this.state = RendererStatus.RUNNING;
		}
		
		TextureManager.reloadTextures();
	}
	
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);
		
		this.resolution.normalize(width, height);

		synchronized (this.stateChangedLock)
		{
			this.state = RendererStatus.RUNNING;
		}
		
		this.startTime = System.nanoTime();
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
		}
	}
	
	// ==================== UTILS
	
	private int compileShader(int type, String shaderCode)
	{
		// Create a new shader object.
		final int shaderObjectId = GLES20.glCreateShader(type);
		
		// Pass in the shader source.
		GLES20.glShaderSource(shaderObjectId, shaderCode);
		
		// Compile the shader.
		GLES20.glCompileShader(shaderObjectId);
		
		// Get the compilation status.
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		
		// Verify the compile status.
		if (compileStatus[0] == 0)
		{
			// If it failed, delete the shader object.
			GLES20.glDeleteShader(shaderObjectId);
			
			return 0;
		}
		
		// Return the shader object ID.
		return shaderObjectId;
	}
	
	private int linkProgram(int vertexShaderId, int fragmentShaderId)
	{
		// Create a new program object.
		final int programObjectId = GLES20.glCreateProgram();
		
		// Attach the vertex shader to the program.
		GLES20.glAttachShader(programObjectId, vertexShaderId);
		
		// Attach the fragment shader to the program.
		GLES20.glAttachShader(programObjectId, fragmentShaderId);
		
		// Link the two shaders together into a program.
		GLES20.glLinkProgram(programObjectId);
		
		// Get the link status.
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
		
		// Verify the link status.
		if (linkStatus[0] == 0)
		{
			// If it failed, delete the program object.
			GLES20.glDeleteProgram(programObjectId);
			
			return 0;
		}
		
		// Return the program object ID.
		return programObjectId;
	}
	
	private int buildProgram(String vertexShaderSource, String fragmentShaderSource)
	{
		// Compile the shaders.
		int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
		int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

		// Link them into a shader program.
		return linkProgram(vertexShader, fragmentShader);
	}
	
	private String readTextFile(Context context, int resourceId)
	{
		StringBuilder builder = new StringBuilder();
		
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try
		{
			inputStream = context.getResources().openRawResource(resourceId);
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String nextLine;
			
			while ((nextLine = bufferedReader.readLine()) != null)
			{
				builder.append(nextLine);
				builder.append('\n');
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeResource(inputStream);
			closeResource(inputStreamReader);
			closeResource(bufferedReader);
		}
		
		return builder.toString();
	}
	
	private void closeResource(Closeable resource)
	{
		if (resource != null)
		{
			try
			{
				resource.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}