package fr.imag.mescal.optft.sim.mainserver;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.optft.elem.BatchTask;
import fr.imag.mescal.optft.elem.Device;
import fr.imag.mescal.optft.elem.Job;
import fr.imag.mescal.optft.prepare.JobTaskSimulator;
import fr.imag.mescal.optft.util.FileControler;
import fr.imag.mescal.optft.util.Initialization;
import fr.imag.mescal.optft.util.NFSControler;
import fr.imag.mescal.optft.util.RdGenerator;
/**
 * generate all batch tasks for one job simulated
 * @author sdi
 *
 */
public class JobEmulatorThread extends Thread {
	private List<Job[]> sampleJobsList;
	private Hashtable<String, Job> schedJobMap;
	
	public JobEmulatorThread(List<Job[]> sampleJobsList, Hashtable<String, Job> schedJobMap) {
		this.sampleJobsList = sampleJobsList;
		this.schedJobMap = schedJobMap;
	}

	public void run()
	{
//		System.out.println("[JobEmulatorThread] starting jobemulatorthread...");
		Job job = null;
		if(Initialization.testMode.equals("dynamic") && Initialization.taskMode.equals("single"))
		{
			job = JobEmulator.ranDynOneJob(sampleJobsList);
			JobTaskSimulator.genJobBTStat(job);
		}
		else if(Initialization.testMode.equals("dynamic")&&Initialization.taskMode.equals("batch"))
		{
//			int btNum = RdGenerator.RAN_SeedGen6.generate_Int(2, 3);
//			job = JobEmulator.ranDynOneJob(sampleJobsList, btNum, 
//					Initialization.minConnectBTNum, Initialization.maxConnectBTNum);
//			JobTaskSimulator.genJobBTStat(job);
			System.err.println("Drror: Unimplemented dynamic + batch mode....!!!!!!!!!!!!!!!!");
			System.exit(0);
		}
		else if(Initialization.testMode.startsWith("static"))
			job = JobEmulator.ranGenOneJob(sampleJobsList.get(0));
		
		schedJobMap.put(String.valueOf(job.getSimID()), job);
		
		System.out.println("[JobEmulatorThread]job.getJobID()="+job.getJobID()+";simJobID="+job.getSimID());
		Iterator<BatchTask> iter = job.batchTaskList.iterator();
		
		for(int i = 0;iter.hasNext();i++)
		{
			BatchTask bt = iter.next(); //object to push
			String btID = job.getSimID()+"-"+i;
			bt.setBtID(btID);
			synchronized (JobEmulator.monPortMutex) {
				bt.setMonPort(JobEmulator.monPort++);
			}
			//String nfsDevice = NFSControler.getNextContextDevice();
			Device device = NFSControler.getLightestDevice();
			device.setRunningBTNum(device.getRunningBTNum()+1);
			int deviceID = device.getID();
			String nfsDevice = Initialization.cpNFSContextDir+"/"+deviceID;
//			String[] s = nfsDevice.split("/");
//			int deviceID = Integer.parseInt(s[s.length-1]);
			
			bt.setNfsDeviceContextFile(nfsDevice); //tmporary filling
			System.out.println("[JobEmulatorThread]bt:"+bt.getBtID()+";nfsDevice="+nfsDevice);
			
			NFSControler.nfsDeviceMap.put(bt.getBtID(), device);
			//String targetVM = JobEmulator.ranGenVM(deviceID); //target host
			float memSize = bt.getMeanMemSize();
			String targetVM = MemStateChecker.findMaxAvailVM(deviceID, bt, "");
			
			if(targetVM==null){
				System.out.println("[JobEmulatorThread]pending batchtask bt="+bt.getBtID());
				JobEmulator.scheduler.add2List(bt);
			}
			else
			{
				QueueScheduler.schedule(targetVM, bt);
			}
			
		}
		System.out.println("[JobEmulatorThread]All batch tasks of the job "+job.getJobID()+" has been started.");
		
	}
}


