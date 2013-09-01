package fr.imag.mescal.gloudsim.prepare;

import java.util.Iterator;
import java.util.List;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * Based on Google trace, generate new jobtrace.obj files.
 * IN the new obj files, MTBF and MNOF will be compuated based on the limited job length. 
 * For example, limitExeLength = 3600 (one hour), this means that all the less-than-one-hour tasks' MTBF and MNOF are compuated
 * based on the tasks whose lengths are less than one hour. 
 * @author sdi
 *
 */
public class GenSeperateIntervalMTBFJobTrace {


	public static void main(String[] args)
	{
		System.out.println("initilization of loading prop.config");
		Initialization.needToLoadCpFiles = false;
		Initialization.load("prop.config");

		String jobTraceFileName = "jobTrace-dec.obj";
		int limitExeLength = 3600; //500, 1000, 2000, 3000, 3600, 4000, 5000, 6000, 3000000

		List<String> modeList = PVFile.getDir(Initialization.jobTraceDir);
		Iterator<String> iter = modeList.iterator();
		while(iter.hasNext())
		{
			String mode = iter.next();
			String contextFilePath = Initialization.jobTraceDir+"/"+mode+"/"+jobTraceFileName;
			System.out.println("generating new jobtrace with limitlength="+limitExeLength);
			JobTrace newJT = genNewJobTrace(contextFilePath, limitExeLength);
			JobTaskSimulator.fillMTBFMNOFontoTasks(newJT);
			String outputFile = contextFilePath.replace("jobTrace", "jobTrace-"+limitExeLength);
			FileControler.writeObject2File(newJT, outputFile);
		}
		System.out.println("done.");
	}

	private static JobTrace genNewJobTrace(String contextFilePath, int limitExeLength)
	{
		JobTrace jobTrace = FileControler.loadJobTraceFromFile(contextFilePath);
		float sumTotalTaskLength = 0;
		float sumTotalFailNum = 0;
		float totalValidTaskSize = 0;
		float totalValidBTSize = 0;
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			float sumTaskLength = 0;
			float sumFailNum = 0;
			int validTaskSize = 0;
			int validBTSize = 0;
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				if(job.getMakespan()<=limitExeLength)
				{
					int totalTaskNum = job.getTotalTaskNum();
					validTaskSize+=totalTaskNum;
					validBTSize+=job.batchTaskList.size();
					sumTaskLength += job.getMeanTaskLength()*totalTaskNum;
					sumFailNum += job.getMeanTaskFailNum()*job.batchTaskList.size();
				}
			}
			if(validTaskSize>0)
			{
				totalValidTaskSize += validTaskSize;
				totalValidBTSize += validBTSize;
				pc.setMeanTaskLength(sumTaskLength/validTaskSize);
				pc.setMeanTaskFailNum(sumFailNum/validBTSize);
				sumTotalTaskLength += sumTaskLength;
				sumTotalFailNum += sumFailNum;
			}
		}
		jobTrace.setMeanTaskLength(sumTotalTaskLength/totalValidTaskSize);
		jobTrace.setMeanTaskFailNum(sumTotalFailNum/totalValidBTSize);

		return jobTrace;
	}
}
