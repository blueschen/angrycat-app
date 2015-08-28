package com.angrycat.erp.test;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.service.QueryBaseService;

public class TimeTest extends BaseTest {
	public static void main(String[]args){
		testTimeZoneDefault();
	}
	public static void testTimestamp(){
		DateFormat timeFormatFS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat dateFormatFS = new SimpleDateFormat("yyyy-MM-dd");
		String d = "2015-08-10 23:59:59";
		try {
			Timestamp t = new Timestamp(timeFormatFS.parse(d).getTime());
			
			String time = dateFormatFS.format(t);
			System.out.println("time: " + time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public static void testQueryTimestampByStringVal(){
		executeSession((s, acac)->{
			QueryBaseService<DataChangeLog, DataChangeLog> queryService = (QueryBaseService<DataChangeLog, DataChangeLog>)acac.getBean("queryBaseService");
			queryService
				.createFromAlias(DataChangeLog.class.getName(), "p")
				.addWhere(ConditionFactory.putTimestampStart("p.logTime >= :pLogTime"));
			queryService.getSimpleExpressions().get("pLogTime").setValue(new Date(System.currentTimeMillis()));
			queryService.executeQueryList().forEach(d->{
				System.out.println("id: " + d.getDocId() + ", logTime: " + d.getLogTime());
			});;
		});
	}
	public static void testTimeZoneDefault(){
		TimeZone tz = TimeZone.getDefault();
		System.out.println(tz.getID());
		TimeZone tz2 = TimeZone.getTimeZone("GMT+8");
		System.out.println(tz2.getDisplayName());
		System.out.println(tz2.getRawOffset());
	}
}
