package com.misty.graphics;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.SparseArray;

public class Texture
{
	public final int width;
	public final int height;
	
	private final FloatBuffer vertexBuffer;
	private final FloatBuffer textureBuffer;
	private int textureId;
	
	public final Bitmap bitmap;
	private boolean textureLoaded = false; // TODO: mark as false if the activity is paused
	
	private static Resources resources;
	private static SparseArray<Texture> textures = new SparseArray<Texture>();
	
	public Texture(InputStream input)
	{
		this.bitmap = BitmapFactory.decodeStream(input);
		
		this.width = this.bitmap.getWidth();
		this.height = this.bitmap.getHeight();
		
		float[] vertices = new float[]
			{
				// bottom left
				0, 0,
				// top left
				0, this.height,
				// bottom right
				this.width, 0,
				// top right
				this.width, this.height
			};
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		this.vertexBuffer = byteBuffer.asFloatBuffer();
		this.vertexBuffer.put(vertices);
		this.vertexBuffer.flip();
		
		float texture[] = new float[]
			{
				// top left
				0, 1,
				// bottom left
				0, 0,
				// top right
				1, 1,
				// bottom right
				1, 0
			};
		
		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		this.textureBuffer = byteBuffer.asFloatBuffer();
		this.textureBuffer.put(texture);
		this.textureBuffer.flip();
	}
	
	public static Texture getTexture(int resourceId)
	{
		Texture result = Texture.textures.get(resourceId);
		
		if (result == null)
		{
			InputStream input = Texture.resources.openRawResource(resourceId);
			
			result = new Texture(input);
			Texture.textures.put(resourceId, result);
			
			try
			{
				input.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static void unloadTextures()
	{
		int size = Texture.textures.size();
		
		for (int i = 0; i < size; i++)
		{
			Texture texture = Texture.textures.valueAt(i);
			texture.unload();
		}
	}
	
	public static void initialize(Resources resources)
	{
		Texture.resources = resources;
	}
	
	private void loadGLTexture(GL10 screen)
	{
		int[] textureArray = new int[1];
		
		// generate one texture pointer
		screen.glGenTextures(1, textureArray, 0);
		
		// bind it to our array
		screen.glBindTexture(GL10.GL_TEXTURE_2D, textureArray[0]);
		
		this.textureId = textureArray[0];
		
		// use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, this.bitmap, 0);
		
		// create nearest filtered texture
		screen.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		screen.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	}
	
	public void render(GL10 screen, float x, float y)
	{
		if (!this.textureLoaded)
		{
			this.textureLoaded = true;
			loadGLTexture(screen);
		}
		
		// move the origin of the sprite
		screen.glLoadIdentity();
		screen.glTranslatef(x, y, 0);
		
		// bind the previously generated texture
		screen.glBindTexture(GL10.GL_TEXTURE_2D, this.textureId);
		
		// apply filters
		screen.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		screen.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		
		// point to our vertex buffer
		screen.glVertexPointer(2, GL10.GL_FLOAT, 0, this.vertexBuffer);
		screen.glTexCoordPointer(2, GL10.GL_FLOAT, 0, this.textureBuffer);
		
		// draw the vertices as triangle strip
		screen.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void unload()
	{
		this.textureLoaded = false;
	}
}