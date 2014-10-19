package com.misty.graphics;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.khronos.opengles.GL10;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Texture
{
	public final String id;

	public final int width;
	public final int height;
	
	private int textureId;
	private final FloatBuffer vertexBuffer;
	private final FloatBuffer textureBuffer;
	
	public final Bitmap bitmap;
	private boolean textureInitialized = false;
	
	private static AssetManager assetManager;
	private static Map<String, Texture> loadedTextures = new HashMap<String, Texture>();
	
	private Texture(String id, InputStream input)
	{
		this.id = id;
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
	
	public static void initialize(AssetManager assetManager)
	{
		Texture.assetManager = assetManager;
	}
	
	public static Texture getTexture(String texturePath)
	{
		Texture result = null;
		
		if (Texture.loadedTextures.containsKey(texturePath))
		{
			result = Texture.loadedTextures.get(texturePath);
		}
		else
		{
			InputStream input = null;
			
			try
			{
				input = Texture.assetManager.open(texturePath);
				result = new Texture(texturePath, input);
				Texture.loadedTextures.put(texturePath, result);
			}
			catch (Exception e)
			{
			}
			finally
			{
				if (input != null)
				{
					try
					{
						input.close();
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		
		return result;
	}
	
	public static void reloadTextures()
	{
		Collection<Texture> textures = Texture.loadedTextures.values();

		for (Texture texture : textures)
		{
			// texture.initializeTexture(screen);
			// TODO
		}
	}
	
	private void initializeTexture(GL10 screen)
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
	
	private void render(GL10 screen, float x, float y)
	{
		if (!this.textureInitialized)
		{
			this.textureInitialized = true;
			initializeTexture(screen);
		}
		
		// move the origin of the texture
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
}