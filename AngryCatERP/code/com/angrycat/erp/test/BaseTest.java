package com.angrycat.erp.test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.SalesDetail;

public class BaseTest {
	
	public static void executeApplicationContext(Consumer<AnnotationConfigApplicationContext> logic){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		logic.accept(acac);
		acac.close();
	}
	
	protected static void executeSession(BiConsumer<Session, AnnotationConfigApplicationContext>c){
		executeApplicationContext(acac->{
			Session s = null;
			Transaction tx = null;
			try{
				s = acac.getBean(LocalSessionFactoryBean.class).getObject().openSession();
				tx = s.beginTransaction();
				c.accept(s, acac);
				tx.commit();
			}catch(Throwable e){
				tx.rollback();
				e.printStackTrace();
			}finally{
				
				if(s !=null && s.isOpen()){
					s.close();
				}
			}
		});
	}
	
	protected static void multilinePrint(Object obj){
		System.out.println(ReflectionToStringBuilder.toString(obj, ToStringStyle.MULTI_LINE_STYLE));
	}
	
	protected static void multilinePrint(List<?> list){
		list.stream().forEach(d->{
			multilinePrint(d);
		});
	}
	
	private static void testSubstr(){
		String t1 = "rettt.xls";
		String t2 = "eettrrggd.xlsx";
		System.out.println(t1.lastIndexOf("."));
		System.out.println(t1.length());
		System.out.println(t1.substring(t1.lastIndexOf(".")+1, t1.length()));
		
	}
	
	private static void testSystemProperty(){
		String root = System.getProperty("catalina.home");
		System.out.println(root);
	}
	
	private static void testNestedProperty(){
		Member m = new Member();
		m.setName("memberName");
		SalesDetail s = new SalesDetail();
		s.setMember(m);
		s.setFbName("fbName");
		
		String propName1 = "member.name";
		String propName2 = "fbName";
		try{
			System.out.println(PropertyUtils.getNestedProperty(s, propName1));
			System.out.println(PropertyUtils.getNestedProperty(s, propName2));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	
	public static void main(String[]args){
		testNestedProperty();
	}

	
	
	
}
