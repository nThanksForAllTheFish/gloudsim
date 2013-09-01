package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * compute average data
 * @author sdi
 *
 */
public class ComputeAvg {

	public static void main(String[] args)
	{
		String dir = "D:/INRIA-research/IEEE-13rd/gnuplot2/cpcost-mem/cpcost-mem-C";
		String[] files = {"cp-optime-A.txt","cp-optime-B.txt","cp-optime-C.txt"};
		
		float[][] optime = new float[11][7];
				
		for(int i = 0;i<files.length;i++)
		{
			String file = dir+"/"+files[i];
			List<String> linelist = FileControler.readFile(file);
			for(int j = 1;j<linelist.size();j++)
			{
				String line = linelist.get(j);
				String[] data = line.split("\\s");
				for(int k = 0;k<=6;k++)
					optime[j-1][k] += Float.parseFloat(data[k]);
			}
		}
		
		List<String> resultList = new ArrayList<String>();
		resultList.add("tick memsize=10MB memsize=20MB memsize=40MB memsize=80MB memsize=160MB memsize=240MB");

		for(int j = 0;j<optime.length;j++)
		{
			String s = "";
			for(int k = 0;k<=6;k++)
				s+=" "+optime[j][k]/files.length;
			resultList.add(s.trim());
		}
		
		FileControler.print2File(resultList, dir+"/cp-optime.txt");
		System.out.println("done.");
	}
}
