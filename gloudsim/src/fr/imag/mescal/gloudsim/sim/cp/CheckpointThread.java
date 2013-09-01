package fr.imag.mescal.gloudsim.sim.cp;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.util.Cmd;

/**
 * When a checkpoint is to be taken, it should be launched in a new thread, because of huge checkpointing operation time (e.g, 5 seconds).  
 * @author sdi
 *
 */
public class CheckpointThread extends Thread {

	private boolean isInterrupted = false;
	String vmHostName;
	BatchTask bt;
	int curBTIndex;
	private int processID;
	long tick_ = 100;
	public boolean couldCP = true; //used to check if the workload is already finished?
	public Integer mutex = new Integer(0);
	private double initTime;
	private float[] cpPos;
	
	public CheckpointThread(int curBTIndex, BatchTask bt, String vmHostName, int procID, float[] cpPos)
	{
		this.curBTIndex = curBTIndex;
		this.vmHostName = vmHostName;
		this.bt = bt;
		this.processID = procID;
		this.cpPos = cpPos;
		initTime = System.currentTimeMillis()/1000.0;
	}
	
	public void interrupt()
	{
		isInterrupted = true;
		super.interrupt();
	}

	public void run()
	{
		performStaticCheckpoints(cpPos);
	}
	
	public void performStaticCheckpoints(float[] cpPos)
	{
		float t = 0f;
		for(int i = 0;i<cpPos.length&&!isInterrupted;i++)
		{
			for(;t<=cpPos[i]&&!isInterrupted;t=t+0.1f)
			{
				try {
					Thread.sleep(tick_);
				} catch (Exception e) {
					System.out.println("[CheckpointThread]Exception - stop checkpoint thread....");
				}
			}
			if(isInterrupted)
				break;
			synchronized(mutex)
			{
				if(couldCP)
				{
					new CheckpointCmdThread( 
							processID, 
							initTime, 
							bt, 
							curBTIndex,
							this).start();
				}
				else
				{
					System.out.println("[CheckpointThread] no need to checkpoint any more.");
					break;
				}
			}
			i++;
		}
	}
	
//	public void performDynamicCheckpoints(boolean cpOurFormula, float cpCost, float remainLength, float failureNum, float mtbf)
//	{
//		//update statistics 
//		
//		
//	}
}

class CheckpointCmdThread extends Thread
{
	private boolean useNFSDevice;
	private String btID;
	private int curTaskIndex;
	private float curTaskFailureLength;
	private int procID;
	private double initTime;
	BatchTask bt;
	CheckpointThread cpt;
	public CheckpointCmdThread(  
			int procID, 
			double initTime, 
			BatchTask bt,
			int curBTIndex, 
			CheckpointThread cpt)
	{
		this.useNFSDevice = bt.isBetterUseNFSDevice();
		this.btID = bt.getBtID();
		curTaskFailureLength = bt.taskList.get(curBTIndex).getDuration();
		this.procID = procID;
		this.initTime = initTime;
		this.bt = bt;
		this.curTaskIndex = curBTIndex;
		this.cpt = cpt;
	}
	public void run()
	{
		double curTime = System.currentTimeMillis()/1000.0;
		String contextFile = Cmd.cr_checkpoint(btID, bt.getNfsDeviceContextFile(), procID, useNFSDevice);
		if(contextFile==null)
		{
			synchronized (cpt.mutex) {
				cpt.couldCP = false;
			}
		}
		else
		{
			bt.setUsedtoCheckpoint(true);
			float cp_pos = (float)(curTime - initTime);
			System.out.println("[CheckpointCmdThread-Log] "+btID+":"+curTaskIndex+" "+cp_pos+" "+curTaskFailureLength);
		}
	}
}

