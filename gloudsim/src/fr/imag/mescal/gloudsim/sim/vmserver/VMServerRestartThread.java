package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class VMServerRestartThread extends Thread {

	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(
					VMServer.RestartBTPort);
			while (true) {

				System.out.println(VMServer.vmHostName+":"+"[VMServerRstartTaskThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
				} catch (SocketException e) {
					System.out.println(VMServer.vmHostName+":"+"Reinitialize socket...");
					serverSocket = new ServerSocket(VMServer.RestartBTPort);
					continue;
				}
				new RestartThread(client).run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class RestartThread extends Thread
{
	private Socket client;
	private DataInputStream dis;
	
	public RestartThread(Socket client) {
		this.client = client;
	}
	public void run()
	{
		String serverName = client.getInetAddress().getHostName();
		System.out.println(VMServer.vmHostName+":"+"[RestartThread]invoked by: "+ serverName);

		try {
			//TODO:receive restarting information from server
			//TODO:perform restarting command with context.file
			dis = new DataInputStream(client.getInputStream());
			String btID = dis.readUTF();
			TaskExeThread vmrestart = new TaskExeThread(btID, false, VMServer.vmHostName);
			vmrestart.start();
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
