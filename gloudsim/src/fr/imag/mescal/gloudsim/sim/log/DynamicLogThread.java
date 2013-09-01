package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * This class is used to record the simulation log information over time, 
 * instead of generating file at the end. 
 * This is useful when the whole simulation cannot end normally eventually, because
 * the log information can be generated over time. 
 * However, it suffers more or less longer simulation time because of frequent I/O.
 * @author sdi
 *
 */
public class DynamicLogThread extends Thread {

	String schedJobFile = "";
	
	public DynamicLogThread(String schedJobFile) {
		this.schedJobFile = schedJobFile+"2";
	}

	public void run()
	{
		while(true)
		{
			processSchedJob();
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void processSchedJob()
	{
		List<String> list = new ArrayList<String>();
		Iterator iter = JobEmulator.schedJobMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    Job job = (Job)entry.getValue();
		    String jobSimID = String.valueOf(job.getSimID());
		    if(job.getRealWallClockTime()>0&&!Logger.dynSchedJobMap.containsKey(jobSimID))//finish
		    {
			    list.add("#"+job.getSimID()+"-"+job.getJobID()+":"+job.getMakespan()+" "+job.getRealWallClockTime());
			    Iterator<BatchTask> iter2 = job.batchTaskList.iterator();
			    while(iter2.hasNext())
			    {
			    	BatchTask bt = iter2.next();
			    	list.add("  "+bt.toString());
			    }
			    Logger.dynSchedJobMap.put(jobSimID, null);
		    }
		}
		if(!list.isEmpty())
			FileControler.append2File(list, schedJobFile);
	}
}
