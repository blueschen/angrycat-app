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

@Component
@Scope("prototype")
public class SessionExecutable<T> {
	@Autowired
	private LocalSessionFactoryBean sf;
	
	public void executeTXSession(Consumer<Session> consumer){
		Session s = null;
		Transaction tx = null;
		try{
			s = sf.getObject().openSession();
			tx = s.beginTransaction();
			
			consumer.accept(s);
			
			tx.commit();
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			if(tx != null && !tx.wasCommitted()){
				System.out.println("rollback");
				tx.rollback();
			}
			s.close();
		}
	}
	
	public List<T> executeQuerySession(Function<Session, List<T>> func){
		Session s = null;
		List<T> results = Collections.emptyList();
		try{
			s = sf.getObject().openSession();
			results = func.apply(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return results;
	}
}
