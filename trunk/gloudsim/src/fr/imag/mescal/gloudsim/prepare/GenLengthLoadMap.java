package fr.imag.mescal.gloudsim.prepare;

import fr.imag.mescal.gloudsim.sim.vmserver.TaskExecutor;

/**
 * Characterize the relation between task length and tick
 * see TaskExecutor.perfSimulation(length, tick).
 * @author sdi
 *
 */
public class GenLengthLoadMap {
	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("Usage: java GenLengthLoadMap [length] [tick]");
			System.exit(0);
		}
		System.out.println("begin....");
		float length = Float.parseFloat(args[0]);
		float tick = Float.parseFloat(args[1]);
		double init = System.currentTimeMillis()/1000.0;
		TaskExecutor.perfSimulation(length, tick);
		double end = System.currentTimeMillis()/1000.0;
		float duration = (float)(end - init);
		float tuneRate = length/duration;
		System.out.println("length="+length+";tick="+tick+";duration="+duration+";tuneRate="+tuneRate);
		System.out.println("done.");
	}

}
