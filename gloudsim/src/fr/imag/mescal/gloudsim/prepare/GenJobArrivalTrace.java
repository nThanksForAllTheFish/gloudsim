package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * genrate job arrival trace (containing the arrival dates of jobs) based on Google trace.
 * @author sdi
 *
 */
public class GenJobArrivalTrace {

	public static void main(String[] args)
	{
		String file = "jobList-s-startTime.txt";
		String outputFile = "jobArrivalTrace.txt";
		List<String> lineList = FileControler.readFile(file);
		List<String> resultList = new ArrayList<String>();
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			resultList.add(String.valueOf(Float.parseFloat(data[1])-600));
		}
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}
