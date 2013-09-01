package fr.imag.mescal.gloudsim.sim.mainserver;

import java.net.Socket;
import java.util.Hashtable;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.MemState;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;

/**
 * send messages to slave nodes for restarting tasks. 
 * @author sdi
 *
 */
public class StateCheckerRestartTaskThread extends Thread{

	private String targetVM;
	private String batchTaskID;
	private BatchTask bt;
	
	public StateCheckerRestartTaskThread(String targetVM, String batchTaskID) {
		this.targetVM = targetVM;
		this.batchTaskID = batchTaskID;
		bt = JobEmulator.schedBTMap.get(batchTaskID);
	}
	
	public StateCheckerRestartTaskThread(String targetVM, BatchTask batchTask)
	{
		this.targetVM = targetVM;
		this.batchTaskID = batchTask.getBtID();
		this.bt = batchTask;
	}

	public void run()
	{
		Socket socket = null;
		try {
			socket = new Socket(targetVM, VMServer.RestartBTPort);
			TCPClient tcpClient = new TCPClient(socket);
			//message format: 'batch task id'
			String message = batchTaskID;
			tcpClient.pushString(message);
			tcpClient.closeSocket();
			Hashtable<String, BatchTask> runningTable = JobEmulator.vmRunningBTMap.get(targetVM);
			runningTable.put(batchTaskID, bt);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[memStateChecker]Exception Error: server="+targetVM+";port="+VMServer.RestartBTPort);
			MemState ms = MemStateChecker.memStateMap.get(targetVM);
			ms.setCheckAvailRamdiskSize(-1);
			ms.setCheckAvailMemSize(-1);
			JobEmulator.scheduler.add2List(bt);
		}
	}
}
