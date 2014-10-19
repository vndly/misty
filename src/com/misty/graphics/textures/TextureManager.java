package com.misty.graphics.textures;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TextureManager
{
	private static AssetManager assetManager;
	private static Map<String, Texture> loadedTextures = new HashMap<String, Texture>();
	
	public static void initialize(Context context)
	{
		TextureManager.assetManager = context.getAssets();
	}
	
	public static void loadTextures(String... texturesPath)
	{
		for (String texturePath : texturesPath)
		{
			TextureManager.loadTexture(texturePath);
		}
	}

	public static Bitmap getBitmap(String texturePath)
	{
		Bitmap result = null;
		InputStream input = null;

		try
		{
			input = TextureManager.assetManager.open(texturePath);
			result = BitmapFactory.decodeStream(input);
		}
		catch (Exception e)
		{
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (Exception e)
				{
				}
			}
		}

		return result;
	}
	
	public static Texture loadTexture(String texturePath)
	{
		Texture result = new Texture(texturePath);
		TextureManager.loadedTextures.put(texturePath, result);
		
		return result;
	}
	
	public static void reloadTextures()
	{
		Collection<Texture> textures = TextureManager.loadedTextures.values();
		
		for (Texture texture : textures)
		{
			texture.reload();
		}
	}
	
	public static Texture getTexture(String texturePath)
	{
		Texture result = null;
		
		if (TextureManager.loadedTextures.containsKey(texturePath))
		{
			result = TextureManager.loadedTextures.get(texturePath);
		}
		else
		{
			result = TextureManager.loadTexture(texturePath);
		}
		
		return result;
	}
}