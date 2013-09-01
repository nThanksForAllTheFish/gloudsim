package fr.imag.mescal.gloudsim.util;
import java.util.ArrayList;
import java.util.Random;

/**
 * generate random values for simulation.
 * @author sdi
 *
 */
public class RdGenerator {
	public static long RANDOM_SEED = 123456789l;
	public static long RANDOM_SEED2 = 98347150;//999999;//9834715;
	public static long RANDOM_SEED3 = 333444888;
	public static long RANDOM_SEED4 = 92232;
	public static long RANDOM_SEED5 = 111111111;
	public static long RANDOM_SEED6 = 222222222;
	public static long RANDOM_SEED7 = 333333333;
	public final static RdGenerator RAN_SeedGen = new RdGenerator(RANDOM_SEED);
	public final static RdGenerator RAN_SeedGen2 = new RdGenerator(RANDOM_SEED2); //randamly generate a job
	public final static RdGenerator RAN_SeedGen3 = new RdGenerator(RANDOM_SEED3);
	public final static RdGenerator RAN_SeedGen4 = new RdGenerator(RANDOM_SEED4);
	public final static RdGenerator RAN_SeedGen5 = new RdGenerator(RANDOM_SEED5);
	public final static RdGenerator RAN_SeedGen6 = new RdGenerator(RANDOM_SEED6);
	public final static RdGenerator RAN_SeedGen7 = new RdGenerator(RANDOM_SEED7);
	
	private Random r;
	public RdGenerator(long seed) {
		r = new Random(seed);
	}

	public int generate_Int(int start, int end)
	{
		return (int)(start+(end-start+1)*r.nextFloat());
	}
	
	public long generate_Long(long start, long end)
	{
		return (long)(start+(end-start+1)*r.nextDouble());
	}
	
	public float generate_Float(float start, float end)
	{
		return (float)(start+(end-start)*r.nextFloat());
	}

	public double generate_Double(double start, double end)
	{
		return (start+(end-start)*r.nextDouble());
	}
	
	public int[] generate_Int(int start, int rangeSize, int number)
	{
		if(rangeSize==0)
			return null;
		if(number>rangeSize)
			number = rangeSize;
		int[] result = new int[number];
		if(number>rangeSize)
		{			
			for(int i = 0;i<number;i++)
			{
				result[i] = generate_Int(start,rangeSize-1);
			}
		}
		else
		{
			int size = rangeSize;
			ArrayList<Integer> list = new ArrayList<Integer>(size);
			for(int i = start;i<=rangeSize;i++)
			{
				list.add(new Integer(i));
			}
			for(int j = 0;j<number;j++)
			{
				result[j] = list.remove(generate_Int(0,size-1));
				size--;
			}
		}		
		return result;
	}
	
	public static void main(String[] args)
	{
		RdGenerator rGen = new RdGenerator(1234567891);
//		RdGenerator rGen2 = new RdGenerator(2000000);
//		RdGenerator rGen3 = new RdGenerator(100000000);
//		for(int i = 0;i<200;i++)
//		{
//			System.out.println("rGen="+rGen.generate_Int(100, 500)+" rGen2="+rGen2.generate_Long(10, 20)+" rGen3="+rGen3.generate_Float(10, 20));
//			System.err.println("rGen="+rGen.generate_Int(1, 10)+" rGen2="+rGen2.generate_Int(10, 20)+" rGen3="+rGen3.generate_Int(10, 20));
//			//System.out.println("rGen="+rGen.generate_Long(10, 20)+" rGen2="+rGen2.generate_Long(10, 20)+" rGen3="+rGen3.generate_Long(10, 20));
//		}
//		for(int i = 0;i<100;i++)
//			System.out.println(new Random().nextDouble());		
		int[] a = rGen.generate_Int(0,10,6);
		for(int i = 0;i<a.length;i++)
		{
			System.out.print(a[i]+",");
		}
	}
}
