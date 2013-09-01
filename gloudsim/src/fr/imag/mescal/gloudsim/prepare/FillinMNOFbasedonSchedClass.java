package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * The MNOF/MTBF of tasks will be recomputed based on scheduling class. 
 * @author sdi
 *
 */
public class FillinMNOFbasedonSchedClass {
	
	public static final int numSC = 4;
	static PriorityJobSchedClass[] pjSC = new PriorityJobSchedClass[12];
	
	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("java FillinMNOFbasedonSchedClass [jobEventDir] [jobTraceDir]");
			System.exit(0);
		}
		
		for(int i= 0;i<12;i++)
			pjSC[i] = new PriorityJobSchedClass();
		Initialization.needToLoadCpFiles = false;

		String jobTraceFileName = "jobTrace-3600-dec.obj"; //500,1000,....
		String jobEventDir = args[0];
		Initialization.jobTraceDir = args[1];
		
		String jobSchedClassMapFile = jobEventDir+"/jobSchedClassMap.txt";
		Map<String, Integer> jschedClsMap = loadSchedClassMap(jobSchedClassMapFile);

		List<String> modeList = PVFile.getDir(Initialization.jobTraceDir);
		Iterator<String> iter = modeList.iterator();
		while(iter.hasNext())
		{
			String mode = iter.next();
			String contextFilePath = Initialization.jobTraceDir+"/"+mode+"/"+jobTraceFileName;
			JobTrace jobTrace = FileControler.loadJobTraceFromFile(contextFilePath);
			JobTrace newJT = genNewJobTrace(jobTrace, jschedClsMap);
			//key step? 
			JobTaskSimulator.fillMTBFMNOFontoTasks(newJT, pjSC);
			String outputFile = contextFilePath.replace("jobTrace", "jobTrace-SC");
			FileControler.writeObject2File(newJT, outputFile);
		}
		System.out.println("done.");
	}
	
	private static Map<String, Integer> loadSchedClassMap(String jobSchedClassMapFile)
	{
		Map<String, Integer> jobSchedClassMap = new HashMap<String, Integer>();
		List<String> list = FileControler.readFile(jobSchedClassMapFile);
		Iterator<String> iter = list.iterator();
		while(iter.hasNext())
		{
			String line  = iter.next();
			String[] s = line.split(":");
			String jobName = s[0];
			Integer schedClass = Integer.valueOf(s[1].split("\\s")[0]);
			jobSchedClassMap.put(jobName, schedClass);
		}
		return jobSchedClassMap;
	}
	
	private static JobTrace genNewJobTrace(JobTrace jobTrace, Map<String, Integer> jschedClsMap)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			int p = pc.getPriority();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				String jobID = job.getJobID();
				int schedClass = jschedClsMap.get(jobID);
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					bt.setSchedulingClass(schedClass);
					
					pjSC[p].schedClassTaskList[schedClass].add(bt);
					
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						task.setSchedulingClass(schedClass);
					}
				}
			}
			pjSC[p].compute();
		}

		return jobTrace;
	}
}

class PriorityJobSchedClass
{
	//there are 3 scheduling classes in total.
	public List<BatchTask>[] schedClassTaskList = new ArrayList[FillinMNOFbasedonSchedClass.numSC];
	public float schedClassMNOF[] = new float[FillinMNOFbasedonSchedClass.numSC];
	public float schedClassMTBF[] = new float[FillinMNOFbasedonSchedClass.numSC];

	public PriorityJobSchedClass() {
		for(int i = 0;i<FillinMNOFbasedonSchedClass.numSC;i++)
			schedClassTaskList[i] = new ArrayList<BatchTask>();
	}

	public void compute()
	{
		for(int i = 0;i<FillinMNOFbasedonSchedClass.numSC;i++)
		{
			List<BatchTask> list = schedClassTaskList[i];
			Iterator<BatchTask> iter = list.iterator();
			while(iter.hasNext())
			{
				BatchTask btask = iter.next();
				schedClassMNOF[i]+=btask.taskList.size()-1;
				schedClassMTBF[i]+=btask.getMeanTaskLength();
			}
			schedClassMNOF[i]/=list.size();
			schedClassMTBF[i]/=list.size();
		}
	}
}
