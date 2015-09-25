package com.angrycat.erp.test;

import static com.angrycat.erp.condition.ConditionFactory.propertyDesc;
import static com.angrycat.erp.condition.ConditionFactory.putStr;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.businessrule.VipDiscountUseStatus;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.condition.Order;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.model.DataChangeLogDetail;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.query.QueryGenerator;
import com.angrycat.erp.security.Group;
import com.angrycat.erp.security.Role;
import com.angrycat.erp.security.RoleConstants;
import com.angrycat.erp.security.User;
import com.angrycat.erp.security.extend.GroupInfo;
import com.angrycat.erp.security.extend.UserInfo;
import com.angrycat.erp.service.QueryBaseService;




public class DBTest extends BaseTest{	
	public static void selectTest(){
		executeSession((s, acac)->{
			Long count = (Long)s.createQuery("SELECT COUNT(*) FROM " + Member.class.getName()).uniqueResult();
			
			System.out.println("member count: " + count);
			
		});
	}
	
	public static void insertSecurity(){
		executeSession((s, acac)->{
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
		executeSession((s, acac)->{
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
	
	public static void insertSecurityOther(){
		executeSession((s, acac)->{
			
			List<Group> groups = s.createQuery("FROM " + Group.class.getName() + " g WHERE g.info.code = ?").setString(0, "UserGroup").list();
			
			UserInfo userInfo1 = new UserInfo();
			userInfo1.setCode("slowlywu");
			userInfo1.setName("慢慢");
			userInfo1.setNameEng("slowlywu");
			s.save(userInfo1);
			s.flush();
			
			User user1 = new User();
			user1.setUserId("slowlywu");
			user1.setPassword("198615");
			user1.setDefaultGroup(groups.get(0));
			user1.setInfo(userInfo1);
			s.save(user1);
			s.flush();
		});		
	}
	
	
	public static void testRole(){
		executeSession((s, acac)->{
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
	
	
	public static void testInsertDataChangeLog(){
		executeSession((s, acac)->{
			
			for(int i = 0; i < 3; i++){
				DataChangeLog dcl = new DataChangeLog();
				dcl.setDocId("docId" + i);
				dcl.setDocType("docType" + i);
				dcl.setLogTime(new Timestamp(System.currentTimeMillis()));
				dcl.setUserId("userId"+i);
				dcl.setUserName("userName" + i);
				
				for(int j = 0; j <5; j++){
					dcl.getDetails().add(new DataChangeLogDetail("fieldName"+j, "originalContent"+j, "changedContent"+j));
				}
				
				s.save(dcl);
			}
			s.flush();
			s.clear();
			
			
			
			
		});
	}
	
	public static void testBatchSize(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
		System.out.println("batch size: " + sfw.getBatchSize());
		acac.close();
	}
	
	public static void testInsertVipDiscoutnDetails(){
		executeSession((s, acac)->{
			List<Member> members = s.createCriteria(Member.class).list();
			
			Member m = members.get(0);			
			s.evict(m);
			
			m.setToVipDate(new Date(System.currentTimeMillis()));
			
			MemberVipDiscount mvd = acac.getBean(MemberVipDiscount.class);
			VipDiscountUseStatus vdus = acac.getBean(VipDiscountUseStatus.class);
			
			mvd.applyRule(m);
			vdus.applyRule(m);
			
//			m.getVipDiscountDetails().forEach(d->{
//				s.saveOrUpdate(d);
//				s.flush();
//			});
			
			System.out.println(ReflectionToStringBuilder.toString(m, ToStringStyle.MULTI_LINE_STYLE));
			m.getVipDiscountDetails().stream().forEach(d->{
				System.out.println(ReflectionToStringBuilder.toString(d, ToStringStyle.MULTI_LINE_STYLE));
			});
			
			s.saveOrUpdate(m);
			s.flush();
			s.clear();
		});
	}
	
	private static void testSetNull(){
		executeApplicationContext(acac->{
			QueryBaseService<DataChangeLog, DataChangeLog> qbs = (QueryBaseService<DataChangeLog, DataChangeLog>)acac.getBean("queryBaseService");
			qbs.setRootAndInitDefault(DataChangeLog.class);
			qbs
			.addWhere(putStr("p.docType = :pDocType"))
			.addWhere(putStr("p.docId = :pDocId"));
			qbs.getSimpleExpressions().get("pDocType").setValue(null);
			qbs.getSimpleExpressions().get("pDocId").setValue(null);
			QueryGenerator qg = qbs.toQueryGenerator();
			System.out.println(qg.toCompleteStr());
			List<DataChangeLog> list = qbs.executeQueryList();
			System.out.println("list size: " + list.size());
		});
	}
	public static void main(String[]args){
//		insertSecurityAll();
//		insertSecurity();
//		testRole();
//		selectTest();
//		testInsertDataChangeLog();
//		testBatchSize();
//		testInsertVipDiscoutnDetails();
//		testSetNull();
//		testSelfJoin();
//		testOrderBy();
		insertSecurityOther();
	}
	// this example can be used to replace subquery
	private static void testSelfJoin(){
		executeApplicationContext(acac->{
			String clz = DataChangeLog.class.getName();
			QueryBaseService<DataChangeLog, DataChangeLog> qbs = (QueryBaseService<DataChangeLog, DataChangeLog>)acac.getBean("queryBaseService");
			qbs.createFromAlias(clz, "d1")
			.createFromAlias(clz, "d2")
			.addWhere(propertyDesc("d1.docId = d2.docId"))
			.addWhere(putStr("d2.action = :d2Action", "DELETE"));
			
			QueryGenerator qg = qbs.toQueryGenerator();
			System.out.println(qg.toCompleteStr());

			List<DataChangeLog> list = qbs.executeQueryPageable();
			list.stream().forEach(d->{
				System.out.println("d id: " + d.getId());
			});
		});
	}
	
	private static void testOrderBy(){
		executeApplicationContext(acac->{
			Order orderByDocId = new Order("d1.docId", false);
			Order orderByUserId = new Order("d1.userId", false);
			
			String clz = DataChangeLog.class.getName();
			QueryBaseService<DataChangeLog, DataChangeLog> qbs = (QueryBaseService<DataChangeLog, DataChangeLog>)acac.getBean("queryBaseService");
			qbs.createFromAlias(clz, "d1")
			.createFromAlias(clz, "d2")
			.addWhere(propertyDesc("d1.docId = d2.docId"))
			.addWhere(putStr("d2.action = :d2Action", "DELETE"))
			.addOrder(orderByDocId)
			.addOrder(orderByUserId);
			
			QueryGenerator qg = qbs.toQueryGenerator();
//			System.out.println(qg.toCompleteStr());

			List<DataChangeLog> list = qbs.executeQueryPageable();
			list.stream().forEach(d->{
				System.out.println("d id: " + d.getId() + ", d docId: " + d.getDocId());
			});
		});
		
		
		
		
		
	}
}
