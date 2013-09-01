package fr.imag.mescal.gloudsim.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to modify parameters of the configuration files.
 * This is helpful in changing settings for simulation.
 * @author sdi
 *
 */
public class BatchModifyParameters {

	public static void main(String[] args)
	{
		if(args.length!=13)
		{
			System.out.println("Usage: java BatchModifyParameters [retestFile] [paralleldegree] [simJobNum] [numOfPhyHosts] [completeAllWorkload] [loadRatio] [useJobArrivalTrace] [limitLength] [jobTraceFileName] [testMode] [taskMode] [simLength]");
			System.exit(0);
		}
		
		String retestFile = args[0];
		
		String paralleldegree = args[1];
		String simJobNum = args[2];
		String numOfPhyHosts = args[3];
		String completeAllWorkload = args[4];
		String loadRatio = args[5];
		String useJobArrivalTrace = args[6];
		String limitLength = args[7];
		String jobTraceFileName = args[8];
		String testMode = args[9];
		String taskMode = args[10];
		String dynamicSolu = args[11];
		String simLength = args[12];
		
		System.out.println("processing prop.config");
		List<String> list = FileControler.readFile("prop.config");
		List<String> nList = new ArrayList<String>();
		Iterator<String> iter = list.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.startsWith("paralleldegree="))
				nList.add("paralleldegree="+paralleldegree);
			else if(line.startsWith("simJobNum="))
				nList.add("simJobNum="+simJobNum);
			else if(line.startsWith("numOfPhyHosts"))
				nList.add("numOfPhyHosts="+numOfPhyHosts);
			else if(line.startsWith("completeAllWorkload"))
				nList.add("completeAllWorkload="+completeAllWorkload);
			else if(line.startsWith("loadRatio"))
				nList.add("loadRatio="+loadRatio);
			else if(line.startsWith("useJobArrivalTrace"))
				nList.add("useJobArrivalTrace="+useJobArrivalTrace);
			else if(line.startsWith("jobTraceFileName"))
				nList.add("jobTraceFileName="+jobTraceFileName);
			else if(line.startsWith("testMode"))
				nList.add("testMode="+testMode);
			else if(line.startsWith("taskMode"))
				nList.add("taskMode="+taskMode);
			else if(line.startsWith("dynamicSolu"))
				nList.add("dynamicSolu="+dynamicSolu);
			else
				nList.add(line);
		}
		FileControler.print2File(nList, "prop.config");
		
		System.out.println("processing "+retestFile);
		list = FileControler.readFile(retestFile);
		nList = new ArrayList<String>();
		iter = list.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.startsWith("limitLength="))
				nList.add("limitLength="+limitLength);
			else if(line.startsWith("taskMode="))
				nList.add("taskMode="+taskMode);
			else if(line.startsWith("testMode"))
				nList.add("testMode="+testMode);
			else if(line.startsWith("dynamicSolu"))
				nList.add("dynamicSolu="+dynamicSolu);
			else if(line.startsWith("paralleldegree"))
				nList.add("paralleldegree="+paralleldegree);
			else
				nList.add(line);
		}
		FileControler.print2File(nList, retestFile);
		
		System.out.println("processing JobEmulator.sh");
		list = FileControler.readFile("JobEmulator.sh");
		nList = new ArrayList<String>();
		iter = list.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.startsWith("simLength="))
				nList.add("simLength="+simLength);
			else
				nList.add(line);
		}
		FileControler.print2File(nList, "JobEmulator.sh");
		
		System.out.println("paralleldegree="+paralleldegree);
		System.out.println("simJobNum="+simJobNum);
		System.out.println("numOfPhyHosts="+numOfPhyHosts);
		System.out.println("completeAllWorkload="+completeAllWorkload);
		System.out.println("loadRatio="+loadRatio);
		System.out.println("useJobArrivalTrace="+useJobArrivalTrace);
		System.out.println("limitLength="+limitLength);
		System.out.println("jobTraceFileName="+jobTraceFileName);
		System.out.println("testMode="+testMode);
		System.out.println("taskMode="+taskMode);
		System.out.println("simLength="+simLength);
		
	}
}
