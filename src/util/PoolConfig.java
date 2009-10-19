package util;

public class PoolConfig {
	
	public static final boolean INJECT_FILE = false;
	public static final boolean USE_SDL = true;
	
	public static final String REPORT_URL = ".\\output\\report_.csv";
	public static final String CELL_DUMP_URL = "output\\cellDump_.";
	
	public static final String IMAGE_URL = ".\\input\\testSubject.bmp";
	
	public static final int REPORT_FREQUENCY = 100000;
	public static final int DUMP_FREQUENCY = 1000000;

	public static final int MUTATION_RATE = 10737;
	public static final int INFLOW_FREQUENCY = 100;
	public static final int INFLOW_RATE_BASE = 4000;
	public static final int INFLOW_RATE_VARIATION = 8000;
	public static final int POND_SIZE_X = 640;
	public static final int POND_SIZE_Y = 480;
	public static final int POND_DEPTH = 512;
	public static final int FAILED_KILL_PENALTY = 2;
	
	public static final int POND_DEPTH_SYSWORDS = (POND_DEPTH / 16);											
	public static final int SYSWORD_BITS = 32;
	
	public static final int KINSHIP = 0;
	public static final int LINEAGE = 1;
	public static final int MAX_COLOR_SCHEME = 2;
	
	public static final int colorScheme = KINSHIP;
	
	public static final byte N_LEFT = 0;
	public static final byte N_RIGHT = 1;
	public static final byte N_UP = 2;
	public static final byte N_DOWN = 3;
	
	public static final byte EXEC_START_WORD = 0;
	public static final byte EXEC_START_BIT = 4;
	
	public static final byte[] BITS_IN_FOURBIT_WORD = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};
		
	private static PoolConfig instance;

	public static PoolConfig getInstance() {
		if (instance == null) {
			instance = new PoolConfig();			
		}
		return instance;
	}
	
	private PoolConfig() {
		
	}
}
