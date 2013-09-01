package fr.imag.mescal.gloudsim.util;

public class Communication {
	
	private static int portCounter = 4000; // 4000 - 6000
	
	public static synchronized int genPort()
	{
		portCounter++;
		if(portCounter==6000)
			portCounter = 4000;
		return portCounter;
	}
}
