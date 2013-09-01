package fr.imag.mescal.gloudsim.elem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JobTrace contains the whole Google job trace, which just involves the sample Google jobs.
 * pJobList is the key variable (see PJobContainer for details)
 * Each element in pJobList corresponds to a priority. 
 * In Google trace, there are 12 priorities, so there are 12 elements in the list. 
 * Each priority element contains the sample jobs with the corresponding priority.
 * @author sdi
 *
 */
public class JobTrace implements Serializable{

	private static final long serialVersionUID = 6529685098267757690L;
	
	private float meanTaskLength;
	private float meanTaskFailNum;
	private float meanRealWCLength; //Wall clock
	private float meanProcWorkload; //mean workload processed
	private float meanProcRatio; //mean value of the ratio of workload to real wall-clock length
	private float minProcRatio;
	
	public List<PJobContainer> pJobList = new ArrayList<PJobContainer>(); //priority job list
	
	public JobTrace(float meanTaskLength) {
		this.meanTaskLength = meanTaskLength;
	}
	public JobTrace(List<PJobContainer> pJobList) {
		this.pJobList = pJobList;
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
	
	public List<String> toStringList()
	{
		String seperator = "  ";
		List<String> list = new ArrayList<String>();
		list.add("jtMeanLength="+meanTaskLength);
		Iterator<PJobContainer> iter = pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			list.add(seperator+"pjcMeanLength="+pc.getMeanTaskLength()+"  priority="+pc.getPriority());
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				list.add(seperator+seperator+"jobID:"+job.getJobID()+" jobMeanLength="+job.getMeanTaskLength()+"  mkspan="+job.getMakespan()+"  start="+job.getStartTime()+"  s="+job.getSize()+"  vs="+job.getVsize());
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					list.add(seperator+seperator+seperator+"btMeanLength="+bt.getMeanTaskLength()+"  sTime="+bt.getStartTime()+"  totalTaskLength="+bt.getTotalTaskLength() + "  wclockLength="+bt.getWallClockLength());
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						list.add(seperator+seperator+seperator+seperator+task.toString());
					}
				}
			}
		}
		return list;
	}
	
	public List<String> toLogList()
	{
		List<String> list = new ArrayList<String>();
		list.add("# (priority) meanTasklength meanRealWCLength meanProcWorkload meanProcRatio minProcRatio");
		list.add("tr "+meanTaskLength+" "+meanRealWCLength+" "+meanProcWorkload+" "+meanProcRatio+" "+minProcRatio);
		Iterator<PJobContainer> iter = pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			list.add("=pc "+pc.getPriority()+" "+pc.getMeanTaskLength()+
					" "+pc.getMeanRealWCLength()+" "+pc.getMeanProcWorkload()+" "+
					pc.getMeanProcRatio()+" "+pc.getMinProcRatio());	
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				list.add("--job "+job.getMeanTaskLength()+
						" "+job.getMeanRealWCLength()+" "+job.getMeanProcWorkload()+" "+
						job.getMeanProcRatio()+" "+job.getMinProcRatio());
			}
		}
		return list;
	}
}
