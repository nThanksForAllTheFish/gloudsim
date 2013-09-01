package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

public class Observer extends Thread {

	static int dynamicLogTimeCounter = 0;
	
	public List<Integer> sjobParallelismList = new ArrayList<Integer>(); //over time
	public List<Integer> sbtParallelismList = new ArrayList<Integer>(); //over time
	public List<Integer> rjobParallelismList = new ArrayList<Integer>(); //over time
	public List<Integer> rbtParallelismList = new ArrayList<Integer>(); //over time	
	public List<Integer> qLengthList = new ArrayList<Integer>(); //over time
	private final float obInterval = 10f; //seconds

	public void run()
	{
		if(Initialization.dynamicLog&&PVFile.isExist(JobEmulator.observerFile))
			PVFile.deleteFile(JobEmulator.observerFile);
		
		boolean goOn = true;
		while(goOn)
		{
			int sjobP = 0, sbtP = 0, rjobP = 0, rbtP = 0; //scheduled and running job/bt
			synchronized(JobEmulator.schedJobMap)
			{
				Iterator iter = JobEmulator.schedJobMap.entrySet().iterator(); 
				while (iter.hasNext()) { 
				    Map.Entry entry = (Map.Entry) iter.next();  
				    Job job = (Job)entry.getValue(); 
				    if(job.getRealWallClockTime()>0)
				    	sjobP++;
				    else
				    	rjobP++;
				} 
			}
			
			synchronized(JobEmulator.schedBTMap)
			{
				Iterator iter = JobEmulator.schedBTMap.entrySet().iterator(); 
				while (iter.hasNext()) { 
				    Map.Entry entry = (Map.Entry) iter.next(); 
				    BatchTask bt = (BatchTask)entry.getValue(); 
				    if(bt.getRealWallClockLength()>0)
				    	sbtP++;
				    else
				    	rbtP++;
				} 
			}
						
			if(Initialization.dynamicLog)
			{	
				List<String> obList = new ArrayList<String>();
				
				float time = (float)(obInterval*dynamicLogTimeCounter/3600.0); //unit: hour
				dynamicLogTimeCounter++;
				String s = time+" "+sjobP+" "+rjobP+" "+sbtP+" "+rbtP;
				synchronized(JobEmulator.scheduler)
				{
					s+=" "+JobEmulator.scheduler.pendingList.size();
				}
				obList.add(s);
				
				FileControler.append2File(obList, JobEmulator.observerFile+"2");
			}
			sjobParallelismList.add(Integer.valueOf(sjobP));
			sbtParallelismList.add(Integer.valueOf(sbtP)); //The result will only be checked at the end, so no need to syn.
			rjobParallelismList.add(Integer.valueOf(rjobP));
			rbtParallelismList.add(Integer.valueOf(rbtP)); //The result will only be checked at the end, so no need to syn.
			synchronized(JobEmulator.scheduler)
			{
				qLengthList.add(Integer.valueOf(JobEmulator.scheduler.pendingList.size()));
			}
			
			synchronized (JobEmulator.termMutex) {
				if(JobEmulator.termination)
				{
					System.out.println("Stop MemStateChecker....");
					goOn = false;
				}
			}
			
			try {
				int sleepTime = (int)((obInterval-0.3)*1000);
				Thread.sleep(sleepTime); //10 seconds
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public float getObInterval() {
		return obInterval;
	}
	
}
