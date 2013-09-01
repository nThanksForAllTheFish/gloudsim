package fr.imag.mescal.gloudsim.sim.mainserver;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;

/**
 * to monitor the simulation progress and determine when to stop the 
 * simulation
 * @author sdi
 *
 */
public class SimulationProgressMonitorThread extends Thread{
	
	private Hashtable<String, Job> schedJobMap = null;
	private double initTime = System.currentTimeMillis()/1000.0;
	private int endTime;
	
	public SimulationProgressMonitorThread(
			Hashtable<String, Job> schedJobMap, int endTime) 
	{
		this.schedJobMap = schedJobMap;
		this.endTime = endTime;
	}

	public void run()
	{
		while(true)
		{
			//System.out.println("[SimulationProgressMonitorThread] check....");
			boolean alldone = true;
			String unfinishedBT = null;
			if(schedJobMap == null || schedJobMap.isEmpty())
			{
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}
			Iterator iter = schedJobMap.entrySet().iterator(); 
			while (iter.hasNext()) 
			{ 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    Job job = (Job)entry.getValue();
			    if(job.getRealWallClockTime()>0)
			    	continue;
			    boolean jobDone = true;
			    Iterator<BatchTask> iter2 = job.batchTaskList.iterator();
			    float maxRealWCLength = 0;
			    while(iter2.hasNext())
			    {
			    	BatchTask bt = iter2.next();
			    	if(bt.getRealWallClockLength()==0)
			    	{
			    		if(unfinishedBT==null)
			    			unfinishedBT = bt.getBtID();
			    		jobDone = false;
			    		alldone = false;
			    		break;
			    	}
			    	else
			    	{
			    		maxRealWCLength = Math.max(maxRealWCLength, bt.getRealWallClockLength());
			    	}
			    }
			    if(jobDone)
			    	job.setRealWallClockTime(maxRealWCLength);
			}
			if(alldone)
			{
				System.out.println("***********finish: all done.**************");
				break;
			}
			else
			{
	    		int curTime = (int)(System.currentTimeMillis()/1000.0 - initTime);
				System.out.println("[ProgressMonitor]"+curTime+": Unfinished BT====="+unfinishedBT);
				if(curTime>endTime)
				{
					System.out.println("***********finish: break done.**************");
					break;
				}
			}
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("[SimulationProgressMonitorThread]done.");
		synchronized (schedJobMap) {
			//the simulation is done....
			schedJobMap.notify();
		}
	}
}
