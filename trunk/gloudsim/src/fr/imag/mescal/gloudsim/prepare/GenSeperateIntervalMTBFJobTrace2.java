package fr.imag.mescal.gloudsim.prepare;

import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * This is used to generate jobtrace files whose MTBFs are based on stair-style
 * e.g., exponential mode: 0-500, 500-1000, 1000-2000, 2000-4000
 *       linear mode: 0-500,500-1000,1000,1500,....
 * That is, the task's MNOF and MTBF will be computed just based on the "simular" tasks within the same length range.
 * @author sdi
 *
 */
public class GenSeperateIntervalMTBFJobTrace2 {
	public static void main(String[] args)
	{
		System.out.println("initilization of loading prop.config");
		Initialization.needToLoadCpFiles = false;
		Initialization.load("prop.config");

		String jobTraceFileName = "jobTrace-dec.obj";
		int basicExeLength = 500; //500, 1000, 2000, 3000, 3600, 4000, 5000, 6000, 3000000
		int breakPointNum = 8; //then, 5 intervals
		String increaseWay = "linear"; //linear, exponential
		
		List<String> modeList = PVFile.getDir(Initialization.jobTraceDir);
		Iterator<String> iter = modeList.iterator();
		while(iter.hasNext())
		{
			String mode = iter.next();
			String contextFilePath = Initialization.jobTraceDir+"/"+mode+"/"+jobTraceFileName;
			System.out.println("generating new jobtrace with basiclength="+basicExeLength);
			System.out.println("increaseWay="+increaseWay);
			JobTrace newJT = genNewJobTrace(contextFilePath, basicExeLength, breakPointNum, increaseWay);
			String outputFile = Initialization.jobTraceDir+"/"+mode+"/jobTrace-0";
			if (increaseWay.equals("linear")) {
				for (int i = 0; i < breakPointNum; i++)
					outputFile += "-" + (int)(basicExeLength * (i + 1));
				outputFile += "-dec.obj";
			}
			else 
			{
				for (int i = 0; i < breakPointNum; i++)
					outputFile += "-" + (int)(basicExeLength * Math.pow(2, i));
				outputFile += "-dec.obj";
			}
			FileControler.writeObject2File(newJT, outputFile);
		}
		System.out.println("done.");
	}
	
	public static JobTrace genNewJobTrace(String contextFilePath, int basicExeLength, int breakPointNum, String increaseWay)
	{
		int size = breakPointNum+1;
		float[] sum_mnof = new float[size];
		float[] sum_mtbf = new float[size];
		float[] validTaskSize = new float[size];
		float[] validBTSize = new float[size];
		for(int i = 0;i<size;i++)
		{
			sum_mnof[i] = 0;
			sum_mtbf[i] = 0;
			validTaskSize[i] = 0;
			validBTSize[i] = 0;
		}
		JobTrace jobTrace = FileControler.loadJobTraceFromFile(contextFilePath);
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				float jobLength = job.getMakespan();
				int index = (int)(jobLength/basicExeLength);
				if(index > breakPointNum) index = breakPointNum;
				int totalTaskNum = job.getTotalTaskNum();
				validTaskSize[index]+=totalTaskNum;
				validBTSize[index]+=job.batchTaskList.size();
				sum_mtbf[index] += job.getMeanTaskLength()* totalTaskNum;
				sum_mnof[index] += job.getMeanTaskFailNum()* job.batchTaskList.size();
			}
		}
		
		float[] mnof = new float[size];
		float[] mtbf = new float[size];
		for(int i = 0;i<size;i++)
		{
			if(validBTSize[i]!= 0)
				mnof[i] = sum_mnof[i]/validBTSize[i];
			if(validTaskSize[i] != 0)
				mtbf[i] = sum_mtbf[i]/validTaskSize[i];
		}
		
		System.out.println("======mnof and mtbf ======");
		System.out.println("mnof=");
		for(int i = 0;i<size;i++)
			System.out.print(mnof[i]+" ");
		System.out.println();
		System.out.println("mtbf=");
		for(int i = 0;i<size;i++)
			System.out.print(mtbf[i]+" ");
		System.out.println();
		
		iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				int index = (int)job.getMakespan()/basicExeLength;
				if(index > breakPointNum) index = breakPointNum;
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					Iterator<Task> iter4 = bt.taskList.iterator();
					while(iter4.hasNext())
					{
						Task task = iter4.next();
						task.setMNOF(mnof[index]);
						task.setMTBF(mtbf[index]);
					}
				}
			}
		}
		return jobTrace;
	}
}
