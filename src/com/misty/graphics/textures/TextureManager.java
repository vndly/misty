package com.misty.graphics.textures;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.res.AssetManager;

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
	
	public static InputStream getTextureStream(String texturePath) throws IOException
	{
		return TextureManager.assetManager.open(texturePath);
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