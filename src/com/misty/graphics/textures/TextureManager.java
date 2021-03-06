package com.misty.graphics.textures;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.misty.utils.Assets;

public class TextureManager
{
	private static final Object lock = new Object();
	private static final Map<String, Texture> loadedTextures = new HashMap<String, Texture>();
	
	public static void loadTextures(String... texturesPath)
	{
		for (String texturePath : texturesPath)
		{
			TextureManager.loadTexture(texturePath);
		}
	}
	
	public static Texture loadTexture(String texturePath)
	{
		Texture result = new Texture(texturePath);
		
		synchronized (TextureManager.lock)
		{
			TextureManager.loadedTextures.put(texturePath, result);
		}
		
		return result;
	}
	
	public static void reloadTextures()
	{
		synchronized (TextureManager.lock)
		{
			Collection<Texture> textures = TextureManager.loadedTextures.values();
			
			for (Texture texture : textures)
			{
				texture.reload();
			}
		}
	}
	
	public static Texture getTexture(String texturePath)
	{
		Texture result = null;
		
		synchronized (TextureManager.lock)
		{
			if (TextureManager.loadedTextures.containsKey(texturePath))
			{
				result = TextureManager.loadedTextures.get(texturePath);
			}
			else
			{
				result = TextureManager.loadTexture(texturePath);
			}
		}
		
		return result;
	}
	
	protected static Bitmap getBitmap(String texturePath)
	{
		Bitmap result = null;
		InputStream inputStream = null;
		
		try
		{
			inputStream = Assets.getInputStream(texturePath);
			result = BitmapFactory.decodeStream(inputStream);
		}
		catch (Exception e)
		{
		}
		finally
		{
			Assets.close(inputStream);
		}
		
		return result;
	}
}