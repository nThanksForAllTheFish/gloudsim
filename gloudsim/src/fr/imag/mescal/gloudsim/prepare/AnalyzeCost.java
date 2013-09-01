package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * This class is to analyze the checkpoint cost and checkpoint operation time.
 * Note that checkpoint cost means the increment of task wall-clock time due to one checkpoint
 * checkpoint time means the time of performing a checkpoint operation
 * See EstCheckpointCost.java for further information.
 * You need to modify the dir based on your environment.
 * @author sdi
 *
 */
public class AnalyzeCost {

	public static void main(String[] args)
	{
		String dir = "D:/INRIA-research/IEEE-13rd/gnuplot/cpcost-mem";
		String outFileName1 = dir+"/cpcost1-time.txt";
		String outFileName2 = dir+"/cpcost1-cost.txt";
		String[] memDir = {"cpcost-mem1", "cpcost-mem2", "cpcost-mem3", "cpcost-mem5","cpcost-mem9", "cpcost-mem30"};
		List<Element> resultList1 = new ArrayList<Element>();
		List<Element> resultList2 = new ArrayList<Element>();
		//resultList.add("tick memsize=10MB memsize=20MB memsize=40MB memsize=80MB memsize=160MB memsize=240MB");
		
		for(int i = 0;i<memDir.length;i++)
		{
			String dirName = memDir[i];
			String srcFileName = dir+"/"+dirName+"/remoteDiskTaskRestart.cost";
			List<Element> rList1 = new ArrayList<Element>();
			List<Element> rList2 = new ArrayList<Element>();
			processOneCPCost(srcFileName, rList1, rList2);
			if(resultList1.isEmpty())
			{
				resultList1.addAll(rList1);
				resultList2.addAll(rList2);
			}
			else
			{
				merge(resultList1, rList1);
				merge(resultList2, rList2);
			}
		}
		
		FileControler.print2File(resultList1, outFileName1);
		FileControler.print2File(resultList2, outFileName2);
		System.out.println("done.");
	}
	
	public static void merge(List<Element> resultList, List<Element> tobeMergedList)
	{
		for(int j = 0;j<tobeMergedList.size();j++)
		{
			Element e = tobeMergedList.get(j);
			Element re = resultList.get(j);
			if(e.getTick()==re.getTick())
			{
				re.setTime(re.getTime()+" "+e.getTime());
			}
			else
			{
				System.err.println("Error!!!");
				System.exit(0);
			}
		}
	}
	
	public static void processOneCPCost(String srcFileName, List<Element> list1, List<Element> list2)
	{
		List<String> lineList = FileControler.readFile(srcFileName);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split(";");
			int tick = (int)(Float.parseFloat(data[4].split("=")[1].trim())*1000); //millisecond
			String totalTimeString = data[6].split("=")[1].trim();
			float totalTime = Float.parseFloat(totalTimeString);
			String avgCost = String.valueOf((totalTime - 100)/5);
			list1.add(new Element(tick, totalTimeString));
			list2.add(new Element(tick, avgCost));
		}
		Collections.sort(list1);
		Collections.sort(list2);
	}
}

class Element implements Comparable<Element>
{
	private float tick;
	private String time;
	public Element(float tick, String time) {
		this.tick = tick;
		this.time = time;
	}
	public float getTick() {
		return tick;
	}
	public void setTick(float tick) {
		this.tick = tick;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int compareTo(Element e)
	{
		if(this.tick<e.tick)
			return -1;
		else if (this.tick>e.tick)
			return 1;
		else
			return 0;
	}
	public String toString()
	{
		return tick +" "+time;
	}
}
