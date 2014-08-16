package com.misty.math;

public class Vector2D
{
	public static float TO_RADIANS = (1 / 180.0f) * (float)Math.PI;
	public static float TO_DEGREES = (1 / (float)Math.PI) * 180;

	public float x = 0;
	public float y = 0;

	public Vector2D()
	{
		this(0, 0);
	}

	public Vector2D(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vector2D(Vector2D other)
	{
		this.x = other.x;
		this.y = other.y;
	}

	public Vector2D copy()
	{
		return new Vector2D(this.x, this.y);
	}

	public Vector2D set(float x, float y)
	{
		this.x = x;
		this.y = y;

		return this;
	}

	public Vector2D set(Vector2D other)
	{
		return set(other.x, other.y);
	}

	public Vector2D add(float x, float y)
	{
		this.x += x;
		this.y += y;

		return this;
	}

	public Vector2D add(Vector2D other)
	{
		return add(other.x, other.y);
	}

	public Vector2D sub(float x, float y)
	{
		this.x -= x;
		this.y -= y;

		return this;
	}

	public Vector2D sub(Vector2D other)
	{
		return sub(other.x, other.y);
	}

	public Vector2D mul(float scalar)
	{
		this.x *= scalar;
		this.y *= scalar;

		return this;
	}

	public float length()
	{
		return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
	}

	public Vector2D normalize()
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
		// Math.toDegrees(angrad)
		float angle = (float)Math.atan2(this.y, this.x) * Vector2D.TO_DEGREES;

		if (angle < 0)
		{
			angle += 360;
		}

		return angle;
	}

	public Vector2D rotate(float angle)
	{
		// Math.toRadians(angdeg);
		float rad = angle * Vector2D.TO_RADIANS;

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

	public float distance(Vector2D other)
	{
		return distance(other.x, other.y);
	}

	public float distSquared(float x, float y)
	{
		float distX = this.x - x;
		float distY = this.y - y;

		return (distX * distX) + (distY * distY);
	}

	public float distSquared(Vector2D other)
	{
		return distSquared(other.x, other.y);
	}
}