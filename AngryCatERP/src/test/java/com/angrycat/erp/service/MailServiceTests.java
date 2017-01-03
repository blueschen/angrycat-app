package com.angrycat.erp.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class MailServiceTests {
	@Autowired
	private MailService mailService;
	
	@Test
	public void defaultSendSimple(){
		mailService.sendSimple();
	}
	@Test
	public void defaultSendHTML(){
		mailService.sendHTML();
	}
	@Test
	public void generalSendSimple(){
		mailService
			.subject("這是標題")
			.content("這是內容")
			.sendSimple();
	}
	@Test
	public void generalSendHTML(){
		mailService
			.subject("這是標題")
			.content("<span style='color:red;'>這是內容</span>")
			.sendHTML();
	}
	@Test
	public void changeTo(){
		mailService
			.to("angrycat.it.jerrylin@gmail.com")
			.subject("這是標題-給gmail")
			.content("這是內容-給gamil")
			.sendSimple();
	}
}
