package fr.imag.mescal.gloudsim.test;

import java.util.Iterator;

import fr.imag.mescal.gloudsim.elem.MemState;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.sim.mainserver.MemStateChecker;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

public class TestMemState {

	public static void main(String[] args)
	{
		System.out.println("begin......................");
		Initialization.load("prop.config");
		JobEmulator.vmHostList = FileControler.readFile("vmhosts");
		for (int i = 0; i < JobEmulator.vmHostList.size(); i++) {
			if (JobEmulator.vmHostList.get(i).startsWith("#")) {
				JobEmulator.vmHostList.remove(i);
				i--;
			}
		}
		MemStateChecker msc = new MemStateChecker();
		
		for(int i =0;i<500;i++)
		{
			new MemThread().start();
		}
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Iterator<MemState> iter = MemStateChecker.memStateList.iterator();
		while(iter.hasNext())
		{
			MemState ms = iter.next();
			System.out.print(ms+" ");
		}
		System.out.println();
		System.out.println("done.................");
		
	}
}

class MemThread extends Thread
{
	public void run()
	{
		String vm = MemStateChecker.findMaxAvailVM(0, 200);
		if(vm==null)
		{
			System.out.println("vm==null");
		}
		else
			System.out.println("vm="+vm);
	}
}
