package fr.imag.mescal.gloudsim.sim.mainserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.MemState;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * MemStateChecker is used to periodically send message to slave VM nodes, to check the slave nodes' real-time mem states.
 * 
 * @author sdi
 *
 */
public class MemStateChecker extends Thread{
	public static Hashtable<String, MemState> memStateMap = new Hashtable<String, MemState>();
	public static List<MemState> memStateList = new ArrayList<MemState>();
	public static float reservedSize = 50; //MB
	public static float OSMem = 50; 
	
	public MemStateChecker()
	{
		Iterator<String> iter = JobEmulator.vmHostList.iterator();
		while(iter.hasNext())
		{
			String hostname = iter.next();
			MemState ms = new MemState(hostname, 
					Initialization.ramdiskSize-reservedSize, Initialization.memSize-reservedSize, 
					Initialization.memSize-reservedSize);
			memStateMap.put(hostname, ms);
			memStateList.add(ms);
		}
	}
	
	public void run()
	{
		Iterator<String> iter = JobEmulator.vmHostList.iterator();
		while(iter.hasNext())
		{
			String targetHost = iter.next();
			System.out.println("targetHost="+targetHost);
			new MemStateCheckerCommThread(targetHost, memStateMap).start();
		}
	}
	
	public static synchronized String findMaxAvailVM(int avoidDeviceID, float expectedSize)
	{
		Collections.sort(memStateList);
		//System.out.println("memStateList="+printList(memStateList));
		Iterator<MemState> iter = memStateList.iterator();
		while(iter.hasNext())
		{
			MemState ms = iter.next();
			if(ms.getVmID()%Initialization.numOfPhyHosts!=avoidDeviceID && ms.getMinSize()>expectedSize)
			{
				//System.out.println("========ms.getVmHostName()="+ms.getVmHostName()+";ms.getMin="+ms.getMinSize()+";expectedSize="+expectedSize);
				ms.setEstimateAvailMemSize(ms.getEstimateAvailMemSize()-expectedSize);
				return ms.getVmHostName();
			}
		}
		return null;
	}
	
	/**
	 * The next vm hostname should be different from prevVMHost
	 * @param avoidDeviceID
	 * @param bt
	 * @param prevVMHost
	 * @return
	 */
	public static synchronized String findMaxAvailVM(int avoidDeviceID, BatchTask bt, String prevVMHost)
	{
		float expectedSize = bt.getMeanMemSize();
		Collections.sort(memStateList);
		//System.out.println("memStateList="+printList(memStateList));
		Iterator<MemState> iter = memStateList.iterator();
		while(iter.hasNext())
		{
			MemState ms = iter.next();
			if(ms.getVmHostName().equals(prevVMHost)) //avoid using the same hosts for consecutive assignments, otherwise, "mv" operation will encounter error.
				continue;
			if(ms.getVmID()%Initialization.numOfPhyHosts!=avoidDeviceID && ms.getMinSize()>expectedSize)
			{
				ms.setEstimateAvailMemSize(ms.getEstimateAvailMemSize()-expectedSize);
				//System.out.println("========ms.getVmHostName()="+ms.getVmHostName()+";ms.getMin="+ms.getMinSize()+";expectedSize="+expectedSize+";bt="+bt.getBtID());
				return ms.getVmHostName();
			}
		}
		return null;
	}
	
	static String printList(List<MemState> list)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<MemState> iter = list.iterator();
		while(iter.hasNext())
		{
			String vmName = iter.next().toString();
			sb.append(vmName);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	static String printMaxMinMem(List<MemState> list)
	{
		return list.get(0).toString()+" --- "+list.get(list.size()-1).toString();
	}
	
	public static synchronized void procTaskFinishFailure(String vmHost, BatchTask bt)
	{
		Hashtable<String, BatchTask> runningTable = JobEmulator.vmRunningBTMap.get(vmHost);
		runningTable.remove(bt.getBtID());
		MemState ms = MemStateChecker.memStateMap.get(vmHost);
		ms.setEstimateAvailMemSize(ms.getEstimateAvailMemSize()+bt.getMeanMemSize());
	}
}

class MemStateCheckerCommThread extends Thread
{
	private String vmHost;
	private Hashtable<String, MemState> memStateMap;
	
	public MemStateCheckerCommThread(String vmHost,
			Hashtable<String, MemState> memStateMap) {
		this.vmHost = vmHost;
		this.memStateMap = memStateMap;
	}

	public void run()
	{
		boolean goOn = true;
		while(goOn)
		{
			Socket socket = null;
			try {
				socket = new Socket(vmHost, VMServer.MemStateCheckerPort);
				TCPClient client = new TCPClient(socket);
				String msg = client.readReplyMsg();
				client.closeSocket();
				String[] s = msg.split("\\s");
				float availRamdiskSize = Float.parseFloat(s[0]);
				float availMemSize = Float.parseFloat(s[1]);
				MemState ms = memStateMap.get(vmHost);
				ms.setCheckAvailRamdiskSize(availRamdiskSize);
				ms.setCheckAvailMemSize(availMemSize);
				synchronized (JobEmulator.termMutex) {
					if(JobEmulator.termination)
					{
						System.out.println("Stop MemStateChecker....");
						goOn = false;
					}
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				//e.printStackTrace();
				MemState ms = memStateMap.get(vmHost);
				ms.setCheckAvailRamdiskSize(-1);
				ms.setCheckAvailMemSize(-1);
				ms.setEstimateAvailMemSize(-2000);
				//goOn = false;
				//reschedule the previously running batch-tasks
				Hashtable<String, BatchTask> runningTable = JobEmulator.vmRunningBTMap.get(vmHost);
				System.out.println("XXXXXXXXXXXXX[memStateChecker]Exception Error: server="+vmHost+";port="+VMServer.MemStateCheckerPort+";runningTable.size="+runningTable.size());
				if (!runningTable.isEmpty()) { //traverse runningTable
					synchronized (runningTable) 
					{
						Iterator iter = runningTable.entrySet().iterator(); 
						while (iter.hasNext()) { 
						    Map.Entry entry = (Map.Entry) iter.next(); 
						    BatchTask bt = (BatchTask)entry.getValue(); 
//						    bt.setCurTaskIndex(bt.getCurTaskIndex()-1);
						    JobEmulator.scheduler.add2List(bt);
						} 
						runningTable.clear();
					}
				}
				goOn = false; //break the loop
			}
		}
	}
}
