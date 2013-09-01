package fr.imag.mescal.gloudsim.gnuplot;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import fr.imag.mescal.gloudsim.util.ConversionHandler;

/**
 * used by GnuplotComputeDistribution
 * @author sdi
 *
 */
public class GnuplotDistribution {	
	
	public static DecimalFormat df = new DecimalFormat("#.######");
	
	public static double[] genDistribution(double[] rawData, int scale, double min, double max)
	{
		int distrIntervals = scale;
		double unit = (max-min)/distrIntervals;
		double[] discreteData = rawData;
		List<Double> discList = ConversionHandler.convertDoubleArray2DoubleList(discreteData);
		Collections.sort(discList); 
		double[] distResult = new double[distrIntervals];
		Iterator<Double> iter = discList.iterator();
		while(iter.hasNext())
		{
			double sucRate = iter.next();
			int index = (int)(sucRate/unit);
			if(index == distrIntervals)
				index --;
			if(index<0)
			{					
				System.out.println("index<0: index="+index+"; sucRate="+sucRate+"; unit="+unit);
				continue;
			}
			distResult[index]++;
		}
		return distResult;
	}
	
	/**
	 * 
	 * @param rawData the data to be input used to generate the distribution function
	 * @param result the returned value
	 * @param scale the number of the sample points to be drawn, if scale == -1, print all sample points
	 * @return null
	 */
	public static void genDistribution(double[][] rawData, List<String>[] result, int scale)
	{
		int m = rawData.length;
		int distrIntervals = scale;
		
		for(int i =0;i<m;i++)
		{
			double[] discreteData = rawData[i];
			List<Double> discList = ConversionHandler.convertDoubleArray2DoubleList(discreteData);
			Collections.sort(discList); 
			double[] distResult = new double[distrIntervals];
			double[] minmax = GnuplotDistribution.getMinMax(discList);
			double unit = (minmax[1]-minmax[0])/distrIntervals;
			Iterator<Double> iter = discList.iterator();
			while(iter.hasNext())
			{
				double value = iter.next();
				if(GnuplotComputeDistribution.maxValue>0&&value>GnuplotComputeDistribution.maxValue)
					continue;
				int index = (int)((value-minmax[0])/unit);
				if(index == distrIntervals)
					index --;
				distResult[index]++;
			}
			ConversionHandler.convertArray2List4Comp(distResult, result[i], minmax[0], unit);
		}
	}
	
	public static double[] getMinAvgMax(double[] data)
	{
		double max = 0;
		double total = 0;
		double min = data[0];
		for(int i =0;i<data.length;i++)
		{
			double value = data[i];
			if(max<value)
				max = value;
			if(min>value)
				min = value;
			total+=value;
		}
		double avg = total/data.length;
		double[] result = new double[3];
		result[0] = min;
		result[1] = avg;
		result[2] = max;
		
		return result;
	}
	
	public static double[] getMinMax(List<Double> valueList)
	{
		double min = 10000000000.0;
		double max = 0;
		Iterator<Double> iter = valueList.iterator();
		while(iter.hasNext())
		{
			double value = iter.next();
//			if(value>100)
//				break;
			if(min > value)
				min = value;
			if(max < value)
				max = value;
		}
		if(GnuplotComputeDistribution.maxValue==-1)
			return new double[]{min, max};
		else
			return new double[]{min, GnuplotComputeDistribution.maxValue};
	}
	
	public static double[] getMinMax(double[] data)
	{
		double min = data[0];
		double max = 0;
		for(int i = 0;i<data.length;i++)
		{
			double value = data[i];
			if(min>value)
				min = value;
			if(max<value)
				max = value;
		}
		double[] result = new double[2];
		result[0] = min;
		result[1] = max;
		return result;
	}
	
	public static int getMinIndex(double[] data)
	{
		double min = data[0];
		int index = 0;
		for(int i = 0;i<data.length;i++)
		{
			double value = data[i];
			if(min>value)
			{
				min = value;
				index = i;
			}
		}
		return index;
	}
	
	public static double[] genCountPDF(double[] countVector, List<String> resultList)
	{
		double[] countPDFVector = new double[countVector.length];
		double sum = 0;
		for(int i=0;i<countVector.length;i++)
			sum+=countVector[i];
		for(int i = 0; i<countVector.length;i++)
		{
			double value = ((double)countVector[i])/sum;
			countPDFVector[i] = value;
			resultList.add((i+1)+" "+df.format(value));
		}
		return countPDFVector;
	}

	public static double[] genCountCDF(double[] countPDFVector, List<String> resultList,
			double min_value, double x_unit)
	{
		double[] countCDFVector = new double[countPDFVector.length];
		double sum = 0;
		for(int i = 0;i<countPDFVector.length;i++)
		{
			sum += countPDFVector[i];
			countCDFVector[i]=sum;
			String x = df.format(min_value+i*x_unit);
			resultList.add(x+" "+sum);
		}
		return countCDFVector;
	}
	
	public static double[] genMassCDF(double[] countPDFVector, List<String> resultList,
			double min_value, double x_unit)
	{
		double[] massCDFVector = new double[countPDFVector.length];
		double sum_inf = 0;
		for(int i = 0; i<countPDFVector.length;i++)
		{
			double value = countPDFVector[i]*(min_value+i*x_unit);
			sum_inf += value;
		}
		
		double sum_x = 0;
		for(int i = 0;i<countPDFVector.length;i++)
		{
			sum_x += countPDFVector[i]*(i+1)*x_unit;
			double mass = sum_x/sum_inf;
			massCDFVector[i] = mass;
			String x = df.format(min_value+((double)i)*x_unit);
			resultList.add(x+" "+mass);
		}
		
		return massCDFVector;
	}
	
	public static double[] genCountCDF(double[] countPDFVector, List<String> resultList, double x_unit)
	{
		double[] countCDFVector = new double[countPDFVector.length];
		double sum = 0;
		for(int i = 0;i<countPDFVector.length;i++)
		{
			sum += countPDFVector[i];
			countCDFVector[i]=sum;
			String x = df.format((i+1)*x_unit);
			resultList.add(x+" "+sum);
		}
		return countCDFVector;
	}
	
	public static double[] genMassCDF(double[]  countPDFVector, List<String> resultList, double x_unit)
	{
		double[] massCDFVector = new double[countPDFVector.length];
		double sum_inf = 0;
		for(int i = 0; i<countPDFVector.length;i++)
		{
			double value = countPDFVector[i]*(i+1)*x_unit;
			sum_inf += value;
		}
		
		double sum_x = 0;
		for(int i = 0;i<countPDFVector.length;i++)
		{
			sum_x += countPDFVector[i]*(i+1)*x_unit;
			double mass = sum_x/sum_inf;
			massCDFVector[i] = mass;
			String x = df.format(((double)(i+1))*x_unit);
			resultList.add(x+" "+mass);
		}
		
		return massCDFVector;
	}
	
	public static double[] getMinAvgMax(List<String> lineList, int index)
	{
		Iterator<String> iter = lineList.iterator();
		String firstLine = iter.next();
		double firstValue = Double.parseDouble(firstLine.split("\\s")[1]);
		double max = firstValue;
		double total = firstValue;
		double min = firstValue;
		
		while(iter.hasNext())
		{
			String line = iter.next();
			double value = Double.parseDouble(line.split("\\s")[index]);
			if(max<value)
				max = value;
			if(min>value)
				min = value;
			total += value;
		}
		double avg = total/lineList.size();
		double[] result = new double[3];
		result[0] = min;
		result[1] = avg;
		result[2] = max;
		return result;
	}
	
	public static boolean isNumeric(String str){
	    //Pattern pattern = Pattern.compile("[0-9]+(\\.?)[0-9]*"); 
		Pattern pattern = Pattern.compile("[-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?[dD]?");
	    return pattern.matcher(str).matches();
	 } 
	
	public static void main(String[] args)
	{
//		double[][] d = {{1,3,3,4,4,5,5,5.8,7,8},{1,2.2,3.1,4,4.5,4.6,5.4se,6,6.6,8}};
//		List<String>[] r = new ArrayList[2];se
//		r[0] = new ArrayList<String>();
//		r[1] = new ArrayList<String>();
//		genDistribution(d, r,10);
//		System.out.println(r[1].get(0));
		String s = "0";
		System.out.println(isNumeric(s));
		
		
	}
}
