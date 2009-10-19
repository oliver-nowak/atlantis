package atlantis;

import util.PoolConfig;

public class Cell {
	
	public long ID;
	public long parentID;
	public long lineage;
	public int generation;
	public int energy;
	public int[] genome = new int[PoolConfig.POND_DEPTH_SYSWORDS];
	

	Cell() 
	{
		ID = 0;
		parentID = 0;
		lineage = 0;
		generation = 0;
		energy = 0;
		genome = new int[PoolConfig.POND_DEPTH_SYSWORDS];
	}
	
}
