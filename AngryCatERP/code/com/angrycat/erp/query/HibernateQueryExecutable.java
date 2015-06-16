package com.angrycat.erp.query;

import java.util.List;

import org.hibernate.Session;
/**
 * 
 * @author JERRY LIN
 *
 * @param <T>
 */
public interface HibernateQueryExecutable<T> extends QueryExecutable<T> {

	public List<T> executeQueryPageable(Session s);
	public List<T> executeQueryList(Session s);

}
