package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.JERRY;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class MailService {
	private static final String[] DEFAULT_TO = new String[]{JERRY};
	
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	private String subject = "測試標題";
	private String content = "測試內容";
	
 	private String[] to = DEFAULT_TO;
 	private String[] cc;
	
 	public MailService subject(String subject){
 		this.subject = subject;
 		return this;
 	}
 	public MailService content(String content){
 		this.content = content;
 		return this;
 	}
 	public MailService to(String...to){
 		this.to = to;
 		return this;
 	}
 	public MailService cc(String...cc){
 		this.cc = cc;
 		return this;
 	}
	public void sendSimple(){
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
		simpleMailMessage.setTo(to);
		if(cc != null && cc.length > 0){
			simpleMailMessage.setCc(cc);
		}
		simpleMailMessage.setText(content);
		simpleMailMessage.setSubject(subject);
		mailSender.send(simpleMailMessage);
	}
	// ref. http://websystique.com/spring/spring-4-email-with-attachment-tutorial/
	public void sendHTML(){
//		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//		mailSender.setHost("msa.hinet.net");
//		mailSender.setPort(25);
		
		MimeMessagePreparator preparator = new MimeMessagePreparator(){
			public void prepare(MimeMessage mimeMessage) throws Exception{
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				
				helper.setSubject(subject);
				helper.setFrom(JERRY);
				helper.setTo(to);
				if(cc != null && cc.length > 0){
					helper.setCc(cc);
				}
				helper.setText(content, true);
			}
		};
		mailSender.send(preparator);
	}
}
