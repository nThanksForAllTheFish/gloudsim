package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * Estimate the task restarting overhead
 * @author sdi
 *
 */
public class GenEstLog2 {

	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("java GenEstLog2 [isNFSDisk?] [restartLogFileName] [times] [outputFile]");
			System.out.println("java GenEstLog2 est-nfs-restart.log cpcost/nfs-restart.cost");
			System.exit(0);
		}
		
		String restartLogName = args[0];
		String outputFile = args[1];
		
		List<String> resultList = new ArrayList<String>();
		List<String> lineList = FileControler.readFile(restartLogName);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			if(line.contains("cpprogram"))
			{
				String[] data = line.split(";");
				String id = data[1];
				String usedMem = data[2].split("=")[1].replace("MB", "");
				String[] s = data[8].split("=");
				String result = id+" "+usedMem+" "+Float.parseFloat(s[1]);
				resultList.add(result);
			}
		}
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
	}
}
