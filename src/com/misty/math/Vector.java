package com.misty.math;

public class Vector
{
	public float x = 0;
	public float y = 0;
	
	public Vector()
	{
		this(0, 0);
	}
	
	public Vector(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector other)
	{
		this.x = other.x;
		this.y = other.y;
	}
	
	public Vector copy()
	{
		return new Vector(this.x, this.y);
	}
	
	public Vector set(float x, float y)
	{
		this.x = x;
		this.y = y;
		
		return this;
	}
	
	public Vector set(Vector other)
	{
		return set(other.x, other.y);
	}
	
	public Vector add(float x, float y)
	{
		this.x += x;
		this.y += y;
		
		return this;
	}
	
	public Vector add(Vector other)
	{
		return add(other.x, other.y);
	}
	
	public Vector sub(float x, float y)
	{
		this.x -= x;
		this.y -= y;
		
		return this;
	}
	
	public Vector sub(Vector other)
	{
		return sub(other.x, other.y);
	}
	
	public Vector mul(float scalar)
	{
		this.x *= scalar;
		this.y *= scalar;
		
		return this;
	}
	
	public float length()
	{
		return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
	}
	
	public Vector normalize()
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
		float angle = (float)Math.toDegrees(Math.atan2(this.y, this.x));
		
		if (angle < 0)
		{
			angle += 360;
		}
		
		return angle;
	}
	
	public Vector rotate(float angle)
	{
		float rad = (float)Math.toRadians(angle);
		
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
	
	public float distance(Vector other)
	{
		return distance(other.x, other.y);
	}
	
	public float distSquared(float x, float y)
	{
		float distX = this.x - x;
		float distY = this.y - y;
		
		return (distX * distX) + (distY * distY);
	}
	
	public float distSquared(Vector other)
	{
		return distSquared(other.x, other.y);
	}
}