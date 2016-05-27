package com.angrycat.erp.service;

import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.security.Group;
import com.angrycat.erp.security.User;
import com.angrycat.erp.security.extend.GroupInfo;
import com.angrycat.erp.security.extend.UserInfo;

@Service
public class UserInsertService {
	@Autowired
	private SessionFactoryWrapper sfw;
	
	public Group newOneIfNotFound(Session s, String code){
		List<Group> groups = s.createQuery("FROM " + Group.class.getName() + " g WHERE g.info.code = :code").setString("code", code).list();
		Group g = null;
		if(!groups.isEmpty()){
			g = groups.get(0);
		}else{
			GroupInfo info = new GroupInfo();
			info.setName(code);
			info.setNameEng(code);
			info.setCode(code);
			s.save(info);
			s.flush();
			g = new Group();
			g.setInfo(info);
			s.save(g);
			s.flush();
		}
		return g;
	}
	
	public void insertUser(){
		sfw.executeTransaction(s->{
			UserInfo userInfo = new UserInfo();
			userInfo.setCode("User");
			userInfo.setName("小言");
			userInfo.setNameEng("shiuan1919");
			s.save(userInfo);
			s.flush();
			
			User user = new User();
			user.setUserId("shiuan1919");
			user.setPassword("shiuan9191");
			user.setDefaultGroup(newOneIfNotFound(s, "UserGroup"));
			user.setInfo(userInfo);
			s.save(user);
			s.flush();
		});
	}
	
	public static void testInsertUser(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		UserInsertService service = acac.getBean(UserInsertService.class);
		service.insertUser();
		acac.close();
	}
	
	public static void main(String[]args){
		testInsertUser();
	}
}
