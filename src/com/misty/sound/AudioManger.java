package com.misty.sound;

import java.lang.reflect.Field;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class AudioManger
{
	private final float volume = 1.0f;
	private final Context context;
	private final SoundPool soundPool;
	private final SparseIntArray soundsMap;
	private MediaPlayer player;
	private int musicPosition = 0;
	
	public AudioManger(Context context)
	{
		this.context = context;
		this.soundPool = new SoundPool(5, 3, 100);
		this.soundsMap = new SparseIntArray();
	}
	
	public void loadSounds(Class<?> soundClass)
	{
		for (Field field : soundClass.getFields())
		{
			try
			{
				int resourceId = field.getInt(null);
				this.soundsMap.put(resourceId, this.soundPool.load(this.context, resourceId, 1));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void playSound(int soundId)
	{
		this.soundPool.play(this.soundsMap.get(soundId), this.volume, this.volume, 1, 0, 1f);
	}
	
	// TODO: alternative
	public void playSound2(int soundId)
	{
		MediaPlayer player = MediaPlayer.create(this.context, soundId);
		player.setVolume(1f, 1f);
		player.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer player)
			{
				player.release();
			}
		});
		player.start();
	}
	
	public void playMusic(int musicId)
	{
		stopMusic();
		this.player = MediaPlayer.create(this.context, musicId);
		this.player.setLooping(true);
		this.player.setVolume(0.5f, 0.5f);
		this.player.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer player)
			{
				player.release();
			}
		});
		this.player.start();
	}
	
	public void stopMusic()
	{
		if (this.player != null)
		{
			this.player.stop();
		}
	}
	
	public void pauseMusic()
	{
		if (this.player != null)
		{
			this.player.pause();
			this.musicPosition = this.player.getCurrentPosition();
		}
	}
	
	public void resumeMusic()
	{
		if ((this.player != null) && (!this.player.isPlaying()))
		{
			this.player.seekTo(this.musicPosition);
			this.player.start();
		}
	}
	
	public boolean isMusicPlaying()
	{
		return ((this.player != null) && this.player.isPlaying());
	}
}