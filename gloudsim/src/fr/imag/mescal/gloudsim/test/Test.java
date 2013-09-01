package fr.imag.mescal.gloudsim.test;

import java.net.ServerSocket;
import java.net.Socket;

public class Test {
	public static void main(String[] args)
	{
//		System.out.println("print migration state");
//		Initialization.load("prop.config");
//		Logger.printMigModeStat("mig.txt");
//		System.out.println("Simulation is done.");
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(1234);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new TestThread(serverSocket).start();
		try {
			Thread.sleep(10000);
			System.out.println("close port 1234");
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done.");
	}
}

class TestThread extends Thread
{
	ServerSocket ss;
	
	public TestThread(ServerSocket ss) {
		this.ss = ss;
	}

	public void run()
	{
		try {
			Socket client = ss.accept();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("..............");
	}
}
