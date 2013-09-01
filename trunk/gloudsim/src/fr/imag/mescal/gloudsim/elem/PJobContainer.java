package fr.imag.mescal.gloudsim.elem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * priority job container
 * PJobContainer is the element of PJobList in the JobTrace class.
 * @author sdi
 *
 */
// 
public class PJobContainer implements Serializable{
	private static final long serialVersionUID = 6529685098267757691L;
	public List<Job> jobList = new ArrayList<Job>();
	
	private int priority;
	private float meanTaskLength;
	private float meanTaskFailNum;
	private float meanRealWCLength; //Wall clock
	private float meanProcWorkload; //mean workload processed
	private float meanProcRatio; //mean value of the ratio of workload to real wall-clock length
	private float minProcRatio;
	
	public PJobContainer(int priority) {
		this.priority = priority;
	}
	public PJobContainer(int priority, float meanTaskLength) {
		this.priority = priority;
		this.meanTaskLength = meanTaskLength;
	}
	public float getMeanTaskFailNum() {
		return meanTaskFailNum;
	}
	public void setMeanTaskFailNum(float meanTaskFailNum) {
		this.meanTaskFailNum = meanTaskFailNum;
	}
	public float getMeanTaskLength() {
		return meanTaskLength;
	}
	public void setMeanTaskLength(float meanTaskLength) {
		this.meanTaskLength = meanTaskLength;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
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
	}
}
