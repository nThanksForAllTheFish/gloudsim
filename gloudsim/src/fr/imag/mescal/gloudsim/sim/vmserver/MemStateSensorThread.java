package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * MemStateSensorThread is used to detect the memory usage on the current local VM node, 
 * and send the states back to server.
 * @author sdi
 *
 */
public class MemStateSensorThread extends Thread {

	ServerSocket serverSocket = null;
	Socket client = null;
	DataOutputStream dos = null;
	
	public MemStateSensorThread()
	{
		try {
			serverSocket = new ServerSocket(VMServer.MemStateCheckerPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while (true) {
			System.out.println(VMServer.vmHostName+":"+"[MemStateSensor]Start listening.");
			try {
				client = serverSocket.accept();
			} catch (Exception e) {
				System.out.println(VMServer.vmHostName+":"+"Reinitialize socket...");
				try {
					serverSocket = new ServerSocket(
							VMServer.MemStateCheckerPort);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				continue;
			}
			String serverName = client.getInetAddress().getHostName();
			System.out.println(VMServer.vmHostName+":"+"[MemStateSensor]invoked by: "+ serverName);

			try {
				int ramdiskSize = (int)Cmd.getRemainigRamfskMemSize();
				int memSize = (int)(Cmd.getRemainingMemSize() - Initialization.ramdiskSize);
				dos = new DataOutputStream(client.getOutputStream());
				dos.writeUTF(ramdiskSize+" "+memSize);
			} catch (Exception e) {
				e.printStackTrace();
			} finally
			{
				try {
					dos.close();
					client.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
