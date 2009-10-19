package atlantis;


import sdljava.SDLException;

public class Atlantis {
	
	private static Pool nanoPond;
	
	public static void main(String[] args) throws SDLException
	{
		nanoPond = new Pool();	
		nanoPond.injectPool();
		System.out.println("Starting...");
	}	
}
