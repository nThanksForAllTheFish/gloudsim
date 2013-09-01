package fr.imag.mescal.gloudsim.test;

public class TestProgram {

	static Integer mutex = new Integer(0);
	static long remainLoad = 1000000000000l;
	public static void main(String[] args)
	{
		System.out.println("begin....");
		int sleepMilliSec = Integer.parseInt(args[0]);
		while(remainLoad>0)
		{
			synchronized (mutex) {
				remainLoad -= sleepMilliSec;
			}
			if(sleepMilliSec>0)
			{
				try {
					Thread.sleep(sleepMilliSec);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		
		System.out.println("done.");
	}
}
