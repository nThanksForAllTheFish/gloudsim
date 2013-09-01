package fr.imag.mescal.gloudsim.test;

import java.util.HashMap;
import fr.imag.mescal.gloudsim.util.FileControler;

public class ComputeNum {
	
	public static void main(String[] args)
	{
		HashMap<String, String> list = new HashMap<String, String>();
		String line = FileControler.readFileFirstLine("a.txt");
		String[] data = line.split("\\s");
		for(int i = 0;i<data.length;i++)
		{
			String jobID = data[i].split("-")[0];
			if(!list.containsKey(jobID))
				list.put(jobID,null);
		}
		System.out.println(data.length);
		System.out.println("list.size="+list.size());
	}
}
