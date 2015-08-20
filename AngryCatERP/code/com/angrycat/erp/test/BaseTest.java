package com.angrycat.erp.test;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.initialize.config.RootConfig;

public class BaseTest {
	
	protected static void executeSession(BiConsumer<Session, AnnotationConfigApplicationContext>c){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		Session s = null;
		try{
			s = acac.getBean(LocalSessionFactoryBean.class).getObject().openSession();
			c.accept(s, acac);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			
			if(s !=null && s.isOpen()){
				s.close();
			}
			acac.close();
		}
	}
	
	protected static void multilinePrint(Object obj){
		System.out.println(ReflectionToStringBuilder.toString(obj, ToStringStyle.MULTI_LINE_STYLE));
	}
	
	protected static void multilinePrint(List<?> list){
		list.stream().forEach(d->{
			multilinePrint(d);
		});
	}
}
