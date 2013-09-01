package fr.imag.mescal.gloudsim.comm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.BatchTask;
/**
 * TCPClient class is used to open socket for communication between two participants like server and client. 
 * @author sdi
 *
 */
public class TCPClient{
	Socket socket; 
	
	public TCPClient(String server, int port)
	{
		try {
			socket = new Socket(server, port);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception Error: server="+server+";port="+port);
		}
	}
	
	public TCPClient(Socket socket)
	{
		this.socket = socket;
	}
	
	public void closeSocket()
	{
		try {
			this.socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BatchTask readBatchTask()
	{
		try {
			ObjectInputStream dis = new ObjectInputStream(
					socket.getInputStream());
			BatchTask btStateMap = (BatchTask) dis.readObject();
			return btStateMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void pushBatchTask(BatchTask batchTask)
	{	
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					socket.getOutputStream());
			oos.writeObject(batchTask);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pushString(String content)
	{
		try {
			DataOutputStream dos = new DataOutputStream(
					socket.getOutputStream());
			dos.writeUTF(content);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pushInt(int content)
	{
		try {
			DataOutputStream dos = new DataOutputStream(
					socket.getOutputStream());
			dos.writeInt(content);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> readFailBTList()
	{
		try {
			ObjectInputStream dis = new ObjectInputStream(
					socket.getInputStream());
			List<String> failBTList = (List<String>) dis.readObject();
			return failBTList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String readReplyMsg()
	{
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			return dis.readUTF();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isConnected()
	{
		return socket.isConnected() && !socket.isClosed();
	}
}