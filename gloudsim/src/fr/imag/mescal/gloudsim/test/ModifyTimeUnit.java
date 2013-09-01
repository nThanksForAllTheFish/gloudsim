package fr.imag.mescal.gloudsim.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.util.FileControler;

public class ModifyTimeUnit {
	
	public static void main(String[] args)
	{
		String mark = "true-static-single-false-1000-1600";
		String observerDir = "E:/Java-project/CheckpointSim/gnuplot/bk-single-false-parallelism-tr";
		String observerFile = observerDir+"/bk-"+mark+"-tr/observer-"+mark+".txt";
		String newObsFile = observerDir+"/bk-"+mark+"-tr/observer-"+mark+"-new.txt";
		int simLength = 2500; //seconds
		List<String> lineList = FileControler.readFile(observerFile);
		List<String> newList = new ArrayList<String>();
		Iterator<String> iter = lineList.iterator();
		for(int i = 0,j=0;iter.hasNext()&&j<simLength;i++)
		{
			String line = iter.next();
//			if(i%50==0)
//			{
//				String[] data = line.split("\\s");
//				if(j%5==0)
//				{
//					String content = data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5];
//					newList.add(j+" "+content);
//				}
//				j++;
//			}
			String[] data = line.split("\\s");
			float t_h = Float.parseFloat(data[0]);
			int t_s = (int)(t_h*3600);
			String content = data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5];
			newList.add(t_s+" "+content);
		}
		FileControler.print2File(newList, newObsFile);
		System.out.println("done.");
	}
}
