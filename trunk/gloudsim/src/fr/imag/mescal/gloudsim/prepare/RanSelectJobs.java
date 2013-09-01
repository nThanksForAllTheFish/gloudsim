package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.RdGenerator;

/**
 * Randomly select sample jobs based on trace
 * @author sdi
 *
 */
public class RanSelectJobs {
	
	public static void main(String[] args)
	{
		if(args.length!=5)
		{
			System.out.println("java RanSelectJobs [selectMode] [# of selected jobs] [jobTaskNumFile] [priorityJobDir] [outputDir]");
			System.out.println("selectMode: single, or batch, or mix");
			System.exit(0);
		}
		
		String selectMode = args[0];
		int num = Integer.parseInt(args[1]);
		String jobTaskNumFile = args[2];
		String priorityJobDir = args[3];
		String outputDir = args[4];
		
		int minTaskNum = 0;
		int maxTaskNum = 100;
		
		if(selectMode.equals("single"))
		{
			minTaskNum = 1;
			maxTaskNum = 1;
		}
		else if(selectMode.equals("batch"))
		{
			minTaskNum = 2;
			maxTaskNum = 200;
		}
		else if(selectMode.equals("mix"))
		{
			minTaskNum = 1;
			maxTaskNum = 200;
		}
		else
			System.err.println("Wrong input parameters....");
		
		//jobTaskNumMap=/data/clusterdata-2011-1/gnuplotJobEventOutput/jobList-s-numOfTasks.txt
		Map<String, Integer> jobTaskNumMap = FileControler.readFile2MapInteger(jobTaskNumFile, 0, 4);
		
		for(int i =0;i<=11;i=i+1)
		{
			List<String> selectJobList = new ArrayList<String>();
			String srcJobListFile = priorityJobDir+"/priority-"+i+".jid";
			String outputFile = outputDir+"/"+selectMode+"/ranSelPri-"+i+".txt";
			List<String> jobList = FileControler.readFile(srcJobListFile);
			int k = 0;
			for(int j = 0;j<jobList.size();j++)
			{
				int index = RdGenerator.RAN_SeedGen.generate_Int(0, jobList.size()-1);
				String jobID = jobList.get(index);
				Integer taskNum = jobTaskNumMap.get(jobID);
				if(taskNum==null)
				{
					jobList.remove(index);
					continue;
				}
				if(taskNum>=minTaskNum&&taskNum<=maxTaskNum)
				{
					selectJobList.add(jobID+" "+taskNum);
					k++;
					if(k>=num)
						break;
				}
				jobList.remove(index);
			}
			FileControler.print2File(selectJobList, outputFile);
			System.out.println("writing data to "+outputFile);
		}
		System.out.println("done.");
	}
}
