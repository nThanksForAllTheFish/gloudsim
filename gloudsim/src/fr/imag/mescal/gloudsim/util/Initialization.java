package fr.imag.mescal.gloudsim.util;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Device;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.MemCost;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.sim.cp.OptCPAnalyzer;
import fr.imag.mescal.gloudsim.sim.numeric.Analyzer;

public class Initialization {
	public static boolean dynamicLog = true;
	
	public static boolean needToLoadCpFiles = true;
	
	public static int numOfPhyHosts = 8;
	public static boolean completeAllWorkload = true;
	public static int paralleldegree = 1;
	
	public static int ramdiskSize = 1000; //MB
	public static int memSize = 1000; //MB
	public static float simMaxMemSize = 200; //MB
	public static float simMinMemSize = 10; //MB
	
	public static String nfsCpcostFile;
	public static String ramfsCpcostFile;
	public static String nfsRscostFile;
	public static String ramfsRscostFile;
	
	public static String jobTraceDir = "";
	public static String jobTraceFileName = "";
	public static String jobArrivalTraceFile = "";
	public static String mainServerAddress = "";
	public static String cpStateDir = "";
	public static String cpNFSContextDir = "";
	public static String cpLocalContextDir = "";
	public static String debugRootDir = "";
	public static String heapDir = "";
	public static boolean useJobArrivalTrace = false;
	public static int simJobNum = 1;
	public static float maxCapMemSize = 0;
	public static float tuneWorkloadRatio = 0.99f;
	public static List<MemCost> nfsCpcostList = new ArrayList<MemCost>();
	public static List<MemCost> ramfsCpcostList = new ArrayList<MemCost>();
	public static List<MemCost> nfsRscostList = new ArrayList<MemCost>();
	public static List<MemCost> ramfsRscostList = new ArrayList<MemCost>();
	public static boolean cpOurFormula = true;
	
	public static int maxBTNumPerJob;
	
	public static String testMode = "static";//static or dynamic
	public static String taskMode = "single"; //single or batch
	public static int minConnectBTNum; //unimplemented
	public static int maxConnectBTNum;//unimplemented
	public static int[] dynPriority;
	public static boolean dynamicSolu = false;
	public static boolean useRamfs = false;
	public static List<String> blackJobList = new ArrayList<String>(); //some jobs are exceptional

	public static void load(String confFile) {
		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(confFile);
			props.load(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dynamicLog = Boolean.parseBoolean(props.getProperty("dynamicLog"));
		
		numOfPhyHosts = Integer.parseInt(props.getProperty("numOfPhyHosts"));
		for(int i = 0;i<numOfPhyHosts;i++)
			NFSControler.nfsDeviceList.add(new Device(i));
		completeAllWorkload = Boolean.parseBoolean(props.getProperty("completeAllWorkload"));
		paralleldegree = Integer.parseInt(props.getProperty("paralleldegree"));
		
		ramdiskSize = Integer.parseInt(props.getProperty("ramdiskSize"));
		memSize = Integer.parseInt(props.getProperty("memSize"));
		
		jobTraceDir = props.getProperty("jobTraceDir");
		jobTraceFileName = props.getProperty("jobTraceFileName");
		jobArrivalTraceFile = jobTraceDir+"/"+props.getProperty("jobArrivalTraceFile");
		mainServerAddress = props.getProperty("mainServerAddress");
		cpNFSContextDir = props.getProperty("cpNFSContextDir");
		cpLocalContextDir = props.getProperty("cpLocalContextDir");
		cpStateDir = props.getProperty("cpStateDir");
		debugRootDir = props.getProperty("debugRootDir");
		heapDir = props.getProperty("heapDir");
		useJobArrivalTrace = Boolean.parseBoolean(props.getProperty("useJobArrivalTrace"));
		simJobNum = Integer.parseInt(props.getProperty("simJobNum"));
		OptCPAnalyzer.loadRatio = Float.parseFloat(props.getProperty("loadRatio"));
		Analyzer.cpCost = Float.parseFloat(props.getProperty("cpCost")); //deprecated
		maxCapMemSize = Float.parseFloat(props.getProperty("maxCapMemSize"));
		simMaxMemSize = Float.parseFloat(props.getProperty("simMaxMemSize"));
		simMinMemSize = Float.parseFloat(props.getProperty("simMinMemSize"));
		tuneWorkloadRatio = Float.parseFloat(props.getProperty("tuneWorkloadRatio"));
		nfsCpcostFile = props.getProperty("nfsCpcostFile");
		ramfsCpcostFile = props.getProperty("ramfsCpcostFile");
		nfsRscostFile = props.getProperty("nfsRscostFile");
		ramfsRscostFile = props.getProperty("ramfsRscostFile");
		
		if (needToLoadCpFiles) {
			loadCpFile(nfsCpcostFile, nfsCpcostList);
			loadCpFile(ramfsCpcostFile, ramfsCpcostList);
			loadCpFile(nfsRscostFile, nfsRscostList);
			loadCpFile(ramfsRscostFile, ramfsRscostList);
		}
		
		maxBTNumPerJob = Integer.parseInt(props.getProperty("maxBTNumPerJob"));
		//cpOurFormula = Boolean.parseBoolean(props.getProperty("cpOurFormula"));
		testMode = props.getProperty("testMode");
		taskMode = props.getProperty("taskMode");
		minConnectBTNum = Integer.parseInt(props.getProperty("minConnectBTNum"));
		maxConnectBTNum = Integer.parseInt(props.getProperty("maxConnectBTNum"));
		
		String dynPriorS = props.getProperty("dynPriority");
		String[] d = dynPriorS.split("\\s");
		dynPriority = new int[d.length];
		for(int i = 0;i<dynPriority.length;i++)
			dynPriority[i] = Integer.parseInt(d[i]);
		
		dynamicSolu = Boolean.parseBoolean(props.getProperty("dynamicSolu"));
		useRamfs = Boolean.parseBoolean(props.getProperty("useRamfs"));
		
		Cmd.cp_Local_CMD = "cr_checkpoint --pid PID -f "+Initialization.cpLocalContextDir+"/BTID/context.PID";
		Cmd.rst_CMD = "cr_restart CONTEXT";
		
		String blackJobListString = props.getProperty("blackJobList");
		String[] data = blackJobListString.split("\\s");
		for(String a:data)
			blackJobList.add(a);
	}
	
	public static String getCP_NFS_CMD(String cpNFSDevice)
	{
		return "cr_checkpoint --pid PID -f "+cpNFSDevice+"/BTID/context.PID";
	}
	
	public static void loadCpFile(String file, List<MemCost> costList)
	{
		List<String> list = FileControler.readFile(file);
		Iterator<String> iter = list.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			int memID = Integer.parseInt(data[0]);
			float memSize = Float.parseFloat(data[1]);
			float cost = Float.parseFloat(data[2]);
			costList.add(new MemCost(memSize, memID, cost));
		}
		Collections.sort(costList);
	}
	
	public static synchronized void showProgress(double initLogTime, int i, int size, String key)
	{
		String currentTime = DateUtil.getTimeNow(new Date());
		long currentTimeValue = System.currentTimeMillis()/1000;
		System.out.println(currentTime+" : already "+(currentTimeValue-initLogTime)+" sec passed, ("+i+"/"+size+"): "+key);
	}

}

