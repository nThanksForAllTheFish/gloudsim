package fr.imag.mescal.gloudsim.elem;

/**
 * MemState class is used to manage memory state in the course of simulation
 * @author sdi
 *
 */
public class MemState implements Comparable<MemState>
{
	private String vmHostName;
	private int vmID;
	private float checkAvailRamdiskSize;
	private float checkAvailMemSize;
	private float estimateAvailMemSize;
	
	public MemState(String vmHostName, 
			float checkAvailRamdiskSize, float checkAvailMemSize, 
			float estimateAvailMemSize) {
		this.vmHostName = vmHostName;
		vmID = Integer.parseInt(vmHostName.replace("vm", ""));
		this.checkAvailRamdiskSize = checkAvailRamdiskSize;
		this.checkAvailMemSize = checkAvailMemSize;
		this.estimateAvailMemSize = estimateAvailMemSize;
	}
	public synchronized String getVmHostName() {
		return vmHostName;
	}

	public synchronized float getCheckAvailRamdiskSize() {
		return checkAvailRamdiskSize;
	}
	public synchronized void setCheckAvailRamdiskSize(float checkAvailRamdiskSize) {
		this.checkAvailRamdiskSize = checkAvailRamdiskSize;
	}
	public synchronized float getCheckAvailMemSize() {
		return checkAvailMemSize;
	}
	public synchronized void setCheckAvailMemSize(float checkAvailMemSize) {
		this.checkAvailMemSize = checkAvailMemSize;
	}

	public synchronized float getEstimateAvailMemSize() {
		return estimateAvailMemSize;
	}
	public synchronized void setEstimateAvailMemSize(float estimateAvailMemSize) {
		this.estimateAvailMemSize = estimateAvailMemSize;
	}
	public synchronized float getMinSize()
	{
		float check = Math.min(checkAvailRamdiskSize, checkAvailMemSize);
		return Math.min(check, estimateAvailMemSize);
	}
	public int getVmID() {
		return vmID;
	}
	public int compareTo(MemState ms)
	{
		if(this.getMinSize()>ms.getMinSize())
			return -1;
		else if(this.getMinSize()<ms.getMinSize())
			return 1;
		else
			return 0;
	}
	public String toString()
	{
		return vmHostName+":"+this.getMinSize();
	}
}
