package com.misty.graphics.textures;

import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.misty.graphics.VertexArray;

public class Texture
{
	public final String id;
	
	public int width;
	public int height;

	public int textureId;
	public VertexArray vertexArray;

	public Texture(String texturePath)
	{
		this.id = texturePath;

		reload();
	}
	
	private Bitmap getBitmap(String texturePath)
	{
		Bitmap result = null;
		InputStream input = null;
		
		try
		{
			input = TextureManager.getTextureStream(texturePath);
			result = BitmapFactory.decodeStream(input);
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
		
		return result;
	}

	private VertexArray getVertexArray(Bitmap bitmap)
	{
		float imageWidth = bitmap.getWidth();
		float imageHeight = bitmap.getHeight();

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
		
		return new VertexArray(vertices);
	}
	
	private void loadTexture(Bitmap bitmap)
	{
		int[] textureObjectIds = new int[1];
		GLES20.glGenTextures(1, textureObjectIds, 0);
		
		if (textureObjectIds[0] != 0)
		{
			if (bitmap != null)
			{
				// Bind to the texture in OpenGL
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

				// Set filtering: a default must be set, or the texture will be
				// black.
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
				// GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				// GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

				// Load the bitmap into the bound texture.
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
				GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

				// Recycle the bitmap, since its data has been loaded into OpenGL.
				bitmap.recycle();

				// Unbind from the texture.
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
				
				this.textureId = textureObjectIds[0];
			}
			else
			{
				GLES20.glDeleteTextures(1, textureObjectIds, 0);
			}
		}
	}

	public void reload()
	{
		Bitmap bitmap = getBitmap(this.id);

		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();

		this.vertexArray = getVertexArray(bitmap);
		
		loadTexture(bitmap);
	}
}