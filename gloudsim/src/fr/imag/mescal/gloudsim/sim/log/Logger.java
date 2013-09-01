package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * Logger class is used to generate useful analytical result based on the simulation log data.
 * @author sdi
 *
 */
public class Logger {
	public static Integer synMutex = new Integer(0);
	public static int counter = 0;

	public static Hashtable<String, Job> dynSchedJobMap = new Hashtable<String, Job>();
	public static Hashtable<String, BatchTask> dynSchedBTMap = new Hashtable<String, BatchTask>();

	public static void main(String[] args)
	{
		if(args.length!=4)
		{
			System.out.println("Usage: java Logger [jobRealLengthFile] [contextDir] [stateDir] [resultOutputFile]");
			System.out.println("Example: java fr.imag.mescal.gloudsim.sim.log.Logger jobRealLength.log /localfs/contextNFS /cloudNFS/CheckpointSim/cpState result.txt");
			System.exit(0);
		}
		String jobRealLengthFile = args[0];
		String contextDir = args[1];
		String stateDir = args[2];
		String resultOutputFile = args[3];
		List<LogJob> logJobList = new ArrayList<LogJob>();

		Initialization.load("prop.config");
		List<String> mark = new ArrayList<String>();
		mark.add("false");
		FileControler.print2File(mark, "exp.mark");

		//load real length
		System.out.println("load real length ...");
		Hashtable<String, LogJob> logJobMap = loadRealLength(jobRealLengthFile, logJobList);
		Iterator iter = logJobMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String jobID = (String)entry.getKey();
		    String jobString = ((LogJob)entry.getValue()).toString();
		    System.out.println(jobID+" "+jobString);
		}

		//load workload processed
		System.out.println("load workload processed ...");
		loadWorkload(logJobMap, contextDir, stateDir);

		//process logJobMap
		System.out.println("Start processing logjobMap");
		processLogJobMap(logJobList, resultOutputFile);

		mark = new ArrayList<String>();
		mark.add("true");
		FileControler.print2File(mark, "exp.mark");
		System.out.println("done.");
	}

	private static Hashtable<String, LogJob> loadRealLength(String jobRealLengthFile, List<LogJob> logJobList)
	{
		Hashtable<String, LogJob> logJobMap = new Hashtable<String, LogJob>();
		List<String> rlList = FileControler.readFile(jobRealLengthFile);
		Iterator<String> iter = rlList.iterator();
		LogJob logJob = null;
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.startsWith("#"))
			{
				line = line.substring(1); //filter out #
				String[] s = line.split(":");
				String[] ss = s[0].split("-");
				String simJobID = ss[0];
				String jobID = ss[1];
				String[] data = s[1].split("\\s");
				float totalLength = Float.parseFloat(data[0]);
				float realLength = Float.parseFloat(data[1]);
				logJob = new LogJob(jobID, simJobID, totalLength, realLength);
				logJobList.add(logJob);
				logJobMap.put(simJobID, logJob);
			}
			else
			{
				String[] data = line.trim().split("\\s");
				String btID = data[0];
				Float totalLength = Float.valueOf(data[1]);
				Float realLength = Float.valueOf(data[2]);
				logJob.btTotalLengthMap.put(btID, totalLength);
				logJob.btRealLengthMap.put(btID, realLength);
			}
		}
		return logJobMap;
	}

	private static void loadWorkload(Hashtable<String, LogJob> logJobMap, String contextDir, String stateDir)
	{
		System.out.println("check all context file paths and build a map.");
		Map<String, String> btContextMap = new HashMap<String, String>();
		List<String> allContextFileList = PVFile.getRecursiveFiles(contextDir, "");
		Iterator<String> iter = allContextFileList.iterator();
		while(iter.hasNext())
		{
			String contextFilePath = iter.next();
			String[] s = contextFilePath.split("/");
			if(s[s.length-1].startsWith("context"))
			{
				String[] data = contextFilePath.split("/");
				String batchTaskID = data[3];
				btContextMap.put(batchTaskID, contextFilePath);
			}
		}
		List<String> btIDList = PVFile.getDir(stateDir);
		double initLogTime = System.currentTimeMillis()/1000.0;
		Iterator<String> iter2 = btIDList.iterator();
		for (int i = 0;iter2.hasNext();i++) {
			String btIDDir = iter2.next();
			new ParseContextThread(btIDDir,	stateDir, btContextMap, logJobMap).run();
			Initialization.showProgress(initLogTime, i, btContextMap.size(), btIDDir);
		}
	}

	private static void processLogJobMap(List<LogJob> logJobList, String resultOutputFile)
	{
		List<String> resultList = new ArrayList<String>();
		Collections.sort(logJobList);
		Iterator<LogJob> iter = logJobList.iterator();
		while (iter.hasNext()) {
			LogJob logJob = iter.next();
		    logJob.computeProcMakespan();
		    resultList.addAll(logJob.toStringList());
		}
		FileControler.print2File(resultList, resultOutputFile);
	}

	public static void printSchedJobResult(String logFile)
	{
		List<String> list = new ArrayList<String>();
		Iterator iter = JobEmulator.schedJobMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    Job job = (Job)entry.getValue();
		    list.add("#"+job.getSimID()+"-"+job.getJobID()+":"+job.getMakespan()+" "+job.getRealWallClockTime());
		    Iterator<BatchTask> iter2 = job.batchTaskList.iterator();
		    while(iter2.hasNext())
		    {
		    	BatchTask bt = iter2.next();
		    	list.add("  "+bt.toString());
		    }
		}
	    FileControler.print2File(list, logFile);
	}

	public static void compStatResult(JobTrace jobTrace)
	{
		float procRatio_sum_total = 0;
		float realWCLength_sum_total = 0;
		float workload_sum_total = 0;
		float procRatio_min_total = 1;
		int totalNum = 0;

		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			float procRatio_sum_pjob = 0;
			float realWCLength_sum_pjob = 0;
			float workload_sum_pjob = 0;
			float procRatio_min_pjob = 1;

			PJobContainer pc = iter.next();
			int totalJobNum = pc.jobList.size();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				float procRatio_sum_job = 0;
				float realWCLength_sum_job = 0;
				float workload_sum_job = 0;
				float procRatio_min_job = 1;

				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					float procWorkload = bt.getProcWorkload();
					float realWCLength = bt.getRealWallClockLength();
					float procRatio = procWorkload/realWCLength;
					procRatio_min_job = procRatio<procRatio_min_job?procRatio:procRatio_min_job;
					procRatio_sum_job+=procRatio;
					realWCLength_sum_job+=realWCLength;
					workload_sum_job+=procWorkload;
				}
				int btSize = job.batchTaskList.size();
				float meanProcRatio = procRatio_sum_job/btSize;
				float meanProcWorkload = workload_sum_job/btSize;
				float meanRealWCLength = realWCLength_sum_job/btSize;
				job.setMeanProcRatio(meanProcRatio);
				procRatio_sum_pjob+=meanProcRatio;
				job.setMeanProcWorkload(meanProcWorkload);
				workload_sum_pjob+=meanProcWorkload;
				job.setMeanRealWCLength(meanRealWCLength);
				realWCLength_sum_pjob+=meanRealWCLength;
				job.setMinProcRatio(procRatio_min_job);
				if(procRatio_min_pjob>procRatio_min_job)
					procRatio_min_pjob = procRatio_min_job;
			}
			float meanProcRatio_pjob = procRatio_sum_pjob/totalJobNum;
			float meanProcWorkload_pjob = workload_sum_pjob/totalJobNum;
			float meanRealRCLength_pjob = realWCLength_sum_pjob/totalJobNum;
			pc.setMeanProcRatio(meanProcRatio_pjob);
			pc.setMeanProcWorkload(meanProcWorkload_pjob);
			pc.setMeanRealWCLength(meanRealRCLength_pjob);
			pc.setMinProcRatio(procRatio_min_pjob);

			procRatio_sum_total+=procRatio_sum_pjob;
			realWCLength_sum_total+=realWCLength_sum_pjob;
			workload_sum_total+=workload_sum_pjob;
			totalNum+=totalJobNum;
			if(procRatio_min_pjob<procRatio_min_total)
				procRatio_min_total = procRatio_min_pjob;
		}
		jobTrace.setMeanProcRatio(procRatio_sum_total/totalNum);
		jobTrace.setMeanProcWorkload(workload_sum_total/totalNum);
		jobTrace.setMeanRealWCLength(realWCLength_sum_total/totalNum);
		jobTrace.setMinProcRatio(procRatio_min_total);
	}

	public static void printObserver(Observer ob, String obFilePath)
	{
		List<String> obList = new ArrayList<String>();
		float interval = ob.getObInterval();
		for(int i = 0;i<ob.sbtParallelismList.size();i++)
		{
			float time = (float)(interval*i/3600.0); //unit: hour
			obList.add(time+" "+ob.sjobParallelismList.get(i)+" "
			+ob.rjobParallelismList.get(i)+" "+ob.sbtParallelismList.get(i)+" "+ob.rbtParallelismList.get(i)+" "+ob.qLengthList.get(i));
		}
		FileControler.print2File(obList, obFilePath);
	}

	public static void printMigModeStat(String migFile)
	{
		List<String> resultList = new ArrayList<String>();
		List<String> btList = PVFile.getDir(Initialization.cpStateDir);
		Iterator<String> iter = btList.iterator();
		while(iter.hasNext())
		{
			String btDir = iter.next();
			String statePath = Initialization.cpStateDir+"/"+btDir+"/bt.obj";
			if(PVFile.isExist(statePath))
			{
				BatchTask bt = FileControler.loadBatchTaskFromFile(statePath);
				String s = bt.getBtID()+" "+bt.processMigMode[0]+" "+bt.processMigMode[1];
				resultList.add(s);
			}
		}
		FileControler.print2File(resultList, migFile);
	}
}

class LogJob implements Comparable<LogJob>
{
	private String jobID;
	private String simJobID;
	private float procMakespan = 0; //longest processed length
	private float totalLength;
	private float realLength;
	public Hashtable<String, Float> btProcLoadMap = new Hashtable<String, Float>();
	public Hashtable<String, Float> btTotalLengthMap = new Hashtable<String, Float>();
	public Hashtable<String, Float> btRealLengthMap = new Hashtable<String, Float>();

	public LogJob(String jobID, String simJobID, float totalLength, float realLength) {
		this.jobID = jobID;
		this.simJobID = simJobID;
		this.totalLength = totalLength;
		this.realLength = realLength;
	}
	public String getJobID() {
		return jobID;
	}
	public void setJobID(String jobID) {
		this.jobID = jobID;
	}
	public String getSimJobID() {
		return simJobID;
	}
	public void setSimJobID(String simJobID) {
		this.simJobID = simJobID;
	}
	public float getProcMakespan() {
		return procMakespan;
	}
	public void setProcMakespan(float procMakespan) {
		this.procMakespan = procMakespan;
	}
	public float getTotalLength() {
		return totalLength;
	}
	public void setTotalLength(float totalLength) {
		this.totalLength = totalLength;
	}
	public float getRealLength() {
		return realLength;
	}
	public void setRealLength(float realLength) {
		this.realLength = realLength;
	}
	public float getProcRatio()
	{
		return procMakespan/realLength;
	}
	public void computeProcMakespan()
	{
		Iterator iter = btProcLoadMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String btID = (String)entry.getKey();
		    Float procLoad = (Float)entry.getValue();
		    if(procMakespan < procLoad)
		    	procMakespan = procLoad;
		}
	}
	public List<String> toStringList()
	{
		List<String> rList = new ArrayList<String>();
		List<String> btList = new ArrayList<String>();
		Iterator iter = btProcLoadMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String btID = (String)entry.getKey();
		    Float btProcLoad = (Float)entry.getValue();
		    float btTotalLength = btTotalLengthMap.get(btID);
		    float btRealLength = btRealLengthMap.get(btID);
		    realLength = Math.max(btRealLength, realLength);
		    float btProcRatio = btProcLoad/btRealLength;
		    btList.add("  "+btID+" "+btProcLoad+" "+btTotalLength+" "+btRealLength+" "+btProcRatio);
		}
		rList.add("#"+simJobID+" "+jobID+" "+procMakespan+" "+totalLength+" "+realLength+" "+getProcRatio());
		rList.addAll(btList);
		return rList;
	}
	public int compareTo(LogJob other)
	{
		int simJobID = Integer.parseInt(this.simJobID);
		int otherID = Integer.parseInt(other.simJobID);
		if(simJobID<otherID)
			return -1;
		else if(simJobID>otherID)
			return 1;
		else
			return 0;
	}
}

class ParseContextThread extends Thread
{
	private String simJobID;
	private String btID;
	private String stateDir;
	private Map<String, String> btContextMap;
	private Hashtable<String, LogJob> logJobMap;

	public ParseContextThread(String btID,
			String stateDir, Map<String, String> btContextMap, Hashtable<String, LogJob> logJobMap) {
		this.simJobID = btID.split("-")[0];
		this.btID = btID;
		this.stateDir = stateDir;
		this.btContextMap = btContextMap;
		this.logJobMap = logJobMap;
	}
	public void run()
	{
		float remainLoad = 90000000;
		String runstateFile = stateDir+"/"+btID+"/run.state";

		List<String> runstateList = FileControler.readFile(runstateFile);
		if(runstateList!=null)
		{
			int size = runstateList.size();
			if(!runstateList.get(size-1).equals("done"))
			{
				String contextFile = btContextMap.get(btID);
				System.out.println("[ParseContextThread]:contextFile="+contextFile);
				if(contextFile!=null&&PVFile.getFileLength(contextFile)!=0)
				{
					String output = Cmd.cr_restart(contextFile);
					if(output==null)
						System.out.println("[Logger]:execute cr_restart Error! output==null.");
					else if(!output.equals(""))
					{
						System.out.println("[Logger]:execute cr_restart Error!");
						System.out.println("[Logger]:contextFile:"+contextFile);
						System.out.println(output);
					}
					String exeLogFile = stateDir+"/"+btID+"/exe.log";
					if(PVFile.isExist(exeLogFile))
					{
						List<String> exeLogList = FileControler.readFile(exeLogFile);
						String[] lastLine = exeLogList.get(exeLogList.size()-1).split("\\s");
						remainLoad = Float.parseFloat(lastLine[3]);
					}
				}
			}
			else
				remainLoad = 0;
		}
		else
		{
			//System.out.println("contextFile="+contextFile);
			System.out.println("Error: Logger : runstatelist == null!");
			System.out.println("runStateFile="+runstateFile);
			//System.exit(0);
		}
		LogJob logJob = logJobMap.get(simJobID);
		if(logJob!=null)
		{
			float procWorkload = logJob.getTotalLength() - remainLoad;
			if(procWorkload < 0)
				procWorkload = 0;
			logJob.btProcLoadMap.put(btID, Float.valueOf(procWorkload));
		}
		else
		{
			System.out.println("logjob=NULL="+btID+";job's real length does not exist!");
		}

/*		synchronized (Logger.synMutex) {
			Logger.counter++;
			Logger.synMutex.notify();
		}*/
	}
}