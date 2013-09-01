package fr.imag.mescal.gloudsim.sim.vmserver;
import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.sim.cp.OptCPAnalyzer;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.MathTool;
import fr.imag.mescal.gloudsim.util.PVFile;

public class TaskExeThread extends Thread {

	private BatchTask batchTask;
	private String btID;
	private boolean firstRun;
	private String vmHostName;
	
	public TaskExeThread(String btID, boolean firstRun, String vmHostName) {
		this.btID = btID;
		this.firstRun = firstRun;
		this.vmHostName = vmHostName;
	}

	public TaskExeThread(BatchTask batchTask, boolean firstRun, String vmHostName) {
		this.batchTask = batchTask;
		btID = batchTask.getBtID();
		this.firstRun = firstRun;
		this.vmHostName = vmHostName;
	}

	public void run()
	{
		if(firstRun)
		{
			firstCheckpoint();
		}
		else //restart based on checkpoint context file
		{
			System.out.println(VMServer.vmHostName
					+ ":[TaskExeThread]restart TaskFailureMonitor...");
			String btObjFile = VMServer.getBatchTaskFile(btID);

			boolean retry = false;
			do
			{
				if(retry)
				{
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(PVFile.isExist(btObjFile))
					batchTask = FileControler.loadBatchTaskFromFile(btObjFile);
				else
					continue;
				if(batchTask==null)
				{
					System.out.println("batchTask==null");
					retry = true;
				}
				else
				{
					System.out.println("batchTask="+batchTask);
					retry = false;
				}
			}while(retry);

			System.out.println("[TaskExeThread] start cr_restart .....");
			if(!batchTask.isUsedtoCheckpoint())
			{
				System.out.println("[TaskExeThread]used to checkpoint="+false);
				String nfsDevice = Initialization.cpNFSContextDir+"/"+batchTask.getDeviceID();
				batchTask.setNfsDeviceContextFile(nfsDevice);
				firstCheckpoint();
			}
			else
			{
				nextCheckpoint();
			}
		}
	}
	
	private void firstCheckpoint()
	{
		System.out.println("[TaskExeThread]btID="+btID+":firstCheckpoint();contextDevice="+batchTask.getNfsDeviceContextFile());
		int PID = -1;
		String cmd = Cmd.btExecutorTmp+" "+String.valueOf(btID)+" "+batchTask.getTotalTaskLength()+" "+batchTask.getMemID()+" "+batchTask.getMonPort();
		String input[] = cmd.split("\\s");
		System.out.println(VMServer.vmHostName+":[TaskExeThread]"+cmd);
		new Cmd(input).start(); 
		boolean getIDMark = true;
		do
		{
			if(!getIDMark)
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			PID = Cmd.getExeProcessID(new String[]{btID});
			//System.out.println("selfPID="+selfPID);
			if(PID>0)
			{
				getIDMark = true;
//				VMServer.btProcessMap.put(btID, new Integer(PID));
			}
			else
				getIDMark = false;
		}while(!getIDMark);
		
		System.out.println(vmHostName+":[TaskExeThread] pid="+PID);
		//String nfsContextDevice = NFSControler.randomContextDevice(vmHostName);
		String nfsContextDevice = batchTask.getNfsDeviceContextFile();
		String nfsDeviceContextFile = nfsContextDevice+"/"+batchTask.getBtID()+"/context."+PID;
		batchTask.setNfsDeviceContextFile(nfsDeviceContextFile);
		System.out.println("[TaskExeThread]batchTask "+btID+":"+nfsDeviceContextFile);
		
		//List<float[]> cpPos = new ArrayList<float[]>(); // two float[] : local and nfs
		float[] betterCpPos = computeOptimalCPPositions(batchTask, PID);
		
		//printPositions(batchTask.getCurTaskIndex(), vmHostName, cpPos);
		System.out.println(vmHostName+":[TaskExeThread]start TaskFailureMonitor...");
		new TaskFailureMonitor(batchTask, PID, vmHostName, betterCpPos).start();
	}
	
	/**
	 * 
	 * @param bt
	 * @param cpPos
	 * @param processID
	 * @return should use NFS device? true: Y, or otherwise: N
	 */
	private float[] computeOptimalCPPositions(
			BatchTask bt, int processID)
	{
		float MNOF;
		float MTBF;
		int curTaskIndex = bt.getCurTaskIndex();
		Task task = bt.taskList.get(curTaskIndex);
		if(!Initialization.dynamicSolu)
		{
			MNOF = bt.getpJobMNum();
			MTBF = bt.getpJobMTBF();
		}
		else
		{
			MNOF = task.getMNOF();
			MTBF = task.getMTBF();
		}
		this.btID = bt.getBtID();

		//TODO
		float NFSCpCost = bt.getNfsCpcost();
		float NFSRsCost = bt.getNfsRscost();
		float ramfsCpCost = bt.getRamfsCpcost();
		float ramfsRsCost = bt.getRamfsRscost();
		
		float remainLength;
		if(Initialization.dynamicSolu)
		{
			remainLength = task.getBTLength();
			//remainLength = bt.getTotalTaskLength();
		}
		else
		{
			//remainLength = bt.getTotalTaskLength(); //static situation does not need loadRatio, because it was already considered before
			remainLength = bt.taskList.get(0).getBTLength();
		}
	
		//compute optimal checkpointing positions
		float[] betterCpPos;
		float[] nfsCpPos = compCPPos(Initialization.cpOurFormula, NFSCpCost, remainLength, MNOF, MTBF);
		float[] ramfsCpPos = compCPPos(Initialization.cpOurFormula, ramfsCpCost, remainLength, MNOF, MTBF);
//		cpPos.add(ramfsCpPos);
//		cpPos.add(nfsCpPos);
		boolean useNFSDevice = chooseNFSDevice(
				nfsCpPos, ramfsCpPos, 
				ramfsCpCost, ramfsRsCost, 
				NFSCpCost, NFSRsCost, 
				MNOF, MTBF, remainLength);
		
//		System.out.println("before moving ramfs: useNFSDevice="+useNFSDevice);
		if(!useNFSDevice)
		{
			float expectedMemSize = bt.getMeanMemSize();
			System.out.println("[TaskExeThread]expectedMemSize = "+expectedMemSize);
			float remainingRamdiskSize = Cmd.getRemainigRamfskMemSize();
			if(remainingRamdiskSize > expectedMemSize)
			{
				//move() returns 0 means "normal", otherwise, means error!
				int state = 0;
				if(bt.getCurTaskIndex()>0)
					state = Cmd.move(bt.getNfsDeviceContextFile(), Initialization.cpLocalContextDir+"/"+bt.getBtID());
				
				if(state==0)
					bt.setBetterUseNFSDevice(false);
				else
					bt.setBetterUseNFSDevice(true);	
			}
			else
				bt.setBetterUseNFSDevice(true);
		}
		else
		{
			bt.setBetterUseNFSDevice(true);
		}
		
		if(!bt.isBetterUseNFSDevice())
		{
			betterCpPos = ramfsCpPos;
			task.setUseNFSMode(false);
			bt.processMigMode[0]++;
		}
		else
		{
			betterCpPos = nfsCpPos;
			task.setUseNFSMode(true);
			bt.processMigMode[1]++;
		}
		//debugOpPos(remainLength, MNOF, MTBF, ramfsCpPos, nfsCpPos, curTaskIndex, bt);
		return betterCpPos;
	}
	
	private void debugOpPos(float remainLength, float MNOF, float MTBF, float[] ramfsCpPos, float[] nfsCpPos, int curTaskIndex, BatchTask bt)
	{
		System.out.println("*************************************");
		System.out.println("remainLength="+remainLength);
		System.out.println("MNOF="+MNOF+";MTBF="+MTBF);
		System.out.println("[TaskExeThread]ramfsCpPos:=========");
		printPositions(curTaskIndex, ramfsCpPos);
		System.out.println("[TaskExeThread]nfsCpPos:===========");
		printPositions(curTaskIndex, nfsCpPos);
		System.out.println("[TaskExeThread]useNFS?:"+bt.isBetterUseNFSDevice());		
	}
	
	private void nextCheckpoint()
	{
		String contextFile = batchTask.getNfsDeviceContextFile();
		while(true)
		{
			String mvStateFile = Initialization.cpStateDir+"/"+batchTask.getBtID()+"/move.state";
			String stateString = FileControler.readFileFirstLine(mvStateFile);
			if(stateString!=null&&MathTool.isNumeric(stateString))
			{
				int state = Integer.parseInt(stateString);
				System.out.println("batchTask.getCurTaskIndex()="+batchTask.getCurTaskIndex()+";state="+state+";contextFile="+contextFile);
				if(batchTask.isBetterUseNFSDevice() || batchTask.getCurTaskIndex() == state)
				{
					if (PVFile.isExist(contextFile)) 
					{
						System.out.println("[TaskExeThread]contextFile "+ contextFile + " exists. ^_^");
						System.out.println("[TaskExeThread] restart successful...");
						Integer processID = Integer.valueOf(contextFile
								.split("\\.")[1]);
						float[] cpPos = computeOptimalCPPositions(batchTask, processID);
						printPositions(batchTask, cpPos);
						TaskFailureMonitor tfm = new TaskFailureMonitor(batchTask, processID,
								vmHostName, cpPos);
						tfm.start();
						if(!batchTask.isBetterUseNFSDevice())
							contextFile = Initialization.cpLocalContextDir+"/"+btID+"/context."+processID;
							
						String errorString = Cmd.cr_restart(contextFile);
						if(!errorString.equals(""))
							System.out.println("[TaskExeThread]cr_restart Exception Error: " + errorString);
						if(errorString.contains("Restart failed: Device or resource busy"))
						{
							tfm.interrupt();
							new MVContextThread(batchTask, processID).start();
							System.out.println("notify mainserver to retry another node.");
							TCPClient notifyClient = new TCPClient(
									Initialization.mainServerAddress,
									JobEmulator.BTFailureEventPort);
							notifyClient.pushString(btID);
							notifyClient.closeSocket();
						}
						else if(errorString.contains("Restart failed: Input/output error"))
						{
							tfm.interrupt();
							new MVContextThread(batchTask, processID).start();
							System.out.println("notify mainserver to finish this task.");
							TCPClient notifyClient = new TCPClient(
									Initialization.mainServerAddress,
									JobEmulator.BTFinishReceiverPort);
							notifyClient.pushString(btID+" -1");
							notifyClient.closeSocket();
						}
						break;
					}
				}
			}

			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			else //not sure if this would happen? maybe checkpoint happen at the moment the process is being killed.
//			{
//				System.out.println("[TaskExeThread]contextFile "+contextFile+" does NOT exist.-_-!!!!(note isusedcheckpoint is true.)");
//				TCPClient notifyClient = new TCPClient(Initialization.mainServerAddress, JobEmulator.BTFailureEventPort);
//				notifyClient.pushString(btID);
//			}
		}
	}
	
	private boolean chooseNFSDevice(
			float[] nfsCpPos, float[] ramfsCpPos,
			float ramfsCpCost, float ramfsRsCost, 
			float NFSCpCost, float NFSRsCost, 
			float MNOF, float MTBF, float remainLength)
	{
		if(nfsCpPos.length==0&&ramfsCpPos.length!=0)
			return false;
		if(nfsCpPos.length!=0&&ramfsCpPos.length==0)
			return true;
		if(nfsCpPos.length!=0&&ramfsCpPos.length!=0)
		{
			float ramfsCost = 0;
			float nfsCost = 0;
			if(Initialization.cpOurFormula)
			{
				ramfsCost = estTotalCost(ramfsCpCost, ramfsCpPos, ramfsRsCost, MNOF, remainLength);
				nfsCost = estTotalCost(NFSCpCost, nfsCpPos, NFSRsCost, MNOF, remainLength);
			}
			else
			{
				ramfsCost = estTotalCost(ramfsCpCost, ramfsCpPos, ramfsRsCost, remainLength/MTBF, remainLength);
				nfsCost = estTotalCost(NFSCpCost, nfsCpPos, NFSRsCost, remainLength/MTBF, remainLength);
			}
			if(ramfsCost>=nfsCost)
				return true;
			else
				return false;
		}
		else
			return true;
	}
	
	private static float estTotalCost(
			float cpCost, float[] cpPos, float rsCost, float MNOF, float remainLength)
	{
		return (float)(cpCost*(cpPos.length-1)+rsCost*MNOF+remainLength*MNOF/(2*cpPos.length));
	}
	
	public float[] compCPPos(boolean cpOurFormula, float cpCost, float remainLength, float failureNum, float mtbf)
	{
		float[] cpPos = null;
		if(cpOurFormula)
		{
			float cpNum = OptCPAnalyzer.compStaticOptNum(cpCost, remainLength, failureNum, 0.5f);
			cpPos = new float[(int)cpNum];
			float cpInterval = 0;
			if(cpNum!=0)
				cpInterval = remainLength/cpNum;
			System.out.println("[CheckpointThread]btID:"+btID+";failNum="+failureNum+";cpInterval="+cpInterval);
			for(int i = 0;i<(int)cpNum;i++)
				cpPos[i]+=(i+1)*cpInterval;
		}
		else //Young's formula
		{
			float cpInterval = OptCPAnalyzer.compYoungInterval(cpCost, mtbf, 0.5f);
			System.out.println("[CheckpointThread]btID:"+btID+";mtbf="+mtbf+";cpInterval="+cpInterval);
			cpPos = new float[(int)(remainLength/cpInterval)];
			for(int i = 0;i<cpPos.length;i++)
				cpPos[i] = (i+1)*cpInterval;
		}
		return cpPos;
	}
	
	private void printPositions(BatchTask bt, float[] cpPos)
	{
		int curTaskIndex = bt.getCurTaskIndex();
		String cpPoss = VMServer.vmHostName+":"+"cpPos=";
		for(int i = 0;i<cpPos.length;i++)
			cpPoss+=" "+cpPos[i];
		System.out.println("[CheckpointThread]bt.isNFSBetter?"+bt.isBetterUseNFSDevice()+";bt="+btID+";index="+curTaskIndex+";"+cpPoss);
	}
	
	private void printPositions(int curTaskIndex, float[] cpPos)
	{
		String cpPoss = VMServer.vmHostName+":"+"cpPos=";
		for(int i = 0;i<cpPos.length;i++)
			cpPoss+=" "+cpPos[i];
		System.out.println("[CheckpointThread]:bt="+btID+";index="+curTaskIndex+";"+cpPoss);
	}
}

