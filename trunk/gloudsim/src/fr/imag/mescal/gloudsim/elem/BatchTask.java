package fr.imag.mescal.gloudsim.elem;

import java.io.Serializable;
import java.util.List;
/**
 * BatchTask contains a set of tasks connected in SERIES
 * That is, there is a chain of tasks in each BatchTask, and each task represents an uninterrupted execution duration
 * For example, a batchtask has 5 tasks connected inseries, which means the batchtask has 4 failure events.
 * @author sdi
 *
 */
public class BatchTask implements Serializable, Comparable<BatchTask>{
	private static final long serialVersionUID = 6529685098267757693L;

	private String btID; //btID=[simJobID-batchtaskIndex]
	private float startTime;
	private float meanTaskLength;
	private float totalTaskLength; //summed execution length
	private float wallClockLength; //including restart length
	private float realWallClockLength = 0; //0 means "running", non-zero means "finish"
	private float procWorkload;
	public List<Task> taskList;
	private float pJobMTBF;
	private float pJobMNum;
	private float meanMemSize;
	private int monPort;
	private int memID; //corresponding to a mem file to be read in the experiment
	private float cpCost; //used in simulation or other purpose
	private float nfsCpcost; //corresponding cp cost based on the memID
	private float ramfsCpcost;
	private float nfsRscost;
	private float ramfsRscost;
	private boolean betterUseNFSDevice; //to be filled in CheckpointThread.java
	
	private int curTaskIndex = 0;
	private boolean usedtoCheckpoint = false; //whether this batchtask was checked
	private String nfsDeviceContextFile;
	
	private double expSchedTime; //expected schedule time
	private double expFinishTime; //expected finish-time
	
	public int[] processMigMode = new int[2]; //the numbers of migration mode1 (localdisk) and mode2(shareddisk)
	private int schedulingClass;
	
	
	public BatchTask(float startTime, List<Task> taskList) {
		this.startTime = startTime;
		this.taskList = taskList;
		processMigMode[0]=0;
		processMigMode[1]=0;
	}
	
	public float getpJobMTBF() {
		return pJobMTBF;
	}

	public void setpJobMTBF(float pJobMTBF) {
		this.pJobMTBF = pJobMTBF;
	}

	public float getpJobMNum() {
		return pJobMNum;
	}

	public void setpJobMNum(float pJobMNum) {
		this.pJobMNum = pJobMNum;
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	public float getMeanTaskLength() {
		return meanTaskLength;
	}

	public void setMeanTaskLength(float meanTaskLength) {
		this.meanTaskLength = meanTaskLength;
	}
	
	public float getTotalTaskLength() {
		return totalTaskLength;
	}

	public void setTotalTaskLength(float totalTaskLength) {
		this.totalTaskLength = totalTaskLength;
	}

	public float getWallClockLength() {
		return wallClockLength;
	}

	public void setWallClockLength(float wallClockLength) {
		this.wallClockLength = wallClockLength;
	}

	public String getBtID() {
		return btID;
	}

	public void setBtID(String btID) {
		this.btID = btID;
	}
	public int getCurTaskIndex() {
		return curTaskIndex;
	}
	public void setCurTaskIndex(int curTaskIndex) {
		this.curTaskIndex = curTaskIndex;
	}
	public synchronized float getRealWallClockLength() {
		return realWallClockLength;
	}
	public synchronized void setRealWallClockLength(float realWallClockLength) {
		this.realWallClockLength = realWallClockLength;
	}

	public float getProcWorkload() {
		return procWorkload;
	}

	public void setProcWorkload(float procWorkload) {
		this.procWorkload = procWorkload;
	}
	
	public float getMeanMemSize() {
		return meanMemSize;
	}

	public void setMeanMemSize(float meanMemSize) {
		this.meanMemSize = meanMemSize;
	}

	public int getMonPort() {
		return monPort;
	}

	public void setMonPort(int monPort) {
		this.monPort = monPort;
	}

	public int getMemID() {
		return memID;
	}

	public void setMemID(int memID) {
		this.memID = memID;
	}

	public float getNfsCpcost() {
		return nfsCpcost;
	}

	public void setNfsCpcost(float nfsCpcost) {
		this.nfsCpcost = nfsCpcost;
	}

	public float getRamfsCpcost() {
		return ramfsCpcost;
	}

	public void setRamfsCpcost(float ramfsCpcost) {
		this.ramfsCpcost = ramfsCpcost;
	}

	public  synchronized boolean isUsedtoCheckpoint() {
		return usedtoCheckpoint;
	}

	public synchronized void setUsedtoCheckpoint(boolean usedtoCheckpoint) {
		this.usedtoCheckpoint = usedtoCheckpoint;
	}

	public float getNfsRscost() {
		return nfsRscost;
	}

	public void setNfsRscost(float nfsRscost) {
		this.nfsRscost = nfsRscost;
	}

	public float getRamfsRscost() {
		return ramfsRscost;
	}

	public void setRamfsRscost(float ramfsRscost) {
		this.ramfsRscost = ramfsRscost;
	}

	public int compareTo(BatchTask bTask)
	{
		if(this.startTime<bTask.startTime)
			return -1;
		else if(this.startTime>bTask.startTime)
			return 1;
		else
			return 0;
	}
	
	public void decorate(float cpsRatio)
	{
		startTime = startTime*cpsRatio;
		meanTaskLength = meanTaskLength*cpsRatio;
		totalTaskLength = totalTaskLength*cpsRatio;
		wallClockLength = wallClockLength*cpsRatio;
	}

	public String toString()
	{
		String s = btID+" "+totalTaskLength+" "+realWallClockLength+" "+procWorkload+" "+taskList.size()+" "+pJobMTBF+" "+pJobMNum+" "+meanMemSize+" "+memID+" "+nfsCpcost+" "+ramfsCpcost;
		return s;
	}

	public float getCpCost() {
		return cpCost;
	}

	public void setCpCost(float cpCost) {
		this.cpCost = cpCost;
	}

	public boolean isBetterUseNFSDevice() {
		return betterUseNFSDevice;
	}

	public void setBetterUseNFSDevice(boolean betterUseNFSDevice) {
		this.betterUseNFSDevice = betterUseNFSDevice;
	}

	public String getNfsDeviceContextFile() {
		return nfsDeviceContextFile;
	}

	public void setNfsDeviceContextFile(String nfsDeviceContextFile) {
		this.nfsDeviceContextFile = nfsDeviceContextFile;
	}

	public double getExpSchedTime() {
		return expSchedTime;
	}

	public void setExpSchedTime(double expSchedTime) {
		this.expSchedTime = expSchedTime;
	}

	public double getExpFinishTime() {
		return expFinishTime;
	}

	public void setExpFinishTime(double expFinishTime) {
		this.expFinishTime = expFinishTime;
	}
	
	public int getDeviceID()
	{
		String[] s = nfsDeviceContextFile.split("/"); //"/localfs/contextNFS/2/3-0/context.pid"
		return Integer.parseInt(s[3]);
	}
	
	public int getSchedulingClass() {
		return schedulingClass;
	}

	public void setSchedulingClass(int schedulingClass) {
		this.schedulingClass = schedulingClass;
	}

	public BatchTask clone()
	{
		BatchTask bt = new BatchTask(startTime, taskList);
		bt.setMeanTaskLength(meanTaskLength);
		bt.setTotalTaskLength(totalTaskLength);
		bt.setWallClockLength(realWallClockLength); //including restart length
		bt.setRealWallClockLength(realWallClockLength);
		bt.setProcWorkload(procWorkload);
		bt.setpJobMTBF(pJobMTBF);
		bt.setpJobMNum(pJobMNum);
		bt.setMeanMemSize(meanMemSize);
		bt.setMonPort(monPort);
		bt.setMemID(memID); //corresponding to a mem file to be read in the experiment
		bt.setCpCost(cpCost); //used in simulation or other purpose
		bt.setNfsCpcost(nfsCpcost); //corresponding cp cost based on the memID
		bt.setRamfsCpcost(ramfsCpcost);
		bt.setNfsRscost(nfsRscost);
		bt.setRamfsRscost(ramfsRscost);
		bt.setBetterUseNFSDevice(betterUseNFSDevice); //to be filled in CheckpointThread.java
		
		bt.setCurTaskIndex(curTaskIndex);
		bt.setUsedtoCheckpoint(usedtoCheckpoint);
		bt.setNfsDeviceContextFile(nfsDeviceContextFile);
		
		bt.setExpSchedTime(expSchedTime);
		bt.setExpFinishTime(expFinishTime);
		
		bt.processMigMode = processMigMode;
		return bt;
	}
}
