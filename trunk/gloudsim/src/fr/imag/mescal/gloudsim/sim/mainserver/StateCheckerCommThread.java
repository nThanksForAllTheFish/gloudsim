package fr.imag.mescal.gloudsim.sim.mainserver;

import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;

/**
 * @deprecated
 * @author sdi
 *
 */
public class StateCheckerCommThread extends Thread
{
	private String targetHost;
	public StateCheckerCommThread(String targetHost) {
		this.targetHost = targetHost;
	}

	public void run()
	{
		while(true)
		{
//			if(targetHost.endsWith("vm1"))
//				System.out.println("===="+System.currentTimeMillis()+"[StateCheckerCommThread]connecting targetHost:"+targetHost);
			TCPClient client = new TCPClient(targetHost, VMServer.StateCheckerPort);
//			if(targetHost.endsWith("vm1"))
//				System.out.println(System.currentTimeMillis()+"[StateCheckerCommThread]after tcpclient construction");
			List<String> failList = client.readFailBTList();		
//			if(targetHost.endsWith("vm1"))
//				System.out.println(System.currentTimeMillis()+"[StateCheckerCommThread]after reading failbtList");
			client.closeSocket();
			if(!failList.isEmpty())
			{
				printFailList(failList);
				//restart batch tasks
				Iterator<String> iter2 = failList.iterator();
				while(iter2.hasNext())
				{
					String failBTID = iter2.next();
			    	String targetVM = JobEmulator.ranGenVM();
			    	System.out.println("[StateCheckerThread]failBTID="+failBTID+";targetVM="+targetVM);
			    	StateCheckerRestartTaskThread scrtt = new StateCheckerRestartTaskThread(targetVM, failBTID);
			    	scrtt.start();	
				}
			}
			try {
				//while(client.isConnected())
					Thread.sleep(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void printFailList(List<String> failList)
	{
		if(!failList.isEmpty())
		{	
			String s = "";
			for(int i = 0;i<failList.size();i++)
				s+="|"+failList.get(i);
			System.out.println(System.currentTimeMillis()+";targetHost:"+targetHost+";[StateCheckerThread]failList:"+s);			
		}
	}
}
