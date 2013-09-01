package fr.imag.mescal.gloudsim.gnuplot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * Analyze CPU rate Checkpointing cost
 * @author sdi
 *
 */
public class AnalyzeCPURateCPCost {

	static String[] memIndex = {"1","2","3","5","9","28"};
	public static void main(String[] args)
	{
		String rootDir = "D:/INRIA-research/IEEE-13rd/gnuplot2/google-cpcost-cpurate/cpcost-perc";
		String cpcostFile = rootDir+"/CpCostCPURate.data";
		String cpOpTimeFile = rootDir+"/CpOpTimeCPURate.data";
		
		List<String> cpcostList = new ArrayList<String>();
		List<String> cpOpTimeList = new ArrayList<String>();
		for(int i = 10;i<=100;i=i+10)
		{
			float[] cpcost = {0,0,0,0,0,0};
			float[] cpOpTime = {0,0,0,0,0,0};
			for(int j = 1;j<=5;j++)
			{
				String dataDir = rootDir+"/CPCost-"+i+"perc"+j;
				List<String> fileList = PVFile.getFiles(dataDir);
				Iterator<String> iter = fileList.iterator();
				while(iter.hasNext())
				{
					String fileName = iter.next();
					if(fileName.endsWith("cost"))
					{
						String filePath = dataDir+"/"+fileName;
						List<String> lineList = FileControler.readFile(filePath);
						Iterator<String> iter2 = lineList.iterator();
						for(int k=0;iter2.hasNext();k++)
						{
							String line = iter2.next();
							String[] data = line.split(";");
							cpcost[k]+=Float.parseFloat(data[7].split("=")[1]);
						}
					}
					else
					{
						String filePath = dataDir+"/"+fileName;
						List<String> lineList = FileControler.readFile(filePath);
						String s = lineList.get(0);
						String[] data = s.split(":");
						String cMemIndex = data[0];
						float opTime = Float.parseFloat(data[2]);
						int index = getMemIndex(cMemIndex);						
						cpOpTime[index]+=opTime;
					}
				}				
			}
			String costResult = String.valueOf(i);
			String optimeResult = String.valueOf(i);
			for(int j = 0;j<6;j++)
			{
				cpcost[j]/=5;
				costResult+=" "+cpcost[j];
				cpOpTime[j]/=5;
				optimeResult+=" "+cpOpTime[j];
			}
			cpcostList.add(costResult);
			cpOpTimeList.add(optimeResult);
		}
		
		FileControler.print2File(cpcostList, cpcostFile);
		FileControler.print2File(cpOpTimeList, cpOpTimeFile);
		System.out.println("done.");
	}
	
	private static int getMemIndex(String cMemIndex)
	{
		for(int i = 0;i<memIndex.length;i++)
		{	
			if(memIndex[i].equals(cMemIndex))
				return i;
		}
		return -1;
	}
}
