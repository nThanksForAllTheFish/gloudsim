package fr.imag.mescal.gloudsim.sim.cp;

/**
 * The key class to compute the optimal checkpointing interval
 * @author sdi
 *
 */
public class OptCPAnalyzer {
	
	private static double error = 0.000001f;
	private static float mtbf_error = 1f;
	public static float loadRatio = 1f;
	
	public static double f(double lambda, double L, double x)
	{
		double result = Math.pow(Math.E, -lambda*L)+(lambda*x-1)*Math.pow(Math.E, -lambda*x);
		return result;
	}
	
	public static double df(double lambda, double L, double x)
	{
		return lambda*(2-lambda*x)*Math.pow(Math.E, -lambda*x);
	}
	
	public static double run(double initValue, double lambda, double L)
	{
		double updateValue = initValue - f(lambda, L, initValue)/df(lambda, L, initValue);
		if(Math.abs(updateValue - initValue)<=error)
			return updateValue;
		else
			return run(updateValue, lambda, L);
	}
	
	/**
	 * @deprecated
	 * @param lambda = 1/mtbf
	 * @param L
	 * @return
	 */
	public static double compDynInterval_newton(double lambda, double L)
	{
		double initValue = 1/lambda;
		return run(initValue, lambda, L);
	}
			
	/**
	 * @deprecated
	 * @param lambda
	 * @param L
	 * @return
	 */
	public static double compDynInterval_tylor(double lambda, double L)
	{
		double a = Math.pow(Math.E, -lambda*L);
		double b = Math.sqrt(a+1/27);
		double sum = 1+Math.cbrt(-a+b)+Math.cbrt(-a-b);
		double result = 1/lambda*sum;
		return result;
	}
	
	public static double compDynInterval_Young(double lambda, double cpCost, double L)
	{
		double optLength = Math.sqrt(2*1/lambda*cpCost);
		if(optLength<L)
			return optLength;
		else
			return -1;
	}
	
	public static boolean discriminant(double lambda, double optValue, 
			double L, double cpCost)
	{
		double fx = 1-Math.pow(Math.E, -lambda*optValue);
		double fL = 1-Math.pow(Math.E, -lambda*L);
		if(cpCost+optValue*(fx-fL)<0)
			return true;
		else
			return false;
	}
	
	/**
	 * if theta = 1/2, then Young's formula
	 * @param cpCost
	 * @param mtbf
	 * @return
	 */
	public static float compYoungInterval(float cpCost, float mtbf, float theta)
	{
		return (float)Math.sqrt(cpCost*mtbf*mtbf_error/theta);
	}
	
	public static float compStaticOptNum(float cpCost, float taskLength, float failureNum, float theta)
	{
		return (float)Math.sqrt(theta*taskLength*failureNum/cpCost);
	}
	
	public static void main(String[] args)
	{
		double lambda = 0.004234451233;
		double L = 700;
		double newton = compDynInterval_newton(lambda, L);
		double tylor = compDynInterval_tylor(lambda, L);
		System.out.println("newton="+newton);
		System.out.println("tylor="+tylor);
		System.out.println("error="+((tylor/newton)-1));
	}
}
