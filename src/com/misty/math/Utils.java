package com.misty.math;

import java.util.Random;

public class Utils
{
	private static Random random = new Random();

	public static int random(int min, int max)
	{
		return Utils.random.nextInt(max - min) + min;
	}
}