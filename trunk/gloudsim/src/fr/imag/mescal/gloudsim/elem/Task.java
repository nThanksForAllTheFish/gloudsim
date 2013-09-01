package fr.imag.mescal.gloudsim.elem;

import java.io.Serializable;
import java.util.List;

/**
 * Task class is an element that represents a continues/uninterrupted execution period.
 * It contains: 
 * taskID
 * priority: job priority (see Google guide for details)
 * schedulingClass: scheduling class (see Google guide for details)
 * startTime: starting time
 * endTime: the end of the execution period, which means a task failure event
 * duration: endTime - startTime
 * memSize: the memory usage
 * MTBF: mean time between failures
 * MNOF: mean number of failures (MTBF and MNOF are statistics in general)
 * bTLength: batch task length
 * useNFSMode: whether to use NFS or not
 * @author sdi
 *
 */
public class Task implements Serializable, Comparable<Task>{
	private static final long serialVersionUID = 6529685098267757694L;
	private int taskID;
	private int priority;
	private int schedulingClass;
	private float startTime;
	private float endTime; //seconds
	private float duration;
	private float memSize;
	
	private float MTBF;
	private float MNOF;
	private float bTLength;
	
	private boolean useNFSMode = false; //i.e., process migration mode without local disk
	
	public Task(int taskID, int priority, float startTime, float endTime, float duration, float memSize) {
		this.taskID = taskID;
		this.priority = priority;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.memSize = memSize;
	}
	public float getMTBF() {
		return MTBF;
	}

	public void setMTBF(float mTBF) {
		MTBF = mTBF;
	}

	public float getMNOF() {
		return MNOF;
	}

	public void setMNOF(float mNOF) {
		MNOF = mNOF;
	}
	
	public float getBTLength() {
		return bTLength;
	}
	public void setBTLength(float bTLength) {
		this.bTLength = bTLength;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getSchedulingClass() {
		return schedulingClass;
	}
	public void setSchedulingClass(int schedulingClass) {
		this.schedulingClass = schedulingClass;
	}
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
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
	public float getMemSize() {
		return memSize;
	}
	public void setMemSize(float memSize) {
		this.memSize = memSize;
	}
	public boolean isUseNFSMode() {
		return useNFSMode;
	}
	public void setUseNFSMode(boolean useNFSMode) {
		this.useNFSMode = useNFSMode;
	}
	public int compareTo(Task task) {
		if(this.startTime<task.startTime)
			return -1;
		else if(this.startTime>task.startTime)
			return 1;
		else
			return 0;
	}
	
	public void decorate(float cpsRatio)
	{
		startTime = startTime*cpsRatio;
		endTime = endTime*cpsRatio;
		duration = endTime - startTime;
	}
	public String toString()
	{
		return taskID+" "+startTime+" "+endTime+" "+duration;
	}
}
