package fr.imag.mescal.gloudsim.elem;

/**
 * NFS Device, used by NFS controller
 * @author sdi
 *
 */
public class Device implements Comparable<Device> {
	int ID;
	int runningBTNum = 0;
	
	public Device(int iD) {
		ID = iD;
	}

	public int getID() {
		return ID;
	}

	public synchronized int getRunningBTNum() {
		return runningBTNum;
	}

	public synchronized void setRunningBTNum(int runningBTNum) {
		this.runningBTNum = runningBTNum;
	}

	public int compareTo(Device device)
	{
		if(this.runningBTNum<device.runningBTNum)
			return -1;
		else if(this.runningBTNum>device.runningBTNum)
			return 1;
		else
			return 0;
	}
}
