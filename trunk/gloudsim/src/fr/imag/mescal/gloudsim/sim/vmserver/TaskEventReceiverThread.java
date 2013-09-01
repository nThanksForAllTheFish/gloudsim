package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * Every time a task (not batch-task) is done, an event will be delivered.
 * Task Event Receiver Thread is used to receive such an event.
 * For example, task failure or the completion of the whole batch task.
 *
 * OUTDATED and Useless!!
 * This is because the TaskExecutor should directly connect MainServer
 * @deprecated
 * @author sdi
 *
 */
public class TaskEventReceiverThread extends Thread {

	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(
					VMServer.TaskEventReceiverPort);
			while (true) {
				System.out.println(VMServer.vmHostName+":"+"[TaskEventReceiverThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
				} catch (SocketException e) {
					System.out.println(VMServer.vmHostName+":"+"[TaskEventReceiverThread]Reinitialize socket...");
					serverSocket = new ServerSocket(VMServer.TaskEventReceiverPort);
					continue;
				}
				new ReceiverThread(client).run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ReceiverThread extends Thread
{
	private Socket client;

	public ReceiverThread(Socket client) {
		this.client = client;
	}

	public void run()
	{
		String clientAddress = client.getInetAddress().getHostAddress();
		System.out.println("[TaskEventReceiverThread]invoked by: "+ clientAddress);

		try {
			DataInputStream dis = new DataInputStream(client.getInputStream());
			String msg = dis.readUTF();
			//TODO: the whole task is finished, then, notify the main server.
			TCPClient tcpClient = new TCPClient(
					Initialization.mainServerAddress,
					JobEmulator.BTFinishReceiverPort);
			tcpClient.pushString(msg);
			tcpClient.closeSocket();
			dis.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//class KillThread extends Thread
//{
//	private int processID;
//
//	public KillThread(int processID) {
//		this.processID = processID;
//	}
//
//	public void run()
//	{
//		Cmd.killProc(processID);
//	}
//}
