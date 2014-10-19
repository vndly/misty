package com.misty.graphics.textures;

import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import com.misty.graphics.VertexArray;

public class Texture
{
	public final String id;

	public int width;
	public int height;
	
	public int textureId;
	public VertexArray vertexArray;
	
	private final float[] modelMatrix = new float[16];
	private final float[] finalMatrix = new float[16];
	
	private static final int BYTES_PER_FLOAT = 4;
	private static final int VERTICES_LENGTH = 16;
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int STRIDE = (Texture.POSITION_COMPONENT_COUNT + Texture.TEXTURE_COORDINATES_COMPONENT_COUNT) * Texture.BYTES_PER_FLOAT;

	public Texture(String texturePath)
	{
		this.id = texturePath;
		
		Bitmap bitmap = getBitmap(this.id);
		
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
		
		this.vertexArray = getVertexArray(bitmap);
		
		loadTexture(bitmap);
	}

	public void render(float[] projectionMatrix, float x, float y, int uMatrixLocation, int uTextureUnitLocation, int aPositionLocation, int aTextureCoordinatesLocation)
	{
		// setting model matrix and final matrix
		Matrix.setIdentityM(this.modelMatrix, 0);
		Matrix.translateM(this.modelMatrix, 0, x, y, 0f);
		Matrix.multiplyMM(this.finalMatrix, 0, projectionMatrix, 0, this.modelMatrix, 0);

		// setting uniforms
		GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, this.finalMatrix, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
		GLES20.glUniform1i(uTextureUnitLocation, 0);
		
		// rendering the texture
		this.vertexArray.setVertexAttribPointer(0, aPositionLocation, Texture.POSITION_COMPONENT_COUNT, Texture.STRIDE);
		this.vertexArray.setVertexAttribPointer(Texture.POSITION_COMPONENT_COUNT, aTextureCoordinatesLocation, Texture.TEXTURE_COORDINATES_COMPONENT_COUNT, Texture.STRIDE);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, Texture.VERTICES_LENGTH / (Texture.POSITION_COMPONENT_COUNT + Texture.TEXTURE_COORDINATES_COMPONENT_COUNT));
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
		loadTexture(bitmap);
	}
}