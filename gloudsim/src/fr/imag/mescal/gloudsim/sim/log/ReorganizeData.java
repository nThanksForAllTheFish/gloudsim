package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

public class ReorganizeData {

	public static void main(String[] args)
	{
		String workingDir = "E:/Java-project/CheckpointSim/simFailureTrace-basedon-pc2/mix";
		//String fileName = "log-Di_static-0.5.txt";
		String fileName = "log-Young-0.5.txt";
		String filePath = workingDir+"/"+fileName;
		List<String> resultList = new ArrayList<String>();
		String resultFilePathTemp = workingDir+"/"+fileName.split("\\.")[0];
		String resultFilePath = "";
		String allResultFilePath = resultFilePathTemp+"/all.txt";
		List<String> allResultList = new ArrayList<String>();
		List<String> lineList = FileControler.readFile(filePath);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(!line.startsWith("=")&&!line.startsWith("-"))
				continue;
			if(line.startsWith("=pc"))
			{
				if(!resultList.isEmpty())
				{
					System.out.println("printing data to "+resultFilePath);
					FileControler.print2File(resultList, resultFilePath);
				}
				resultList.clear();
				String priority = line.split("\\s")[1];
				resultFilePath = resultFilePathTemp+"/p-"+priority+".txt";
				continue;
			}
			String[] data = line.split("\\s");
			String rLine = data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5];
			allResultList.add(rLine);
			resultList.add(rLine);
		}
		FileControler.print2File(allResultList, allResultFilePath);
		System.out.println("done.");
	}
}
