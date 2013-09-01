package fr.imag.mescal.gloudsim.sim.vmserver;

import java.util.ArrayList;
import java.util.List;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.sim.cp.CheckpointThread;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * To monitor task failure events and notify simulation server
 * @author sdi
 *
 */
public class TaskFailureMonitor extends Thread {

	private boolean isInterrupted = false;
	private String vmHostName;
	private BatchTask bt;
	private int procID;
	private float[] cpPos;
	
	public TaskFailureMonitor(BatchTask bt, int procID, String vmHostName, float[] cpPos) {
		this.bt = bt;
		this.procID = procID;
		this.vmHostName = vmHostName;
		this.cpPos = cpPos;
	}
	
	public void interrupt()
	{
		 isInterrupted = true;
		 if(this.isAlive())
			 super.interrupt();
	}

	public void run()
	{
//		System.out.println(VMServer.vmServerName+":[TaskFailureMonitor]start TaskFailureMonitor");
		int curIndex = bt.getCurTaskIndex();
		Task task = bt.taskList.get(curIndex);
		float nextFailure = task.getDuration();
		long tick_ = 100; //100 milliseconds
		
		//float remainLength = bt.getTotalTaskLength();
		//float cpCost = bt.getCpCost();
		//float curTaskFailureLength = bt.taskList.get(bt.getCurTaskIndex()).getDuration();
		//new CheckpointThread(bt.getBtID(), bt.getCurTaskIndex(), curTaskFailureLength,  
		//		procID, Initialization.cpOurFormula, cpCost, 
		//		remainLength, bt.getpJobMNum(), bt.getpJobMTBF()).start();
		System.out.println(VMServer.vmHostName+":[TaskFailureMonitor]start checkpoint thread...:procID="+procID+";btID="+bt.getBtID()+";duration="+task.getDuration());
		
		while(Cmd.getExeProcessID(new String[]{bt.getBtID()})<=0)
		{
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println("[TaskFailureMonitor]Exception: - waiting for process id....");
			}
		}
		
		CheckpointThread ct = null; 
		if (!isInterrupted) {
			ct = new CheckpointThread(bt.getCurTaskIndex(), bt, vmHostName, procID, cpPos);
			ct.start();
		}
		//System.out.println("[TaskFailureMonitor]:nextFailure="+nextFailure);
		//double beforeFailureCheck = System.currentTimeMillis()/1000.0;
		for(float c = 0;c<=nextFailure&&!isInterrupted;c=c+0.1f)
		{
			try {
				Thread.sleep(tick_);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!isInterrupted) {
			if(ct!=null)
				ct.interrupt();
			//float failureDuration = (float) (System.currentTimeMillis() / 1000.0 - beforeFailureCheck);
			//System.out.println("[TaskFailureMonitor]after failure event:"+ failureDuration);
			bt.setCurTaskIndex(bt.getCurTaskIndex() + 1);
			String runstateFile = Initialization.cpStateDir + "/"
					+ bt.getBtID() + "/run.state";
			while (!PVFile.isExist(runstateFile)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//float afterrunStateFile = (float) (System.currentTimeMillis() / 1000.0 - beforeFailureCheck);
			//System.out.println("[TaskFailureMonitor]after checking runstateFile:"+ afterrunStateFile);
			new MVContextThread(bt, procID).start();
			FileControler.writeObject2File(bt,
					VMServer.getBatchTaskFile(bt.getBtID()));
			if (FileControler.readFile(runstateFile).size() == 1) {
				if (bt.getCurTaskIndex() >= bt.taskList.size()) {
					//				VMServer.btIDBTMap.remove(bt.getBtID());
					//VMServer.btProcessMap.remove(bt.getBtID());
					//TODO: notify main server
					if (!Initialization.completeAllWorkload) {
						new KillProcThread(bt.getBtID(), procID).start();
						TCPClient tcpClient = new TCPClient(
								Initialization.mainServerAddress,
								JobEmulator.BTFinishReceiverPort);
						tcpClient.pushString(bt.getBtID() + " -1");
						tcpClient.closeSocket();
					}
				} else {
					new KillProcThread(bt.getBtID(), procID).start();
				}
				System.out.println("[TaskFailureMonitor]bt=" + bt.getBtID()
						+ ";isBetterUseNFSDevice()="
						+ bt.isBetterUseNFSDevice());
			}
		}
	}
}

class MVContextThread extends Thread
{
	BatchTask bt;
	int procID;
	
	public MVContextThread(BatchTask bt, int procID) {
		this.bt = bt;
		this.procID = procID;
	}

	public void run()
	{
		if(!bt.isBetterUseNFSDevice())
		{
			String ramfsContextFile = Initialization.cpLocalContextDir+"/"+bt.getBtID()+"/context."+procID;
    		//String[] s = bt.getNfsDeviceContextFile().split("/"); // "/contextNFS/2/3-0/context.pid"
    		String cpNFSDevice = Initialization.cpNFSContextDir+"/"+bt.getDeviceID();
    		int moveState = Cmd.move(ramfsContextFile, cpNFSDevice+"/"+bt.getBtID());
			if(moveState==1)
			{
				System.out.println("move:error");
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				moveState = Cmd.move(ramfsContextFile, cpNFSDevice+"/"+bt.getBtID());
			}
			//TODO set a tag to mark its completion
			System.out.println("[TaskFailureMonitor]move:bt.getCurTaskIndex()="+bt.getCurTaskIndex()+";btid="+bt.getBtID());
		}
		List<String> mvstate = new ArrayList<String>();
		mvstate.add(String.valueOf(bt.getCurTaskIndex()));
		FileControler.print2File(mvstate, Initialization.cpStateDir+"/"+bt.getBtID()+"/move.state");
	}
}

class KillProcThread extends Thread
{
	private String btID;
	private int processID;

	public KillProcThread(String btID, int processID) {
		this.btID = btID;
		this.processID = processID;
	}
	
	public void run()
	{
		boolean state = Cmd.killProc(processID);
		System.out.println("[TaskFailureMonitor][KillProcThread]:kill-state="+state);
		if(state) //this means that the process is already dead (i.e., the task is finished normally)
		{
//			VMServer.btProcessMap.remove(btID);
//			VMServer.btIDBTMap.remove(btID);
			//TODO: notify the event of killing the process
			//System.out.println("[TaskFailureMonitor]:load="+FileControler.loadBatchTaskFromFile(VMServer.getBatchTaskFile(bt.getBtID())));
			//VMServer.btProcessMap.remove(bt.getBtID());
			System.out.println("[TaskFailureMonitor]connecting mainserver's BTFailureEventPort: "+Initialization.mainServerAddress+":"+JobEmulator.BTFailureEventPort);
			TCPClient notifyClient = new TCPClient(Initialization.mainServerAddress, JobEmulator.BTFailureEventPort);				
			notifyClient.pushString(btID); 
			notifyClient.closeSocket();
		}
	}
}
