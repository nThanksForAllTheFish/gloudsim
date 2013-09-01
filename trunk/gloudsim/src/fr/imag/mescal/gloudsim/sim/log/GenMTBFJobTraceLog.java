package fr.imag.mescal.gloudsim.sim.log;

import java.util.ArrayList;
import java.util.List;
import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.elem.PJobContainer;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * used to characterize and analyze the difference between MTBF and MNOF in google trace.
 * @author sdi
 *
 */
public class GenMTBFJobTraceLog {

	static int priNum = 10;
	public static void main(String[] args)
	{
		List<String> rList = new ArrayList<String>();
		Initialization.load("prop.config");
		String limitString = "1000 3600 3000000";
		String[] s = limitString.split("\\s");
		int[] limits = new int[s.length];
		for(int i = 0;i<s.length;i++)
		{
			limits[i] = Integer.parseInt(s[i]);
			System.out.println("limits["+i+"]="+limits[i]);
			rList.addAll(genMNOFMTBF(limits[i]));
		}
		FileControler.print2File(rList, "mnof-mtbf-table.txt");
		System.out.println("done.");
	}

	public static List<String> genMNOFMTBF(int limit)
	{
		System.out.println("genMNOFMTBF:"+limit);
		String[] lines = new String[priNum];
		for(int i = 0;i<lines.length;i++)
		{
			//int j = i*2+1;
			int j = i;
			lines[i] = " & "+j;
		}
		String rootDirPath = Initialization.jobTraceDir;
		String[] modes = new String[]{"batch", "single", "mix"};
		for(String mode:modes)
		{
			String modeDir = rootDirPath+"/"+mode;
			String traceFile = "";
//			if(limit>=2600000)
//				traceFile = modeDir+"/jobTrace-dec.obj";
//			else
			traceFile = modeDir+"/jobTrace-"+limit+"-dec.obj";;
			System.out.println("reading traceFile:"+traceFile);
			genTableLines(traceFile, lines);
		}
		List<String> rList = new ArrayList<String>();
		for(int i = 0;i<lines.length;i++)
		{
			rList.add(lines[i]);
		}
		return rList;
	}

	private static void genTableLines(String traceFile, String[] lines)
	{
		JobTrace jobTrace = FileControler.loadJobTraceFromFile(traceFile);
		List<PJobContainer> pJobList = jobTrace.pJobList;
		for(int i = 0;i<priNum;i++)
		{
			//int j = i*2+1;
			int j = i;
			PJobContainer pc = pJobList.get(j);
			lines[i] = lines[i]+" & " + (pc.getPriority()+1) +" & " +pc.getMeanTaskFailNum()+" & "+pc.getMeanTaskLength();
		}
	}
}
