package com.angrycat.erp.test;

import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.model.ParameterCategory;

public class Test {
	public static void main(String[]args){
		testInsertDB();
	}
	
	public static void testInsertDB(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		LocalSessionFactoryBean lsfb = acac.getBean(LocalSessionFactoryBean.class);
		SessionFactory sf = lsfb.getObject();
		Session s = null;
		try{
			s = sf.openSession();
			
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
			
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			s.close();
			sf.close();
		}
		
		acac.close();
	}
}
