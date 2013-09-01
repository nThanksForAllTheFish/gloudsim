package fr.imag.mescal.gloudsim.gnuplot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

public class RescaleTaskUsage {
	
	public static void main(String[] args)
	{
		
		int cpuCap = 8;
		int memCap = 16000;
		
		String dir = "D:/INRIA-research/IEEE-13rd/gnuplot2/google-usage-per-task";
		String srcFileName = "usagePerTask.data";
		String srcFilePath = dir+"/"+srcFileName;
		String tgtFilePath = dir+"/usagePerTask"+cpuCap+"-"+memCap+".txt";
		
		List<String> srcList = FileControler.readFile(srcFilePath);
		List<String> tgtList = new ArrayList<String>();
		
		Iterator<String> iter = srcList.iterator();
		while(iter.hasNext())
		{
			String s = iter.next();
			String[] data = s.split("\\s");
			String taskID = data[0];
			double newCPU = Double.parseDouble(data[1])*cpuCap;
			double newMem = Double.parseDouble(data[2])*memCap;
			tgtList.add(taskID+" "+newCPU+" "+newMem);
		}
		
		FileControler.print2File(tgtList, tgtFilePath);
		System.out.println("done.");
	}
}
