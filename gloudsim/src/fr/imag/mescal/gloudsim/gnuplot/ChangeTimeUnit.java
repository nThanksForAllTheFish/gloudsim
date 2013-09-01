package fr.imag.mescal.gloudsim.gnuplot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * Change time unit between seconds and milliseconds or others
 * @author sdi
 *
 */
public class ChangeTimeUnit {

	public static void main(String[] args)
	{
		//String rootDir = "D:/INRIA-research/IEEE-13rd/experiment/bk-false-static-single-false-3600/bk-false-static-single-false-3600-4000-tr";
		String rootDir = "E:/Java-project/CheckpointSim/gnuplot/bk-single-false-tr/bk-false-static-single-false-4000-arr-tr";
		String inputFile = "observer-false-static-single-false-4000-arr.txt";
		String outputFile = rootDir+"/"+inputFile+"2";
		
		List<String> resultList = new ArrayList<String>();
		
		List<String> lineList = FileControler.readFile(rootDir+"/"+inputFile);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			double time = Double.parseDouble(data[0]);
			double newTime = time*60; //change from hour to minute
			String newLine = String.valueOf(newTime);
			for(int i = 1;i<data.length;i++)
				newLine+=" "+data[i];
			resultList.add(newLine);
		}
		
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}
