package fr.imag.mescal.gloudsim.elem;

/**
 * This is an element class used for characterizing the memory cost
 * @author sdi
 *
 */
public class MemCost implements Comparable<MemCost>
{
	private float usedMemSize;
	private int memID;
	private float cost;
	public MemCost(float usedMemSize, int memID, float cost) {
		this.usedMemSize = usedMemSize;
		this.memID = memID;
		this.cost = cost;
	}
	public float getUsedMemSize() {
		return usedMemSize;
	}
	public void setUsedMemSize(float usedMemSize) {
		this.usedMemSize = usedMemSize;
	}
	public int getMemID() {
		return memID;
	}
	public void setMemID(int memID) {
		this.memID = memID;
	}
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	public int compareTo(MemCost other)
	{
		if(this.usedMemSize<other.usedMemSize)
			return -1;
		else if(this.usedMemSize>other.usedMemSize)
			return 1;
		else
			return 0;
	}
}