package fr.imag.mescal.gloudsim.sim.vmserver;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import fr.imag.mescal.gloudsim.comm.TCPClient;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * The key class used to perform task execution, using a real program with a loop.
 * 
 * @author sdi
 *
 */
public class TaskExecutor extends Thread {

	public static int monPort;
	public static float totalExeLength;
	public static float remainLoad;
	public static Integer mutex = new Integer(0);
	public static String btID;
	public static boolean experimentMark;
	public static double init;
	
	static ServerSocket serverSocket;
	
	public static void main(String[] args)
	{
		//TODO: args should not be port number, but batch ID!!
		Initialization.load("prop.config");

		if(args.length!=4)
		{
			System.out.println("Usage: java TaskExecutor [btID] [totalExeLength] [memID] [monPort]");
			System.exit(0);
		}
		btID = args[0];
		totalExeLength = Float.parseFloat(args[1]);
		String memID = args[2];
		monPort = Integer.parseInt(args[3]);
		System.out.println("[TaskExecutor] start: btID:"+btID+";totalExeLength="+totalExeLength+";memID="+memID);

		String mark = FileControler.readFileFirstLine("exp.mark");
		experimentMark = Boolean.parseBoolean(mark);
		List<String> list = new ArrayList<String>();
		list.add(args[3]);//monPort
		FileControler.print2File(list, Initialization.cpStateDir+"/"+btID+"/run.state");

		String heapFile = Initialization.heapDir+"/"+memID+".heap";
		String s = PVFile.read(heapFile);

		new TaskExecutor().start();

		//TODO: Perform simulation as follows.
		System.out.println(btID+":[TaskExecutor]start performing simulation");
		init = System.currentTimeMillis()/1000.0;
		float tick = 0.1f;
		perfSimulation(totalExeLength,tick);
		double end = System.currentTimeMillis()/1000.0;
		float realWallClockTime = (float)(end - init);
		System.out.println(btID+":[TaskExecutor]finish performing simulation");

		if(experimentMark)
		{
			record(end);
//			PVFile.delete(VMServer.getContextFile(btID));
			List<String> runList = new ArrayList<String>();
			runList.add("done");
			String runStateFile = Initialization.cpStateDir+"/"+btID+"/run.state";
			FileControler.append2File(runList, runStateFile);
			//TODO: notify the completion of the batch task
			System.out.println(btID+":[TaskExecutor]start sending msg to mainserver "+Initialization.mainServerAddress+":"+JobEmulator.BTFinishReceiverPort);
			TCPClient tcpClient = new TCPClient(Initialization.mainServerAddress, JobEmulator.BTFinishReceiverPort);
			tcpClient.pushString(btID+" "+realWallClockTime);
			tcpClient.closeSocket();
		}

		System.out.println(btID+":[TaskExecutor]done. s.length()="+s.length());
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void record(double cur)
	{
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String vmServerName = addr.getHostName().toString();
			List<String> list = new ArrayList<String>();
			float du = (float)(cur - init);
			synchronized (mutex) {
				remainLoad = remainLoad < 0 ? 0 : remainLoad;
				list.add(du + " " + vmServerName + " " + totalExeLength + " "
						+ remainLoad);
			}
			FileControler.append2File(list, Initialization.cpStateDir
					+ "/" + btID + "/exe.log");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void perfSimulation(float totalExeLength, float tick)
	{
//		float originalLoad = bt.getWallClockLength();
		remainLoad = totalExeLength*Initialization.tuneWorkloadRatio;

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

	public void run()
	{
		Socket client = null;
		try {
			serverSocket = new ServerSocket(monPort);
			while (true) {
				System.out.println("[TaskExecutor]Start listening .");
				try {
					client = serverSocket.accept();
				} catch (SocketException e) {
					experimentMark = Boolean.parseBoolean(FileControler.readFileFirstLine("exp.mark"));
					String runStateFile = Initialization.cpStateDir+"/"+btID+"/run.state";
					monPort = Integer.parseInt(FileControler.readFileFirstLine(runStateFile));
					System.out.println("[TaskExecutor]Reinitialize socket... expMark:"+experimentMark+";monPort="+monPort);
					serverSocket = new ServerSocket(monPort);
					double cur = System.currentTimeMillis()/1000.0;
					System.out.println("init="+init+";cur="+cur);
					record(cur);
					if(!experimentMark)
						System.exit(0);
					continue;
				} finally
				{
					if(client!=null&&!client.isClosed())
					{
						client.close();
					}
				}
//				Initialization.mainServerAddress = client.getInetAddress().getHostAddress();
//				System.out.println("[TaskExecutor]invoked by: "+ Initialization.mainServerAddress);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if(client!=null&&!client.isClosed())
			{
				try {
					client.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
