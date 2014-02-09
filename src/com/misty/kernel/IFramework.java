package com.misty.kernel;

import com.misty.text.Text;

public interface IFramework
{
	public int random(int min, int max);
	
	public void setBackgroundImage(int resourceId);
	
	public void removeBackgroundImage();
	
	public int getScreenWidth();
	
	public int getScreenHeight();
	
	public void playSound(int soundId);
	
	public void playMusic(int musicId);
	
	public void stopMusic();
	
	public void addText(Text text);
	
	public void removeText(Text text);
}