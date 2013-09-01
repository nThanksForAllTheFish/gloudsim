package fr.imag.mescal.gloudsim.prepare;

import fr.imag.mescal.gloudsim.util.Cmd;

/**
 * Checkpointing simultaneously on the same hardware (e.g., on NFS) may encounter conflict problem. 
 * See my SC'13 paper for details.
 * @author sdi
 *
 */
public class EvaluateConflict {

	public static void main(String[] args)
	{
		if(args.length==0)
		{
			System.out.println("Usage: java EvaluateConflict [mem1] [mem2] [mem3] ....");
			System.exit(0);			
		}
		int threadNum = args.length;
		for(int i = 0;i < threadNum;i++)
		{
			String memID = args[i];
			String cmd = "cr_run ";
			new Cmd(cmd).start();
		}
	}
}
