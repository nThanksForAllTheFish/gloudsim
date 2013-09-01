package fr.imag.mescal.gloudsim.util;

import java.util.Date;

/**
 * to show progress in a loop
 * @author sdi
 *
 */
public class Progress {
	public static void showProgress(double initLogTime, int i, int size, String comment)
	{
		String currentTime = DateUtil.getTimeNow(new Date());
		long currentTimeValue = System.currentTimeMillis()/1000;
		System.out.println(currentTime+" : already "+(currentTimeValue-initLogTime)+" sec passed, ("+i+"/"+size+"): "+comment);
	}
}
