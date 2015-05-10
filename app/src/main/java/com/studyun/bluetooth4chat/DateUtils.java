package com.studyun.bluetooth4chat;

import android.annotation.SuppressLint;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author Edward
 * @version 1.0.0 2014-9-4
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {
	public static final String _YYYY_MM_DD = "yyyy-MM-dd";
	public static final String _Y_M_D_H_M_S="yyyy-MM-dd HH:mm:ss";
	
	public static String now(){
		SimpleDateFormat df = new SimpleDateFormat(_Y_M_D_H_M_S);
		return df.format(new Date());
	}
	
	public static Date getNow(){
		return new Date();
	}
	
	public static String dateToStr(Date date){
		SimpleDateFormat df = new SimpleDateFormat(_Y_M_D_H_M_S);
		return df.format(date);
	}
	
	public static String dateToStr(Date date,String format){
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}	
	public static Date strToDate(String date,String format) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.parse(date);
	}	
	
	public static String yesterday(){
		 Calendar c = Calendar.getInstance();
		 c.setTime(new Date());
	     int day = c.get(Calendar.DATE);
	     c.set(Calendar.DATE, day - 1);

	     String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	     return dayBefore+" 00:00";
	}
	
	public static String tomrrow(){
		Calendar c = Calendar.getInstance();
		 c.setTime(new Date());
	     int day = c.get(Calendar.DATE);
	     c.set(Calendar.DATE, day + 1);
	     String dayAfter = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
         return dayAfter+" 00:00";
	}
	
	/**
	 * @param date1
	 * @param date2
	 */
    public static int compareDate(Date date1, Date date2) {
        return date1.compareTo(date2);
    }
	/**
	 * @param date1
	 * @param date2
	 */
    public static int compareDate(String date1, String date2) {
        DateFormat df = new SimpleDateFormat(_YYYY_MM_DD);
        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }
    public static String DateToString(String format,Date date1){
    	return new SimpleDateFormat(format).format(date1);
    }
    
    /**
    * @param specifiedDay,
    * @return
    */
	public static String getSpecifiedDayAfter(String specifiedDay) {
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat(_YYYY_MM_DD).parse(specifiedDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + 1);

		String dayAfter = new SimpleDateFormat(_YYYY_MM_DD)
				.format(c.getTime());
		return dayAfter;
	}

	public static Date getSpecifiedDayAfter(Date d) {
		Date date = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		date = calendar.getTime();
		return date;
	}
	
	/**
	* @param 
	* @return
	* @throws ParseException 
	*/
	public static Long getDiffDay(String beginDate, String endDate) {
		SimpleDateFormat formatter = new SimpleDateFormat(_YYYY_MM_DD);
		Long checkday = 0l;
		try {
			checkday = (formatter.parse(endDate).getTime() - formatter.parse(
					beginDate).getTime())
					/ (1000 * 24 * 60 * 60);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			checkday = null;
		}
		return checkday;
	}  

	public static Long getDiffDay(Date beginDate, Date endDate) {
		SimpleDateFormat format = new SimpleDateFormat(_YYYY_MM_DD);
		String strBeginDate = format.format(beginDate);

		String strEndDate = format.format(endDate);
		return getDiffDay(strBeginDate, strEndDate);
	}
	
	/**
	 * @return
	 */
	public static boolean isWeekend(Date bdate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(bdate);

		int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 6 || week == 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * @param specifiedYear
	 * @param num
	 * @return
	 */
	public static Date getSpecifiedYearAfter(Date specifiedYear,int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(specifiedYear);
		int year = c.get(Calendar.YEAR);
		c.set(Calendar.YEAR, year + num);

		return c.getTime();
	}
	
	/**
	 * @param specifiedMinute
	 * @param num
	 * @return
	 */
	public static Date getSpecifiedMinuteAfter(Date specifiedMinute, int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(specifiedMinute);
		c.add(Calendar.MINUTE, num);

		return c.getTime();
	}
	
	/**
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int dateCompareTo(Date date1,Date date2){
		 Calendar c1=Calendar.getInstance();
		 Calendar c2=Calendar.getInstance();
		 c1.setTime(date1);
		 c2.setTime(date2);

		 return c1.compareTo(c2);
	}
}
