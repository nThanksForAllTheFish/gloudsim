package fr.imag.mescal.gloudsim.prepare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

/**
 * compute average checkpointing cost
 * @author sdi
 *
 */
public class ComputeAvgCost {
	
	public static void main(String[] args)
	{
		String[] srcFiles = new String[]{"remotedisk1.cost","remotedisk2.cost","remotedisk3.cost","remotedisk5.cost","remotedisk6.cost","remotedisk8.cost"};
		String tgtFile = "cost/remotedisk-mv-restart.cost";
		List<float[]> dataList = new ArrayList<float[]>();
		List<String> sList = new ArrayList<String>();
		List<String> lineLists = FileControler.readFile(srcFiles[0]);
		Iterator<String> iters = lineLists.iterator();
		while(iters.hasNext())
		{
			String line = iters.next();
			String[] s = line.split("\\s");
			dataList.add(new float[srcFiles.length]);
			sList.add(s[1]);
		}
		
		for(int i = 0;i<srcFiles.length;i++)
		{
			String file = srcFiles[i];
			List<String> lineList = FileControler.readFile(file);
			System.out.println("lineList.size()="+lineList.size());
			Iterator<String> iter = lineList.iterator();
			for(int j = 0;iter.hasNext()&&j<lineList.size();j++)
			{
				String line = iter.next();
				String[] s = line.split("\\s");
				float[] data = dataList.get(j);
				data[i] = Float.parseFloat(s[2]);
			}
		}
		
		List<String> resultList = new ArrayList<String>();
		for(int i = 0;i<sList.size();i++)
		{
			float sum = 0;
			float[] data = dataList.get(i);
			for(int j = 0;j<data.length;j++)
				sum+=data[j];
			float mean = sum/data.length;
			resultList.add((i+1)+" "+sList.get(i)+" "+mean);
		}
		
		FileControler.print2File(resultList, tgtFile);
		System.out.println("done.");
	}
}
