package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * Parse checkpionting cost, just for characterization.
 * @author sdi
 *
 */
public class ParseCPCost {

	public static void main(String[] args)
	{
		String fileName = "cpcost.txt";
		String outputFile = "cpcost2.txt";
		List<String> lineList = FileControler.readFile(fileName);
		Iterator<String> iter = lineList.iterator();
		iter.next();
		List<String> memList = new ArrayList<String>();
		List<float[]> costList = new ArrayList<float[]>();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			String mem = data[0];
			memList.add(mem);
			float[] cost = new float[5];
			for(int i = 0;i<5;i++)
				cost[i] = Float.parseFloat(data[2+i]) - Float.parseFloat(data[1]);
			costList.add(cost);
		}
		
		List<String> resultList = new ArrayList<String>();
		String fieldLine = "num ";
		Iterator<String> iter2 = memList.iterator();
		while(iter2.hasNext())
			fieldLine += iter2.next()+" ";
		resultList.add(fieldLine.trim());
		
		for(int i = 0;i<costList.get(0).length;i++)
		{
			String line = String.valueOf(i+1);
			for(int j = 0;j<costList.size();j++)
			{
				line+=" "+costList.get(j)[i];
			}
			
			resultList.add(line);
		}
		
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}
