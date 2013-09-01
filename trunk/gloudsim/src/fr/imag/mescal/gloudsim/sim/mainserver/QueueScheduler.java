package fr.imag.mescal.gloudsim.sim.mainserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.MemState;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * When there are over-many jobs submitted, the total resource in the cluster
 * will become inadquate. Then, QueueScheduler will help queuing batchtasks.
 * Note: BatchTask.java refers to a defacto task mentioned in my SC'13 paper, while Task.java means an uninterrupted execution duration.  
 * @author sdi
 *
 */
public class QueueScheduler extends Thread {

	public List<BatchTask> pendingList = new ArrayList<BatchTask>();
	
	public void run()
	{
		boolean goOn = true;
		while (goOn) {
			synchronized (MemStateChecker.class) {
				//System.out.println("memStateList="
				//		+ MemStateChecker.printList(MemStateChecker.memStateList));
				System.out.println("======memStateList=" + MemStateChecker.printMaxMinMem(MemStateChecker.memStateList));
			}
			synchronized (this) {
				for (int i = 0;i<pendingList.size();i++) {
					BatchTask bt = pendingList.get(i);
					int deviceID = bt.getDeviceID();
					float expectedSize = bt.getMeanMemSize();
					String targetVM = MemStateChecker.findMaxAvailVM(deviceID, expectedSize); //don't have to forcely use different hosts, because the queue must delay more or less 
					if(targetVM==null){
						//System.out.println("Pending batchtask bt="+bt.getBtID()+":bt.getMeanMemSize()="+bt.getMeanMemSize());
					}
					else
					{
						if(JobEmulator.schedBTMap.containsKey(bt.getBtID()))
							reschedule(targetVM, bt);
						else
							schedule(targetVM, bt);
						pendingList.remove(i);
						i--;
					}
				}
				System.out.println("[QScheduler]qList "+qListToString());
			}
			synchronized (JobEmulator.termMutex) {
				if(JobEmulator.termination)
				{
					System.out.println("Stop QueueScheduler....");
					goOn = false;
				}
			}
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void add2List(BatchTask bt)
	{
		pendingList.add(bt);
	}
	
	public static void schedule(String targetVM, BatchTask bt)
	{
		//System.out.println("------schedule:targetVM="+targetVM+";bt="+bt.getBtID()+";bt-mem="+bt.getMeanMemSize()*Initialization.maxCapMemSize+";bt="+bt);
		//double check whether memory is ok!!
		MemState ms = MemStateChecker.memStateMap.get(targetVM);
		float targetVMMemSize = Math.min(ms.getCheckAvailMemSize(), ms.getCheckAvailRamdiskSize());
		if(targetVMMemSize>=bt.getMeanMemSize())
		{
			System.out.println("[JobEmulatorThread:schedule]btID="+bt.getBtID()+";targetVM="+targetVM+"------->>>");
			List<String> mvstate = new ArrayList<String>();
			mvstate.add("-1");
			FileControler.print2File(mvstate, Initialization.cpStateDir+"/"+bt.getBtID()+"/move.state");
			JobEmulatorConnectThread ject = new JobEmulatorConnectThread(targetVM, bt);
			ject.start();
		}
		else
		{
			System.out.println("[JobEmulatorThread:schedule]btID="+bt.getBtID()+";targetVM="+targetVM+"<<<-------");
			JobEmulator.scheduler.add2List(bt);
		}
	}
	
	public static void reschedule(String targetVM, BatchTask bt)
	{
		System.out.println("[JobEmulatorThread:reschedule]btID="+bt.getBtID()+";targetVM="+targetVM+";bt="+bt.getBtID());
    	StateCheckerRestartTaskThread scrtt = new StateCheckerRestartTaskThread(targetVM, bt);
    	scrtt.start();	
	}
	
	public String qListToString()
	{
		if(pendingList.isEmpty())
			return "is empty.";
		StringBuilder sb = new StringBuilder();
//		sb.append("="+pendingList.size());
		Iterator<BatchTask> iter = pendingList.iterator();
		while(iter.hasNext())
		{
			BatchTask bt = iter.next();
			sb.append(bt.getBtID());
			sb.append(" ");
		}
		return sb.toString().trim();
	}
}
