package fr.imag.mescal.gloudsim.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.Device;

public class NFSControler {
	public static int NFSDevice = 0;
	public static List<Device> nfsDeviceList = new ArrayList<Device>();
	public static Hashtable<String, Device> nfsDeviceMap = new Hashtable<String, Device>(); //btID:deviceID
	
	/**
	 * generate a random nfs server (excluding the host nfs server that contains the current vm instance)
	 * @return
	 */
	public static String randomContextDevice(String curVM)
	{
		int vmID = Integer.parseInt(curVM.replace("vm", ""));
		int selfNFSID = vmID%Initialization.numOfPhyHosts;
		int nfsIndex = selfNFSID;
		while(nfsIndex==selfNFSID)
		{
			nfsIndex = RdGenerator.RAN_SeedGen7.generate_Int(0, Initialization.numOfPhyHosts-1);
			System.out.println("[NFSControler]:nfsIndex="+nfsIndex);
		}
		System.out.println("[NFSControler]:nfsIndex======="+nfsIndex);
		return Initialization.cpNFSContextDir+"/"+nfsIndex;
	}
	
	public static String randomContextDevice()
	{
		return Initialization.cpNFSContextDir+"/"+RdGenerator.RAN_SeedGen7.generate_Int(0, Initialization.numOfPhyHosts-1);
	}
	
	public static synchronized String getNextContextDevice()
	{
		int nextDevice = NFSDevice++;
		nextDevice = nextDevice%Initialization.numOfPhyHosts;
		return Initialization.cpNFSContextDir+"/"+nextDevice;
	}
	
	public static synchronized Device getLightestDevice()
	{
		Collections.sort(nfsDeviceList);
		return nfsDeviceList.get(0);
	}
}
