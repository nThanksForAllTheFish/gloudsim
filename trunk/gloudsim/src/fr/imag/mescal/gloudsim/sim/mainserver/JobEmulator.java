package fr.imag.mescal.gloudsim.sim.mainserver;

/**
 * The entry start of the MainServer 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Device;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.prepare.JobTaskSimulator;
import fr.imag.mescal.gloudsim.sim.log.DynamicLogThread;
import fr.imag.mescal.gloudsim.sim.log.Logger;
import fr.imag.mescal.gloudsim.sim.log.Observer;
import fr.imag.mescal.gloudsim.util.ConversionHandler;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;
import fr.imag.mescal.gloudsim.util.RdGenerator;

/**
 * This is the entry point of the whole simulation.
 * @author sdi
 *
 */
public class JobEmulator {
	public static final Integer termMutex = new Integer(0);
	public static boolean termination = false;
	
	public static String observerFile = "";
	
	public static float theta = 0.9f;
	public static int VMServerStartBTPort = 2340; // created by VMServer, and
													// connected by
													// JobEmulatorThread
	public static int TaskExecutorPort = 2350; // created by TaskExecutor, and
												// connected by VMServer
	// public static int StateCheckerPort = 2360; //created by VMServer's
	// StateSensorThread, connected by JobEmulator?
	public static int BTFinishReceiverPort = 2360;
	public static int BTFailureEventPort = 2370;

	public static int monPort = 10000; // 10000 - 30000
	public static Integer monPortMutex = new Integer(0);

	static JobTrace jobTrace;
	private static int simJobIDCounter = 0;
	public static List<String> vmHostList = new ArrayList<String>();

	public static Hashtable<String, Job> schedJobMap = new Hashtable<String, Job>();
	public static Hashtable<String, BatchTask> schedBTMap = new Hashtable<String, BatchTask>();
	public static HashMap<String, Hashtable<String,BatchTask>> vmRunningBTMap = new HashMap<String, Hashtable<String,BatchTask>>();//<vmHost,list<btID,bt>>

	public static QueueScheduler scheduler;

	private static void printSampleList(List<Job[]> sampleList)
	{
		Iterator<Job[]> iter = sampleList.iterator();
		while(iter.hasNext())
		{
			Job[] jobs = iter.next();
			System.out.println("[Jobemulator:sample] ***************** size = "+jobs.length);
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 7) {
			System.out
					.println("java JobEmulator [priority] [limitLength] [simLength] [configFile] [observerFile] [cpOurFormula] [migrationStatFile]");
			System.exit(0);
		}

		String jobRealLengthFile = "jobRealLength.log";
		
		// priority=-1(random selection in 0,2,4,6), -2(dynamic change),
		// >0(specific priority)
		int priority = Integer.parseInt(args[0]);
		int limitLength = Integer.parseInt(args[1]);
		int simLength = Integer.parseInt(args[2]);
		String configFile = args[3];
		observerFile = args[4];
		Initialization.cpOurFormula = Boolean.parseBoolean(args[5]);
		String migrationStatFile = args[6]; //statistics of migration mode
		System.out.println("start loading config file.");
		Initialization.load(configFile);
		List<String> mark = new ArrayList<String>();
		mark.add("true");
		FileControler.print2File(mark, "exp.mark");

		vmHostList = FileControler.readFile("vmhosts");
		for (int i = 0; i < vmHostList.size(); i++) {
			if (vmHostList.get(i).startsWith("#")) {
				vmHostList.remove(i);
				i--;
			}
		}
		
		Iterator<String> iterator = vmHostList.iterator();
		while(iterator.hasNext())
		{
			String vmHost = iterator.next();
			vmRunningBTMap.put(vmHost, new Hashtable<String,BatchTask>());
		}
		
		System.out.println("vmhosts:");
		Iterator<String> iter = vmHostList.iterator();
		while (iter.hasNext()) {
			String vmhost = iter.next();
			System.out.print(vmhost + " ");
		}
		System.out.println();

		System.out.println("jobArrivalTraceFile="
				+ Initialization.jobArrivalTraceFile);

		System.out.println("Start loading sample jobs...");
		String jobTraceObjFile = Initialization.jobTraceDir + "/" + Initialization.taskMode
				+ "/" + Initialization.jobTraceFileName;
		jobTrace = JobTaskSimulator.loadSampleJobs(jobTraceObjFile);
		JobTaskSimulator.initTargetLoad(jobTrace);
		JobTaskSimulator.initMemSizeandCosts(jobTrace);
		System.out
				.println("Finish loading sample jobs, init targetload and memsize.");
		System.out.println("Building static job list....");
		List<Job[]> sampleJobList = null;
		if (Initialization.testMode.startsWith("static"))
		{
			sampleJobList = buildStaticJobList(priority, limitLength, Initialization.maxBTNumPerJob);
			printSampleList(sampleJobList);
		}
		else
		{	// Initialization.testMode.equals("dynamic")||Initialization.testMode.equals("static_mix_priority")
			sampleJobList = buildDynamicJobList(Initialization.dynPriority,
					limitLength, Initialization.maxBTNumPerJob);
			printSampleList(sampleJobList);
		}

		int[] jobNumPerSec = null;
		if (Initialization.useJobArrivalTrace) {
			System.out.println("loading job arrival trace file...");
			jobNumPerSec = getJobNumsPerSecArray(
					Initialization.jobArrivalTraceFile, simLength);
		}

		// System.out.println("start stateChecker thread....");
		// //check states
		// Iterator<String> iter_c = JobEmulator.vmHostList.iterator();
		// while(iter_c.hasNext())
		// {
		// String targetHost = iter_c.next();
		// new StateCheckerCommThread(targetHost).start();
		// }

		System.out.println("start BatchTaskFailureEventReceiverThread");
		new BatchTaskFailureEventReceiverThread().start();

		System.out.println("start finish receiver thread....");
		BatchTaskFinishReceiverThread btfrt = new BatchTaskFinishReceiverThread();
		btfrt.start();

		System.out.println("start simulationProgressMonitorThread...");
		new SimulationProgressMonitorThread(schedJobMap, limitLength+simLength).start();

		System.out.println("start Mem state Checker Thread");
		new MemStateChecker().run();

		System.out.println("initialize queue scheduler....");
		scheduler = new QueueScheduler();
		scheduler.start();
		
		System.out.println("start observer thread..");
		Observer ob = new Observer();
		ob.start();
		
		if(Initialization.dynamicLog)
		{
			System.out.println("start dynamic log ....");
			new DynamicLogThread(jobRealLengthFile).start();
		}
		
		System.out.println("Start generating jobs....");
		//Job[] sampleJobs = ConversionHandler.convertList2Array(sampleJobList);
		if (jobNumPerSec != null) // jobOverlay is true (use jobTraceFile to
									// simulate jobs)
			simJobbasedonTraceFile(sampleJobList, jobNumPerSec);
		else
			// Initialization.jobOverlap is false (don't use jobTraceFile)
			simJobOnebyOne(sampleJobList);

		try {
			synchronized (schedJobMap) {
				schedJobMap.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized (termMutex) {
			//notify QueueScheduler to stop
			termination = true;
		}
		// TODO: call log class to output results....
//		if(!Initialization.dynamicLog)
//		{
//		
//		}
		System.out.println("call log file....");
		Logger.printSchedJobResult(jobRealLengthFile);
		System.out.println("print observer");
		Logger.printObserver(ob, observerFile);
		System.out.println("print migration state");
		Logger.printMigModeStat(migrationStatFile);
		System.out.println("Simulation is done.");	
		System.exit(0);
	}

	public static void simJobbasedonTraceFile(List<Job[]> sampleJobsList,
			int[] jobNumPerSec) {
		// new SimulateJobThread(sampleJobs, jobNumPerSec, schedJobMap,
		// schedBTMap).start();
		if (jobNumPerSec != null) {
			for (int i = 0; i < jobNumPerSec.length; i++) {
				for (int j = 0; j < jobNumPerSec[i]; j++) {
					JobEmulatorThread jet = new JobEmulatorThread(sampleJobsList,
							schedJobMap);
					jet.start();
				}
				try {
					Thread.sleep(1000); // simulate 1 second
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("[JobEmulator]Error: jobNumPerSec==null!");
		}
	}

	public static void simJobOnebyOne(List<Job[]> sampleJobs) {
		int loops = Initialization.paralleldegree;
		System.out.println("Initialization.paralleldegree="+Initialization.paralleldegree);
		for (int i = 0; i < loops; i++)
		{
			new JobEmulatorThread(sampleJobs, schedJobMap).start();
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			synchronized (schedBTMap) {
				for (int i = 0; i < Initialization.simJobNum - loops; i++) {
					schedBTMap.wait();
					new JobEmulatorThread(sampleJobs, schedJobMap).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param jobArrivalTraceFile
	 * @param simLength
	 *            simulation length in seconds
	 * @return
	 */
	public static int[] getJobNumsPerSecArray(String jobArrivalTraceFile,
			int simLength) {
		List<String> lineList = FileControler.readFile(jobArrivalTraceFile);
		float[] arrivals = new float[lineList.size()];
		Iterator<String> iter = lineList.iterator();
		for (int i = 0; iter.hasNext(); i++)
			arrivals[i] = Float.parseFloat(iter.next());

		int[] numPerSec = new int[simLength];
		for (int a : numPerSec)
			a = 0;
		for (int i = 0; i < arrivals.length; i++) {
			int j = (int) arrivals[i];
			if (j > simLength - 1)
				break; 
			numPerSec[j]++;
		}
		return numPerSec;
	}

	public static int getPriorityIndex(JobTrace jobTrace, int priority)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		for(int i = 0;iter.hasNext();i++)
		{
			PJobContainer pJC = iter.next();
			if(priority==pJC.getPriority())
				return i;
		}
		return -1;
	}
	
	public static List<Job[]> buildDynamicJobList(int[] priority, int limitLength, int btNum) {
		List<Job[]> totalJobsList = new ArrayList<Job[]>();

		for (int i = 0; i < priority.length; i++) {
			int index = getPriorityIndex(jobTrace, priority[i]); 
			PJobContainer pjc = jobTrace.pJobList.get(index);
			for(int j = 0;j<pjc.jobList.size();j++)
			{
				Job job = pjc.jobList.get(j);
				if (job.getMakespan() > limitLength||job.batchTaskList.size()>btNum) {
					pjc.jobList.remove(j);
					j--;
				}
			}
			Job[] jobs = ConversionHandler.convertList2Array(pjc.jobList);
			totalJobsList.add(jobs);
		}

		return totalJobsList;
	}

	public static List<Job[]> buildStaticJobList(int priority, int limitLength, int btNum) {
		List<Job[]> totalJobsList = new ArrayList<Job[]>();
		List<Job> totalJobList = new ArrayList<Job>();
		if (priority < 0) {
			for (PJobContainer pjc : jobTrace.pJobList)
				totalJobList.addAll(pjc.jobList);
		} 
		else 
		{
			int index = getPriorityIndex(jobTrace, priority);
			totalJobList.addAll(jobTrace.pJobList.get(index).jobList);
		}

		for (int i = 0; i < totalJobList.size(); i++) {
			Job job = totalJobList.get(i);
			if (job.getMakespan() > limitLength||job.batchTaskList.size()>btNum) {
				totalJobList.remove(i);
				i--;
			}
		}
		
		Job[] jobs = ConversionHandler.convertList2Array(totalJobList);
		totalJobsList.add(jobs);
	
		return totalJobsList;
	}

	public static Job ranGenOneJob(Job[] sampleJobs) {
		boolean ok = false;
		Job job = null;
		do {
			int index = RdGenerator.RAN_SeedGen2.generate_Int(0,
					sampleJobs.length - 1);
			job = sampleJobs[index].clone();
			if (Initialization.blackJobList.contains(job.getJobID()))
				ok = false;
			else
				ok = true;
		} while (!ok);
		synchronized (jobTrace) {
			job.setSimID(simJobIDCounter++);
		}

		return job;
	}
	
	/**
	 * 
	 * @param sampleJobs
	 * @return
	 */
	public static Job ranDynOneJob(List<Job[]> sampleJobsList) {
		String new_jobID = "";
		List<BatchTask> new_btList = new ArrayList<BatchTask>();
		List<Task> new_taskList = new ArrayList<Task>();
		BatchTask new_bt = new BatchTask(0, new_taskList);
		new_btList.add(new_bt);
		float totalExeLength = 0;
		Iterator<Job[]> iter = sampleJobsList.iterator();
		while(iter.hasNext())
		{
			Job[] sampleJobs = iter.next();
			int jobIndex = RdGenerator.RAN_SeedGen2.generate_Int(0,
					sampleJobs.length - 1);
			Job job = sampleJobs[jobIndex].clone();
			new_jobID += "|" + job.getJobID();
			int btIndex = RdGenerator.RAN_SeedGen4.generate_Int(0,
					job.batchTaskList.size() - 1);
			BatchTask bt = job.batchTaskList.get(btIndex);

			new_bt.taskList.addAll(bt.taskList);
			totalExeLength += bt.getTotalTaskLength();
		}
		new_bt.setTotalTaskLength(totalExeLength);
		new_jobID = new_jobID.substring(1);
		Job newJob = new Job(new_jobID);
		newJob.batchTaskList = new_btList;
		synchronized (jobTrace) {
			newJob.setSimID(simJobIDCounter++);
		}
//		System.out.println("newJob=====================");
//		System.out.println(newJob.toString());
		return newJob;
	}

	public static String ranGenVM() {
		int index = RdGenerator.RAN_SeedGen3.generate_Int(0,
				vmHostList.size() - 1); // there are 56 vms
		return vmHostList.get(index);
	}

	public static String ranGenVM(int nfsDeviceID) {
		String vmHost = null;
		int targetIndex = nfsDeviceID;
		while (targetIndex % Initialization.numOfPhyHosts == nfsDeviceID) {
			int index = RdGenerator.RAN_SeedGen3.generate_Int(0,
					vmHostList.size() - 1); // there are 56 vms
			vmHost = vmHostList.get(index);
			targetIndex = Integer.parseInt(vmHost.replace("vm", ""));
		}
		return vmHost;
	}
}

// class ParallelGenJobThread extends Thread
// {
// Job[] sampleJobs;
// Hashtable<String, Job> schedJobMap;
// Hashtable<String, BatchTask> schedBTMap;
//
// public ParallelGenJobThread(Job[] sampleJobs,
// Hashtable<String, Job> schedJobMap,
// Hashtable<String, BatchTask> schedBTMap) {
// this.sampleJobs = sampleJobs;
// this.schedJobMap = schedJobMap;
// this.schedBTMap = schedBTMap;
// }
//
// public void run()
// {
// new JobEmulatorThread(sampleJobs, schedJobMap, schedBTMap).start();
// synchronized(sampleJobs)
// {
// sampleJobs.notify();
// }
// }
// }

// class SimulateJobThread extends Thread
// {
// Job[] sampleJobs;
// int[] jobNumPerSec;
// Hashtable<String, Job> schedJobMap;
// Hashtable<String, BatchTask> schedBTMap;
//
// public SimulateJobThread(Job[] sampleJobs,
// int[] jobNumPerSec, Hashtable<String, Job> schedJobMap,
// Hashtable<String, BatchTask> schedBTMap) {
// this.sampleJobs = sampleJobs;
// this.jobNumPerSec = jobNumPerSec;
// this.schedJobMap = schedJobMap;
// this.schedBTMap = schedBTMap;
// }
//
// public void run()
// {
//
// }
//
// }
