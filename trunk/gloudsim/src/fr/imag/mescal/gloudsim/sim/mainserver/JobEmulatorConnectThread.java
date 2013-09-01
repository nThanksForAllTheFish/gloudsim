package fr.imag.mescal.gloudsim.sim.mainserver;

import java.net.Socket;
import java.util.Hashtable;
import java.util.List;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.MemState;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;
/**
 * Connect VM to start the task execution (process)
 * @author sdi
 *
 */
public class JobEmulatorConnectThread extends Thread {

	private String targetVM;
	private BatchTask bt;

	public JobEmulatorConnectThread(String targetVM, BatchTask bt) {
		this.targetVM = targetVM;
		this.bt = bt;
	}

	public void run()
	{
		boolean connectOK = true;
		Socket socket = null;
		try {
			socket = new Socket(targetVM, JobEmulator.VMServerStartBTPort);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[memStateChecker]Exception Error: server="+targetVM+";port="+JobEmulator.VMServerStartBTPort);
			MemState ms = MemStateChecker.memStateMap.get(targetVM);
			ms.setCheckAvailRamdiskSize(-1);
			ms.setCheckAvailMemSize(-1);
			connectOK = false;
		}
		
		if(connectOK)
		{
			TCPClient tcpClient = new TCPClient(socket);
			double schedTime = System.currentTimeMillis()/1000.0;
			bt.setExpSchedTime(schedTime);
			tcpClient.pushBatchTask(bt);
			tcpClient.closeSocket();
			//start running the bt
			Hashtable<String, BatchTask> runningTable = JobEmulator.vmRunningBTMap.get(targetVM);
			runningTable.put(bt.getBtID(), bt);
			JobEmulator.schedBTMap.put(bt.getBtID(), bt);
		}
		else
		{
			JobEmulator.scheduler.add2List(bt);
		}
	}
	
}
