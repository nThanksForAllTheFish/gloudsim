package fr.imag.mescal.gloudsim.prepare;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import fr.imag.mescal.gloudsim.util.Cmd;
import fr.imag.mescal.gloudsim.util.FileControler;
import fr.imag.mescal.gloudsim.util.Initialization;

/**
 * This is the key class to characterize and compute the checkpoint cost.
 * Simulated tasks (see vmserver.TaskExecutor.java) are generated using an real amount of simulated memories.
 * At the end of the task execution, it will load a file to simulate the memory occupation. 
 * Note that the real memory usage and the size of the loaded file are different, so it's necessary to build a mapping between them.
 * @author sdi
 *
 */
public class EstCheckpointCost {

	public static int port = 12345;
	public static Hashtable<String, List<Float>> memSizePIDMap = new Hashtable<String, List<Float>>();
	public static Hashtable<String, Float> rMemSizeMap = new Hashtable<String, Float>();
	public static void main(String[] args)
	{
		if(args.length<8){
			System.out.println("Usage: java EstCheckpointCost [minSize] [maxSize] [contextDir] [outputFile] [exeLength] [sleepTick] [# of checkpoints]");
			System.out.println("example: java EstCheckpointCost 1 35 /tmp cpCost.sam 100 0.1 5 12345");
			System.exit(0);
		}
		int minSize = Integer.parseInt(args[0]);
		int maxSize = Integer.parseInt(args[1]);
		String contextDir = args[2];
		String outputFile = args[3];
		String exeLength = args[4];
		String sleepTick = args[5];
		int times = Integer.parseInt(args[6]);
		port = Integer.parseInt(args[7]);
		int sleepTimes = -1;
		if(args.length>=9)
			sleepTimes = Integer.parseInt(args[8]);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("estimate the pure computation time....");

		long initLogTime = (long)(System.currentTimeMillis()/1000.0);
		for(int i = minSize;i<=maxSize;i++)
		{
			String fileName = "/cloudNFS/CheckpointSim/heapMemFiles/"+i+".heap";
			String cmdline = "cr_run java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.gloudsim.prepare.EstCPProgram ";
			cmdline+=i+" "+fileName+" "+exeLength+" "+sleepTick+" "+port+" "+sleepTimes;
			System.out.println("cmdline="+cmdline);
			new Cmd(cmdline.split("\\s")).start();

			try {
				Socket client = serverSocket.accept();
				String clientAddress = client.getInetAddress()
						.getHostAddress();
				System.out.println("[EstCheckpointCost]invoked by: "+clientAddress);
				DataInputStream dis = new DataInputStream(client.getInputStream());
				String msg = dis.readUTF();
				dis.close();
				client.close();
				String[] data = msg.split("\\s");
				String memSize = data[0];
				String processID = data[1];
				Float realMemSize = Float.valueOf(data[2]);
				rMemSizeMap.put(memSize, realMemSize);
				System.out.println("[EstCheckpointCost]:"+msg);
				for(int j = 0;j<times;j++)//times=8
				{
					new EstCPThread(memSize, contextDir, processID).start();
					if(args.length<9)
						Thread.sleep(15000);
					else
						Thread.sleep(5000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(args.length<9&&i%10==0)
				Initialization.showProgress(initLogTime, i, maxSize, fileName);
		}

		System.out.println("begin writing results to a file.....");
		List<Cost> resultList = new ArrayList<Cost>();
		Iterator iter = memSizePIDMap.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String memSize = (String)entry.getKey();
		    List<Float> costList = (List<Float>)entry.getValue();
		    float sumCost = 0;
		    for(int k = 0;k<costList.size();k++)
		    	sumCost += costList.get(k);
		    float meanCost = sumCost / costList.size();
		    Cost cost = new Cost(Integer.parseInt(memSize), rMemSizeMap.get(memSize), meanCost, costList);
		    System.out.println(cost);
		    resultList.add(cost);
		}
		Collections.sort(resultList);
		FileControler.print2File(resultList, outputFile);
		System.out.println("done.");
//		System.exit(0);
	}
}

class Cost implements Comparable<Cost>
{
	private int memSize;
	private float meanCost;
	private float rMemSize;
	private List<Float> costList;

	public Cost(int memSize, float rMemSize, float meanCost, List<Float> costList) {
		this.memSize = memSize;
		this.rMemSize = rMemSize;
		this.meanCost = meanCost;
		this.costList = costList;
	}

	public int compareTo(Cost other)
	{
		if(this.memSize<other.memSize)
			return -1;
		else if(this.memSize>other.memSize)
			return 1;
		else
			return 0;
	}

	public String toString()
	{

		String s = memSize+":"+rMemSize+":"+meanCost+":";

		String costString = "";
		for(int k = 0;k<costList.size();k++)
	    	s+=costList.get(k)+" ";
	    s=s+costString.trim();
		return s;
	}


}

class EstCPThread extends Thread
{
	private String contextDir;
	private String processID;
	private String memSize;

	public EstCPThread(String memSize, String contextDir, String processID) {
		this.memSize = memSize;
		this.contextDir = contextDir;
		this.processID = processID;
	}

	public void run()
	{
		double start = System.currentTimeMillis()/1000.0;
		Cmd.cr_checkpoint_specDir(contextDir, processID);
		double end = System.currentTimeMillis()/1000.0;
		Float duration = Float.valueOf((float)(end - start));
		List<Float> costList = EstCheckpointCost.memSizePIDMap.get(memSize);
		if(costList==null)
		{
			costList = new ArrayList<Float>();
			EstCheckpointCost.memSizePIDMap.put(memSize, costList);
		}
		costList.add(duration);
	}
}
