package fr.imag.mescal.gloudsim.prepare;

import java.util.List;

import fr.imag.mescal.gloudsim.elem.JobTrace;
import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * A test program of load job trace
 * @author sdi
 *
 */
public class TestLoadJobTrace {

	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("java TestLoadJobTrace [jobTraceObjFile] [jobTraceTxtFile]");
			System.exit(0);
		}
		String jobTraceObjFile = args[0];
		String jobTraceTxtFile = args[1];
		System.out.println("start loading sample jobs...");
		JobTrace jt = JobTaskSimulator.loadSampleJobs(jobTraceObjFile);
		List<String> list = jt.toStringList();
		System.out.println("start writing txt to file: "+jobTraceTxtFile);
		FileControler.print2File(list, jobTraceTxtFile);
		System.out.println("done.");
	}
}
