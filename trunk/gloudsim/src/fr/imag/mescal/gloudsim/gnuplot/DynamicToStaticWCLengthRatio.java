package fr.imag.mescal.gloudsim.gnuplot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * generate comparison data about wall-clock length ratio for dynamic case and static case
 * @author sdi
 *
 */
public class DynamicToStaticWCLengthRatio {
	
	public static void main(String[] args)
	{
		List<String> dList = FileControler.readFile("E:/Java-project/CheckpointSim/gnuplot/backup15-dyn-new-MNOF/bk-true-dynamic-single-true-3600-arr-tr/result/job.txt");
		List<String> sList = FileControler.readFile("E:/Java-project/CheckpointSim/gnuplot/backup15-dyn-new-MNOF/bk-true-dynamic-single-false-3600-arr-tr/result/job.txt");
		String outputFile = "E:/Java-project/CheckpointSim/gnuplot/backup15-dyn-new-MNOF/dyn-compareLength.txt";
		
		List<JobCompareItem> compareList = new ArrayList<JobCompareItem>();
		
		System.out.println("constructing comparelist");
		Iterator<String> iter = dList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			String jobID = data[0];
			Iterator<String> iter2 = sList.iterator();
			while(iter2.hasNext())
			{
				String line2 = iter2.next();
				String[] data2 = line2.split("\\s");
				String jobID2 = data2[0];
				if(jobID.equals(jobID2))
				{
					JobCompareItem jci = new JobCompareItem(jobID, Float.parseFloat(data[1]), Float.parseFloat(data2[1]));
					compareList.add(jci);
					break;
				}
			}
		}
		
		System.out.println("sorting...");
		Collections.sort(compareList);
		
		System.out.println("output result to file");
		List<String> resultList = new ArrayList<String>();
		Iterator<JobCompareItem> iter3 = compareList.iterator();
		int size = compareList.size();
		for(int i = 0;iter3.hasNext();i++)
		{
			JobCompareItem item = iter3.next();
			resultList.add(((float)(i*10000/size))+" "+item.toString()+" 1");
		}
		
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}

class JobCompareItem implements Comparable<JobCompareItem>
{
	private String jobID; 
	private float dynamicLength;
	private float staticLength;
	public JobCompareItem(String jobID, float dynamicLength, float staticLength) {
		this.jobID = jobID;
		this.dynamicLength = dynamicLength;
		this.staticLength = staticLength;
	}
	
	public float getD2SRatio()
	{
		return dynamicLength/staticLength;
	}
	
	public float getS2DRatio()
	{
		return staticLength/dynamicLength;
	}
	
	public int compareTo(JobCompareItem o)
	{
		float sd = staticLength/dynamicLength;
		float sd2 = o.staticLength/o.dynamicLength;
		if(sd<sd2)
			return -1;
		else if(sd>sd2)
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return getS2DRatio()+" "+getD2SRatio();
	}
}
