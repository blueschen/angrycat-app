package com.angrycat.erp.scheduletask;

import static com.angrycat.erp.common.EmailContact.BLUES;
import static com.angrycat.erp.common.EmailContact.IFLY;
import static com.angrycat.erp.common.EmailContact.JERRY;
import static com.angrycat.erp.common.EmailContact.MIKO;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.service.TimeService;
import com.angrycat.erp.shortnews.MitakeSMSHttpPost;
import com.angrycat.erp.test.BaseTest;

@Component
public class MemberVIP {
	
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	@Autowired
	private MitakeSMSHttpPost mitakeSMSHttpPost;
	@Autowired
	private TimeService timeService;
	
	@PostConstruct
	public void init(){}
	
	// cron expression ref. http://quartz-scheduler.org/api/2.2.0/org/quartz/CronExpression.html
	// 0 0 0 1 * ?代表每月一號
	// */5 * * * * ?代表每五秒
	/**
	 * 1 0 0 1 * ?每月1號凌晨0點1秒啟動排程
	 * 簡訊通知:本月生日，且VIP尚未使用及過期的會員
	 */
	@Scheduled(cron="1 0 0 1 * ?")
	public void birthVIP(){
		LocalDate now = LocalDate.now();
		int date = now.getDayOfMonth();
		if(date == 1){
			int month = now.getMonthValue();
			String BIRTH_VIP_MSG = "OHM Beads祝您生日快樂，{month}月壽星可享單筆訂單8折優惠，誠品敦南專櫃與網路通路皆可使用，詳情請洽02-27716304";
			String content = BIRTH_VIP_MSG.replace("{month}", String.valueOf(month));
			
			String hql = "SELECT p "
					+ "FROM com.angrycat.erp.model.Member p "
					+ "join p.vipDiscountDetails detail "
					+ "WHERE month(p.birthday) = (:pBirthday) "
					+ "AND detail.effectiveEnd >= (:pEffectiveEnd) "
					+ "AND detail.discountUseDate IS NULL";
			
			Map<String, Object> params = new HashMap<>();
			params.put("pBirthday", month);
			params.put("pEffectiveEnd", timeService.atStartOfToday());
			
			StringBuffer sb = mitakeSMSHttpPost.sendShortMsgToMembers(hql, params, content);
			String sendMsg = sb.toString();
			
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(MIKO);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(month + "月VIP生日優惠簡訊發送後訊息");
			String[] cc = new String[]{IFLY,BLUES,JERRY};
			simpleMailMessage.setCc(cc);
			mailSender.send(simpleMailMessage);
		}
	}
	
	/**
	 * 0 30 1 1 * ?每月1號凌晨一點半起動排程
	 * 簡訊通知:下月VIP到期的會員
	 */
	@Scheduled(cron="0 30 1 1 * ?")
	public void shortMsgNotifyNextMonthExpired(){
		String queryHql = "SELECT m FROM " + Member.class.getName() + " m WHERE m.toVipEndDate >= :startDate AND m.toVipEndDate <= :endDate";
		
		java.sql.Date startDayOfNextMonth = timeService.nextMonthFirstDayMidnight();
		java.sql.Date endDayOfNextMonth = timeService.nextMonthLastDayMidnight();
		int nextMonth = timeService.nextMonthValue();
		
		Map<String, Object> params = new HashMap<>();
		params.put("startDate", startDayOfNextMonth);
		params.put("endDate", endDayOfNextMonth);
		
		String template = "您的OHM VIP會員資格將於下個月(" + nextMonth + "月)到期，到期日為{toVipEndDate}";
		StringBuffer sb = mitakeSMSHttpPost.sendShortMsgToMembers(queryHql, params, (m->{
			Date toVipEndDate = m.getToVipEndDate();
			String dateStr = DatetimeUtil.DF_yyyyMMdd_DASHED.format(toVipEndDate);
			String content = template.replace("{toVipEndDate}", dateStr);
			return content;
		}));
		
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
		simpleMailMessage.setTo(MIKO);
		String sendMsg = sb.toString();
		simpleMailMessage.setText(sendMsg);
		simpleMailMessage.setSubject(nextMonth + "月VIP到期簡訊發送後訊息");
		String[] cc = new String[]{IFLY,BLUES,JERRY};
		simpleMailMessage.setCc(cc);
		mailSender.send(simpleMailMessage);
	}
	
	
	/**
	 * 0 0 1 * * ?每日凌晨一點啟動排程
	 * 如果是VIP，但已超過有效截止日，就改為非VIP
	 */
	@Scheduled(cron="0 0 1 * * ?")
	public void cancelVIPIfExpired(){
		sfw.executeSaveOrUpdate(s->{
			java.sql.Date todayMidnight = timeService.todayMidnight();
			String queryHql = "SELECT m.name, m.idNo, m.mobile, m.id FROM " + Member.class.getName() + " m WHERE m.toVipEndDate < :today AND m.important = 1";
			List<Object[]> members = s.createQuery(queryHql).setDate("today", todayMidnight).list();
			List<String> items = members.stream()
									.map(m->StringUtils.join(m, "|"))
									.collect(Collectors.toList());
			String sendMsg = StringUtils.join(items, "\n");
			
			String updatHql = "UPDATE " + Member.class.getName() + " m SET m.important = :important WHERE m.toVipEndDate < :today AND m.important = 1";
			int count = s.createQuery(updatHql).setBoolean("important", false).setDate("today", todayMidnight).executeUpdate();
			
			String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(todayMidnight);
			
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(MIKO);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(dateStr + "VIP失效更改共:" + count + "筆");
			
			String[] cc = new String[]{IFLY, BLUES, JERRY};
			simpleMailMessage.setCc(cc);
			mailSender.send(simpleMailMessage);
		});
	}
	
	private static void testCancelVIPIfExpired(){
		BaseTest.executeApplicationContext(acac->{
			MemberVIP vip = acac.getBean(MemberVIP.class);
			vip.cancelVIPIfExpired();
		});
	}
	/**
	 * http://stackoverflow.com/questions/31726418/localdatetime-remove-the-milliseconds
	 * 測試LocalDateTime列印時間格式
	 * 如何(關閉)顯示毫秒
	 */
	private static void testPrintLocalDateTime(){
		LocalDateTime now = LocalDateTime.now();
		System.out.println(now); // 沒有設定，預設連帶顯示毫秒數字
		System.out.println(now.withNano(0)); // 納秒為0，不會顯示毫秒數字
	}
	
	private static void testLocalDateToDate(){
		LocalDate now = LocalDate.now();
		System.out.println(now);
		LocalDateTime startOfDay = now.atStartOfDay(); // 調整時間到午夜，並轉成LocalDateTime；
		LocalDateTime specifiedMidnightOfDay = now.atTime(0, 0, 0); // 手動指定時、分、秒
		// LocalDate轉成LocalDateTime之後才能轉成Date
		Instant instant1 = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
		Instant instant2 = specifiedMidnightOfDay.atZone(ZoneId.systemDefault()).toInstant();
		
		Date d1 = Date.from(instant1);
		Date d2 = Date.from(instant2);
		
		System.out.println("startOfDay Date: " + d1);
		System.out.println("specifiedMidnightOfDay Date: " + d2);
		
	}
	
	public static void main(String[]args){
	}
}
