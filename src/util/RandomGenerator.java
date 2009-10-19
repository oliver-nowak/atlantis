package util;

import java.util.Random;


public class RandomGenerator {
	
	public static Random rng;
	public static MersenneTwisterFast mt;
	
	private static RandomGenerator instance;
	
	public static RandomGenerator getInstance()
	{
		if (instance == null) {
			instance = new RandomGenerator();
		}
		return instance;
	}
	
	private RandomGenerator() 
	{
		
	}
	
	public void initRandom() 
	{
		mt = new MersenneTwisterFast();
	}
	
	public int getRandom()
	{		
		return mt.nextInt(Integer.MAX_VALUE);
	}	
}
