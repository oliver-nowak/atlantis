package atlantis;

import sdljava.SDLException;
import sdljava.SDLMain;
import sdljava.event.SDLEvent;
import sdljava.image.SDLImage;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;
import util.PoolConfig;
import util.ReportManager;
import util.RandomGenerator;

public class Pool {
	private Cell pond[][]; 
	private StatCounter statCounters;
	private ReportManager reportManager;	
	private RandomGenerator utilities;
	private SDLSurface screen;
	private SDLSurface image;
	private SDLEvent event;
	
	private int[] outputBuf;
	private int sdlPitch;
	private int imagePitch;
	
	private boolean injectImage    = PoolConfig.INJECT_FILE;
	private boolean renderToScreen = PoolConfig.USE_SDL;
	
	public Pool() throws SDLException 
	{
		
	}
	
	public void injectPool() throws SDLException
	{
		initVars();	
		initRandom();
		initSDL();
		
		if (injectImage) {
			injectImageSurface(); // NOTE doesnt work yet
		}
		
		initPool();
		
		launch();
	}

	private void initVars() 
	{		
		pond 			= new Cell[PoolConfig.POND_SIZE_X][PoolConfig.POND_SIZE_Y];
		reportManager 	= new ReportManager();
		statCounters 	= StatCounter.getInstance();
		outputBuf 		= new int[PoolConfig.POND_DEPTH_SYSWORDS];		
		utilities 		= RandomGenerator.getInstance();		
	}
	
	private void initRandom() 
	{
		utilities.initRandom();
		
		for(int i = 0; i < 1024; ++i) {
			utilities.getRandom();
		}
	}
	
	private void initSDL() throws SDLException
	{
		if (renderToScreen) {
			SDLMain.init(SDLMain.SDL_INIT_VIDEO);
			SDLVideo.wmSetCaption("nanopond", "nanopond");
			screen = SDLVideo.setVideoMode(PoolConfig.POND_SIZE_X, PoolConfig.POND_SIZE_Y, 8, 0);
			
			sdlPitch = screen.getPitch();
			event = SDLEvent.waitEvent();
		}
	}
	
	private void injectImageSurface() throws SDLException
	{
		String _file = PoolConfig.IMAGE_URL;
		
		image = SDLImage.load(_file);	
		imagePitch = image.getPitch();

		SDLRect _imageMask = new SDLRect(0,0, screen.getWidth(), screen.getHeight() );
		
		int success = image.blitSurface(_imageMask, screen, _imageMask);			
		
		if (success == 0) {
			screen.flip();
		} else {
			System.out.println("blitting surface was unsuccessful");
			SDLMain.quit();
		}	
	}
	
	private void initPool()
	{
		for (int x = 0; x < PoolConfig.POND_SIZE_X; ++x) {
			for (int y = 0; y < PoolConfig.POND_SIZE_Y; ++y) {	
				System.out.println("seeding Cell " + x + " x " + y);
				pond[x][y] = new Cell();
				pond[x][y].ID = 0;
				pond[x][y].parentID = 0;
				pond[x][y].lineage = 0;
				pond[x][y].generation = 0;
				pond[x][y].energy = 0;
				for (int i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS; ++i) {
					pond[x][y].genome[i] = ~((int)0);
				}
			}
		}
	}
			
	private void launch() throws SDLException
	{
		Cell pptr;
		Cell tmpptr;
		
		int x;
		int y;
		int i;		
		int currentWord;
		int wordPtr;
		int shiftPtr;
		int inst;
		int tmp;	
		int ptr_wordPtr;
		int ptr_shiftPtr;		
		int reg;		
		int facing;				
		int loopStackPtr;		
		int falseLoopDepth;	
		int stop;
		
		int[] loopStack_wordPtr = new int[PoolConfig.POND_DEPTH];
		int[] loopStack_shiftPtr = new int[PoolConfig.POND_DEPTH];
		
		long clock = 0;
		long cellIdCounter = 0;
		
		for (;;) {
			
			while (SDLEvent.pollEvent() != null) {
				System.out.println(event.getType());
				if (event.getType() == SDLEvent.SDL_QUIT) {
					SDLMain.quit();
				}
			}
			
			System.out.println("clock : " + clock);
			
			if ((++clock % PoolConfig.REPORT_FREQUENCY) == 1) {
				reportManager.doReport(clock, pond);
				
				if (renderToScreen) {
					screen.updateRect(0, 0, PoolConfig.POND_SIZE_X, PoolConfig.POND_SIZE_Y);
				}				
			}
			
			if ((clock % PoolConfig.DUMP_FREQUENCY) == 1) {
				reportManager.doDump(clock, pond);
			}
			
			if ((clock % PoolConfig.INFLOW_FREQUENCY) == 1) {
				x = utilities.getRandom() % PoolConfig.POND_SIZE_X;
				y = utilities.getRandom() % PoolConfig.POND_SIZE_Y;

				pptr = pond[x][y];
				pptr.ID = cellIdCounter;
				pptr.parentID = 0;
				pptr.lineage = cellIdCounter;
				pptr.generation = 0;
				pptr.energy += PoolConfig.INFLOW_RATE_BASE + (utilities.getRandom() % PoolConfig.INFLOW_RATE_VARIATION);
				
				if (injectImage) {
					for (i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS; ++i) {
						pptr.genome[i] = image.getPixelData().get(x + (y*sdlPitch));
					}
				} else {
					for (i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS; ++i) { 
						pptr.genome[i] = utilities.getRandom();	
					}
				}
							
				++cellIdCounter;
				
				if (renderToScreen) {
					if (screen.mustLock()) {
						screen.lockSurface();
					}

					screen.getPixelData().put(x + (y * sdlPitch), getColor(pptr));
					
					if (screen.mustLock()) {
						screen.unlockSurface();
					}
				}				
			}
						
			x = utilities.getRandom() % PoolConfig.POND_SIZE_X;
			y = utilities.getRandom() % PoolConfig.POND_SIZE_Y;
			pptr = pond[x][y];
			
			for (i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS; ++i) {
				outputBuf[i] = ~((int)0);
			}
			
			ptr_wordPtr = 0;
			ptr_shiftPtr = 0;
			reg = 0;
			loopStackPtr = 0;
			wordPtr = PoolConfig.EXEC_START_WORD;
			shiftPtr = PoolConfig.EXEC_START_BIT;
			facing = 0;
			falseLoopDepth = 0;
			stop = 0;
			
			currentWord = pptr.genome[0];
			
			statCounters.cellExecutions += 1.0;
			
			while (pptr.energy > 0 && stop != 1) {
				inst = (currentWord >> shiftPtr) & 0xf;

				if ((utilities.getRandom() & 0xffffffff) < PoolConfig.MUTATION_RATE) {
					tmp = utilities.getRandom();
					if ( (tmp & 0x80) == 1) {
						inst = tmp & 0xf;
					} else {
						reg = tmp & 0xf;
					}
				}
				
				--pptr.energy;
				
				if (falseLoopDepth == 1) {
					if (inst == 0x9) {
						++falseLoopDepth;
					} 
					else if (inst == 0xa) {
						--falseLoopDepth;
					}
				} else {
					statCounters.instructionExecutions[inst] += 1.0;
					
					switch(inst) {
					case 0x0: // ZERO
						reg = 0;
						ptr_wordPtr = 0;
						ptr_shiftPtr = 0;
						facing = 0;
						break;
					case 0x1: // FWD
						ptr_shiftPtr +=4;
						if (ptr_shiftPtr >= PoolConfig.SYSWORD_BITS) {
							ptr_wordPtr++;
							if (ptr_wordPtr >= PoolConfig.POND_DEPTH_SYSWORDS) {
								ptr_wordPtr = 0;
							}
							ptr_shiftPtr = 0;
						}
						break;
					case 0x2: // BACK
						if (ptr_shiftPtr > 0) {
							ptr_shiftPtr -= 4;
						} else {
							if (ptr_wordPtr > 0) {
								--ptr_wordPtr;
							} else {
								ptr_wordPtr = PoolConfig.POND_DEPTH_SYSWORDS - 1; 
							}
							ptr_shiftPtr = PoolConfig.SYSWORD_BITS - 4;
						}
						break;
					case 0x3: // INC
						reg = (reg + 1) & 0xf;
						break;
					case 0x4: // DEC
						reg = (reg - 1) & 0xf;
						break;
					case 0x5: // READG
						reg = (pptr.genome[ptr_wordPtr] >> ptr_shiftPtr) & 0xf;
						break;
					case 0x6: // WRITEG
						pptr.genome[ptr_wordPtr] &= ~(((int)0xf) << ptr_shiftPtr);
						pptr.genome[ptr_wordPtr] |= reg << ptr_shiftPtr;
						currentWord = pptr.genome[wordPtr];
						break;
					case 0x7: // READB
						reg = (outputBuf[ptr_wordPtr] >> ptr_shiftPtr) & 0xf;
						break;
					case 0x8: // WRITEB
						outputBuf[ptr_wordPtr] &= ~(((int)0xf) << ptr_shiftPtr);
						outputBuf[ptr_wordPtr] |= reg << ptr_shiftPtr;
						break;
					case 0x9: // LOOP
						if (reg > 0) {
							if (loopStackPtr >= PoolConfig.POND_DEPTH) {
								stop = 1;
							} else {
								loopStack_wordPtr[loopStackPtr] = wordPtr;
								loopStack_shiftPtr[loopStackPtr] = shiftPtr;
								++loopStackPtr;
							}
						} else {
							falseLoopDepth = 1;
						}
						break;
					case 0xa: // REP
						if (loopStackPtr > 0) {
							--loopStackPtr;
							if (reg > 0) {
								wordPtr = loopStack_wordPtr[loopStackPtr];
								shiftPtr = loopStack_shiftPtr[loopStackPtr];
								currentWord = pptr.genome[wordPtr];
								continue;
							}
						}
						break;
					case 0xb: // TURN
						facing = reg & 3;
						break;
					case 0xc: // XCHG
						shiftPtr +=4;
						if (shiftPtr >= PoolConfig.SYSWORD_BITS) {
							wordPtr++;
							if (wordPtr >= PoolConfig.POND_DEPTH_SYSWORDS) {
								wordPtr = PoolConfig.EXEC_START_WORD;
								shiftPtr = PoolConfig.EXEC_START_BIT;
							} else {
								shiftPtr = 0;
							}
						}
						tmp = reg;
						reg = (pptr.genome[wordPtr] >> shiftPtr) & 0xf;
						pptr.genome[wordPtr] &= ~(((int)0xf) << shiftPtr);
						pptr.genome[wordPtr] |= tmp << shiftPtr;
						currentWord = pptr.genome[wordPtr];
						break;
					case 0xd: // KILL (#13!!)
						tmpptr = getNeighbor(x,y,facing);
						if (accessAllowed(tmpptr, reg, false)== true) {
							if (tmpptr.generation > 2) {
								++statCounters.viableCellsKilled;																
							}
							tmpptr.genome[0] = ~((int)0);
							tmpptr.genome[1] = ~((int)0);
							tmpptr.ID = cellIdCounter;
							tmpptr.parentID = 0;
							tmpptr.lineage = cellIdCounter;
							tmpptr.generation = 0;
							++cellIdCounter;
						} else if (tmpptr.generation > 2) {
							tmp = pptr.energy / PoolConfig.FAILED_KILL_PENALTY;
							if (pptr.energy > tmp) {
								pptr.energy -= tmp;
							} else {
								pptr.energy = 0;
							}
						}
						break;
					case 0xe: // SHARE
						tmpptr = getNeighbor(x,y,facing);
						if (accessAllowed(tmpptr, reg, true) == true) {
							if (tmpptr.generation > 2) {
								++statCounters.viableCellShares;								
							}
							tmp = pptr.energy + tmpptr.energy;
							tmpptr.energy = tmp / 2;
							pptr.energy = tmp - tmpptr.energy;
						}
						break;
					case 0xf: // STOP
						stop = 1;
						break;
					}
				}
				
				shiftPtr +=4;
				if (shiftPtr >= PoolConfig.SYSWORD_BITS) {
					wordPtr++;
					if (wordPtr >= PoolConfig.POND_DEPTH_SYSWORDS) {
						wordPtr = PoolConfig.EXEC_START_WORD;
						shiftPtr = PoolConfig.EXEC_START_BIT;
					} else {
						shiftPtr = 0;
					}
					currentWord = pptr.genome[wordPtr];
				}				
			}
			
			if ((outputBuf[0] & 0xff) != 0xff) {
				tmpptr = getNeighbor(x,y,facing);
				if ((tmpptr.energy > 0) && accessAllowed(tmpptr, reg, false) == true) {
					if (tmpptr.generation > 2) {
						++statCounters.viableCellsReplaced;
					}
					tmpptr.ID = ++cellIdCounter;
					tmpptr.parentID = pptr.ID;
					tmpptr.lineage = pptr.lineage;
					tmpptr.generation = pptr.generation + 1;
					for (i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS; ++i) {
						tmpptr.genome[i] = outputBuf[i];
					}
				}
			}
			
			if (renderToScreen) {
				if (screen.mustLock()) {
					screen.lockSurface();
				}
							
				screen.getPixelData().put(x + (y*sdlPitch), getColor(pptr));
				
				if (x > 0) {
					screen.getPixelData().put((x-1) + (y*sdlPitch), getColor(pond[x-1][y]));
					if (x < (PoolConfig.POND_SIZE_X-1)) {
						screen.getPixelData().put((x+1) + (y*sdlPitch), getColor(pond[x+1][y]));
					} else {
						screen.getPixelData().put((0) + (y*sdlPitch), getColor(pond[0][y]));
					}
				} else {
					screen.getPixelData().put((PoolConfig.POND_SIZE_X-1) + (y*sdlPitch), getColor(pond[PoolConfig.POND_SIZE_X-1][y]));
					screen.getPixelData().put((1) + (y*sdlPitch), getColor(pond[1][y]));
				}
				
				if (y > 0) {
					screen.getPixelData().put((x) + ((y-1)*sdlPitch), getColor(pond[x][y-1]));
					if (y < (PoolConfig.POND_SIZE_Y-1)) {
						screen.getPixelData().put((x) + ((y+1)*sdlPitch), getColor(pond[x][y+1]));
					} else {
						screen.getPixelData().put((x), getColor(pond[x][0]));
					}
				} else {
					screen.getPixelData().put((x) + ((PoolConfig.POND_SIZE_Y-1)*sdlPitch), getColor(pond[x][PoolConfig.POND_SIZE_Y-1]));
					screen.getPixelData().put((x + sdlPitch), getColor(pond[x][1]));
				}
				
				if (screen.mustLock()) {
					screen.unlockSurface();
				}
			}
		}
	}
		
	private Cell getNeighbor(int _x, int _y, int _dir) 
	{
		switch(_dir) 
		{
		case PoolConfig.N_LEFT:
			if (_x > 0) {
				return pond[_x-1][_y];
			} else {
				return pond[PoolConfig.POND_SIZE_X -1][_y];
			}
		case PoolConfig.N_RIGHT:
			if (_x < (PoolConfig.POND_SIZE_X - 1)) {
				return pond[_x+1][_y];
			} else {
				return pond[0][_y];
			}
		case PoolConfig.N_UP:
			if (_y > 0) {
				return pond[_x][_y - 1];
			} else {
				return pond[_x][PoolConfig.POND_SIZE_Y - 1];
			}
		case PoolConfig.N_DOWN:
			if (_y < (PoolConfig.POND_SIZE_Y - 1)) {
				return pond[_x][_y+1];
			} else {
				return pond[_x][0];
			}
		}
		return pond[_x][_y];
	}
	
	private boolean accessAllowed(Cell _c2, int _c1guess, boolean _sense)
	{ 
		if (_sense == true) {
			return (((utilities.getRandom() & 0xf) >= PoolConfig.BITS_IN_FOURBIT_WORD[(_c2.genome[0] & 0xf) ^ (_c1guess & 0xf)])||(_c2.parentID == 0));
		} else {
			return (((utilities.getRandom() & 0xf) <= PoolConfig.BITS_IN_FOURBIT_WORD[(_c2.genome[0] & 0xf) ^ (_c1guess & 0xf)])||(_c2.parentID == 0));
		}
	}
	
	private byte getColor(Cell _c)
	{
		int _word;
		int _sum;
		int _opcode; 
		int _skipnext;

		if ( _c.energy > 0) {
			switch(PoolConfig.colorScheme) 
			{
			case PoolConfig.KINSHIP:
				if (_c.generation > 2) { 
					_sum = 0;
					_skipnext = 0;
					for (int i = 0; i < PoolConfig.POND_DEPTH_SYSWORDS && (_c.genome[i] != ~((int)0));++i) {
						_word = _c.genome[i];
						for(int j = 0; j < PoolConfig.SYSWORD_BITS/4; ++j, _word >>=4) {
							_opcode = _word & 0xf;
							if (_skipnext > 0) {
								_skipnext = 0;
							} else {
								if (_opcode != 0xf) {
									_sum += _opcode;
								}
								if (_opcode == 0xc) {
									_skipnext = 1;
								}
							}
						}
					}
					return (byte) ((_sum % 192) + 64);
				}
				break;
			case PoolConfig.LINEAGE:
				return (byte) ((_c.generation > 1) ? (((byte) _c.lineage) | (byte)1) : 0);
			case PoolConfig.MAX_COLOR_SCHEME:
				break;
			}
		}
		return (byte) 0;
	}
}
