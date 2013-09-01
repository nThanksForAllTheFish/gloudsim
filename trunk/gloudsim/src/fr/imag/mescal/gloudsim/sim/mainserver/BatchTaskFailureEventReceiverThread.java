package fr.imag.mescal.gloudsim.sim.mainserver;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.util.NFSControler;

/**
 * When a batchtask runs into a failure event (i.e., switch between two consecutive tasks), 
 * BatchTaskFailureEventReceiverThread will receive a notification.
 * @author sdi
 *
 */
public class BatchTaskFailureEventReceiverThread extends Thread {
	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(
					JobEmulator.BTFailureEventPort);
			while (true) {
				System.out.println("[BatchTaskFailureEventReceiverThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
					//System.out.println("[BatchTaskFinishReceiverThread]accpet....");
				} catch (SocketException e) {
					System.out.println("[BatchTaskFailureEventReceiverThread]Reinitialize socket...");
					serverSocket = new ServerSocket(JobEmulator.BTFailureEventPort);
					continue;
				}
				new FailureEventReceiverThread(client).start();//no need to make a new thread 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class FailureEventReceiverThread extends Thread
{
	private Socket client = null;
	private DataInputStream dis = null;
	
	public FailureEventReceiverThread(Socket client) {
		this.client = client;
	}
	
	public void run()
	{
		try {
			dis = new DataInputStream(client.getInputStream());
			String btID = dis.readUTF();
			String vmHost = client.getInetAddress().getHostName();
			System.out.println("[FailureEventReceiverThead]failed btID=" + btID
					+ ";client=" + vmHost);
			BatchTask bt = JobEmulator.schedBTMap.get(btID);
			
			MemStateChecker.procTaskFinishFailure(vmHost, bt);
			
			int avoidDeviceID = NFSControler.nfsDeviceMap.get(btID).getID();
			//String targetVM = JobEmulator.ranGenVM(avoidDeviceID); //target host

			//float memSize = bt.getMeanMemSize();
			String targetVM = MemStateChecker.findMaxAvailVM(avoidDeviceID, bt, vmHost);			
	    	System.out.println("[FailureEventReceiverThread]failBTID="+btID+";targetVM="+targetVM);
	    	if(targetVM!=null)
	    	{
	    		StateCheckerRestartTaskThread scrtt = new StateCheckerRestartTaskThread(targetVM, bt);
		    	scrtt.start();
	    	}	
	    	else
	    	{
	    		JobEmulator.scheduler.add2List(bt);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		} finally
		{
			try {
				dis.close();
				client.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}