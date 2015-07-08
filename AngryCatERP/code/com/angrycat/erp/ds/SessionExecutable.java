package com.angrycat.erp.ds;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("prototype")
public class SessionExecutable<T> implements Test<T>{
	private LocalSessionFactoryBean lsfb;
	
	@Autowired
	public SessionExecutable(LocalSessionFactoryBean lsfb){
		this.lsfb = lsfb;
	}
	
	public void executeTXSession(Consumer<Session> consumer){
		Session s = null;
		Transaction tx = null;
		try{
			s = lsfb.getObject().openSession();
			tx = s.beginTransaction();
			
			consumer.accept(s);
			
			tx.commit();
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			if(tx != null && !tx.wasCommitted()){
				tx.rollback();
			}
			s.close();
		}
	}
	
	public List<T> executeQuerySession(Function<Session, List<T>> func){
		Session s = null;
		List<T> results = Collections.emptyList();
		try{
			s = lsfb.getObject().openSession();
			results = func.apply(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return results;
	}
	
	@Transactional
	public void executeTransaction(Consumer<Session>consumer){
		Session s = lsfb.getObject().getCurrentSession();
		consumer.accept(s);
	}
	
	public void add(){
		System.out.println("dddd");
	}
}
