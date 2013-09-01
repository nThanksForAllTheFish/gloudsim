package fr.imag.mescal.gloudsim.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A convenient tool for managing files
 * @author sdi
 *
 */
public class PVFile {
    public static int number = 0;
    public PVFile(){
    }

    public static List<String> getSubFile(String path){
        List<String> list = new LinkedList<String>();
            List<String>ls = getDir(path);
            if(ls.size()>0){
                Iterator<String>it =ls.iterator();
                while(it.hasNext()){
                    String curpath=path+File.separator+it.next();
                    List<String> sublist = getSubFile(curpath);
                    list.addAll(sublist);
                }
            }else{
                ls = getFiles(path);
                if(ls.size()>0){
                    Iterator<String> it = ls.iterator();
                    while(it.hasNext()){
                        String fileName = path + File.separator + it.next();
                        list.add(fileName);
                    }
                }
            }
        return list;
    }
    
    public static List<String> getDir(String path){
        List<String>list = getFileByType(path, 1);
        return list;
    }
    
    public static List<String> getFiles(String path){
        List<String>list = getFileByType(path, 2);
        return list;
    }
    
    private static List<String> getFileByType(String path ,int type)
    {
        List<String>list = new LinkedList<String>();
        List<String>ls = getLs(path);
        if(ls.size()>0){
            Iterator<String> it = ls.iterator();
            while(it.hasNext()){
                String filename = it.next();
                File f = new File(path,filename);
                if(type==1 && f.isDirectory()) 
                    list.add(filename);
                else if(type==2 && f.isFile()) 
                    list.add(filename);
            }
        }
        return list;
    }
    
    private static List<String> getLs(String path){
        List<String>list = new LinkedList<String>();
        File dir = new File(path);
        if(dir.isDirectory()){
            String[] fileNames = dir.list();
            for(int i=0; i<fileNames.length;i++)
            {
                list.add(fileNames[i]);
            }
        }
        return list;
    }
    
    /**
     * Write a sentence to a file
     * @param filename
     * @param line
     * @throws IOException
     */
    public static void writeRec(String filename,String line) throws IOException
    {
        File output=new File(filename);
        if (!output.exists()) {
            output.createNewFile();
           }
        RandomAccessFile   raf   =   new   RandomAccessFile(output,   "rw"); 
        FileChannel   fc   =   raf.getChannel();   
        FileLock   fl   =   fc.tryLock();   
  
        if   (fl.isValid())   {   
            raf.seek(0);
            raf.writeBytes(String.valueOf(line));
            fl.release();   
        }   
        raf.close();  
    }
    
    /**
     * Read file's content
     * @param filename
     * @return
     */
    public static String read(String filename)
    {
        String line="";
        try{
            File input=new File(filename);
            if (!input.exists()) {
                input.createNewFile();
               }
            InputStreamReader read = new InputStreamReader(new FileInputStream(input),"GBK");
            BufferedReader reader=new BufferedReader(read);
            line=reader.readLine();
            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public static List<String> parseFile(String file){
        List<String>list = new LinkedList<String>();
        try{
            File input=new File(file);
            InputStreamReader read = new InputStreamReader(new FileInputStream(input),"utf-8");
            BufferedReader reader=new BufferedReader(read);
            String line=null;
            while((line=reader.readLine())!=null)
            {
                list.add(line);
            }
            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static boolean deleteFile(String fileName){     
        File file = new File(fileName);     
        if(file.isFile() && file.exists()){     
            file.delete();     
            System.out.println("Deleting File: "+fileName+" OK delete");     
            return true;     
        }else{     
            System.out.println("Deleting File: "+fileName+" Failed delete");     
            return false;     
        }     
    }     
         
    public static boolean deleteDir(String dir){     
             
        if(!dir.endsWith(File.separator)){     
            dir = dir+File.separator;     
        }     
        File dirFile = new File(dir);     
             
        if(!dirFile.exists() || !dirFile.isDirectory()){     
            System.out.println("delete dir failure: "+dir+"");     
            return false;     
        }     
        boolean flag = true;     
             
        File[] files = dirFile.listFiles();     
        for(int i=0;i<files.length;i++){                    
            if(files[i].isFile()){     
                flag = deleteFile(files[i].getAbsolutePath());     
                if(!flag){     
                    break;     
                }     
            }     

            else{     
                flag = deleteDir(files[i].getAbsolutePath());     
                if(!flag){     
                    break;     
                }     
            }     
        }     
             
        if(!flag){     
            System.out.println("failed of deleting file!");     
            return false;     
        }     
             
        if(dirFile.delete()){     
            System.out.println("deleting "+dir+" OK delte");     
            return true;     
        }else{     
            System.out.println("deleting "+dir+" failed delete");     
            return false;
        }     
    }
           
    public static void checkCreateDir(String dirPath)
    {	
		File  dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();
    }
    
    public static boolean isExist(String path)
    {
    	File file = new File(path);
    	if(file.exists())
    		return true;
    	else
    		return false;
    }
    
    public static File checkCreateFile(String filePath)
    {
		String[] s = filePath.split("/|\\\\");
		File file;
		String dirPath = "";
		for(int i = 0;i<s.length-1;i++)
		{
			dirPath+=s[i]+"/";
		}
		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();
        file = new File(filePath);        
        try {
			if (!file.exists())
				file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
    }
    
    public static void delete(String filePath)
    {
    	File file = new File(filePath);
		if(file.exists())
			if(!file.delete())
			{
				System.err.println("Failure to delete "+file.getPath());
				System.exit(0);
			}
    }
    
    /**
     * 
     * @param srcDirPath
     * @param extension
     * @param number, this is used to record how many files are already loaded.
     * @return
     */
    public static List<String> getRecursiveFiles(String srcDirPath, String extension)
    {
    	List<String> fileList = new ArrayList<String>();
    	List<String> fileNameList = PVFile.getFiles(srcDirPath);
    	Iterator<String> iter = fileNameList.iterator();
    	for(;iter.hasNext();number++)
    	{
    		String fileName = iter.next();
    		if(extension.equals(""))
    			fileList.add(srcDirPath+"/"+fileName);
    		else if(fileName.endsWith(extension))
    			fileList.add(srcDirPath +"/"+fileName);
    		if(number%4000==0)
    			System.out.println("Load "+number+" "+extension+"-files: "+srcDirPath);
    	}
    	
    	List<String> dirList = PVFile.getDir(srcDirPath);
    	Iterator<String> iter2 = dirList.iterator();
    	while(iter2.hasNext())
    	{
    		String dir = iter2.next();
    		fileList.addAll(getRecursiveFiles(srcDirPath+"/"+dir, extension));    		
    	}
    	return fileList;
    }
    
    public static long getFileLength(String filePath)
    {
    	File file = new File(filePath);
    	return file.length();
    }
    
    public static void main(String[] args)
    {
    	String path = "E:\\weka";
    	List<String> getSubFileList = getSubFile(path);
    	List<String> getDirList = getDir(path);
    	List<String> getFilesList = getFiles(path);
    	List<String> getFileByTypeList = getFileByType(path, 1);
    	List<String> getFileByTypeList2 = getFileByType(path, 2);
    	List<String> getLsList = getLs(path);
    	System.out.println("");
    	
    }
}