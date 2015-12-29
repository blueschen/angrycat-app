package com.angrycat.erp.scheduletask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.shortnews.MitakeSMSHttpPost;
@Component
public class ShortNews {
	private static final String BIRTH_VIP_MSG = "OHM Beads祝您生日快樂，{month}月壽星可享單筆訂單8折優惠，誠品敦南專櫃與網路通路皆可使用，詳情請洽02-27716304";
	
	@Autowired
	private MitakeSMSHttpPost mitakeSMSHttpPost;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	// cron expression ref. http://quartz-scheduler.org/api/2.2.0/org/quartz/CronExpression.html
	// 0 0 0 1 * ?代表每月一號
	// */5 * * * * ?代表每五秒
//	@Scheduled(cron="*/5 * * * * ?")
	@Scheduled(cron="1 0 0 1 * ?")
	public void birthVIP(){
		LocalDate now = LocalDate.now();
		int date = now.getDayOfMonth();
		if(date == 1){
			int month = now.getMonthValue();
			//mitakeSMSHttpPost.setTestMode(true);
			String msg = BIRTH_VIP_MSG.replace("{month}", String.valueOf(month));
			StringBuffer sb = mitakeSMSHttpPost.sendShortMsgToBirthMonth(month, msg);
			if(sb != null){
				String sendMsg = sb.toString();
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
				simpleMailMessage.setTo("jerrylin@ohmbeads.com.tw");
				simpleMailMessage.setText(sendMsg);
				simpleMailMessage.setSubject(month + "月VIP生日優惠簡訊發送後訊息");
				List<String> cc = new ArrayList<>();
				cc.add("iflywang@ohmbeads.com.tw");
				cc.add("blueschen@ohmbeads.com.tw");
				if(sendMsg.contains("簡訊點數即將用完")){
					cc.add("joycechang@ohmbeads.com.tw");
				}
				simpleMailMessage.setCc(cc.toArray(new String[cc.size()]));
				mailSender.send(simpleMailMessage);
			}
		}
	}
	
	// ref. http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mail.html
	public void testMail(){
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
		simpleMailMessage.setTo("eeit54@gmail.com");
		simpleMailMessage.setText("This is a testing");
		simpleMailMessage.setSubject("Testing mail");
		List<String> cc = new ArrayList<>();
		cc.add("jerrylin@ohmbeads.com.tw");
		cc.add("yuanchi1126@yahoo.com.tw");
		simpleMailMessage.setCc(cc.toArray(new String[cc.size()]));
		
		mailSender.send(simpleMailMessage);
	}
	
	public static void main(String[]args){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		
		ShortNews shortNews = acac.getBean(ShortNews.class);
		shortNews.testMail();
		
		acac.close();
	}
}
