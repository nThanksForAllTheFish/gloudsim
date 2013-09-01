package fr.imag.mescal.gloudsim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.mescal.gloudsim.elem.BatchTask;
import fr.imag.mescal.gloudsim.elem.JobTrace;

public class FileControler {

	/**
	 * Note that the FileWriter must be instantiated with "true" argument.
	 * Example: FileWriter fw = new FileWriter(filePath, true); BufferedWriter
	 * bw = new BufferedWriter(fw); append2File(line,bw); bw.close();
	 * 
	 * @param line
	 * @param fw
	 * @throws IOException
	 */
	public static void append2File(String line, BufferedWriter bw)
			throws IOException {
		bw.append(line);
	}

	public static File print2File(List list, String filePath) {
		File file = PVFile.checkCreateFile(filePath);
		try {
			FileWriter fw = new FileWriter(filePath);
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator it = list.iterator();
			while (it.hasNext()) {
				String xyz = it.next().toString();
				bw.write(xyz);
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static File print2File(Map map, String filePath) {
		File file = PVFile.checkCreateFile(filePath);
		try {
			FileWriter fw = new FileWriter(filePath);
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object val = entry.getValue();
				bw.write(val.toString());
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void append2File(List<String> list, String filePath) {
		PVFile.checkCreateFile(filePath);
		try {
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator<String> it = list.iterator();
			while (it.hasNext()) {
				String xyz = it.next();
				bw.write(xyz);
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param readResult
	 * @param fileName: format: key value
	 */
	public static Map<String, String> readFile2Map(String fileName) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				String[] s = line.split("\\s");
				String key = s[0];
				String value = s[1];
				map.put(key, value);
			}
			in.close();
			fr.close();
			return map;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}

	public static Map<String, String> readFile2Map(String fileName, String splitter) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				String[] s = line.split(splitter);
				String key = s[0];
				String value = s[1];
				map.put(key, value);
			}
			in.close();
			fr.close();
			return map;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}
	
	/**
	 * 
	 * @param readResult
	 * @param fileName: format: key value
	 */
	public static Map<String, Integer> readFile2MapInteger(String filePath) {
		PVFile.checkCreateFile(filePath);
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				String[] s = line.split("\\s");
				String key = s[0];
				Integer value = Integer.parseInt(s[1]);
				map.put(key, value);
			}
			in.close();
			fr.close();
			return map;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}
	
	public static Map<String, Integer> readFile2MapInteger(String filePath, int i, int j) {
		PVFile.checkCreateFile(filePath);
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				String[] s = line.split("\\s");
				String key = s[i];
				Integer value = Integer.parseInt(s[j]);
				map.put(key, value);
			}
			in.close();
			fr.close();
			return map;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}
	
	/**
	 * 
	 * @param readResult
	 * @param fileName
	 */
	public static List<String> readFile(String fileName) {
		if(!PVFile.isExist(fileName))
			return null;
		List<String> readResult = new ArrayList<String>();
		// try-catch block is used to catch any possible exception when
		// executing this program,
		// such as maybe the file named fileName doesn't exist at all.
		try {
			// FileReader is a class extendting InputStreamReader, and
			// InputStreamReader extends Reader.
			// Reader is the argument of RufferedReader(Reader in).
			// # FileReader() is a connection stream for characters, that
			// connects to a text file.
			// # BufferedReader can be viewed as a buffer used for higher
			// efficiency.
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				readResult.add(line);
			}
			in.close();
			fr.close();
			return readResult;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}
	
	public static List<String> readFileSpecIndex(String fileName, int index) {
		if(!PVFile.isExist(fileName))
			return null;
		List<String> readResult = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			while ((line = in.readLine()) != null) {
				// concatenate lines
				readResult.add(line.split("\\s")[index]);
			}
			in.close();
			fr.close();
			return readResult;
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}

	public static String readFileFirstLine(String fileName) {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			String line;
			// read the text one line after another line, until no more line to
			// be read.
			if ((line = in.readLine()) != null) {
				// concatenate lines
				in.close();
				fr.close();
				return line;
			}
			in.close();
			fr.close();
		} catch (Exception e) {
			// as long as JVM encounters an exception when executing the
			// program,
			// this catch(){} will catch it and do something.
			System.err.print(e);
		}
		return null;
	}

	public static String screenInput(String prompt) {
		System.out.println(prompt);
		byte[] b = new byte[100];
		try {
			System.in.read(b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String s = "";
		s = new String(b);
		return s;
	}
	
	public static void writeObject2File(JobTrace jobTrace, String outputFile)
	{
		PVFile.checkCreateFile(outputFile);
		try {
			FileOutputStream fout = new FileOutputStream(outputFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(jobTrace);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeObject2File(BatchTask batchTask, String outputFile)
	{
		PVFile.checkCreateFile(outputFile);
		try {
			FileOutputStream fout = new FileOutputStream(outputFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(batchTask);
			fout.flush();
			oos.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static JobTrace loadJobTraceFromFile(String srcObjFile)
	{
		try {
			FileInputStream fin = new FileInputStream(srcObjFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			return (JobTrace) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static BatchTask loadBatchTaskFromFile(String srcObjFile)
	{
		try {
			FileInputStream fin = new FileInputStream(srcObjFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			return (BatchTask) ois.readObject();
		} catch (Exception e) {
			System.out.println("reload batchtask: srcObjFile="+srcObjFile);
		}
		return null;
	}
	
	/**
	 * 
	 * @param targetFile
	 * @param size MB
	 */
	public static void genFileWithSize(String targetFile, int size)
	{
		try {
			FileWriter fw = new FileWriter(targetFile, true);
			PrintWriter pw = new PrintWriter(fw);
			int lager = size * 1024 * 1024 ; 
			for (int i = 0; i < lager; i++) {
				String s = String.valueOf(RdGenerator.RAN_SeedGen2.generate_Int(0, 9));
				pw.print(s);
			}
			pw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		JobTrace jt = null;
//		writeObject2File(jt, "abc/test.trace");
		jt = loadJobTraceFromFile("abc/test.trace");
		System.out.println("done.");
	}
}
