package fr.imag.mescal.gloudsim.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComputeCost {
	static List<String> sList = new ArrayList<String>();
	public static void main(String[] args)
	{
		String[] files = new String[]{"ramfs.cost","ramfs2.cost","ramfs3.cost"};
		List<Float> list1 = new ArrayList<Float>();
		List<Float> list2 = new ArrayList<Float>();
		List<Float> list3 = new ArrayList<Float>();
		System.out.println(files[0]);
		process(files[0], list1);
		System.out.println(files[1]);
		process(files[1], list2);
		System.out.println(files[2]);
		process(files[2], list3);
 
		List<String> newList = new ArrayList<String>();
		for(int i =0;i<list1.size();i++)
		{
			float v1 = list1.get(i);
			float v2 = list2.get(i);
			float v3 = list3.get(i);
			float sum = v1+v2+v3-Math.min(v1, Math.min(v2, v3));
			float mean = sum/2;
			newList.add((i+1)+" "+sList.get(i)+" "+mean);
		}
		FileControler.print2File(newList, "ramfs-ag.cost");
		System.out.println("done.");
	}
	
	public static void process(String file, List<Float> list)
	{
		List<String> lineList = FileControler.readFile(file);
		Iterator<String> iter = lineList.iterator();
		while(iter.hasNext())
		{
			String line = iter.next();
			String[] data = line.split("\\s");
			sList.add(data[1]);
			list.add(Float.valueOf(data[2]));
		}
	}
}
