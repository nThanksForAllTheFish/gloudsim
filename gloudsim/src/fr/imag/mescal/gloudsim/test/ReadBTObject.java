package fr.imag.mescal.gloudsim.test;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.util.FileControler;

public class ReadBTObject {
	
	public static void main(String[] args)
	{
		String filePath = "bt-300-0.obj";
		BatchTask batchTask = FileControler.loadBatchTaskFromFile(filePath);
		if(batchTask==null)
			System.out.println("batchtask="+null);
		else
			System.out.println("batchtask="+batchTask.toString());
	}
			
}
