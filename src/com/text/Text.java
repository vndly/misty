package com.text;

public class Text
{
	private String text = "";
	private int x = 0;
	private int y = 0;
	private final Font font;
	
	public Text(String text, int x, int y, Font font)
	{
		this.text = text;
		this.x = x;
		this.y = y;
		this.font = font;
	}
	
	public String getText()
	{
		return this.text;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	
	public int getX()
	{
		return this.x;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public int getY()
	{
		return this.y;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public Font getFont()
	{
		return this.font;
	}
}