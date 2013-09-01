package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.sim.mainserver.JobEmulator;
import fr.imag.mescal.gloudsim.util.Initialization;
import fr.imag.mescal.gloudsim.util.PVFile;

/**
 * This is the entry point of the client end
 * @author sdi
 *
 */

public class VMServer {
	public static String vmHostName = "";
	public static int TaskEventReceiverPort = 2800;
	public static int TaskProcessIDNotifyPort = 2900; //useless!
	public static int StateCheckerPort = 3000;
	public static int RestartBTPort = 3100;
	public static int MemStateCheckerPort = 3500;
	public static Hashtable<String, Integer> btProcessMap = new Hashtable<String, Integer>();
//	public static Hashtable<String, BatchTask> btIDBTMap = new Hashtable<String, BatchTask>();
	
	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("Usage: java VMServer [configFile] [ourFormula]");
			System.out.println("Error: args.length="+args.length);
			System.exit(0);
		}
		System.out.println("[VMServer]load configuration file.");
		vmHostName = getHostName();
		System.out.println("[VMServer] current vm host = "+vmHostName);
		Initialization.load(args[0]);
		Initialization.cpOurFormula = Boolean.parseBoolean(args[1]);
		
//		System.out.println("[VMServer]start ProcessIDReceiverThread....");
//		new ProcessIDReceiverThread().start();
		
//		System.out.println("[VMServer]start TaskEventReceiverThread....");
//		new TaskEventReceiverThread().start();
		
//		System.out.println("[VMServer]start StateSensorThread....");
//		new StateSensorThread().start();
		System.out.println("[VMServer]start VMServerRestartThread.....");
		new VMServerRestartThread().start();
		System.out.println("[VMServer]start MemStateSensorThread.....");
		new MemStateSensorThread().start();
		
		try {
			ServerSocket serverSocket = new ServerSocket(
					JobEmulator.VMServerStartBTPort);
			while (true) {
				System.out.println("[VMServer]Start listening (of jobs generated).");
				Socket client = null;
				try {
					client = serverSocket.accept(); 
				} catch (SocketException e) {
					System.out.println("[VMServer]Reinitialize socket...");
					serverSocket = new ServerSocket(JobEmulator.VMServerStartBTPort); 
					continue;
				}
			
				Initialization.mainServerAddress = client.getInetAddress().getHostAddress();
				System.out.println("[VMServer]invoked by: "+ Initialization.mainServerAddress);
				
				ObjectInputStream dis = new ObjectInputStream(client.getInputStream());
				BatchTask bt = (BatchTask)dis.readObject();
//				String btID = bt.getBtID();
//				btIDBTMap.put(btID, bt);
	
				TaskExeThread vmst = new TaskExeThread(bt, true, vmHostName);				
				vmst.run(); //
				
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String buildContextFile(String contextRoot, String batchTaskID, int processID)
	{
		return contextRoot+"/"+batchTaskID+"/context."+processID;
	}
	
	/**
	 * Deprecated!
	 * @param contextRoot
	 * @param batchTaskID
	 * @return
	 */
	public static String getContextFile(String contextRoot, String batchTaskID)
	{
		List<String> fileList = PVFile.getFiles(contextRoot+"/"+batchTaskID);
		Iterator<String> iter = fileList.iterator();
		while(iter.hasNext())
		{
			String fileName = iter.next();
			if(fileName.startsWith("context"))
				return contextRoot+"/"+batchTaskID+"/"+fileName;
		}
		return null;
	}
	
	public static String getBatchTaskFile(String batchTaskID)
	{
		return Initialization.cpStateDir+"/"+batchTaskID+"/bt.obj";
	}
	
	public static String getHostName() {
	  InetAddress ia = null;
	  try {
	   ia = InetAddress.getLocalHost();
	  } catch (UnknownHostException e) {
	   e.printStackTrace();
	  } 
	  if (ia == null ) {
	   return "some error..";
	  }
	  else 
	   return ia.getHostName();
	 }
}
