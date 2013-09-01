package fr.imag.mescal.gloudsim.util;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import fr.imag.mescal.gloudsim.sim.mainserver.MemStateChecker;
import fr.imag.mescal.gloudsim.sim.vmserver.VMServer;

/**
 * a useful toolkit to call external command by java program.
 * For example, linux commands like ls, ps, ifconfig; and XEN's command like xm create .... 
 * @author sdi
 *
 */
public class Cmd extends Thread
{
	public static String btExecutorTmp = "cr_run java -XX:-UsePerfData -Xmx400m -cp lib/cpsim.jar fr.imag.mescal.gloudsim.sim.vmserver.TaskExecutor";
	//public static String cp_NFS_CMD;
	public static String cp_Local_CMD;
	public static String rst_CMD;
    String[] threadInput;
    
	public Cmd(String[] input)
    {
		threadInput = input;
    }
	
	public Cmd(String input)
	{
		threadInput = input.split("\\s");
	}
	
//	public Cmd(String logFile, String[] input)
//	{
//		this.logFile = logFile;
//		threadInput = input;
//	}
	
	public static int CmdExec(String cmdline)
	{
		//System.out.println("CmdExec Command: "+cmdline);
		String[] cmd = cmdline.split("\\s");
		StringOutputStream sos = new StringOutputStream();
		StringOutputStream ses = new StringOutputStream();
		Cmd.CmdExec(cmd, sos, ses);
		if(!sos.getString().equals(""))
			System.out.println(sos.getString());
		if(!ses.getString().equals(""))
		{
			System.out.println(ses.getString());
			return 1;
		}
		else
			return 0;
	}
	
	public static String CmdExec(String[] cmd)
	{
		StringOutputStream sos = new StringOutputStream();
		StringOutputStream ses = new StringOutputStream();
		Cmd.CmdExec(cmd, sos, ses);
		if(!sos.getString().equals(""))
			System.out.println(sos.getString());
		return ses.getString();		
	}
	
    public static int CmdExec(String cmdline, StringOutputStream sos)
    {
		//System.out.println("CmdExec Command: "+cmdline);
		String[] cmd = cmdline.split("\\s");
		StringOutputStream ses = new StringOutputStream();
		Cmd.CmdExec(cmd, sos, ses);
		if(!ses.getString().equals(""))
		{
			System.out.println(ses.getString());
			return 1;
		}
		return 0;
    }
    
    public static int CmdExec(String cmdline[], OutputStream os, OutputStream es)
    {
        try {
            Process p;
            
           // System.out.println("Executing "+cmdline[0]);
            p = Runtime.getRuntime().exec(cmdline);
	        if(os != null&&es!=null)
	        {
	            BufferedInputStream input = new BufferedInputStream(p.getInputStream());
	            BufferedInputStream error = new BufferedInputStream(p.getErrorStream());
	            BufferedOutputStream output = new BufferedOutputStream(os);
	            BufferedOutputStream errorOutput = new BufferedOutputStream(es);
	            byte b[] = new byte[1024];
	            for(int bytesRead = input.read(b); bytesRead != -1; bytesRead = input.read(b))
	                output.write(b, 0, bytesRead);
	
	            byte c[] = new byte[1024];
	            for(int bytesRead = error.read(c); bytesRead != -1; bytesRead = error.read(c))
	                errorOutput.write(c, 0, bytesRead);
	            
	            output.close();
	            errorOutput.close();
	            input.close();
	        } else {
	            System.out.println("Getting input");	        	
	        	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        	String line;
	        	String lineError;
	            System.out.println("Getting line");	        	
	        	while((line = input.readLine()) != null) { 
	        		System.out.println(line);
	        	}
	        	System.out.println("Getting error");
	        	while((lineError = error.readLine()) != null) { 
	        		System.out.println(lineError);
	        	}
	            System.out.println("closing");	        	
	        	input.close();	        	
	        }
	        p.waitFor();
	        return p.exitValue();
		} catch (IOException e) {
			e.printStackTrace();
			return 2;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 2;
		}

    }
    
    public static int CmdExec(String cmdline[], OutputStream os)
    {

        try {
            Process p;

            System.out.println("Executing "+cmdline[0]);
            p = Runtime.getRuntime().exec(cmdline);
	        if(os != null)
	        {
	            BufferedInputStream input = new BufferedInputStream(p.getInputStream());
	            BufferedOutputStream output = new BufferedOutputStream(os);
	            byte b[] = new byte[1024];
	            for(int bytesRead = input.read(b); bytesRead != -1; bytesRead = input.read(b))
	                output.write(b, 0, bytesRead);
	
	            output.close();
	            input.close();
	        } else {
	            System.out.println("Getting input");	        	
	        	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        	String line;
	            System.out.println("Getting line");	        	
	        	while((line = input.readLine()) != null) { 
	        		System.out.println(line);
	        	}
	            System.out.println("closing");	        	
	        	input.close();	        	
	        }
	        p.waitFor();
	        return p.exitValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 2;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 2;
		}
    }

    public static int copy(String srcFile, String tgtDir)
    {
    	String cmd = "cp "+srcFile+" "+tgtDir;
    	//System.out.println("[Cmd]:"+cmd);
    	int state = CmdExec(cmd);
    	return state;
    }
    
    /**
     * 
     * @param srcFile
     * @param tgtDir
     * @return 0 means normal state ; 1 means exception
     */
    public static int move(String srcFile, String tgtDir)
    {
    	PVFile.checkCreateDir(tgtDir);
      	String cmd = "mv "+srcFile+" "+tgtDir;
    	//System.out.println("[Cmd]:"+cmd);
    	int state = CmdExec(cmd);
    	return state;    	
    }
    
    public static String cr_checkpoint(String pid)
    {
    	String cmd = "cr_checkpoint --pid "+pid;
    	//System.out.println("[Cmd]:"+cmd);
    	String[] input = cmd.split("\\s");
        StringOutputStream sos = new StringOutputStream();
        CmdExec(input, sos);
        return "context."+String.valueOf(pid);
    }
    
    public static String cr_checkpoint_specDir(String dir, String pid)
    {
    	String cmd = "cr_checkpoint --pid "+pid+" -f "+dir+"/context."+pid;
    	//System.out.println("[Cmd]:"+cmd);
    	String[] input = cmd.split("\\s");
        CmdExec(input);
        return "context."+String.valueOf(pid);
    }
    
    public static String cr_checkpoint_kill(String pid)
    {
    	String cmd = "cr_checkpoint --pid "+pid+" --kill";
    	String[] input = cmd.split("\\s");
        StringOutputStream sos = new StringOutputStream();
        CmdExec(input, sos);
        return "context."+String.valueOf(pid); 	
    }
    
    /**
     * 
     * @param btID
     * @param pid
     * @param nfsDevice is the device to be NFS ? Yes or no otherwise
     * @return
     */
    public static String cr_checkpoint(String btID, String cpNFSContextFile, int pid, boolean nfsDevice)
    {
    	String cmd = null;
    	if(nfsDevice)
    	{
    		String[] s = cpNFSContextFile.split("/"); // "/localfs/contextNFS/2/3-0/context.pid"
    		String cpNFSDevice = Initialization.cpNFSContextDir+"/"+s[3];
        	PVFile.checkCreateDir(cpNFSDevice+"/"+btID);
        	String cp_NFS_CMD = Initialization.getCP_NFS_CMD(cpNFSDevice);
        	cmd = cp_NFS_CMD.replace("PID", String.valueOf(pid)).replace("BTID", btID); 
    	}
    	else
    	{
        	PVFile.checkCreateDir(Initialization.cpLocalContextDir+"/"+btID);
        	cmd = cp_Local_CMD.replace("PID", String.valueOf(pid)).replace("BTID", btID);
    	}

    	//System.out.println("cr_checkpoint:cmd="+cmd);
        int state = CmdExec(cmd);
        if(state==1)
        	return null;
        else
        	return "context."+String.valueOf(pid);
    }
    
    public static String cr_restart(String contextFile)
    {	
		String cmd = rst_CMD.replace("CONTEXT", contextFile);
    	//System.out.println("cr_restart:cmd="+cmd);
    	String[] input = cmd.split("\\s");
        String error = CmdExec(input);        
        return error;
    }
    
    /**
     * 
     * @param pid
     * @return boolean: true: success ; false: failed (i.e., there was no such a process)
     */
    public static boolean killProc(int pid)
    {
    	String cmd = "kill -9 "+pid;
    	System.out.println(VMServer.vmHostName+":killProcess:cmd="+cmd);
    	String[] input = cmd.split("\\s");
        StringOutputStream sos = new StringOutputStream();
        StringOutputStream error = new StringOutputStream();
        CmdExec(input, sos, error);
        if(error.getString().equals(""))
        	return true;
        else
        {
        	System.out.println("[Cmd:killProc]error.getString()="+error.getString());
        	return false;
        }
    }
    
    public static int getExeProcessID(String[] args)
    {
    	String argString = "";
    	for(String s:args)
    		argString+=s+" ";
    	String keyword = Cmd.btExecutorTmp.replace("cr_run", "")+" "+argString.trim();
    	List<Integer> pidList = getRunningPID(keyword.trim());
    	if(pidList.isEmpty())
    		return -1;
    	else
    		return pidList.get(0);
    }
    
    public static List<Integer> getRunningPID(String keyWord)
    {
    	List<Integer> pidList = new ArrayList<Integer>();
    	String input[] = {
    			"ps", "aux"//, "|", "grep", "[j]ava -XX:-UsePerfData"//, "|", "awk '{print $2}'"
    	};
        StringOutputStream sos = new StringOutputStream();
        StringOutputStream ses = new StringOutputStream();
        CmdExec(input, sos, ses);
//        System.out.println(sos.getString());
//        System.out.println("keyword====="+keyWord);
        if(!ses.getString().equals(""))
        	System.out.println(ses.getString());
        String[] s = sos.getString().split("\\n");
        for(int i = 0;i<s.length;i++)
        {
        	if(s[i].contains(keyWord))
        	{
        		String[] data = s[i].split("\\s+");
        		pidList.add(new Integer(data[1]));
        	}
        }
        
        return pidList;
    }
    
    public void run()
    {
    	String errMsg = Cmd.CmdExec(threadInput);
		if(errMsg!="")
			System.out.println("[Cmd]error"+errMsg);
			
    }
    
    /**
     * 
     * @param expectedMemSize
     * @return  the value is restSize
     */
    public static float getRemainigRamfskMemSize()
    {
		StringOutputStream sos = new StringOutputStream();
    	Cmd.CmdExec("df -k", sos);
        String[] s = sos.getString().split("\\n");
        for(int i = 0;i<s.length;i++)
        {
        	if(s[i].contains("tmpfs"))
        	{
        		String[] data = s[i].split("\\s+");
        		int restSize = (int)(Long.parseLong(data[3])/1000-MemStateChecker.reservedSize); //10MB
        		System.out.println("[Cmd]rest ramdisk Size = "+restSize);
        		return restSize;
        	}
        }
        return -1;
    }
    
    public static float getRemainingMemSize()
    {
		StringOutputStream sos = new StringOutputStream();
    	Cmd.CmdExec("free -m", sos);
        String[] s = sos.getString().split("\\n");
        for(int i = 0;i<s.length;i++)
        {
        	if(s[i].contains("buffers/cache:"))
        	{
        		String[] data = s[i].split("\\s+");
        		int restSize = (int)(Float.parseFloat(data[3]));//-MemStateChecker.reservedSize); //reserve 10MB
        		System.out.println("[Cmd]rest mem Size = "+restSize);
        		return restSize;
        	}
        }
        return -1;
    }
    
    public static void main(String args[])
    {
//        String input[] = {
//        	"ps", "sched-credit", "-d", "vm8", "-c", "0", "-w", "500", "xm", ";", "sched-credit", "-d", "vm16", "-c", "0", "-w", "500"
//        };
    	String input[] = {
    			"jps"
    	};
//    	String input2[] = {
//    			"ipconfig"
//    	};
        StringOutputStream sos = new StringOutputStream();
        CmdExec(input, sos);
//        CmdExec(input2, sos);
        System.out.println(sos.getString());
    }
}