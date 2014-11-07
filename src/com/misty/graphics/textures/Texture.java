package com.misty.graphics.textures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class Texture
{
	public final String path;
	public final int width;
	public final int height;
	public final int[][] pixelMap;;
	
	private int textureId;
	private final FloatBuffer floatBuffer;
	private final float[] modelMatrix = new float[16];
	private final float[] finalMatrix = new float[16];
	
	private static final int BYTES_PER_FLOAT = 4;
	private static final int VERTICES_LENGTH = 16;
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int STRIDE = (Texture.POSITION_COMPONENT_COUNT + Texture.TEXTURE_COORDINATES_COMPONENT_COUNT) * Texture.BYTES_PER_FLOAT;
	
	public Texture(String texturePath)
	{
		this.path = texturePath;
		
		Bitmap bitmap = TextureManager.getBitmap(this.path);
		
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
		
		this.pixelMap = new int[this.width][this.height];
		
		for (int i = 0; i < this.width; i++)
		{
			for (int j = 0; j < this.height; j++)
			{
				this.pixelMap[i][j] = bitmap.getPixel(i, j);
			}
		}
		
		this.floatBuffer = getFloatBuffer(bitmap);
		
		loadTexture(bitmap);
	}
	
	public void render(float[] projectionMatrix, float x, float y, float scaleX, float scaleY, float angle, float orientationHorizontal, float orientationVertical, int uMatrixLocation, int uTextureUnitLocation, int aPositionLocation, int aTextureCoordinatesLocation)
	{
		Matrix.setIdentityM(this.modelMatrix, 0);
		Matrix.translateM(this.modelMatrix, 0, x + (this.width / 2), y + (this.height / 2), 0);
		Matrix.rotateM(this.modelMatrix, 0, angle, 0, 0, 1);
		Matrix.scaleM(this.modelMatrix, 0, scaleX, scaleY, 1);
		
		if (orientationHorizontal == -1)
		{
			Matrix.scaleM(this.modelMatrix, 0, orientationHorizontal, 1, 1);
		}
		if (orientationVertical == -1)
		{
			Matrix.scaleM(this.modelMatrix, 0, 1, orientationVertical, 1);
		}
		
		Matrix.multiplyMM(this.finalMatrix, 0, projectionMatrix, 0, this.modelMatrix, 0);
		
		// setting uniforms
		GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, this.finalMatrix, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
		GLES20.glUniform1i(uTextureUnitLocation, 0);
		
		// rendering the texture
		setVertexAttribPointer(0, aPositionLocation, Texture.POSITION_COMPONENT_COUNT, Texture.STRIDE);
		setVertexAttribPointer(Texture.POSITION_COMPONENT_COUNT, aTextureCoordinatesLocation, Texture.TEXTURE_COORDINATES_COMPONENT_COUNT, Texture.STRIDE);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, Texture.VERTICES_LENGTH / (Texture.POSITION_COMPONENT_COUNT + Texture.TEXTURE_COORDINATES_COMPONENT_COUNT));
	}
	
	private void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride)
	{
		this.floatBuffer.position(dataOffset);
		GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT, false, stride, this.floatBuffer);
		GLES20.glEnableVertexAttribArray(attributeLocation);
		this.floatBuffer.position(0);
	}
	
	private FloatBuffer getFloatBuffer(Bitmap bitmap)
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
			    
			    -imageWidth / 2, imageHeight / 2, 0f, 0f, //
			    -imageWidth / 2, -imageHeight / 2, 0f, 1f, //
			    imageWidth / 2, imageHeight / 2, 1f, 0f, //
			    imageWidth / 2, -imageHeight / 2, 1f, 1f
			};
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * Texture.BYTES_PER_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer result = byteBuffer.asFloatBuffer();
		result.put(vertices);
		
		return result;
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
				
				// Set filtering: a default must be set, or the texture will be black.
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
				
				// Load the bitmap into the bound texture.
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
				
				// Note: Following code may cause an error to be reported in the
				// ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
				// Failed to generate texture mipmap levels (error=3)
				// No OpenGL error will be encountered (glGetError() will return
				// 0). If this happens, just squash the source image to be
				// square. It will look the same because of texture coordinates,
				// and mipmap generation will work.
				// GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
				
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
		Bitmap bitmap = TextureManager.getBitmap(this.path);
		loadTexture(bitmap);
	}
}