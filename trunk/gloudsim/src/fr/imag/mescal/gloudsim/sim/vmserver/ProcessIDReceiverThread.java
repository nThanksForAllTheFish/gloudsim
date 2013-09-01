package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * receive messages from TaskExecutor
 * Outdated and useless!!
 * This is because TaskExeThread can take over the work (maintaining btID:procID map)
 * @deprecated
 * @author sdi
 *
 */
public class ProcessIDReceiverThread extends Thread {
	
	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(VMServer.TaskProcessIDNotifyPort);
			while (true) {
				//System.out.println("[ProcessIDReceiverThread]Start listening.");
				System.out.println(VMServer.vmHostName+":"+"[ProcessIDReceiverThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
				} catch (SocketException e) {
					System.out.println(VMServer.vmHostName+":"+"Reinitialize socket...");
					serverSocket = new ServerSocket(VMServer.TaskProcessIDNotifyPort);
					continue;
				}
				new PIDReceiverThread(client).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class PIDReceiverThread extends Thread
{
	private Socket client;
	
	public PIDReceiverThread(Socket client) {
		this.client = client;
	}
	
	public void run()
	{
		try {
			String clientAddress = client.getInetAddress().getHostAddress();
			System.out.println(VMServer.vmHostName + ":"
					+ "[PIDReceiverThread]invoked by: " + clientAddress);
			DataInputStream dis = new DataInputStream(client.getInputStream());
			String message = dis.readUTF();
			String[] data = message.split("\\s"); //message: "btID processID"
			String btID = data[0];
			int processID = Integer.parseInt(data[1]);
			VMServer.btProcessMap.put(btID, processID);
			ObjectOutputStream dos = new ObjectOutputStream(
					client.getOutputStream());
//			dos.writeObject(VMServer.btIDBTMap.get(btID));
//			VMServer.btIDBTMap.remove(btID);
			dis.close();
			dos.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
