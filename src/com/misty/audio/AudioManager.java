package com.misty.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseIntArray;

public class AudioManager
{
	private final Context context;
	private final SoundPool soundPool;
	private final SparseIntArray soundsMap;
	private MediaPlayer player;
	private int audioPosition = 0;
	
	public AudioManager(Context context)
	{
		this.context = context;

		this.soundsMap = new SparseIntArray();

		this.soundPool = new SoundPool(20, android.media.AudioManager.STREAM_MUSIC, 100);
		this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener()
		{
			@Override
			public void onLoadComplete(SoundPool soundPool, int resourceId, int status)
			{
				if (status == 0)
				{
					playbackSound(resourceId);
				}
			}
		});
	}
	
	private void loadSound(int soundId)
	{
		int resourceId = this.soundPool.load(this.context, soundId, 1);
		this.soundsMap.put(soundId, resourceId);
	}
	
	public void playSound(int soundId)
	{
		int resourceId = this.soundsMap.get(soundId);
		
		if (resourceId == 0)
		{
			loadSound(soundId);
		}
		else
		{
			playbackSound(resourceId);
		}
	}
	
	public void playbackSound(int resourceId)
	{
		this.soundPool.play(resourceId, 0.5f, 0.5f, 1, 0, 1f);
	}
	
	public void playAudio(int audioId)
	{
		stopMusic();
		
		this.player = MediaPlayer.create(this.context, audioId);
		this.player.setLooping(true);
		this.player.setVolume(1f, 1f);
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
	
	private void stopMusic()
	{
		if (this.player != null)
		{
			this.player.stop();
			this.player.release();
		}
	}
	
	public void resumeAudio()
	{
		if ((this.player != null) && (!this.player.isPlaying()))
		{
			this.player.seekTo(this.audioPosition);
			this.player.start();
		}
	}
	
	public void pauseAudio()
	{
		if (this.player != null)
		{
			this.player.pause();
			this.audioPosition = this.player.getCurrentPosition();
		}
	}
	
	public void stopAudio()
	{
		stopMusic();
		
		if (this.soundPool != null)
		{
			int size = this.soundsMap.size();
			
			for (int i = 0; i < size; i++)
			{
				this.soundPool.unload(this.soundsMap.get(this.soundsMap.keyAt(i)));
			}
			
			this.soundPool.release();
		}
	}
	
	public boolean isAudioPlaying()
	{
		return ((this.player != null) && this.player.isPlaying());
	}
}