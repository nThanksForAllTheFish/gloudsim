package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * Randomly select jobs based on priority, and the number of tasks per job is around 10 - 200.
 * @author sdi
 *
 */
public class ClassifyJobonPriority {
	
	public static void main(String[] args)
	{
		System.out.println("begin......");
		if(args.length!=2)
		{
			System.out.println("java ClassifyJobonPriority [priorityFile] [outputDir]");
			System.exit(0);
		}
		Map<String, List<String>> priorityJobIDMap = new HashMap<String, List<String>>();
		String priorityFile = args[0];
		String outputDir = args[1];
		List<String> lineList = FileControler.readFile(priorityFile);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			String jobID = data[0];
			String priority = data[1];
			List<String> pList = priorityJobIDMap.get(priority);
			if(pList==null)
			{
				pList = new ArrayList<String>();
				priorityJobIDMap.put(priority, pList);
			}
			pList.add(jobID);
		}
		System.out.println("Write results to "+outputDir);
		Iterator iter2 = priorityJobIDMap.entrySet().iterator(); 
		while (iter2.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter2.next(); 
		    String priority = (String)entry.getKey(); 
		    List<String> list = (List<String>)entry.getValue();
		    String outputFile = outputDir+"/priority-"+priority+".jid";
		    FileControler.print2File(list, outputFile);
		} 
		System.out.println("done.");
	}
}
