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
		insertSecurityAll();
//		insertSecurity();
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
	
	public static void insertSecurityAll(){
		executeSession(s->{
			Role role = new Role();
			role.setName("User");
			s.save(role);
			s.flush();
			
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setCode("UserGroup");
			groupInfo.setName("使用者群組");
			groupInfo.setNameEng("UserGroup");
			s.save(groupInfo);
			s.flush();
			
			Group group = new Group();
			group.setInfo(groupInfo);
			s.save(group);
			s.flush();
			
			group.getRoles().add(role);
			s.flush();
			
			UserInfo userInfo1 = new UserInfo();
			userInfo1.setCode("User");
			userInfo1.setName("使用者");
			userInfo1.setNameEng("User");
			s.save(userInfo1);
			s.flush();
			
			User user1 = new User();
			user1.setUserId("straycat034");
			user1.setPassword("Qq95430621");
			user1.setDefaultGroup(group);
			user1.setInfo(userInfo1);
			s.save(user1);
			s.flush();
			
			UserInfo userInfo2 = new UserInfo();
			userInfo2.setCode("User");
			userInfo2.setName("使用者");
			userInfo2.setNameEng("User");
			s.save(userInfo2);
			s.flush();
			
			User user2 = new User();
			user2.setUserId("yuanrosie");
			user2.setPassword("0605");
			user2.setDefaultGroup(group);
			user2.setInfo(userInfo2);
			s.save(user2);
			s.flush();
			
			UserInfo userInfo3 = new UserInfo();
			userInfo3.setCode("User");
			userInfo3.setName("使用者");
			userInfo3.setNameEng("User");
			s.save(userInfo3);
			s.flush();
			
			User user3 = new User();
			user3.setUserId("aaa2547860");
			user3.setPassword("2547860");
			user3.setDefaultGroup(group);
			user3.setInfo(userInfo3);
			s.save(user3);
			s.flush();
			
			UserInfo userInfo4 = new UserInfo();
			userInfo4.setCode("User");
			userInfo4.setName("使用者");
			userInfo4.setNameEng("User");
			s.save(userInfo4);
			s.flush();
			
			User user4 = new User();
			user4.setUserId("mikowang");
			user4.setPassword("wsy2xxn00");
			user4.setDefaultGroup(group);
			user4.setInfo(userInfo4);
			s.save(user4);
			s.flush();
			
			UserInfo userInfo5 = new UserInfo();
			userInfo5.setCode("User");
			userInfo5.setName("使用者");
			userInfo5.setNameEng("User");
			s.save(userInfo5);
			s.flush();
			
			User user5 = new User();
			user5.setUserId("joycechang");
			user5.setPassword("297297");
			user5.setDefaultGroup(group);
			user5.setInfo(userInfo5);
			s.save(user5);
			s.flush();
			
			UserInfo userInfo6 = new UserInfo();
			userInfo6.setCode("User");
			userInfo6.setName("使用者");
			userInfo6.setNameEng("User");
			s.save(userInfo6);
			s.flush();
			
			User user6 = new User();
			user6.setUserId("iflywang");
			user6.setPassword("120105");
			user6.setDefaultGroup(group);
			user6.setInfo(userInfo6);
			s.save(user6);
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
