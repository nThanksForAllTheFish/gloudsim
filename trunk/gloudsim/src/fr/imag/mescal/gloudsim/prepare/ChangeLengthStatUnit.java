package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * The original simulation time unit is the real-world time, because the simulation is truely performed on a cluster.
 * This class helps to rescale the time units if needed. 
 * @author sdi
 *
 */
public class ChangeLengthStatUnit {

	public static void main(String[] args)
	{
		List<String> newList = new ArrayList<String>();
		String lengthFile = "E:/Java-project/CheckpointSim/lengthstat/pdf/batch-length.txt_0.cdf";
		String newFile = "E:/Java-project/CheckpointSim/lengthstat/pdf/batch-length.txt_0_new.cdf";
		List<String> lineList = FileControler.readFile(lengthFile);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			float time = (float)(Float.parseFloat(data[0])/3600.0);
			String newLine = time+" "+data[1];
			newList.add(newLine);
		}
		FileControler.print2File(newList, newFile);
		System.out.println("done.");
	}
}
