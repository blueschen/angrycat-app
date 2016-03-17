package com.angrycat.erp.scheduletask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.shortnews.MitakeSMSHttpPost;
@Component
public class ShortNews {
	
	@Autowired
	private MitakeSMSHttpPost mitakeSMSHttpPost;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	

	
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
