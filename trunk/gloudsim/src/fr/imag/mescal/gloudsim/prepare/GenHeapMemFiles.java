package fr.imag.mescal.gloudsim.prepare;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * This class is to generate a lot of files, whose sizes are 2, 4, 6, ...., 200 MB
 * 
 * @author sdi
 *
 */
public class GenHeapMemFiles {

	static String targetDir;
	static int start;
	static int end;
	static int interval;
	static String extention = "heap";
	public static void main(String[] args)
	{
		System.out.println("begin...");
		if(args.length!=4)
		{
			System.out.println("Usage: java GenHeapMemFiles [targetDir] [start] [end] [interval]");
			System.out.println("Example: java GenHeapMemFiles /cloudNFS/CheckpointSim/heapMemFiles 2 10 2");
			System.exit(0);
		}
		targetDir = args[0];
		start = Integer.parseInt(args[1]);
		end = Integer.parseInt(args[2]);
		interval = Integer.parseInt(args[3]);
		
		for(int i = start;i<=end;i+=interval)
		{
			String genFileName = targetDir+"/"+i+"."+extention;
			FileControler.genFileWithSize(genFileName, i); //generate a file with ix1024x1024Mb
			if(i%10==0)
				System.out.println("i="+i);
		}
		System.out.println("done.");
	}
}
