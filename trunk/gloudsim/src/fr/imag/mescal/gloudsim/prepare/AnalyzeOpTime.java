package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * to process and characterize operation time of checkpointing 
 * @author sdi
 *
 */
public class AnalyzeOpTime {

	public static void main(String[] args)
	{
		String dir = "D:/INRIA-research/IEEE-13rd/gnuplot2/cpcost-mem/cpcost-mem-C";
		String outFileName = dir+"/cp-optime.txt";
		String[] memDir = {"cpcost-mem1", "cpcost-mem2", "cpcost-mem3", "cpcost-mem5","cpcost-mem9", "cpcost-mem30"};
		List<Element> resultList = new ArrayList<Element>();
		//resultList.add("tick memsize=10MB memsize=20MB memsize=40MB memsize=80MB memsize=160MB memsize=240MB");
		
		for(int i = 0;i<memDir.length;i++)
		{
			String dirName = memDir[i];
			String srcFileName = dir+"/"+dirName+"/cpcost-nfs.sam";
			List<Element> rList = new ArrayList<Element>();
			processOneCPOpTime(srcFileName, rList);
			if(resultList.isEmpty())
			{
				resultList.addAll(rList);
			}
			else
			{
				merge(resultList, rList);
			}
		}
		
		FileControler.print2File(resultList, outFileName);
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
	
	public static void processOneCPOpTime(String srcFileName, List<Element> list)
	{
		List<String> lineList = FileControler.readFile(srcFileName);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split(":");
			String s = data[0].split("\\.")[0];
			int exp = Integer.parseInt(s.split("-")[2]);
			int tick = (int)(5*Math.pow(2, exp));
			String avgTime = data[3];
			list.add(new Element(tick, avgTime));
		}
		Collections.sort(list);
	}
}
