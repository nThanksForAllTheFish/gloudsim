package fr.imag.mescal.gloudsim.prepare;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * estimate the task restarting overhead over remote local ramdisk
 * migration type A:
 * 1. copy memory from local disk to NFS disk
 * 2. send a msg to the target host (EstRestartOverheadServer.java)
 * 3. wait for the completion notification and msg
 * @author sdi
 *
 */
public class EstRestartOverhead2 {
	public static Integer mutex = new Integer(0);
	public static int completeNotifyPort = 20000;
	public static int firstStartPort = 30000;

	public static void main(String[] args)
	{
		if(args.length!=8)
		{
			System.out.println("java EstRestartOverhead2 [targetVM] [minSize] [maxSize] [contextDir] [nfsDir] [exeLength] [sleepTick] [outputFile]");
			System.exit(0);
		}
		Initialization.needToLoadCpFiles = false;
		Initialization.load("prop.config");
		String targetVM = args[0];
		int minSize = Integer.parseInt(args[1]);
		int maxSize = Integer.parseInt(args[2]);
		String contextDir = args[3];
		String nfsDir = args[4];
		String exeLength = args[5];
		String sleepTick = args[6];
		String outputFile = args[7];
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(firstStartPort);
		} catch (Exception e) {
			e.printStackTrace();
		}

		long initLogTime = (long)(System.currentTimeMillis()/1000.0);

		for(int i = minSize;i<=maxSize;i++)
		{
			String fileName = "/cloudNFS/CheckpointSim/heapMemFiles/"+i+".heap";
			String cmdline = "cr_run java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.gloudsim.prepare.EstCPProgram ";
			cmdline+=i+" "+fileName+" "+exeLength+" "+sleepTick+" "+firstStartPort+" "+VMServer.getHostName()+" "+outputFile;
			new Cmd(cmdline.split("\\s")).start();

			try {
				Socket client = serverSocket.accept();
//				double initTimePoint = System.currentTimeMillis()/1000.0;
//				String clientAddress = client.getInetAddress()
//						.getHostAddress();
//				System.out.println("[EstCheckpointCost]invoked by: "+clientAddress);
				new TimeCounterThread2().start();
				DataInputStream dis = new DataInputStream(client.getInputStream());
				String msg = dis.readUTF();
				dis.close();
				client.close();
				String[] data = msg.split("\\s");
				String memSize = data[0];
				String processID = data[1];
				System.out.println("[EstCheckpointCost]:"+msg);

				synchronized (mutex) {
					mutex.wait();
				}
//				System.out.println("start EstCPThread...");
				new EstCPThread(memSize, contextDir, processID).start();
				Thread.sleep(15000);

//				System.out.println("killProc... processID="+processID);
				if(!Cmd.killProc(Integer.parseInt(processID)))
					System.out.println("kill process error");
				Thread.sleep(3000);
				//copy
//				System.out.println("start copying mem...");
				String srcFile = contextDir+"/context."+processID;
				String tgtDir = nfsDir;
				Cmd.move(srcFile, tgtDir);
				//notify targetVM
				TCPClient tcpClient = new TCPClient(targetVM, EstRestartOverheadServer.restartPort);
				String message = tgtDir+"/context."+processID;
				tcpClient.pushString(message);
				tcpClient.closeSocket();
				//read result
//				String replyMsg = tcpClient.readReplyMsg();
//				System.out.println(replyMsg);
				try {
					ServerSocket serverSocket2 = new ServerSocket(completeNotifyPort);
					serverSocket2.accept();
					System.out.println("complete notification received: i="+i);
					serverSocket2.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Initialization.showProgress(initLogTime, i, maxSize, fileName);
			System.out.println("cleaning /ramfs");
			String deleteCmd = "rm -rf /ramfs/*";
			Cmd.CmdExec(deleteCmd);
		}
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done.");
	}
}

class TimeCounterThread2 extends Thread
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
		synchronized (EstRestartOverhead2.mutex) {
			EstRestartOverhead2.mutex.notify();
		}
	}
}