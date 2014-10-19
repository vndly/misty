package com.misty.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.opengl.GLES20;

public class VertexArray
{
	private final FloatBuffer floatBuffer;

	public VertexArray(float[] vertexData)
	{
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexData.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		this.floatBuffer = byteBuffer.asFloatBuffer();
		this.floatBuffer.put(vertexData);
	}

	public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride)
	{
		this.floatBuffer.position(dataOffset);
		GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT, false, stride, this.floatBuffer);
		GLES20.glEnableVertexAttribArray(attributeLocation);
		this.floatBuffer.position(0);
	}
}