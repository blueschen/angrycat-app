package com.angrycat.erp.service;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;

@Service
@Scope("prototype")
public class MergeBaseService<T> {
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Transactional
	public T saveOrUpdate(T t){
		Session s = sfw.currentSession();
		s.saveOrUpdate(t);
		s.flush();
		
		return t;
	}
}
