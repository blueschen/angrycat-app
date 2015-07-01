package com.angrycat.erp.test;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.function.Consumer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.ds.SessionExecutable;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.model.ParameterCategory;
import com.angrycat.erp.query.QueryConfig;
import com.angrycat.erp.query.QueryGenerator;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.ParameterCrudService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {
	public static void main(String[]args){
//		testInsertParameterToDB();
//		testInsertAngryCatMemberToDB();
//		testDateParse();
//		testInitParameterCrudService();
//		testInitSessionExecutable();
		testStrCaseInsensitive();
	}
	
	public static void executeSession(Consumer<Session> c){

		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		LocalSessionFactoryBean lsfb = acac.getBean(LocalSessionFactoryBean.class);
		SessionFactory sf = lsfb.getObject();
		Session s = null;
		try{
			s = sf.openSession();
			c.accept(s);
			
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			s.close();
			sf.close();
		}
		
		acac.close();		
	}
	
	public static void testInsertParameterToDB(){
		executeSession(s->{
			ParameterCategory pc = null;
			String catId = "testItem";
			String findCat = "FROM " + ParameterCategory.class.getName() + " p WHERE p.id = ?";
			Iterator<ParameterCategory> itr = s.createQuery(findCat).setString(0, catId).iterate();
			if(itr.hasNext()){
				pc = itr.next();
			}else{
				pc = new ParameterCategory();
				pc.setId(catId);
				pc.setName("測試項目");
				s.save(pc);
				s.flush();
			}
			
			for(int i = 0; i < 30; i++){
				Parameter p = new Parameter();
				p.setCode("pcode_" + i);
				p.setNameDefault("pname_" + i);
				p.setParameterCategory(pc);
				p.setSequence(i);
				p.setNote("pnote_" + i);
				
				s.save(p);
			}
			s.flush();
			s.clear();
		});
	}
	
	public static void testInsertAngryCatMemberToDB(){
		executeSession(s->{
			for(int i = 0; i < 30; i++){
				Member acm = new Member();
				acm.setAddress("aaddress_" + i);
				acm.setBirthday(new Date(System.currentTimeMillis()-1000*60*60*24*365*i));
				acm.setEmail("aemail_" + i);
				acm.setGender(i%2!=0?0:1);
				acm.setIdNo("aidNo_" + i);
				acm.setImportant(i%2!=0);
				acm.setMobile("amobile_"+i);
				acm.setName("aname_"+i);
				acm.setNameEng("anameEng_"+i);
				acm.setNote("anote_"+i);
				acm.setPostalCode("acode_"+i);
				acm.setToVipDate(new Date(System.currentTimeMillis()-1000*60*60*24*182*i));
				
				s.save(acm);
			}
			s.flush();
			s.clear();
		});
	}
	
	public static void testConvertToJson(){
		ObjectMapper om = new ObjectMapper();
		
		
	}
	
	public static void testDateParse(){
		String p = "EEE, d MMM yyyy HH:mm:ss Z";
		String d = "Fri Jun 19 2015 08:00:00 GMT+0800";
		DateFormat df = new SimpleDateFormat(p); 
		try{
			java.util.Date date = df.parse(d);
			System.out.println(date);
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	public static void testInitParameterCrudService(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		
		ParameterCrudService p = acac.getBean(ParameterCrudService.class);
		p.saveOrMerge(null);
		
		acac.close();
	}
	
	public static void testInitSessionExecutable(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		com.angrycat.erp.ds.Test<User> se = acac.getBean(com.angrycat.erp.ds.Test.class);
	
		SessionExecutable<User> u = getProxyTargetObject(se);
		u.add();
		acac.close();
	}
	
	public static <T>T getProxyTargetObject(Object proxy){
		T t = null;
		try{
			if(AopUtils.isJdkDynamicProxy(proxy)){
				t = (T)((Advised)proxy).getTargetSource().getTarget();
			}else{
				t = (T)proxy;
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return t;
	}
	
	public static void testStrCaseInsensitive(){
		QueryConfig qc = new QueryConfig();
		qc.createFromAlias("Member", "p")
		.addSelect("p")
		.addWhere(ConditionFactory.putStr("p.name LIKE :pName", MatchMode.START, "Bob"))
		.addWhere(ConditionFactory.putStrCaseInsensitive("p.nameEng LIKE :pNameEng", MatchMode.END, "John"));
		QueryGenerator qg = qc.toQueryGenerator();
		System.out.println(qg.toCompleteStr());
		qg.getParams().forEach((k,v)->{
			System.out.println("param " + k + ", value: " + v);
		});
	}
}
