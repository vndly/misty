package com.misty.audio;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import com.misty.utils.Assets;

public class AudioManager
{
	private final SoundPool soundPool;
	private final Map<String, Integer> soundsMap;
	private MediaPlayer player;
	private int audioPosition = 0;
	
	public AudioManager()
	{
		this.soundsMap = new HashMap<String, Integer>();
		
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
	
	private void loadSound(String soundPath)
	{
		AssetFileDescriptor assetDescriptor = null;
		
		try
		{
			assetDescriptor = Assets.getAssetFileDescriptor(soundPath);
			int resourceId = this.soundPool.load(assetDescriptor, 1);
			this.soundsMap.put(soundPath, resourceId);
		}
		catch (IOException e)
		{
		}
		finally
		{
			closeDescriptor(assetDescriptor);
		}
	}
	
	public void playSound(String soundPath)
	{
		if (this.soundsMap.containsKey(soundPath))
		{
			playbackSound(this.soundsMap.get(soundPath));
		}
		else
		{
			loadSound(soundPath);
		}
	}
	
	private void playbackSound(int resourceId)
	{
		this.soundPool.play(resourceId, 0.5f, 0.5f, 1, 0, 1f);
	}
	
	public void playMusic(String musicPath)
	{
		stopMusic();
		
		AssetFileDescriptor assetDescriptor = null;
		
		try
		{
			assetDescriptor = Assets.getAssetFileDescriptor(musicPath);
			
			this.player = new MediaPlayer();
			this.player.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			this.player.setLooping(true);
			this.player.setVolume(1f, 1f);
			
			this.player.setOnPreparedListener(new OnPreparedListener()
			{
				@Override
				public void onPrepared(MediaPlayer player)
				{
					player.start();
				}
			});
			
			this.player.prepare();
		}
		catch (IOException e)
		{
		}
		finally
		{
			closeDescriptor(assetDescriptor);
		}
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
			Collection<Integer> soundsIds = this.soundsMap.values();
			
			for (Integer soundId : soundsIds)
			{
				this.soundPool.unload(soundId);
			}
			
			this.soundPool.release();
		}
	}
	
	public boolean isAudioPlaying()
	{
		return ((this.player != null) && this.player.isPlaying());
	}
	
	private void closeDescriptor(AssetFileDescriptor assetDescriptor)
	{
		if (assetDescriptor != null)
		{
			try
			{
				assetDescriptor.close();
			}
			catch (IOException e)
			{
			}
		}
	}
}