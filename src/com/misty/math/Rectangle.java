package com.misty.math;

public class Rectangle
{
	public float x = 0;
	public float y = 0;
	public float width = 0;
	public float height = 0;

	public Rectangle(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rectangle(Rectangle rect)
	{
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
	}
	
	public Rectangle set(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		return this;
	}

	public Rectangle setX(float x)
	{
		this.x = x;

		return this;
	}

	public Rectangle setY(float y)
	{
		this.y = y;

		return this;
	}

	public Rectangle setWidth(float width)
	{
		this.width = width;

		return this;
	}

	public Rectangle setHeight(float height)
	{
		this.height = height;

		return this;
	}

	public Vector getPosition()
	{
		return new Vector(this.x, this.y);
	}

	public Rectangle setPosition(Vector point)
	{
		this.x = point.x;
		this.y = point.y;

		return this;
	}

	public Rectangle setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;

		return this;
	}

	public Rectangle setSize(float width, float height)
	{
		this.width = width;
		this.height = height;

		return this;
	}

	public Rectangle setSize(float sizeXY)
	{
		this.width = sizeXY;
		this.height = sizeXY;

		return this;
	}

	public Vector getSize(Vector size)
	{
		return size.set(this.width, this.height);
	}

	public boolean contains(float x, float y)
	{
		return (this.x <= x) && (this.x + this.width >= x) && (this.y <= y) && (this.y + this.height >= y);
	}

	public boolean contains(Vector point)
	{
		return contains(point.x, point.y);
	}
	
	public boolean contains(Rectangle rectangle)
	{
		float xmin = rectangle.x;
		float xmax = xmin + rectangle.width;

		float ymin = rectangle.y;
		float ymax = ymin + rectangle.height;

		return ((xmin > this.x && xmin < this.x + this.width) && (xmax > this.x && xmax < this.x + this.width)) && ((ymin > this.y && ymin < this.y + this.height) && (ymax > this.y && ymax < this.y + this.height));
	}
	
	public boolean intersects(Rectangle rectangle)
	{
		return (this.x < rectangle.x + rectangle.width) && (this.x + this.width > rectangle.x) && (this.y < rectangle.y + rectangle.height) && (this.y + this.height > rectangle.y);
	}

	public Rectangle set(Rectangle rect)
	{
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;

		return this;
	}

	public Rectangle merge(Rectangle rect)
	{
		float minX = Math.min(this.x, rect.x);
		float maxX = Math.max(this.x + this.width, rect.x + rect.width);
		this.x = minX;
		this.width = maxX - minX;

		float minY = Math.min(this.y, rect.y);
		float maxY = Math.max(this.y + this.height, rect.y + rect.height);
		this.y = minY;
		this.height = maxY - minY;

		return this;
	}

	public Rectangle merge(float x, float y)
	{
		float minX = Math.min(this.x, x);
		float maxX = Math.max(this.x + this.width, x);
		this.x = minX;
		this.width = maxX - minX;

		float minY = Math.min(this.y, y);
		float maxY = Math.max(this.y + this.height, y);
		this.y = minY;
		this.height = maxY - minY;

		return this;
	}

	public Rectangle merge(Vector Point)
	{
		return merge(Point.x, Point.y);
	}
	
	public Rectangle merge(Vector[] vecs)
	{
		float minX = this.x;
		float maxX = this.x + this.width;
		float minY = this.y;
		float maxY = this.y + this.height;

		for (Vector v : vecs)
		{
			minX = Math.min(minX, v.x);
			maxX = Math.max(maxX, v.x);
			minY = Math.min(minY, v.y);
			maxY = Math.max(maxY, v.y);
		}

		this.x = minX;
		this.width = maxX - minX;
		this.y = minY;
		this.height = maxY - minY;

		return this;
	}
	
	public float getAspectRatio()
	{
		return (this.height == 0) ? Float.NaN : this.width / this.height;
	}
	
	public Vector getCenter()
	{
		return new Vector(this.x + this.width / 2, this.y + this.height / 2);
	}

	public Rectangle setCenter(float x, float y)
	{
		setPosition(x - this.width / 2, y - this.height / 2);
		
		return this;
	}

	public Rectangle setCenter(Vector point)
	{
		setPosition(point.x - this.width / 2, point.y - this.height / 2);

		return this;
	}
	
	public float area()
	{
		return this.width * this.height;
	}

	public float perimeter()
	{
		return 2 * (this.width + this.height);
	}
}