package com.angrycat.erp.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
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
	 * 取得當天的起始點，譬如2015-11-05 00:00:00
	 * @return java.sql.Date
	 */
	public Date atStartOfToday(){
		LocalDate today = LocalDate.now();
		Date date = new Date(today.atStartOfDay(zoneId).toInstant().toEpochMilli());
		return date;
	}

	private static void testAtStartOfToday(){
		TimeService ts = new TimeService();
		Date d = ts.atStartOfToday();
		System.out.println(new java.util.Date(d.getTime()));
	}
	public static void main(String[]args){
		testAtStartOfToday();
	}
	
}
