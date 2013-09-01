package fr.imag.mescal.gloudsim.prepare;

import java.util.Iterator;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * rebuild the jobTrace, such that the first task of each task list starts from 0.
 * the consecutive tasks in a batchtask list are adjacent on the time point (starttime and endtime). 
 * @author sdi
 *
 */
public class DecorateJobTrace {
	
	
	public static void main(String[] args)
	{
		if(args.length!=3)
		{
			System.out.println("java TestLoadJobTrace [cpsRatio] [srcJobTraceObjFile] [decJobTraceObjFile]");
			System.exit(0);
		}
		float cpsRatio = Float.parseFloat(args[0]);
		String srcJobTraceObjFile = args[1];
		String decJobTraceObjFile = args[2];
		System.out.println("start loading sample jobs...");
		JobTrace jobTrace = JobTaskSimulator.loadSampleJobs(srcJobTraceObjFile);
		System.out.println("start decorating job trace.");
		decorate(jobTrace, cpsRatio);
		System.out.println("starting writing object to file: "+decJobTraceObjFile);
		FileControler.writeObject2File(jobTrace, decJobTraceObjFile);
		System.out.println("done.");
	}
	
	/**
	 * rebuild the trace to ease the following simulation
	 * @param jobTrace
	 * @param compressRatio (cpsRatio) time will be compressed in scales. 
	 * For example, 29 days --> 1 days
	 */
	private static void decorate(JobTrace jobTrace, float cpsRatio)
	{
		jobTrace.decorate(cpsRatio);
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			pc.decorate(cpsRatio);
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				job.decorate(cpsRatio);
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					bt.decorate(cpsRatio);
					float startTime = bt.getStartTime();
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						task.decorate(cpsRatio);
						task.setStartTime(task.getStartTime()-startTime);
						task.setEndTime(task.getEndTime()-startTime);
					}
				}
			}
		}
	}
}
