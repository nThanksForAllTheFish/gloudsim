package fr.imag.mescal.gloudsim.prepare;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * 1. wait for the task restarting notification message.
 * 2. cr_retart the task over NFS disk
 * 3. notify completion to the source host
 * @author sdi
 *
 */
public class EstRestartOverheadServer {
	public static int restartPort = 12345;

	public static void main(String[] args)
	{
		Initialization.needToLoadCpFiles = false;
		Initialization.load("prop.config");
		while (true) {
			ServerSocket serverSocket = null;
			try {
				System.out.println("start restartoverhead server....");
				serverSocket = new ServerSocket(restartPort);
				Socket client = serverSocket.accept();
//				System.out.println("restart msg received....");
				DataInputStream dis = new DataInputStream(
						client.getInputStream());
				String contextFile = dis.readUTF();
//				System.out.println("contextFile=" + contextFile);
				String error = Cmd.cr_restart(contextFile);
				System.out.println("error=" + error);
				client.close();
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
