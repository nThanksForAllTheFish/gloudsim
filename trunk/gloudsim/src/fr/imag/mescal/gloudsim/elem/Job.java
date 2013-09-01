package fr.imag.mescal.gloudsim.elem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simulated job based on the Google trace's sample job
 * In the sim.mainserver.JobEmulator class, many job instances will be generated based on sample jobs.
 * Sample jobs are stored in the obj files (see simFailureTrace directory in the root directory)
 * @author sdi
 *
 */
public class Job implements Serializable{

	private static final long serialVersionUID = 6529685098267757692L;
	
	private int simID; //simulated jobID
	private String jobID; //sample jobID
	private int priority;
	private int type;
	private float startTime = 0;
	private float endTime = 0;
	private float meanTaskLength = 0;
	private float meanTaskFailNum;
	private float meanRealWCLength; //Wall clock
	private float meanProcWorkload; //mean workload processed
	private float meanProcRatio; //mean value of the ratio of workload to real wall-clock length
	private float minProcRatio;
	
	private int size = 0;
	private int vsize = 0;
	private float makespan; //maximum length of batchtask
	private float realWallClockTime = 0; //0 means running, otherwise, finish.
	public List<BatchTask> batchTaskList = new ArrayList<BatchTask>();
	
	public Job(String jobID) {
		this.jobID = jobID;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public void setInfo(float startTime, float endTime, int size, int vsize)
	{
		this.startTime = startTime;
		this.endTime = endTime;
		this.size = size;
		this.vsize = vsize;
	}
	public float getMeanTaskFailNum() {
		return meanTaskFailNum;
	}
	public void setMeanTaskFailNum(float meanTaskFailNum) {
		this.meanTaskFailNum = meanTaskFailNum;
	}
	public int getSimID() {
		return simID;
	}
	public void setSimID(int simID) {
		this.simID = simID;
	}
	public String getJobID() {
		return jobID;
	}
	public void setJobID(String jobID) {
		this.jobID = jobID;
	}
	public float getMakespan() {
		return makespan;
	}
	public void setMakespan(float makespan) {
		this.makespan = makespan;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public float getStartTime() {
		return startTime;
	}
	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}
	public float getEndTime() {
		return endTime;
	}
	public void setEndTime(float endTime) {
		this.endTime = endTime;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getVsize() {
		return vsize;
	}
	public void setVsize(int vsize) {
		this.vsize = vsize;
	}
	public float getMeanTaskLength() {
		return meanTaskLength;
	}
	public void setMeanTaskLength(float meanTaskLength) {
		this.meanTaskLength = meanTaskLength;
	}
	public synchronized float getRealWallClockTime() {
		return realWallClockTime;
	}
	public synchronized void setRealWallClockTime(float realWallClockTime) {
		this.realWallClockTime = realWallClockTime;
	}
	public float getMeanRealWCLength() {
		return meanRealWCLength;
	}
	public void setMeanRealWCLength(float meanRealWCLength) {
		this.meanRealWCLength = meanRealWCLength;
	}
	public float getMeanProcWorkload() {
		return meanProcWorkload;
	}
	public void setMeanProcWorkload(float meanProcWorkload) {
		this.meanProcWorkload = meanProcWorkload;
	}
	public float getMeanProcRatio() {
		return meanProcRatio;
	}
	public void setMeanProcRatio(float meanProcRatio) {
		this.meanProcRatio = meanProcRatio;
	}
	public float getMinProcRatio() {
		return minProcRatio;
	}
	public void setMinProcRatio(float minProcRatio) {
		this.minProcRatio = minProcRatio;
	}
	public void decorate(float cpsRatio)
	{
		meanTaskLength = meanTaskLength*cpsRatio;
		startTime = startTime*cpsRatio;
		endTime = endTime*cpsRatio;
	}
	public float getFailureRatioofBatchTasks()
	{
		Iterator<BatchTask> iter = this.batchTaskList.iterator();
		int counter = 0;
		while(iter.hasNext())
		{
			BatchTask bt = iter.next();
			if(bt.taskList.size()>1)
				counter++;
		}
		return ((float)counter)/((float)this.batchTaskList.size());
	}
	
	public int getTotalTaskNum()
	{
		int totalTaskNum = 0;
		Iterator<BatchTask> iter = batchTaskList.iterator();
		while(iter.hasNext())
		{
			BatchTask bt = iter.next();
			totalTaskNum += bt.taskList.size();
		}
		return totalTaskNum;
	}
	
	public Job clone()
	{
		Job job = new Job(jobID);
		job.setSimID(simID);
		job.setPriority(priority);
		job.setType(type);
		job.setStartTime(startTime);
		job.setEndTime(endTime);
		job.setMeanTaskLength(meanTaskLength);
		job.setMeanTaskFailNum(meanTaskFailNum);
		job.setMeanRealWCLength(meanRealWCLength);
		job.setMeanProcRatio(meanProcRatio);
		job.setMeanProcWorkload(meanProcWorkload);
		job.setMinProcRatio(minProcRatio);
		job.setSize(size);
		job.setVsize(vsize);
		job.setMakespan(makespan);
		job.setRealWallClockTime(realWallClockTime);

		job.batchTaskList = new ArrayList<BatchTask>();
		Iterator<BatchTask> iter = this.batchTaskList.iterator();
		while(iter.hasNext())
		{
			BatchTask bt = iter.next();
			job.batchTaskList.add(bt.clone());
		}
		return job;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("******************************\n");
		sb.append("simID="+simID+"\n");
		sb.append("jobID="+jobID+"\n");
		sb.append("priority="+priority+"\n");
		sb.append("startTime="+startTime+"\n");
		sb.append("endTime="+endTime+"\n");
		sb.append("meanTaskLength="+meanTaskLength+"\n");
		sb.append("meanTaskFailNum="+meanTaskFailNum+"\n");
		sb.append("size="+size+"\n");
		sb.append("vsize="+vsize+"\n");
		sb.append("--------------------------------\n");
		//just check the first batch task
		Iterator<Task> iter = batchTaskList.iterator().next().taskList.iterator();
		while(iter.hasNext())
		{
			Task task = iter.next();
			sb.append(task.toString()+":MNOF="+task.getMNOF()+";MTBF="+task.getMTBF()+"\n");
		}
		return sb.toString();
	}
}
