package fr.imag.mescal.gloudsim.sim.mainserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.RdGenerator;

/**
 * used to simulate failure events based on the trace.
 * @author sdi
 *
 */
public class FailureEventSimulator {
	
	private List<float[]> sampleArrayList = null; //based on priority
	public static float[] MTBF; 
	
	public FailureEventSimulator(List<float[]> sampleArrayList) {
		this.sampleArrayList = sampleArrayList;
	}
	
	public FailureEventSimulator(String filePath)
	{
		loadSamples(filePath);
	}
	
	public FailureEventSimulator()
	{}
	
	/**
	 * 
	 * @param filePath
	 * Example of content: 
	 * 			# 230.1234
	 * 			123.5
	 * 			230.2
	 * 			198.6
	 * 			.....
	 * @return
	 */
	private List<double[]> loadSamples(String filePath)
	{		
		List<double[]> sampleList = new ArrayList<double[]>();
		for(int i = 0;i<12;i++)
		{
			String priority = String.valueOf(i);
			String file = filePath.replace("PRIORITY", priority);
			List<String> lineList = FileControler.readFile(file);
			double[] sample = new double[lineList.size()-1];
			Iterator<String> iter = lineList.iterator();
			String metaLine = iter.next();
			String[] s = metaLine.split("\\s"); //# 230.1234
			MTBF[i] = Float.parseFloat(s[1]);
			for(int j = 0;iter.hasNext();j++)
			{
				String line = iter.next();
				sample[j] = Float.parseFloat(line);
			}
			sampleList.add(sample);
		}
		return sampleList;
	}
	
	public float[] genFailures(int numOfFailures, int priority)
	{
		float[] failures = new float[numOfFailures];
		float[] samples = sampleArrayList.get(priority);
		for(int i = 0;i<numOfFailures;i++)
		{
			int index = RdGenerator.RAN_SeedGen.generate_Int(0, samples.length-1);
			failures[i] = samples[index];
		}
		return failures;
	}
	
	public float[] genFailures(float taskLength, int priority)
	{
		int numOfFailures = (int)(taskLength/MTBF[priority]);
		return genFailures(numOfFailures, priority);
	}
	
	public float genFailure(int priority)
	{
		float[] samples = sampleArrayList.get(priority);
		int index = RdGenerator.RAN_SeedGen.generate_Int(0, samples.length-1);
		return samples[index];
	}
	
}