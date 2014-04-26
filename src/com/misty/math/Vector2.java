package com.misty.math;

public class Vector2
{
	public static float TO_RADIANS = (1 / 180.0f) * (float)Math.PI;
	public static float TO_DEGREES = (1 / (float)Math.PI) * 180;
	
	public float x = 0;
	public float y = 0;
	
	public Vector2()
	{
		this(0, 0);
	}
	
	public Vector2(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Vector2 other)
	{
		this.x = other.x;
		this.y = other.y;
	}
	
	public Vector2 copy()
	{
		return new Vector2(this.x, this.y);
	}
	
	public Vector2 set(float x, float y)
	{
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	public Vector2 set(Vector2 other)
	{
		return set(other.x, other.y);
	}
	
	public Vector2 add(float x, float y)
	{
		this.x += x;
		this.y += y;
		
		return this;
	}
	
	public Vector2 add(Vector2 other)
	{
		return add(other.x, other.y);
	}
	
	public Vector2 sub(float x, float y)
	{
		this.x -= x;
		this.y -= y;
		
		return this;
	}
	
	public Vector2 sub(Vector2 other)
	{
		return sub(other.x, other.y);
	}
	
	public Vector2 mul(float scalar)
	{
		this.x *= scalar;
		this.y *= scalar;
		
		return this;
	}
	
	public float length()
	{
		return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
	}
	
	public Vector2 normalize()
	{
		float len = length();
		
		if (len != 0)
		{
			this.x /= len;
			this.y /= len;
		}
		
		return this;
	}
	
	public float angle()
	{
		float angle = (float)Math.atan2(this.y, this.x) * Vector2.TO_DEGREES;
		
		if (angle < 0)
		{
			angle += 360;
		}
		
		return angle;
	}
	
	public Vector2 rotate(float angle)
	{
		float rad = angle * Vector2.TO_RADIANS;
		
		float cos = (float)Math.cos(rad);
		float sin = (float)Math.sin(rad);
		
		float newX = (this.x * cos) - (this.y * sin);
		float newY = (this.x * sin) + (this.y * cos);
		
		this.x = newX;
		this.y = newY;
		
		return this;
	}
	
	public float distance(float x, float y)
	{
		float distX = this.x - x;
		float distY = this.y - y;
		
		return (float)Math.sqrt((distX * distX) + (distY * distY));
	}
	
	public float distance(Vector2 other)
	{
		return distance(other.x, other.y);
	}
	
	public float distSquared(float x, float y)
	{
		float distX = this.x - x;
		float distY = this.y - y;
		
		return (distX * distX) + (distY * distY);
	}
	
	public float distSquared(Vector2 other)
	{
		return distSquared(other.x, other.y);
	}
}