package fr.imag.mescal.gloudsim.sim.vmserver;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * Deprecated!
 * @deprecated
 * @author sdi
 *
 */
public class StateSensorThread extends Thread {
	
	public void run()
	{
		try {
			ServerSocket serverSocket = new ServerSocket(
					VMServer.StateCheckerPort);
			while (true) {
				System.out.println("[StateSensorThread]Start listening.");
				Socket client = null;
				try {
					client = serverSocket.accept();
				} catch (SocketException e) {
					System.out.println("Reinitialize socket...");
					serverSocket = new ServerSocket(VMServer.StateCheckerPort);
					continue;
				}
				new SensorThread(client).run(); //Note: we can't make a new thread, otherwise one msg will cause many responses...
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

class SensorThread extends Thread
{
	private Socket client;

	public SensorThread(Socket client) {
		this.client = client;
	}
	public void run()
	{
//		String clientAddress = client.getInetAddress().getHostAddress();
		//System.out.println("[SensorThread]invoked by: "+ clientAddress);
		
		//check process states
		List<Integer> aliveProcIDList = Cmd.getRunningPID("java -XX:-UsePerfData");
		
		List<String> failBTList = new ArrayList<String>();
		Iterator iter = VMServer.btProcessMap.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 
		    String btID = (String)entry.getKey(); 
		    Integer processID = (Integer)entry.getValue();
		    if(!aliveProcIDList.contains(processID))
		    	failBTList.add(btID);
		} 
		
		//remove all fail tasks in btProcessMap
		for(int i = 0;i<failBTList.size();i++)
		{
			String btID = failBTList.get(i);
//			VMServer.btIDBTMap.remove(btID);
			List<String> states = FileControler.readFile(Initialization.cpStateDir+"/"+btID+"/run.state");
			//states==null means that the task hasn't been started at all yet.
	    	if(states==null||states.size()==2) //this means that there are two lines: the first line: monPort, and the second line is "done".
	    	{
	    		failBTList.remove(i);
	    		i--;
	    	}
	    	else
	    		VMServer.btProcessMap.remove(btID);
		}
		
		if(!failBTList.isEmpty())
			System.out.println("[SensorThread]:failBTList.size()="+failBTList.size());
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					client.getOutputStream());
			//System.out.println("[SensorThread]:writeObject failBTList");
			oos.writeObject(failBTList);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
