package fr.imag.mescal.gloudsim.sim.numeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

public class ComputeMaxAvgMin {
	
	public static void main(String[] args)
	{
		String dir = "E:/Java-project/CheckpointSim/simFailureTrace/mix/accurate-mnof/";
		//String srcFile = "log-Di_static-0.5.txt";
		String srcFile = "log-Young-0.5.txt";
		String outputFile = srcFile.replace("txt", "data");
		
		List<String> lineList = FileControler.readFile(dir+"/"+srcFile);
		Iterator<String> iter = lineList.iterator();
		PC[] pc = new PC[13]; //
		int priority = -1; //for pc
		int counter = 0;
		List<PC> resultList = new ArrayList<PC>();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(!(line.startsWith("-") || line.startsWith("=")))
				continue;
			String[] data = line.split("\\s");
			String tag = data[0];
			if(tag.startsWith("="))
			{
				if(priority>=0)
				{
					pc[priority].avg = pc[priority].avg/counter;
					counter = 0;
					resultList.add(pc[priority]);
				}
				priority = Integer.parseInt(data[1]);
				pc[priority] = new PC(priority);
			}
			else if(tag.startsWith("-"))// -
			{
				float avg_wpr = Float.parseFloat(data[4]);
				float min_wpr = Float.parseFloat(data[5]);
				PC pcon = pc[priority];
				pcon.avg+=avg_wpr;
				pcon.min = Math.min(pcon.min, min_wpr);
				pcon.max = Math.max(pcon.max, avg_wpr);
				counter++;
			}
			else
				System.out.println("error...");
		}
		Collections.sort(resultList);
		FileControler.print2File(resultList, dir+"/"+outputFile);
		System.out.println("done.");
	}
}

class PC implements Comparable<PC>
{
	
	public PC(int priority) {
		this.priority = priority;
	}
	int priority;
	public float max = 0;
	public float avg = 0;
	public float min = 1;
	
	public int compareTo(PC pc)
	{
		if(this.priority<pc.priority)
			return -1;
		else if(this.priority>pc.priority)
			return 1;
		else 
			return 0;
	}
	
	public String toString()
	{
		return (priority+1)+" "+min+" "+avg+" "+max;
	}
}
