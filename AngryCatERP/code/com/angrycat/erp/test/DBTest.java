package com.angrycat.erp.test;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.security.Group;
import com.angrycat.erp.security.Role;
import com.angrycat.erp.security.RoleConstants;
import com.angrycat.erp.security.User;
import com.angrycat.erp.security.extend.GroupInfo;
import com.angrycat.erp.security.extend.UserInfo;

public class DBTest {
	public static void main(String[]args){
		insertSecurity();
//		testRole();
	}
	public static void insertSecurity(){
		executeSession(s->{
			int roleCount = 10;
			for(int i = 0; i < roleCount; i++){
				Role role = new Role();
				role.setName("ROLE_Test_" + (i+1));
				s.save(role);
			}
			s.flush();
			
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setCode("System");
			groupInfo.setName("系統管理");
			groupInfo.setNameEng("System Manager");
			s.save(groupInfo);
			s.flush();
			
			Group group = new Group();
			group.setInfo(groupInfo);
			s.save(group);
			s.flush();
			
			List<Role> roles = s.createCriteria(Role.class).list();
			group.getRoles().addAll(roles);
			s.flush();
			
			UserInfo userInfo = new UserInfo();
			userInfo.setCode("System_root");
			userInfo.setName("系統管理員");
			userInfo.setNameEng("System manager");
			s.save(userInfo);
			s.flush();
			
			User user = new User();
			user.setUserId("root");
			user.setPassword("sysmanager");
			user.setDefaultGroup(group);
			user.setInfo(userInfo);
			s.save(user);
			s.flush();
		});		
	}
	
	public static void testRole(){
		executeSession(s->{
			Iterator<User> itr = s.createQuery("FROM " + User.class.getName() + " u WHERE u.userId = ?").setString(0, "root").iterate();
			if(itr.hasNext()){
				User u = itr.next();
				System.out.println("user is belong to ROOT: " + u.isBelongToRole(RoleConstants.ROOT));
				Iterator<Role> foundRole = s.createQuery("FROM " + Role.class.getName() + " r WHERE r.name = ?").setString(0, "ROLE_Test_1").iterate();
				if(foundRole.hasNext()){
					System.out.println("defualt group info name: " + u.getDefaultGroup().getInfo().getName());
					u.getDefaultGroup().getRoles().stream().forEach(r->{
						System.out.println("user defaulGroup role: " + r.getName());
					});
					System.out.println("user defaultGroup is belong to ROLE_Test_1: " + u.getDefaultGroup().isBelongToRole(foundRole.next().getId()));
				}
				
			}
			
		});
		
		
		
	}
	
	public static void executeSession(Consumer<Session>c){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		Session s = null;
		try{
			s = acac.getBean(LocalSessionFactoryBean.class).getObject().openSession();
			c.accept(s);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			
			if(s !=null && s.isOpen()){
				s.close();
			}
			acac.close();
		}
	}
}
