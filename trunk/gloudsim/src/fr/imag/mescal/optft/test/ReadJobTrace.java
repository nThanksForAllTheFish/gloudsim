package fr.imag.mescal.optft.test;

import java.util.Iterator;

import fr.imag.mescal.optft.elem.BatchTask;
import fr.imag.mescal.optft.elem.Job;
import fr.imag.mescal.optft.elem.JobTrace;
import fr.imag.mescal.optft.elem.PJobContainer;
import fr.imag.mescal.optft.prepare.JobTaskSimulator;

public class ReadJobTrace {

	public static String jobTraceFile = "simFailureTrace/single/jobTrace-dec.obj";
	
	public static void main(String[] args)
	{
		JobTrace jobTrace = JobTaskSimulator.loadSampleJobs(jobTraceFile);
		for(int i = 0;i<jobTrace.pJobList.size();i++)
		{
			PJobContainer pc = jobTrace.pJobList.get(i);
			int priority = pc.getPriority()+1;
			float meanFailNum = pc.getMeanTaskFailNum();
			float meanFailLength = pc.getMeanTaskLength();
			
			System.out.println(priority+"\t"+meanFailNum+"\t"+meanFailLength+"\t"+computeMeanBTLength(pc));
		}
	}
	
	public static float computeMeanBTLength(PJobContainer pc)
	{
		float sumBTLength = 0;
		int size = 0;
		Iterator<Job> iter2 = pc.jobList.iterator();
		while(iter2.hasNext())
		{
			Job job = iter2.next();
			Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
			size+=job.batchTaskList.size();
			while(iter3.hasNext())
			{
				BatchTask bt = iter3.next();
				sumBTLength+=bt.getTotalTaskLength();
			}
		}
		return sumBTLength/size;
	}
}
