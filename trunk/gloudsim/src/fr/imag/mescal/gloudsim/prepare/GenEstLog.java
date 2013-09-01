package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
/**
 * estimate the total overhead (e.g., the checkpointing cost) 
 * @author sdi
 *
 */
public class GenEstLog {

	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("Usage: java GenEstLog [logName] [times] [outputFile]");
			System.out.println("Example: java GenEstLog est.log 5 cpcost/0.1.cost");
			System.exit(0);
		}
		String logName = args[0];
		int times_ = Integer.parseInt(args[1]);
		String outputFile = args[2];
		List<String> resultList = new ArrayList<String>();
		List<String> lineList = FileControler.readFile(logName);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.contains("cpprogram"))
			{
				String[] data = line.split(";");
				String id = data[1];
				String usedMem = data[2].split("=")[1].replace("MB", "");
				float totalLength = Float.parseFloat(data[3].split("=")[1]);
				String du = data[6].split("=")[1];
				float du_ = (Float.parseFloat(du)-totalLength/Initialization.tuneWorkloadRatio)/times_;
				String result = id+" "+usedMem+" "+du_;
				resultList.add(result);
			}
		}
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}
