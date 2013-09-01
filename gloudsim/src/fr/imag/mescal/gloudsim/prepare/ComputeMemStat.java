package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * compute statistics of memory used in Google trace, e.g., average, length, etc.
 * @author sdi
 *
 */
public class ComputeMemStat {
	
	public static float memCapacity = 32000;
	
	public static void main(String[] args)
	{
		String outputFile = "mem.stat";
		String outputFile2 = "length.stat";

		List<String> memList = new ArrayList<String>();
		memList.addAll(genMemStat("single"));
		memList.addAll(genMemStat("batch"));
		memList.addAll(genMemStat("mix"));
		
		FileControler.print2File(memList, outputFile);
		
		List<String> lengthList = new ArrayList<String>();
		lengthList.addAll(genBTLengthStat("single"));
		lengthList.addAll(genBTLengthStat("batch"));
		lengthList.addAll(genBTLengthStat("mix"));
		
		FileControler.print2File(lengthList, outputFile2);
		System.out.println("done.");
		
	}
	
	private static List<String> genMemStat(String mode)
	{
		List<String> memList = new ArrayList<String>();
		
		memList.add("*mode="+mode+" ============================="); //single, batch, or mix
		
		int totalSize = 0;
		float sumMem = 0;
		float minMem = Float.MAX_VALUE;
		float maxMem = 0;
		String objFile = "simFailureTrace/"+mode+"/jobTrace.obj";

		List<String> tmpList = new ArrayList<String>();
		
		JobTrace jt = FileControler.loadJobTraceFromFile(objFile);
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					totalSize++;
					float mem = bt.getMeanMemSize()*memCapacity;
					sumMem += mem;
					if(minMem>mem) minMem = mem;
					if(maxMem<mem) maxMem = mem;
					tmpList.add(String.valueOf(mem));
				}
			}
		}
		float meanMem = sumMem/totalSize;
		memList.add("min ="+minMem);
		memList.add("max ="+maxMem);
		memList.add("mean="+meanMem);
		memList.addAll(tmpList);
		return memList;
	}
	
	private static List<String> genBTLengthStat(String mode)
	{
		List<String> lengthList = new ArrayList<String>();
		
		lengthList.add("*mode="+mode+" ============================="); //single, batch, or mix
		
		int totalSize = 0;
		float sumLength = 0;
		float minLength = Float.MAX_VALUE;
		float maxLength = 0;
		String objFile = "simFailureTrace/"+mode+"/jobTrace.obj";

		List<String> tmpList = new ArrayList<String>();
		
		JobTrace jt = FileControler.loadJobTraceFromFile(objFile);
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					totalSize++;
					float length = bt.getTotalTaskLength();
					sumLength += length;
					if(minLength>length) minLength = length;
					if(maxLength<length) maxLength = length;
					tmpList.add(String.valueOf(length));
				}
			}
		}
		float meanLength = sumLength/totalSize;
		lengthList.add("min ="+minLength);
		lengthList.add("max ="+maxLength);
		lengthList.add("mean="+meanLength);
		lengthList.addAll(tmpList);
		return lengthList;
	}
}
