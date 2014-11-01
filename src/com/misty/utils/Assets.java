package com.misty.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class Assets
{
	private static Context context;
	
	public static void initialize(Context context)
	{
		Assets.context = context;
	}
	
	public static InputStream getInputStream(String path) throws IOException
	{
		return Assets.context.getAssets().open(path);
	}
	
	public static AssetFileDescriptor getAssetFileDescriptor(String path) throws IOException
	{
		return Assets.context.getAssets().openFd(path);
	}
	
	public static void close(Closeable resource)
	{
		if (resource != null)
		{
			try
			{
				resource.close();
			}
			catch (IOException e)
			{
			}
		}
	}
}