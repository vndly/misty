package com.misty.graphics.textures;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Texture
{
	public final String id;

	public final int width;
	public final int height;
	
	private int textureId;
	private final FloatBuffer vertexBuffer;
	private final FloatBuffer textureBuffer;
	
	public final Bitmap bitmap;
	
	public Texture(String id, InputStream input)
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

	public void reload()
	{
		// TODO
	}
}