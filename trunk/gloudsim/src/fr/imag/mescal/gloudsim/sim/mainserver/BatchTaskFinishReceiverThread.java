package fr.imag.mescal.gloudsim.sim.mainserver;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Device;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.NFSControler;

/**
 * When a batchtask is finished, 
 * BatchTaskFinishReceiverThread will receive the notification.
 * @author sdi
 *
 */
public class BatchTaskFinishReceiverThread extends Thread {
	
	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(
					JobEmulator.BTFinishReceiverPort);
			while (true) {
				System.out.println("[BatchTaskFinishReceiverThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
					//System.out.println("[BatchTaskFinishReceiverThread]accpet....");
				} catch (SocketException e) {
					System.out.println("[BatchTaskFinishReceiverThread]Reinitialize socket...");
					serverSocket = new ServerSocket(JobEmulator.BTFinishReceiverPort);
					continue;
				}
				new FinishReceiverThread(client).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class FinishReceiverThread extends Thread
{
	private Socket client = null;
	private DataInputStream dis = null;

	public FinishReceiverThread(Socket client) {
		this.client = client;
	}
	public void run()
	{
		try {
			dis = new DataInputStream(client.getInputStream());
			String msg = dis.readUTF();
			String vmHost = client.getInetAddress().getHostName();
			System.out.println("[BatchTaskFinishReceiverThread]msg=" + msg+";client="+vmHost+"-------------done.-------------");
			String[] data = msg.split("\\s");
			String btID = data[0];
			BatchTask bt = JobEmulator.schedBTMap.get(btID);
			
			MemStateChecker.procTaskFinishFailure(vmHost, bt);
			
			Device device = NFSControler.nfsDeviceMap.get(btID);
			device.setRunningBTNum(device.getRunningBTNum()-1);
			float wc_time = Float.parseFloat(data[1]);

			if(wc_time<=0)
			{
//				double expFinishTime = System.currentTimeMillis()/1000.0;
//				bt.setExpFinishTime(expFinishTime);
//				float totalWCLength = (float)(expFinishTime - bt.getExpSchedTime());
				float totalWCLength = -1;
				bt.setRealWallClockLength(totalWCLength);
				JobEmulator.schedBTMap.remove(btID);
				JobEmulator.schedJobMap.remove(btID.split("-")[0]);
			}
			else
				bt.setRealWallClockLength(wc_time);
			
			if (!Initialization.useJobArrivalTrace) {
				synchronized (JobEmulator.schedBTMap) {
					JobEmulator.schedBTMap.notify();
				}
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
