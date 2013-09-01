package fr.imag.mescal.gloudsim.prepare;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.sim.cp.OptCPAnalyzer;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.elem.MemCost;
import fr.imag.mescal.gloudsim.util.PVFile;
import fr.imag.mescal.gloudsim.util.Progress;

/**
 * This "JobTaskSimulator" can only blindly find a batch of tasks, but most of the selected 
 * tasks each just have one execution period, instead of many consecutive durations as expected.
 * Note: multiple execution periods mean multiple failure events in the middle of the whole execution.
 * (So, I write JobTaskSimulator2 instead.)
 * @author sdi
 *
 */
public class JobTaskSimulator {
	
	public static int minSubTaskNum = 2;
	public static int maxSubTaskNum = 100;
	public static float failureRatioofBatchTasks = 0.5f; //if there are 10 batch tasks in a job, and 5 of them each have failure events, then, 5/10=0.5
	
	public static JobTrace loadSampleJobs(String srcTraceDataFile)
	{
		return FileControler.loadJobTraceFromFile(srcTraceDataFile);	
	}
	
	public static void dumpSampleJobs(int maxJobNum,
			String selectJobDir, String jobTaskTraceDir, String tgtTraceDataFile)
	{
		System.out.println("init job list....");
		List<PJobContainer> jobConList = initJobList(selectJobDir); 
		JobTrace jt = new JobTrace(jobConList);
		System.out.println("extracting task trace from "+jobTaskTraceDir);
		extractTaskTrace(maxJobNum, jt, jobTaskTraceDir); //key step
		System.out.println("removing useless jobs");
		filteroutJobs(jt);
		System.out.println("computing mean task length");
		//computeMeanTaskLength(jt);
		computeMeanTaskLengthWithoutLastSubTask(jt);
		fillinBTTaskStat(jt);
		System.out.println("fill in tasks' MTBF and MNOF");
		fillMTBFMNOFontoTasks(jt);
		System.out.println("writing joblist object to "+tgtTraceDataFile);
		FileControler.writeObject2File(jt, tgtTraceDataFile);
	}
	
	public static void fillinBTTaskStat(JobTrace jt)
	{
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			float pJobMTBF = pc.getMeanTaskLength();
			float pJobMNum = pc.getMeanTaskFailNum();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					bt.setpJobMTBF(pJobMTBF);
					bt.setpJobMNum(pJobMNum);
				}
			}
		}
	}
	
	public static void filteroutJobs(JobTrace jt)
	{
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			for(int i = 0;i<pc.jobList.size();i++)
			{
				Job job = pc.jobList.get(i);
				if(job.getSize()==0)
				{
					pc.jobList.remove(i);
					i--;
				}
			}
		}
	}
	
	public static void computeMeanTaskLength(JobTrace jt)
	{
		float totalTaskLength = 0;
		int totalTaskNum = 0;
		int totalBTNum = 0;
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			float pJobTotalTaskLength = 0;
			int pJobTotalTaskNum = 0;
			int pJobBatchTaskNum = 0;
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				float jobSumLength = 0;
				float maxBatchTaskLength = 0;
				int jobTotalTaskNum = 0;
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					Iterator<Task> iter4 = bt.taskList.iterator();
					float btSumLength = 0;
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						btSumLength += task.getDuration();
					}
					float btMean = btSumLength/bt.taskList.size();
					jobSumLength += btSumLength;
					bt.setMeanTaskLength(btMean);
					float eTime = bt.taskList.get(bt.taskList.size()-1).getEndTime();
					float sTime = bt.taskList.get(0).getStartTime();
					bt.setWallClockLength(eTime - sTime);
					bt.setTotalTaskLength(btSumLength);
					if(maxBatchTaskLength<btSumLength)
						maxBatchTaskLength = btSumLength;
					jobTotalTaskNum += bt.taskList.size();
				}
				pJobBatchTaskNum += job.batchTaskList.size();
				float jobMean = jobSumLength/jobTotalTaskNum;
				job.setMeanTaskLength(jobMean);
				job.setMakespan(maxBatchTaskLength);
				pJobTotalTaskLength+=jobSumLength;
				float meanJobTaskFailNum = jobTotalTaskNum/job.batchTaskList.size();
				job.setMeanTaskFailNum(meanJobTaskFailNum);
				pJobTotalTaskNum += jobTotalTaskNum;
			}
			pc.setMeanTaskLength(pJobTotalTaskLength/pJobTotalTaskNum);
			totalTaskNum += pJobTotalTaskNum;
			totalTaskLength += pJobTotalTaskLength;
			if(pJobBatchTaskNum!=0)
			{
				pc.setMeanTaskFailNum(pJobTotalTaskNum/pJobBatchTaskNum);
				totalBTNum += pJobBatchTaskNum;
			}
		}
		jt.setMeanTaskLength(totalTaskLength/totalTaskNum);
		jt.setMeanTaskFailNum(totalTaskNum/totalBTNum);
	}

	public static void computeMeanTaskLengthWithoutLastSubTask(JobTrace jt)
	{
		float totalFailTaskLength = 0;
		int totalFailTaskNum = 0;
		int totalBTNum = 0;
		Iterator<PJobContainer> iter = jt.pJobList.iterator();
		while(iter.hasNext())
		{
			float pJobFailTaskLength = 0;
			int pJobBatchTaskNum = 0;
			int pJobFailTaskNum = 0;
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				float jobFailSumLength = 0;
				float maxBatchTaskLength = 0;
				int jobFailTaskNum = 0;
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					Iterator<Task> iter4 = bt.taskList.iterator();
					float btSumLength = 0;
					float btFailSumLength = 0;
					for(int i = 0;iter4.hasNext();i++)
					{
						Task task = iter4.next();
						btSumLength += task.getDuration();
						if(i<bt.taskList.size()-1)
							btFailSumLength += task.getDuration();
					}
					float btMean = btFailSumLength/(bt.taskList.size()-1); //btMean is the MTBF
					jobFailSumLength += btFailSumLength;
					bt.setMeanTaskLength(btMean);
					float eTime = bt.taskList.get(bt.taskList.size()-1).getEndTime();
					float sTime = bt.taskList.get(0).getStartTime();
					bt.setWallClockLength(eTime - sTime);
					bt.setTotalTaskLength(btSumLength);
					if(maxBatchTaskLength<btSumLength)
						maxBatchTaskLength = btSumLength;
					jobFailTaskNum += bt.taskList.size()-1;// the number of failures (excluding the last subtask)
				}
				pJobBatchTaskNum += job.batchTaskList.size();
				float jobMean = jobFailSumLength/jobFailTaskNum;
				job.setMeanTaskLength(jobMean);
				job.setMakespan(maxBatchTaskLength);
				pJobFailTaskLength+=jobFailSumLength;
				float meanJobTaskFailNum = jobFailTaskNum/job.batchTaskList.size();
				job.setMeanTaskFailNum(meanJobTaskFailNum);
				pJobFailTaskNum += jobFailTaskNum;
			}
			pc.setMeanTaskLength(pJobFailTaskLength/pJobFailTaskNum);
			totalFailTaskNum += pJobFailTaskNum;
			totalFailTaskLength += pJobFailTaskLength;
			if(pJobBatchTaskNum!=0)
			{
				pc.setMeanTaskFailNum(pJobFailTaskNum/pJobBatchTaskNum);
				totalBTNum += pJobBatchTaskNum;
			}
		}
		jt.setMeanTaskLength(totalFailTaskLength/totalFailTaskNum);
		jt.setMeanTaskFailNum(totalFailTaskNum/totalBTNum);
	}
	
	public static void extractTaskTrace(int maxJobNum,
			JobTrace jobTrace, String jobTaskTraceDir)
	{
		int counter = 0;
		Iterator<PJobContainer> conIter = jobTrace.pJobList.iterator();
		while(conIter.hasNext())
		{
			PJobContainer pcon = conIter.next();
			Iterator<Job> iter = pcon.jobList.iterator();
			long initLogTime = System.currentTimeMillis()/1000;
			for(int i = 1;iter.hasNext();i++)
			{
				Job job = iter.next();
				job.setPriority(pcon.getPriority());
				if(i%400==0)
					Progress.showProgress(initLogTime, i, pcon.jobList.size(), job.getJobID());

				String jobTaskTraceFile = constructJobFilePath(jobTaskTraceDir, job.getJobID());
				List<String> taskUsageLineList = FileControler.readFile(jobTaskTraceFile);
				boolean state = extractOneJob(taskUsageLineList, job);
				if(!state)
				{
					job.setSize(0);
					continue;
				}
				if(maxJobNum>0) //if maxJobNum>0 instead of -1, it means that we need to select correct jobs here, instead of the previous algorithm (RanSelectJobs.java)
				{
					if(job.getFailureRatioofBatchTasks()>=failureRatioofBatchTasks)
					{
						counter++;
						if(counter>=maxJobNum)
							break;
					}
					else
					{
						job.setSize(0); //the 'useless' mark to be used in the filterout function
						continue;
					}
				}	
			}
		}
	}
	
	public static boolean extractOneJob(List<String> taskUsageLineList, Job job)
	{
		Map<String, List<Task>> taskIDTraceMap = new HashMap<String, List<Task>>();
		Iterator<String> iter2 = taskUsageLineList.iterator();
		String meta = iter2.next(); //meta
		String[] s = meta.split("\\s");
		float startTime = Float.parseFloat(s[2]);
		float endTime = Float.parseFloat(s[3]);
		int size = Integer.parseInt(s[4]);
		int vsize = Integer.parseInt(s[5]);
		job.setInfo(startTime, endTime, size, vsize);
		boolean state = true;
		while(iter2.hasNext())
		{
			String line = iter2.next();
			String[] data = line.split("\\s");
			String taskIDMID = data[0];
			String[] dat = taskIDMID.split("@");
			String taskID = dat[0];
			float tStart = Float.parseFloat(data[1]);
			float tEnd = Float.parseFloat(data[2]);
			float duration = tEnd - tStart;
			if(data.length<=5)
			{
				state = false;
				break;
			}
			float memSize = Float.parseFloat(data[5]);
			
			Task task = new Task(Integer.parseInt(taskID), job.getPriority(), tStart, tEnd, duration, memSize);
			List<Task> tList = taskIDTraceMap.get(taskID);
			if(tList==null)
			{
				tList = new ArrayList<Task>();
				taskIDTraceMap.put(taskID, tList);
			}
			tList.add(task);
		}
		if(!state)
			return false;
		Iterator iter3 = taskIDTraceMap.entrySet().iterator(); 
		while (iter3.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter3.next(); 
		    String taskID = (String)entry.getKey(); 
		    List<Task> ttList = (List<Task>)entry.getValue();
		    Collections.sort(ttList);
		    BatchTask bTask = new BatchTask(ttList.get(0).getStartTime(), ttList);
		    float meanMemSize = compMeanMemSize(ttList);
		    bTask.setMeanMemSize(meanMemSize);
		    job.batchTaskList.add(bTask);
		} 
		Collections.sort(job.batchTaskList);
		return true;
	}
	
	public static float compMeanMemSize(List<Task> ttList)
	{
		float sumMemSize = 0;
		Iterator<Task> iter = ttList.iterator();
		while(iter.hasNext())
		{
			Task tt = iter.next();
			float memSize = tt.getMemSize();
			sumMemSize += memSize;
		}
		return sumMemSize/ttList.size();
	}
	
	public static List<PJobContainer> initJobList(String selectJobDir)
	{
		List<PJobContainer> jobConList = new ArrayList<PJobContainer>();
		List<String> fileNameList = PVFile.getFiles(selectJobDir);
		Iterator<String> iter = fileNameList.iterator();
		while(iter.hasNext())
		{
			String fileName = iter.next();
			if(fileName.endsWith("txt"))
			{
				int priority = Integer.parseInt(fileName.split("\\.")[0].split("-")[1]);
				PJobContainer pJobContainer = new PJobContainer(priority);
				jobConList.add(pJobContainer);
				String filePath = selectJobDir+"/"+fileName;
				List<String> lineList = FileControler.readFile(filePath);
				Iterator<String> iter2 = lineList.iterator();
				while(iter2.hasNext())
				{
					String line = iter2.next();
					String[] data = line.split("\\s");
					Job job = new Job(data[0]);
					pJobContainer.jobList.add(job);
				}	
			}
		}

		return jobConList;
	}
	
	public static void main(String[] args)
	{
		if(args.length!=4)
		{
			System.out.println("java JobTaskSimulator [max number of jobs] [selectJobFile] [jobTaskTraceDir] [tgtTraceDataFile] ");
			System.out.println("max number of jobs: if its value is <0, it means all jobs. If its value is set to a positive number, it means to find such number of satisfied jobs.");
			System.out.println("by 'satisfy', I mean meeting the requirement about the number of subtasks in a seriel task");
			System.out.println("selectJobFiles are created by RanSelectJobs. If you want to select all jobs instead of randomly selections, you could give it a big number as parameter.");
			System.out.println("tgtTraceDataFile is different based on [max number of jobs]. If it is <0, '..../simJobTrace/...'; if it is >0, '..../simFailureTrace/...'");
			System.exit(0);
		}
		int maxJobNum = Integer.parseInt(args[0]); //maxJobNum? read the help info...
		String selectJobFile = args[1];
		String jobTaskTraceDir = args[2];
		String tgtTraceDataFile = args[3];
		System.out.println("begin dumping object data...");
		dumpSampleJobs(maxJobNum, selectJobFile, jobTaskTraceDir, tgtTraceDataFile);
		System.out.println("done.");
	}
	
	public static String constructJobFilePath(String jobUsageDir, String jobID)
	{
		char[] charArray = jobID.toCharArray();
		String path = jobUsageDir+"/";
		for(int i = 0;i<charArray.length;i++)
			path+=charArray[i]+"/";
		path+=jobID+".usage";
		return path;
	}
	
	public static void fillMTBFMNOFontoTasks(JobTrace jobTrace)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
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
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						task.setMTBF(bt.getpJobMTBF());
						task.setMNOF(bt.getpJobMNum());
						task.setBTLength(bt.getTotalTaskLength());
					}
				}
			}
		}
	}
	
	public static void fillMTBFMNOFontoTasks(JobTrace jobTrace, PriorityJobSchedClass[] pjSC)
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
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						int schedClass = task.getSchedulingClass();
						task.setMTBF(pjSC[p].schedClassMTBF[schedClass]);
						task.setMNOF(pjSC[p].schedClassMNOF[schedClass]);
						task.setBTLength(bt.getTotalTaskLength());
					}
				}
			}
		}
	}
	
	public static void genJobBTStat(Job job)
	{
		float makespan = 0;
		Iterator<BatchTask> iter = job.batchTaskList.iterator();
		while(iter.hasNext())
		{
			float totalExeLength = 0;
			float sumMemSize = 0;
			//float sumMNOF = 0;
			BatchTask bt = iter.next();
			Iterator<Task> iter2 = bt.taskList.iterator();
			while(iter2.hasNext())
			{
				Task task = iter2.next();
				float duration = task.getDuration()*OptCPAnalyzer.loadRatio;
				task.setDuration(duration);
				totalExeLength += duration;
				sumMemSize += task.getMemSize();
				//sumMNOF += task.getMNOF();
			}
			float meanMemSize = sumMemSize / bt.taskList.size();
			iter2 = bt.taskList.iterator();
			while(iter2.hasNext())
			{
				Task task = iter2.next();
				task.setMemSize(meanMemSize);
			}
			if(makespan < totalExeLength)
				makespan = totalExeLength;
			float meanTaskLength = totalExeLength / bt.taskList.size();
			Task firstTask = bt.taskList.get(0);
			bt.setpJobMNum(firstTask.getMNOF());
			bt.setpJobMTBF(firstTask.getMTBF());
			bt.setMeanMemSize(meanMemSize);
			bt.setMeanTaskLength(meanTaskLength); //simulate static solution.
			bt.setTotalTaskLength(totalExeLength); //suppose the total task length can still be predicted precisely in the dynamic case with static solution.
		}
		job.setMakespan(makespan);
		setJobCost(job);
	}
	
	public static void initTargetLoad(JobTrace jobTrace)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
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
					bt.setTotalTaskLength(bt.getTotalTaskLength()*OptCPAnalyzer.loadRatio);
				}
			}
		}
	}
	
	public static void initMemSizeandCosts(JobTrace jobTrace)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				setJobCost(job);
			}
		}
	}
	
	public static void setJobCost(Job job)
	{
		Iterator<BatchTask> iter = job.batchTaskList.iterator();
		while(iter.hasNext())
		{
			BatchTask bt = iter.next();
			setBTCost(bt);
		}
	}
	
	public static void setBTCost(BatchTask bt)
	{
		float meanMemSize = bt.getMeanMemSize()*Initialization.maxCapMemSize;
//		if(meanMemSize>=Initialization.simMaxMemSize)
//			meanMemSize = Initialization.simMaxMemSize;
//		if(meanMemSize<=Initialization.simMinMemSize)
//			meanMemSize = Initialization.simMinMemSize;
		
		MemCost ramfsCpcost = fitCpMemCost(meanMemSize, true);
		MemCost nfsCpcost = fitCpMemCost(meanMemSize, false);
		MemCost ramfsRscost = fitRsMemCost(meanMemSize, true);
		MemCost nfsRscost = fitRsMemCost(meanMemSize, false);
		
		bt.setMeanMemSize(ramfsCpcost.getUsedMemSize());
		//modify bt's cpcost and memID
		bt.setMemID(ramfsCpcost.getMemID());
		bt.setRamfsCpcost(ramfsCpcost.getCost());
		bt.setNfsCpcost(nfsCpcost.getCost());
		bt.setRamfsRscost(ramfsRscost.getCost());
		bt.setNfsRscost(nfsRscost.getCost());
	}
	
	/**
	 * 
	 * @param oriMemSize
	 * @param selectLocalDisk is the target device local-ramdisk? or nfs?
	 * @return
	 */
	public static MemCost fitCpMemCost(float oriMemSize, boolean selectLocalDisk)
	{
		List<MemCost> cpcostList = selectLocalDisk?Initialization.ramfsCpcostList:Initialization.nfsCpcostList;
		Iterator<MemCost> iter = cpcostList.iterator();
		MemCost prevCost = iter.next();
		while(iter.hasNext())
		{
			MemCost cost = iter.next();
			if(cost.getUsedMemSize()<oriMemSize)
				prevCost = cost;
			else
				return prevCost;
		}
		return prevCost;
	}
	
	/**
	 * 
	 * @param oriMemSize
	 * @param selectLocalDisk is the target device local-ramdisk? or nfs?
	 * @return
	 */
	public static MemCost fitRsMemCost(float oriMemSize, boolean selectLocalDisk)
	{
		List<MemCost> costList = selectLocalDisk?Initialization.ramfsRscostList:Initialization.nfsRscostList;
		Iterator<MemCost> iter = costList.iterator();
		MemCost prevCost = iter.next();
		while(iter.hasNext())
		{
			MemCost cost = iter.next();
			if(cost.getUsedMemSize()<oriMemSize)
				prevCost = cost;
			else
				return prevCost;
		}
		return prevCost;
	}
}