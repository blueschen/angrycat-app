package com.angrycat.erp.common;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class DatetimeUtil {
	/**
	 * retrieve Date<br>
	 * hour, minute, second, millisec all are set to zero
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static Date getFirstMinuteOfDay(int year, int month, int day){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DAY_OF_MONTH, day);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	/**
	 * see the method {@link #getFirstMinuteOfDay(int year, int month, int day)}.
	 */
	public static Date getFirstMinuteOfDay(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	public static Calendar getFirstMinute(Calendar c){
		c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
		return c;
	}
	public static Date addOneDayToFirstMinute(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	public static Date getLastMinuteOfDay(int year, int month, int day){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DAY_OF_MONTH, day);
		c = getLastMinute(c);
		return new Date(c.getTimeInMillis());
	}
	public static Calendar getLastMinute(Calendar c){
		c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
		return c;
	}
	/**
	 * ex: 2015-09-10~2015-09-09
	 * @param c
	 * @return
	 */
	public static Date addOneYearToLastMinute(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.YEAR, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		c = getLastMinute(c);
		Date oneYearAfter = new Date(c.getTime().getTime());
		return oneYearAfter;
	}
	/**
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getLastDateOfMonth(int year, int month){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		return getLastMinuteOfDay(year, month, lastDay);
	}
	public static void testAddOneDayToFirstMinute(){
		Date today = new Date(System.currentTimeMillis());
		System.out.println("add One day: " + addOneDayToFirstMinute(today));
	}
	private static void testGetFirstMinute(){
		Calendar c = Calendar.getInstance();
		c = getFirstMinute(c);
		System.out.println(c.getTime());
	}
	private static void testGetLastMinute(){
		Calendar c = Calendar.getInstance();
		c = getLastMinute(c);
		System.out.println(c.getTime());
	}
	private static void testAddOneYearToLastMinute(){
		Date today = new Date(System.currentTimeMillis());
		System.out.println("today: " + today);
		System.out.println("one year after: " + addOneYearToLastMinute(today));
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2016);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 29);
		Date time = new Date(c.getTimeInMillis());
		System.out.println("test time: " + time);
		System.out.println("one year after: " + addOneYearToLastMinute(time));
	}
	public static void testGetLastDateOfMonth(){
		System.out.println("last day: " + getLastDateOfMonth(2015, 11));
	}
	private static void testGetLastMinuteOfDay(){
		System.out.println(getLastMinuteOfDay(2015, 11, 12));
	}
	private static void testGetFirstMinuteOfDay(){
		Date d = getFirstMinuteOfDay(2015, 11, 12);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(d.getTime());
		System.out.println("hour: " + c.get(Calendar.HOUR_OF_DAY) + ", minute: " + c.get(Calendar.MINUTE) + ", sec: " + c.get(Calendar.SECOND));
	}
	private static void test(){
		int year = 2015;
		int month = 11;
		int day = 12;
		
		Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		LocalDateTime d = LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.MAX);
		
		
		ZoneId zoneId = TimeZone.getDefault().toZoneId();
		System.out.println("JVM time zone id: " + zoneId);
		System.out.println("JVM time zone offset: " + TimeZone.getDefault().getRawOffset() + " milliseconds");
		System.out.println("JVM time zone offset: " + TimeZone.getDefault().getRawOffset()/(60*60*1000) + " hours"); // converting to hours differing from Greenwich, ex. Asia/Taipei is GMT+8, the result is 28,800,000 milliseconds 
		System.out.println("JVM time zone ids: " + Arrays.asList(TimeZone.getAvailableIDs(TimeZone.getDefault().getRawOffset())));
		System.out.println("JVM support time zone ids: " + Arrays.asList(TimeZone.getAvailableIDs()));
	}
	public static void main(String[]args){
		test();
	}
}
