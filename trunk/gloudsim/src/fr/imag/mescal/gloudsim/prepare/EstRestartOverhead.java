package fr.imag.mescal.gloudsim.prepare;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * estimate the task restarting overhead over NFS
 * migration type B (as for migration type, see my SC'13 paper)
 * @author sdi
 *
 */
public class EstRestartOverhead {
	public static Integer mutex = new Integer(0);

	public static void main(String[] args)
	{
		if(args.length!=6)
		{
			System.out.println("java EstRestartOverhead [minSize] [maxSize] [contextDir] [exeLength] [sleepTick] [times] [port]");
			System.exit(0);
		}
		Initialization.needToLoadCpFiles = false;
		Initialization.load("prop.config");
		int minSize = Integer.parseInt(args[0]);
		int maxSize = Integer.parseInt(args[1]);
		String contextDir = args[2];
		String exeLength = args[3];
		String sleepTick = args[4];
		int port = Integer.parseInt(args[5]);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}

		long initLogTime = (long)(System.currentTimeMillis()/1000.0);

		for(int i = minSize;i<=maxSize;i++)
		{
			String fileName = "/cloudNFS/CheckpointSim/heapMemFiles/"+i+".heap";
			String cmdline = "cr_run java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.gloudsim.prepare.EstCPProgram ";
			cmdline+=i+" "+fileName+" "+exeLength+" "+sleepTick+" "+port;
			new Cmd(cmdline.split("\\s")).start();

			try {
				Socket client = serverSocket.accept();
//				double initTimePoint = System.currentTimeMillis()/1000.0;
//				String clientAddress = client.getInetAddress()
//						.getHostAddress();
//				System.out.println("[EstCheckpointCost]invoked by: "+clientAddress);
				new TimeCounterThread().start();
				DataInputStream dis = new DataInputStream(client.getInputStream());
				String msg = dis.readUTF();
				String[] data = msg.split("\\s");
				String memSize = data[0];
				String processID = data[1];
				System.out.println("[EstCheckpointCost]:"+msg);

				synchronized (mutex) {
					mutex.wait();
				}
				new EstCPThread(memSize, contextDir, processID).start();
				Thread.sleep(15000);

				if(!Cmd.killProc(Integer.parseInt(processID)))
					System.out.println("kill process error");
				Thread.sleep(3000);

				String error = Cmd.cr_restart(contextDir+"/context."+processID);
				System.out.println("error="+error);
				
				dis.close();
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			Initialization.showProgress(initLogTime, i, maxSize, fileName);
		}
		System.out.println("done.");
	}
}

class TimeCounterThread extends Thread
{

	public void run()
	{
		System.out.println("start sleeping 10 seconds.");
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end sleeping 10 seconds.");
		synchronized (EstRestartOverhead.mutex) {
			EstRestartOverhead.mutex.notify();
		}
	}
}