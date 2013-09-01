package fr.imag.mescal.gloudsim.gnuplot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * compare the results between Young's fomula and our new formula (see my SC'13 paper for details)
 * @author sdi
 *
 */
public class YoungToOurFormulaWCLengthRatio {
	public static void main(String[] args)
	{
		String youngDir = "E:/Java-project/CheckpointSim/gnuplot/bk-single-false-tr/bk-false-static-single-false-1000-arr-tr/result";
		String ourDir = "E:/Java-project/CheckpointSim/gnuplot/bk-single-false-tr/bk-true-static-single-false-1000-arr-tr/result";
		String fileName = "job.txt";
		String youngPath = youngDir+"/"+fileName;
		String ourPath = ourDir+"/"+fileName;
		
		String output = "E:/Java-project/CheckpointSim/gnuplot/job-length-ratio/compare-1000.txt";
		String output2 = "E:/Java-project/CheckpointSim/gnuplot/job-length-ratio/compare-1000-2.txt";
		
		List<String> youngList = FileControler.readFile(youngPath);
		List<String> ourList = FileControler.readFile(ourPath);
		
		List<CompareElement> compareList = new ArrayList<CompareElement>();
		List<CompareElement2> compareList2 = new ArrayList<CompareElement2>();
		
		for(int i = 1,j=1;i<youngList.size();i++)
		{
			String[] data1 = youngList.get(i).split("\\s");
			float yLength = Float.parseFloat(data1[1]);
			String[] data2 = ourList.get(i).split("\\s");
			float oLength = Float.parseFloat(data2[1]);
			float ratio = yLength/oLength;
			float extra = yLength - oLength;
			if(yLength<1000)
			{
				compareList2.add(new CompareElement2(j,oLength,yLength));
				j++;
			}
			compareList.add(new CompareElement(i, ratio, extra));
		}
		
		Collections.sort(compareList);
		Collections.sort(compareList2);
		retagIndex(compareList);
		retagIndex(compareList2);
		FileControler.print2File(compareList, output);
		FileControler.print2File(compareList2, output2);
		System.out.print("done.");
	}
	
	static void retagIndex(List list)
	{
		for(int i = 0;i<list.size();i++)
		{
			Object o = list.get(i);
			if(o instanceof CompareElement)
				((CompareElement)o).setId(i+1);
			else
				((CompareElement2)o).setId(i+1);
		}
	}
}

class CompareElement implements Comparable<CompareElement>
{
	private int id;
	private float ratio;
	private float extra;
	
	public CompareElement(int id, float ratio, float extra) {
		this.id = id;
		this.ratio = ratio;
		this.extra = extra;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public float getRatio() {
		return ratio;
	}
	public void setRatio(float ratio) {
		this.ratio = ratio;
	}
	public float getExtra() {
		return extra;
	}
	public void setExtra(float extra) {
		this.extra = extra;
	}
	
	public int compareTo(CompareElement ce)
	{
		if(this.ratio<ce.ratio)
			return -1;
		else if(this.ratio>ce.ratio)
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return (id*10)+" "+ratio+" "+extra+" "+1;
	}
}

class CompareElement2 implements Comparable<CompareElement2>
{
	private int id;
	private float oLength;
	private float yLength;
	
	public CompareElement2(int id, float oLength, float yLength) {
		this.id = id;
		this.oLength = oLength;
		this.yLength = yLength;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int compareTo(CompareElement2 ce)
	{
		if(oLength<ce.oLength)
			return -1;
		else if(oLength>ce.oLength)
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return (id*10)+" "+oLength+" "+yLength;
	}
}