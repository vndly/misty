package com.misty.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.json.JSONException;
import org.json.JSONObject;
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
	
	public static JSONObject getJsonObject(String path) throws IOException, JSONException
	{
		String content = Assets.readFile(path);
		
		return new JSONObject(content);
	}
	
	public static String readFile(String path) throws IOException
	{
		String result = "";
		
		InputStream inputStream = null;
		
		try
		{
			inputStream = Assets.getInputStream(path);
			result = Assets.readInputStreamAsString(inputStream);
		}
		finally
		{
			Assets.close(inputStream);
		}
		
		return result;
	}
	
	private static String readInputStreamAsString(InputStream inputStream) throws IOException
	{
		String result = "";
		
		Reader reader = null;
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder builder = new StringBuilder();
			int data = 0;
			
			while ((data = reader.read()) != -1)
			{
				builder.append((char)data);
			}
			
			result = builder.toString();
		}
		finally
		{
			Assets.close(reader);
		}
		
		return result;
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