package fr.imag.mescal.gloudsim.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.imag.mescal.gloudsim.elem.Job;

public class ConversionHandler {

    public static List<Double> convertDoubleArray2DoubleList(double[] data)
    {
    	List<Double> dataList = new ArrayList<Double>();
    	for(int i = 0;i<data.length;i++)
    		dataList.add(data[i]);
    	return dataList;
    }
	
	public static double[] convertArray2List4Comp(double[] countVector, List<String> countList, double offset, double x_unit)
	{
		double[] result = new double[countVector.length];
		
		int size = countVector.length;
		
		double sum = 0;
		for(int i = 0;i<size;i++)
			sum += countVector[i];
		
		for(int i = 0;i<size;i++)
		{
			String s = String.valueOf(offset+x_unit*(i+1));
			result[i] = countVector[i]/sum;
			s += " "+String.valueOf(result[i]);
			countList.add(s);
		}
		return result;
	}
	
	public static Job[] convertList2Array(List<Job> jobList)
	{
		Job[] jobArray = new Job[jobList.size()];
		Iterator<Job> iter = jobList.iterator();
		for(int i = 0;iter.hasNext();i++)
		{
			Job job = iter.next();
			jobArray[i] = job;
		}
		return jobArray;
	}
	
    public static double[][] convertDoubleLists2DoubleArrays(List<Double>[] dataList)
    {
    	int size = dataList[0].size();
    	double[][] dataArrays = new double[dataList.length][size];
    	for(int i = 0;i<dataList.length;i++)
    	{
    		Iterator<Double> iter = dataList[i].iterator();
    		for(int j = 0;iter.hasNext();j++)
    		{
    			dataArrays[i][j] = iter.next();
    		}
    	}
    	return dataArrays;
    }
    
    public static double[] convertStringList2DoubleArray(List<String> dataList, int index)
    {
    	double[] result = new double[dataList.size()];
    	Iterator<String> iter = dataList.iterator();
    	for(int i = 0;iter.hasNext();i++)
    	{
    		String[] data = iter.next().split("\\s");
    		result[i] = Double.parseDouble(data[index]);
    	}
    	return result;
    }
}
