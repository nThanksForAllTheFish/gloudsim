package fr.imag.mescal.gloudsim.prepare;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * This class is called by EstCheckpointCost.java. 
 * EstCPProgram serves as the simulated part, see perfSimulation() function.
 * Also see TaskExecutor.java.
 * @author sdi
 *
 */
public class EstCPProgram extends Thread {
	public static int port = 12345;
	public static int monPort = 22222;
	static double length;
	static float tick;
	static int sleepTimes;
	static double remainLoad;
	static double program_start;
	static double program_end;
	static float sleepTime = 18; ////3 is because of Thread.sleep(3000) in EstRestartOverhead.java, 15 is because of Thread.sleep(15000);
	static double program_midStart = 0;
	static Integer mutex = new Integer(0);
	public static void main(String[] args)
	{
		String memSize = args[0];
		String fileName = args[1];
		length = Float.parseFloat(args[2]);
		tick = Float.parseFloat(args[3]);
		port = Integer.parseInt(args[4]);
		String sourceVM = "vm1";
		//String outputFile = "/cloudNFS/CheckpointSim/remoteDiskTaskRestart.cost";
		String outputFile = "/cloudNFS/CheckpointSim/rate-"+length+"-"+sleepTimes+".cost";
		if(args.length==7)
		{
			sourceVM = args[5];
			outputFile = args[6];
		}
		if(args.length == 6)
			sleepTimes = Integer.parseInt(args[5]);
		String cmdline = "java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.gloudsim.prepare.EstCPProgram ";
		cmdline+=memSize+" "+fileName;
		int pID = Cmd.getRunningPID(cmdline).get(0);
		String s = PVFile.read(fileName);
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
		long usedMemorySize = memoryUsage.getUsed();
		float realMemSize = (float)(usedMemorySize/1024.0/1024.0); //MB
		String msg = memSize+" "+pID+" "+realMemSize;

		TCPClient c = new TCPClient("localhost", port);
		c.pushString(msg);
		c.closeSocket();
		if(args.length<6)
			new EstCPProgram().start();
		program_start = System.currentTimeMillis()/1000.0;
		if(args.length==6)
			perfSimulation(length, sleepTimes, tick);
		else
			perfSimulation(length, tick);
		program_end = (System.currentTimeMillis()/1000.0);
		float duration = (float)(program_end - program_start);
//		float totalcost = duration - length;
		float X = (float)(program_end - program_midStart);
		float R = duration - sleepTime + - X - 10; //10 means the checkpointing at 10th second.
		System.out.println("program_start="+program_start+";program_midStart="+program_midStart);
		String resultLine = "";
		if(args.length==6)
		{
			resultLine = "[cpprogram];"+memSize+";usedMem="+realMemSize+"MB;loadlength="+length+";tick="+tick+";mem.length="+s.length()+";du="+duration+";totalcost="+(duration-10f)+";R="+R;
			System.out.println(resultLine);			
		}
		else
		{
			resultLine = "[cpprogram];"+memSize+";usedMem="+realMemSize+"MB;loadlength="+length+";tick="+tick+";mem.length="+s.length()+";du="+duration+";totalcost="+(duration-103.07f)+";R="+R;
			System.out.println(resultLine);			
		}
		List<String> rList = new ArrayList<String>();
		rList.add(resultLine);
		FileControler.append2File(rList, outputFile);
//		TCPClient tcpClient = new TCPClient(sourceVM, EstRestartOverhead2.completeNotifyPort);
//		tcpClient.closeSocket();
//		System.exit(0);
	}

	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(monPort);
			System.out.println("[TaskExecutor]Start listening .");
			Socket client = null;
			try {
				client = serverSocket.accept();
			} catch (SocketException e) {
//					System.out.println("[TaskExecutor]Reinitialize socket... expMark:monPort="+monPort);
//					serverSocket = new ServerSocket(monPort);
				program_midStart = System.currentTimeMillis()/1000.0;
//				System.out.println("program_start="+program_start+";program_midStart="+program_midStart);
//					continue;
			} finally
			{
				client.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void perfSimulation(double totalExeLength, int sleepTimes, float tick)
	{
		System.out.println("executing perfSimulation: "+totalExeLength+","+sleepTimes+","+tick);
		remainLoad = totalExeLength;
		long interval = (long)(remainLoad/sleepTimes);
		int sleepInterval = (int)(tick*1000);
		for(int i = 1;i<remainLoad;i++)
		{
			if(i%interval==0)
			{
				try {
					Thread.sleep(sleepInterval);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void perfSimulation(double totalExeLength, float tick)
	{
		System.out.println("executing perfSimulation: "+totalExeLength+","+tick);
//		floa t originalLoad = bt.getWallClockLength();
		remainLoad = totalExeLength*Initialization.tuneWorkloadRatio;
		if(tick==0)
		{
			while(remainLoad>0)
			{
				synchronized (mutex) {
					remainLoad--;
				}
			}
			return;
		}
				
		long tick_ = (long)(tick*1000);
//		System.out.println("remainLoad="+remainLoad);
//		System.out.println("tick_="+tick_);
		while(remainLoad>0)
		{
			synchronized (mutex) {
				remainLoad -= tick;
			}
			try {
				Thread.sleep(tick_);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
