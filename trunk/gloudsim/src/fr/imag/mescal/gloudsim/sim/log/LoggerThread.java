package fr.imag.mescal.gloudsim.sim.log;

import java.io.DataInputStream;
import java.net.Socket;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.Job;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
/**
 * Outdated!!
 * @author sdi
 *
 */
public class LoggerThread extends Thread{

	private Socket client;
	
	public LoggerThread(Socket client) {
		this.client = client;
	}

	public void run()
	{
		try {
			String clientAddress = client.getInetAddress().getHostAddress();
			System.out.println("[BatchTaskFinishReceiverThread]invoked by: "
					+ clientAddress);
			DataInputStream dis = new DataInputStream(client.getInputStream());
			String msg = dis.readUTF();
			//TODO: the whole task is finished, then, receive the notification at the main server.
			String[] data = msg.split("\\s");
			String batchTaskID = data[0];
			float realWallClockTime = Float.parseFloat(data[1]);
			String[] s = batchTaskID.split("-");
			String jobSimID = s[0];
			int btIndex = Integer.parseInt(s[1]);
			synchronized(Logger.synMutex)
			{
				Job job = JobEmulator.schedJobMap.get(jobSimID);
				BatchTask bt = job.batchTaskList.get(btIndex);
				bt.setRealWallClockLength(realWallClockTime);
			}
			dis.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
