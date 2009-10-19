package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import atlantis.Cell;
import atlantis.StatCounter;

public class ReportManager {
	
	private boolean reportFormatted = false;
	private boolean cellDumpFormatted = false;
	
	private long lastTotalViableReplicators = 0;
	
	private StatCounter statCounters;
	
	
	public ReportManager() 
	{
		statCounters = StatCounter.getInstance();
	}

	public void renderDump(long clock, byte[][] pondColorArray)
	{
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(".\\output\\renderDump.txt",true));
			pw.println("writing render log");
			
			for (int y = 0; y < PoolConfig.POND_SIZE_Y; ++y) {
				for (int x = 0; x < PoolConfig.POND_SIZE_X; ++x) {			
					pw.print(pondColorArray[x][y] + " ");
				}
				pw.println();
			}
		
			pw.flush();
			pw.close();
			
		} catch (Exception e) {}
	}
	
	public void doReport(long clock, Cell[][] pond) 
	{
		int x, y;
		int totalActiveCells = 0;
		long totalEnergy = 0;
		long totalViableReplicators = 0;
		int maxGeneration = 0;
		boolean append = true;
		
		for (x = 0; x < PoolConfig.POND_SIZE_X; ++x) {
			for (y = 0; y < PoolConfig.POND_SIZE_Y; ++y) {
				Cell _c = pond[x][y];
				if (_c.energy > 0) {
					++totalActiveCells;
					totalEnergy += _c.energy;
					if (_c.generation > 2) {
						++totalViableReplicators;
					}
					if (_c.generation > maxGeneration) {
						maxGeneration = _c.generation;							
					}
				}
			}
		}
		
		float _fltTotalActiveCells = totalActiveCells;
		float _fltPondSize = (PoolConfig.POND_SIZE_X * PoolConfig.POND_SIZE_Y);
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(PoolConfig.REPORT_URL,append));
			
			if (!reportFormatted) {
				pw.println("clock" + " size" + " totalCells" + " totalEnergy" + " totalActiveCells" + " percentActive" + 
						" totalViableReplicators" +" maxGeneration" + " viableCellsReplaced" + " viableCellsKilled" + 
						" viableCellsShares" + " ZERO" + " FWD" + " BACK" + " INC" + " DEC" + " READG" + " WRITEG" + 
						" READB" + " WRITEB" + " LOOP" + " REP" + " TURN" + " XCHG" + " KILL" + " SHARE" + " STOP" + 
						" averageExecutionFrequency" + " averageMetabolism");
				
				reportFormatted = true;
			}
					
			pw.print(clock + " " + 
					 (PoolConfig.POND_SIZE_X + "x" + PoolConfig.POND_SIZE_Y) + " " +
					 (PoolConfig.POND_SIZE_X * PoolConfig.POND_SIZE_Y) + " " +					 
					 totalEnergy + " " + 
					 totalActiveCells + " " + 
					 ((_fltTotalActiveCells / _fltPondSize) * 100) + " " +
					 totalViableReplicators + " " + 
					 maxGeneration + " " + 
					 statCounters.viableCellsReplaced + " " + 
					 statCounters.viableCellsKilled + " " + 
					 statCounters.viableCellShares + " ");
			
			double totalMetabolism = 0.0;
			
			for (int _x = 0; _x < 16; ++_x) {
				totalMetabolism += statCounters.instructionExecutions[_x];
				
				Double _avgExecFreq;
				if (statCounters.cellExecutions > 0.0) {
					_avgExecFreq = statCounters.instructionExecutions[_x] / statCounters.cellExecutions;
				} else {
					_avgExecFreq = 0.0;
				}
				pw.print(_avgExecFreq + " ");
			}
			
			Double _avgMetabolism;
			if (statCounters.cellExecutions > 0.0) {
				_avgMetabolism = totalMetabolism / statCounters.cellExecutions;
			} else {
				_avgMetabolism = 0.0;
			}
			pw.println(_avgMetabolism + " ");
						
			if (lastTotalViableReplicators > 0 && totalViableReplicators == 0) {
				pw.println("[EVENT] Viable replicators have gone extinct. Please reserve a moment of silence.\n");
			}
			else if (lastTotalViableReplicators == 0 && totalViableReplicators > 0) {
				pw.println("[EVENT] Viable replicators have appeared!");
			}
					
			lastTotalViableReplicators = totalViableReplicators;			
			statCounters.reset();
			
			pw.flush();
			pw.close();
		} catch (Exception e) {
			System.out.println(">>>>>>>>>>>>>>>>>>>ERROR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
			System.out.println(e + " [ERROR] Could not open file for writing.");
			e.printStackTrace();
			System.out.println(">>>>>>>>>>>>>>>>>>>ERROR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		}
	}
	
	public void doDump(long clock, Cell[][] pond) 
	{
		String file = PoolConfig.CELL_DUMP_URL + clock;
		int x, y, _wordPtr, _shiftPtr, _inst, _stopCount, i;
		Cell pptr;
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.println("dumping cells to file");
			System.out.println("[INFO] Dumping viable cells at time " + clock);
			
			if (!cellDumpFormatted) {
				pw.println("ID" + " parentID" + " lineage" + " generation" + " genome");
				
				cellDumpFormatted = true;
			}
			
			for (y = 0; y < PoolConfig.POND_SIZE_Y; ++y) {
				for (x = 0; x < PoolConfig.POND_SIZE_X; ++x) {				
					pptr = pond[x][y];

					if (pptr.energy > 0 && pptr.generation > 2) {
						pw.println();
						pw.print(pptr.ID + "," + 
								 pptr.parentID + "," + 
								 pptr.lineage + "," + 
								 pptr.generation + ",");
						
						_wordPtr = 0;
						_shiftPtr = 0;
						_stopCount = 0;
						
						for (i = 0; i < PoolConfig.POND_DEPTH; ++i) {
							_inst = ( pptr.genome[_wordPtr] >> _shiftPtr	) & 0xf;
							pw.print(Integer.toHexString(_inst));
							if (_inst == 0xf) {
								if (++_stopCount >= 4) {
									break;
								}
							} else {
								_stopCount = 0;
							}
							_shiftPtr += 4;
							if (_shiftPtr >= PoolConfig.SYSWORD_BITS) {
								_wordPtr++;
								if (_wordPtr >= PoolConfig.POND_DEPTH_SYSWORDS) {
									_wordPtr = 0;
									_shiftPtr = 4;
								} else {
									_shiftPtr = 0;
								}
							}
						}						
					}					
				}
			}
			pw.println("done");
			pw.flush();
			pw.close();
		} catch (Exception e){
			System.out.println(">>>>>>>>>>>>>>>>>>>ERROR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
			System.out.println(e + " [ERROR] Could not open file for writing.");
			e.printStackTrace();
			System.out.println(">>>>>>>>>>>>>>>>>>>ERROR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		}	
	}
	
	public void dumpCell(String _file, Cell _cell) {
		int _wordPtr, _shiftPtr, _inst, _stopCount, i;
		
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(_file)));
			
			if (_cell.energy > 0 && _cell.generation > 2) {
				_wordPtr = 0;
				_shiftPtr = 0;
				_stopCount = 0;
				for (i = 0; i < PoolConfig.POND_DEPTH; ++i) {
					_inst = (_cell.genome[_wordPtr] >> _shiftPtr) & 0xf;
					pw.print(_inst);
					if (_inst == 0xf) {
						if (++_stopCount >= 4) {
							break;
						}
					} else {
						_stopCount = 0;
					}
					if ((_shiftPtr += 4) >= PoolConfig.SYSWORD_BITS) {
						if (++_wordPtr >= PoolConfig.POND_DEPTH_SYSWORDS) {
							_wordPtr = PoolConfig.EXEC_START_WORD;
							_shiftPtr = PoolConfig.EXEC_START_BIT;
						} else {
							_shiftPtr = 0;
						}
					}
				}
			}
			pw.flush();
			pw.close();
		} catch (Exception e){}
	}
}
