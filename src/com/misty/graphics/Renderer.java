package com.misty.graphics;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import com.misty.R;
import com.misty.graphics.textures.Texture;
import com.misty.kernel.Engine;

public class Renderer implements android.opengl.GLSurfaceView.Renderer
{
	private long startTime;
	
	private final Context context;
	private final Engine engine;
	private final GLSurfaceView screen;
	private final ScreenResolution resolution;
	
	// state
	private RendererStatus state = null;
	private final Object stateChangedLock = new Object();
	
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
	
	private int textureId = 0;
	
	public Renderer(Context context, Engine engine, GLSurfaceView screen, ScreenResolution resolution)
	{
		this.context = context;
		this.engine = engine;
		this.screen = screen;
		this.resolution = resolution;
		this.startTime = System.nanoTime();
		
		engine.setRenderer(this, resolution);
	}
	
	private static final int BYTES_PER_FLOAT = 4;
	private static final int VERTICES_LENGTH = 16;
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int STRIDE = (Renderer.POSITION_COMPONENT_COUNT + Renderer.TEXTURE_COORDINATES_COMPONENT_COUNT) * Renderer.BYTES_PER_FLOAT;
	
	private VertexArray vertexArray;

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
		this.textureId = loadTexture(this.context, R.drawable.texture);
		
		// Creating model matrix
		float[] modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, x, y, 0f);
		
		// Creating final matrix
		float[] finalMatrix = new float[16];
		Matrix.multiplyMM(finalMatrix, 0, this.projectionMatrix, 0, modelMatrix, 0);

		// ------------------------------------------------------------------

		GLES20.glUniformMatrix4fv(this.uMatrixLocation, 1, false, finalMatrix, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
		GLES20.glUniform1i(this.uTextureUnitLocation, 0);
		
		// ------------------------------------
		
		this.vertexArray.setVertexAttribPointer(0, this.aPositionLocation, Renderer.POSITION_COMPONENT_COUNT, Renderer.STRIDE);
		this.vertexArray.setVertexAttribPointer(Renderer.POSITION_COMPONENT_COUNT, this.aTextureCoordinatesLocation, Renderer.TEXTURE_COORDINATES_COMPONENT_COUNT, Renderer.STRIDE);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, Renderer.VERTICES_LENGTH / (Renderer.POSITION_COMPONENT_COUNT + Renderer.TEXTURE_COORDINATES_COMPONENT_COUNT));
	}

	private int loadTexture(Context context, int resourceId)
	{
		final int[] textureObjectIds = new int[1];
		GLES20.glGenTextures(1, textureObjectIds, 0);
		
		if (textureObjectIds[0] == 0)
		{
			return 0;
		}
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		
		// Read in the resource
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
		
		if (bitmap == null)
		{
			GLES20.glDeleteTextures(1, textureObjectIds, 0);
			return 0;
		}

		float imageWidth = bitmap.getWidth();
		float imageHeight = bitmap.getHeight();

		// Bind to the texture in OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
		
		// Set filtering: a default must be set, or the texture will be
		// black.
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		// GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		
		// Note: Following code may cause an error to be reported in the
		// ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
		// Failed to generate texture mipmap levels (error=3)
		// No OpenGL error will be encountered (glGetError() will return
		// 0). If this happens, just squash the source image to be
		// square. It will look the same because of texture coordinates,
		// and mipmap generation will work.
		
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		
		// Recycle the bitmap, since its data has been loaded into
		// OpenGL.
		bitmap.recycle();
		
		// Unbind from the texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		// --------------
		float[] vertices =
			{
				// Order of coordinates: X, Y, S, T

				// A----C
				// | /|
				// | / |
				// | / |
				// |/ |
				// B----D

				// Note: T is inverted!

				0f, imageHeight, 0f, 0f, //
				0f, 0f, 0f, 1f, //
				imageWidth, imageHeight, 1f, 0f, //
				imageWidth, 0f, 1f, 1f
			};
		
		this.vertexArray = new VertexArray(vertices);
		
		return textureObjectIds[0];
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
		GLES20.glClearColor(1f, 0f, 0f, 1f);// TODO: CHANGE COLOR
		
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
		
		// TODO
		// Texture.reloadTextures();
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
		
		// Resources.Sprites.initialize(this.context, RESOLUTION_X, RESOLUTION_Y);
		
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