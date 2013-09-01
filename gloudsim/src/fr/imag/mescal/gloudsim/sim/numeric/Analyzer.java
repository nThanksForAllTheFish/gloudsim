package fr.imag.mescal.gloudsim.sim.numeric;

import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.elem.Task;
import fr.imag.mescal.gloudsim.prepare.JobTaskSimulator;
import fr.imag.mescal.gloudsim.sim.cp.OptCPAnalyzer;
import fr.imag.mescal.gloudsim.sim.log.Logger;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * Analyzer class here is used to perform numerical simulation,
 * instead of real simulation on a cluster.
 * @author sdi
 *
 */
public class Analyzer {

	public static float cpCost = 2;

	public static void main(String[] args)
	{
		System.out.println("begin....");
		Initialization.load("prop.config");
		//Initialization.jobTraceDir = "simFailureTrace-ori";
		System.out.println("test with mode=single");
		test("single");
		System.out.println("test with mode=batch");
		test("batch");
		System.out.println("test with mode=mix");
		test("mix");
		System.out.println("done.");
	}

	private static void test(String traceMode)
	{
		String srcObjFile = Initialization.jobTraceDir+"/"+traceMode+"/jobTrace-dec.obj";
		JobTrace jobTrace = FileControler.loadJobTraceFromFile(srcObjFile);
		JobTaskSimulator.initTargetLoad(jobTrace);
		initCPCost(jobTrace);
		System.out.println("execute numeric simulation theta = 0.5.(Young)...");
		numericSim(jobTrace, traceMode, 0.5f, "Young");
		System.out.println("execute numeric simulation theta = 1 .(Young)...");
		numericSim(jobTrace, traceMode, 1f, "Young");
		System.out.println("execute numeric simulation theta = 0.5.(Static)...");
		numericSim(jobTrace, traceMode, 0.5f, "Di_static");
		System.out.println("execute numeric simulation theta = 1 .(Static)...");
		numericSim(jobTrace, traceMode, 1f, "Di_static");
	}

	private static void initCPCost(JobTrace jobTrace)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					bt.setCpCost(cpCost);
				}
			}
		}
	}

	private static void numericSim(JobTrace jobTrace, String traceMode, float theta, String formula)
	{
		execute(jobTrace, theta, formula);
		Logger.compStatResult(jobTrace);
		List<String> logList = jobTrace.toLogList();
		String outputFile = Initialization.jobTraceDir+"/"+traceMode+"/log-"+formula+"-"+theta+".txt";
		FileControler.print2File(logList, outputFile);
	}

	/**
	 *
	 * @param jobTrace
	 * @param theta
	 * @param formula: Young or Di_static or Di_dynamic
	 */
	public static void execute(JobTrace jobTrace, float theta, String formula)
	{
		Iterator<PJobContainer> iter = jobTrace.pJobList.iterator();
		while(iter.hasNext())
		{
			PJobContainer pc = iter.next();
			Iterator<Job> iter2 = pc.jobList.iterator();
			while(iter2.hasNext())
			{
				Job job = iter2.next();
				Iterator<BatchTask> iter3 = job.batchTaskList.iterator();
				while(iter3.hasNext())
				{
					BatchTask bt = iter3.next();
					//float mtbf = bt.getMeanTaskLength();
					//float meanFNum = bt.taskList.size()-1;
					float mtbf = pc.getMeanTaskLength();
					float meanFNum = pc.getMeanTaskFailNum();
					compWorkloadRealLength(bt, theta, formula, mtbf, meanFNum);
				}
			}
		}
	}

	public static float[] compYoungCheckpoints(BatchTask batchTask, float theta, float mtbf)
	{
		float totalLength = batchTask.getTotalTaskLength();
		float cpInterval = OptCPAnalyzer.compYoungInterval(batchTask.getCpCost(), mtbf, theta);
		int cpNum = (int)(totalLength/cpInterval)+1;
		float[] cpPos = new float[cpNum];
		for(int i = 0;i<cpNum;i++)
			cpPos[i]+=(i+1)*cpInterval;
		return cpPos;
	}

	public static int compFailNumbers(float targetLength, BatchTask batchTask)
	{
		Iterator<Task> iter = batchTask.taskList.iterator();
		int totalFail = 0;
		float sumDuration = 0;
		while(iter.hasNext())
		{
			Task task = iter.next();
			sumDuration += task.getDuration();
			if(sumDuration>targetLength)
				break;
			totalFail++;
		}
		return totalFail;
	}

	public static float[] compStaticCheckpoints(BatchTask batchTask, float theta, float meanFNum)
	{
		float targetLength = batchTask.getTotalTaskLength();//*OptCPAnalyzer.loadRatio;
		//int totalFailNum = compFailNumbers(targetLength, batchTask);
		float totalFailNum = meanFNum*OptCPAnalyzer.loadRatio;
		float cpNum = OptCPAnalyzer.compStaticOptNum(batchTask.getCpCost(), targetLength, totalFailNum, theta);
		float[] cpPos = new float[(int)cpNum];
		float cpInterval = 0;
		if(cpNum!=0)
			cpInterval = targetLength/cpNum;
		for(int i = 0;i<(int)cpNum;i++)
			cpPos[i]+=(i+1)*cpInterval;
		return cpPos;
	}

	public static float[] failPos(BatchTask batchTask)
	{
		float[] failPos = new float[batchTask.taskList.size()];
		Iterator<Task> iter = batchTask.taskList.iterator();
		float lastFail = 0;
		for(int i = 0;iter.hasNext()&&i<batchTask.taskList.size();i++)
		{
			Task task = iter.next();
			//float endTime = task.getEndTime();
			failPos[i] = lastFail+task.getDuration();
			lastFail = failPos[i];
		}
		return failPos;
	}

	/**
	 *
	 * @param chInterval
	 * @param failPos
	 * @return float[2]: workload and wallclock length
	 */
	public static float[] compProcessedWorkload(float cpCost, BatchTask batchTask, float chInterval, float[] failPos)
	{
		float targetLoad = batchTask.getTotalTaskLength();//*OptCPAnalyzer.loadRatio;
		if(chInterval==0)
			return new float[]{targetLoad, targetLoad};
		float sumWorkload = 0;
		float segWorkload = 0;
		float totalFailCost = 0;
//		int failures = 0;
		for(int i = 0;i<failPos.length;i++)
		{
			float failureDuration = i==0?failPos[i]:failPos[i]-failPos[i-1];
			while(segWorkload<failureDuration)
			{
				if(i==failPos.length-1)
				{
					segWorkload += failureDuration;
					break;
				}
				else
					segWorkload += chInterval;
			}
			if(i==failPos.length-1)
			{
				sumWorkload += segWorkload;
				break;
			}
			else
			{
//				failures++;
				segWorkload -= chInterval;
				totalFailCost += (failureDuration - segWorkload);
				sumWorkload += segWorkload;
				segWorkload = 0;
			}
			if(sumWorkload>=targetLoad)
			{
				break;
			}
		}
		if(sumWorkload<targetLoad)
			targetLoad = sumWorkload;
		float totalcpCost = ((int)(targetLoad/chInterval))*cpCost;

//		float restartCost = 0;
//		for(int i = 1;i<batchTask.taskList.size();i++)
//		{
//			Task task2 = batchTask.taskList.get(i);
//			Task task1 = batchTask.taskList.get(i-1);
//			restartCost += task2.getStartTime() - task1.getEndTime();
//			if(i>failures)
//				break;
//		}
		float realLength = targetLoad+totalcpCost+totalFailCost;
		return new float[]{targetLoad, realLength};
	}

	public static float compFailCost(float[] chPos, float[] failPos)
	{
		float sumFailCost = 0;
		int j = 0;
		for(int i = 0;i<failPos.length;i++)
		{
			for(;j<chPos.length;j++)
			{
				float curFailPos = failPos[i];
				float curChPos = chPos[j];
				if(curFailPos<curChPos)
				{
					if(j==0)
						sumFailCost += failPos[i];
					else
					{
						if(i!=failPos.length-1) //the last failure is not a failure
							sumFailCost += (failPos[i] - chPos[j-1]);
					}
					break;
				}
			}
		}
		return sumFailCost;
	}

	public static float compRealLength(BatchTask batchTask, float theta, float meanFNum)
	{
		float[] cpPos = compStaticCheckpoints(batchTask, theta, meanFNum);
		float[] failPos = failPos(batchTask);
		float failCost = compFailCost(cpPos, failPos);
		float realLength = batchTask.getWallClockLength()+cpPos.length*batchTask.getCpCost()+failCost;
		return realLength;
	}

	/**
	 *
	 * @param batchTask
	 * @param theta
	 * @param formula: Young or Di_static or Di_dynamic
	 */
	public static void compWorkloadRealLength(BatchTask batchTask, float theta, String formula, float mtbf, float meanFNum)
	{

		float[] cpPos = null;
		if(formula.equals("Young"))
			cpPos = compYoungCheckpoints(batchTask, theta, mtbf);
		else if(formula.equals("Di_static"))
			cpPos = compStaticCheckpoints(batchTask, theta, meanFNum);
		else //formula == Di_dynamic
		{
			//TODO
			//cpPos = compDynamicCheckpoints(batchTask, theta);
		}
		if(cpPos==null)
			System.out.println("[Analyzer]cpPos=null! mtbf="+mtbf+";meanFNum="+meanFNum+";taskLength="+batchTask.getTotalTaskLength());
		else
		{
			if(cpPos.length!=0)
				System.out.println("[Analyzer]cpPos="+cpPos[0]+";mtbf="+mtbf+";meanFNum="+meanFNum+";taskLength="+batchTask.getTotalTaskLength());
			else
				System.out.println("[Analyzer]cpPos.length=0;mtbf="+mtbf+";meanFNum="+meanFNum+";taskLength="+batchTask.getTotalTaskLength());
		}
		float[] failPos = failPos(batchTask);
//		float failCost = compFailCost(cpPos, failPos);
//		float realLength = batchTask.getWallClockLength()+cpPos.length*cpCost;
//		batchTask.setRealWallClockLength(realLength);
		float cpLength = 0;
		if(cpPos.length!=0)
			cpLength = cpPos[0];
		float[] result = compProcessedWorkload(batchTask.getCpCost(), batchTask, cpLength, failPos);
		batchTask.setProcWorkload(result[0]);
		batchTask.setRealWallClockLength(result[1]);
	}
}
