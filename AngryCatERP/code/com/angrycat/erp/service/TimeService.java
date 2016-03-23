package com.angrycat.erp.service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.test.BaseTest;
/**
 * 封裝JDK 8 time API
 * @author JerryLin
 *
 */
@Service
@Scope("prototype")
public class TimeService {
	private ZoneId zoneId = ZoneId.systemDefault();
	/**
	 * 取得當天的午夜，譬如2015-11-05 00:00:00
	 * @return java.sql.Date
	 */
	public Date atStartOfToday(){
		LocalDate today = LocalDate.now();
		Date date = new Date(today.atStartOfDay(zoneId).toInstant().toEpochMilli());
		return date;
	}
	public int nextMonthValue(){
		LocalDate now = LocalDate.now();
		LocalDate plusOneMonth = now.plusMonths(1);
		int month = plusOneMonth.getMonthValue();
		return month;
	}
	/**
	 * 下個月最後一天午夜
	 * @return
	 */
	public Date nextMonthLastDayMidnight(){
		LocalDateTime todayMidnight = todayMidnightDateTime();
		LocalDateTime oneMonthAfter = todayMidnight.plusMonths(1);
		LocalDateTime lastDate = oneMonthAfter.withDayOfMonth(oneMonthAfter.toLocalDate().lengthOfMonth());
		
		return toSqlDate(lastDate);
	}
	/**
	 * 下個月第一天午夜
	 * @return
	 */
	public Date nextMonthFirstDayMidnight(){
		LocalDateTime todayMidnight = todayMidnightDateTime();
		LocalDateTime oneMonthAfter = todayMidnight.plusMonths(1);
		LocalDateTime startDate = oneMonthAfter.withDayOfMonth(1);
		
		return toSqlDate(startDate);
	}
	/**
	 * 今天午夜，但回傳LocalDateTime
	 * @return
	 */
	private LocalDateTime todayMidnightDateTime(){
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime datetime = today.withHour(0).withMinute(0).withSecond(0).withNano(0);
		// 另外一種寫法是從LocalDate轉:
//		LocalDate ld = LocalDate.now();
//		LocalDateTime ldt = ld.atStartOfDay();
		return datetime;
	}
	/**
	 * 今天午夜(0時0分0秒0毫秒)
	 * @return
	 */
	public Date todayMidnight(){
		LocalDateTime datetime = todayMidnightDateTime();
		
		return toSqlDate(datetime);
	}
	/**
	 * 將LocalDateTime轉成java.util.Date
	 * @param localDatetime
	 * @return
	 */
	public java.util.Date toDate(LocalDateTime localDatetime){
		Instant instant = localDatetime.atZone(zoneId).toInstant();
		java.util.Date date = Date.from(instant);
		return date;
	}
	/**
	 * 將LocalDateTime轉成java.sql.Date
	 * @param localDatetime
	 * @return
	 */
	private Date toSqlDate(LocalDateTime localDatetime){
		java.util.Date date = toDate(localDatetime);
		return new java.sql.Date(date.getTime());
	}
	
	private static void testAtStartOfToday(){
		TimeService ts = new TimeService();
		Date d = ts.atStartOfToday();
		System.out.println(new java.util.Date(d.getTime()));
	}
	private static void testNextMonthValue(){
		BaseTest.executeApplicationContext(acac->{
			TimeService ts = acac.getBean(TimeService.class);
			System.out.println(ts.nextMonthValue());
		});
	}
	public static void main(String[]args){
		testNextMonthValue();
	}
}
