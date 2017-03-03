package com.angrycat.erp.component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import com.angrycat.erp.initialize.config.RootConfig;

@Component
@Scope("prototype")
@Qualifier("sessionFactoryWrapper")
public class SessionFactoryWrapper {
	
	@Autowired
	private LocalSessionFactoryBean lsfb;
	
	public LocalSessionFactoryBean getLocalSessionFactoryBean(){
		return lsfb;
	}
	public void setLocalSessionFactoryBean(LocalSessionFactoryBean lsfb){
		this.lsfb = lsfb;
	}
	public SessionFactory getSessionFactory(){
		return lsfb.getObject();
	}
	
	public Session currentSession(){
		return getSessionFactory().getCurrentSession();
	}
	
	public Session openSession(){
		return getSessionFactory().openSession();
	}
	
	public StatelessSession openStatelessSession(){
		return getSessionFactory().openStatelessSession();
	}
	
	public void closeSession(Session s, Transaction tx){
		if(tx != null && !tx.wasCommitted()){
			tx.rollback();
		}
		if(s != null){
			s.close();
		}
	}
	
	public int getBatchSize(){
		String batchSizeStr = lsfb.getConfiguration().getProperty("hibernate.jdbc.batch_size");
		int batchSize = StringUtils.isNumeric(batchSizeStr) ? Integer.parseInt(batchSizeStr) : Integer.parseInt(RootConfig.DEFAULT_BATCH_SIZE);
		return batchSize;
	}
	
	public void executeSession(Consumer<Session> c){
		Session s = null;
		try{
			s = openSession();
			c.accept(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
	}
	
	public <T>List<T> executeSession(Function<Session, List<T>> f){
		Session s = null;
		List<T> results = Collections.emptyList();
		try{
			s = openSession();
			results = f.apply(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return results;
	}
	
	public void executeTransaction(Consumer<Session> c){
		Session s = null;
		Transaction tx = null;
		try{
			s = openSession();
			tx = s.beginTransaction();
			c.accept(s);
			tx.commit();
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
	}
	
	public void executeSaveOrUpdate(Consumer<Session> c){
		executeTransaction(c);
	}
	
	public <T>List<T> executeFindResults(Function<Session, List<T>> execute){
		Session s = null;
		List<T> results = Collections.emptyList();
		try{
			s = openSession();
			results = execute.apply(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return results;
	}
}
