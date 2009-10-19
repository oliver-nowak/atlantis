package atlantis;


public class StatCounter {
	
	public double[] instructionExecutions = new double[16];
	public double cellExecutions;
	public int viableCellsReplaced;
	public int viableCellsKilled;
	public int viableCellShares;
	
	private static StatCounter instance;
	
	public static StatCounter getInstance()
	{
		if (instance == null) {
			instance = new StatCounter();
		}
		return instance;
	}
	
	private StatCounter()
	{
		
	}

	public void reset()
	{
		instructionExecutions = new double[16];
		cellExecutions = 0.0;
		viableCellsReplaced = 0;
		viableCellsKilled = 0;
		viableCellShares = 0;
	}
}
