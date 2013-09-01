package fr.imag.mescal.gloudsim.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Calendar;

/**
 * Date toolkit
 * @author sdi
 */
public class DateUtil
{

    private static String datePattern = "yyyy-MM-dd";

    private static String timePattern = "HH:mm";

    /**
     * Return 缺省的日期格式 (yyyy/MM/dd)
     *
     * @return 在页面中显示的日期格式
     */
    public static String getDatePattern()
    {
        return datePattern;
    }
    /**
     * 根据日期格式，返回日期按datePattern格式转换后的字符串
     *
     * @param aDate
     *            日期对象
     * @return 格式化后的日期的页面显示字符串
     */
    public static final String getDate(Date aDate)
    {
        SimpleDateFormat df = null;
        String returnValue = "";
        if (aDate != null)
        {
            df = new SimpleDateFormat(datePattern);
            returnValue = df.format(aDate);
        }
        return (returnValue);
    }
    public static final String getDate(String pattern)
    {
        Date date = new Date();
        return getDate(date, pattern);
    }
    public static final String getDate(Date date, String pattern)
    {
        SimpleDateFormat df = null;
        String returnValue = "";
        if (date != null)
        {
            df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }
        return (returnValue);
    }
    public static Date getDate(String dateString, String pattern)
    {
        SimpleDateFormat df = null;
        Date date = new Date();
        if (dateString != null)
        {
            try
            {
                df = new SimpleDateFormat(pattern);
                date = df.parse(dateString);
            }
            catch(Exception e)
            {}
        }
        return date;
    }
    /**
     * 按照日期格式，将字符串解析为日期对象
     *
     * @param aMask
     *            输入字符串的格式
     * @param strDate
     *            一个按aMask格式排列的日期的字符串描述
     * @return Date 对象
     * @see java.text.SimpleDateFormat
     * @throws ParseException
     */
    public static final Date convertStringToDate(String aMask, String strDate)
    {
        SimpleDateFormat df = null;
        Date date = null;
        df = new SimpleDateFormat(aMask);
        try
        {
        	date = df.parse(strDate);
        }
        catch (ParseException pe)
        {}
        return (date);
    }

    /**
     * This method returns the current date time in the format: yyyy/MM/dd HH:MM
     * a
     *
     * @param theTime
     *            the current time
     * @return the current date/time
     */
    public static String getTimeNow(Date theTime)
    {
        return getDateTime(timePattern, theTime);
    }

    /**
     * This method returns the current date in the format: yyyy/MM/dd
     *
     * @return the current date
     * @throws ParseException
     */
    public static Calendar getToday() throws ParseException
    {
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(datePattern);
        // This seems like quite a hack (date -> string -> date),
        // but it works ;-)
        String todayAsString = df.format(today);
        Calendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(todayAsString));
        return cal;
    }
    /**
     * This method generates a string representation of a date's date/time in
     * the format you specify on input
     *
     * @param aMask
     *            the date pattern the string is in
     * @param aDate
     *            a date object
     * @return a formatted string representation of the date
     *
     * @see java.text.SimpleDateFormat
     */
    public static final String getDateTime(String aMask, Date aDate)
    {
        SimpleDateFormat df = null;
        String returnValue = "";
        df = new SimpleDateFormat(aMask);
        returnValue = df.format(aDate);
        return (returnValue);
    }
    /**
     * 根据日期格式，返回日期按datePattern格式转换后的字符串
     * @param aDate Date
     * @return String
     */
    public static final String convertDateToString(Date aDate)
    {
        return getDateTime(datePattern, aDate);
    }
    /**
     * 按照日期格式，将字符串解析为日期对象
     * @param strDate String
     * @return Date
     * @throws ParseException
     */
    public static Date convertStringToDate(String strDate)
    {
        Date aDate = convertStringToDate(datePattern, strDate);
        return aDate;
    }

    public static String getYear()
    {
        Date date = new Date();
        return getDate(date, "yyyy");
    }
    public static String getMonth()
    {
        Date date = new Date();
        return getDate(date, "MM");
    }
    public static String getDay()
    {
        Date date = new Date();
        return getDate(date, "dd");
    }
    /**
     * 返回小时
     *
     * @param date
     * 日期
     * @return 返回小时
     */
    public static int getHour(java.util.Date date)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.get(java.util.Calendar.HOUR_OF_DAY);
    }
    /**
     * 返回分钟
     *
     * @param date
     * 日期
     * @return 返回分钟
     */
    public static int getMinute(java.util.Date date)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.get(java.util.Calendar.MINUTE);
    }
    /**
     * 返回秒钟
     *
     * @param date
     * 日期
     * @return 返回秒钟
     */
    public static int getSecond(java.util.Date date)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.get(java.util.Calendar.SECOND);
    }
    /**
     * 返回毫秒
     *
     * @param date
     * 日期
     * @return 返回毫秒
     */
    public static long getMillis(java.util.Date date)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.getTimeInMillis();
    }
    /**
     * 日期相加
     *
     * @param date
     * 日期
     * @param day
     * 天数
     * @return 返回相加后的日期
     */
    public static java.util.Date addDate(java.util.Date date, int day)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(getMillis(date) + ((long) day) * 24 * 3600 * 1000);
        return c.getTime();
    }
    /**
     * 日期相减
     *
     * @param date
     * 日期
     * @param date1
     * 日期
     * @return 返回相减后的日期
     */
    public static int diffDate(java.util.Date date, java.util.Date date1)
    {
        return (int) ((getMillis(date) - getMillis(date1)) / (24 * 3600 * 1000));
    }
    public static int diffDateToHour(Date date, Date date1)
    {
        return (int) ((getMillis(date) - getMillis(date1)) / (1000 * 60* 60));
    }
}
