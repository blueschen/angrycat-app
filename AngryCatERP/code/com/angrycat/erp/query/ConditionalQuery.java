package com.angrycat.erp.query;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class ConditionalQuery<T> extends QueryConfig implements
		HibernateQueryExecutable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6758897561899832430L;

	private SessionFactory sessionFactory;
	
	private int currentPage = 1;
	private int countPerPage = 10;
	private PageNavigator pageNavigator;
	
	public ConditionalQuery(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	@Override
	public List<T> executeQueryPageable() {
		List<T> resultset = executeSession(s->{
			List<T> results = executeQueryPageable(s);
			return results;
		});			
		return resultset;
	}
	
	@Override
	public List<T> executeQueryPageable(Session s) {
		QueryGenerator generator = toQueryGenerator();
		String alias = generator.getRootAlias();
		String fromTarget = generator.getFrom();
		String selectTarget = generator.getSelect();
		String id = QueryGenerator.DEFAULT_IDENTIFIER;
		String identifier = alias + "." + id;
		
		String selectTotalCount = "SELECT COUNT(DISTINCT " + identifier + ")";
		String selectTotalCountFrom = selectTotalCount + "\nFROM " + fromTarget;
		String join =  StringUtils.isNotBlank(generator.getJoin()) ? ("\n"+generator.getJoin()) : "";
		String where =  StringUtils.isNotBlank(generator.getWhere()) ? ("\nWHERE "+generator.getWhere()) : "";
		String getTotalCount = selectTotalCountFrom + join + where;
//		System.out.println("getTotalCount:\n" + getTotalCount);
		
		String selectDistinctRootId = "SELECT DISTINCT " + identifier;
		String selectDistinctRootIdFrom = selectTotalCountFrom.replace(selectTotalCount, selectDistinctRootId);
		String orderBy = generator.getOrderByAddId();
//		String orderBy = StringUtils.isNotBlank(generator.getOrderBy()) ? ("\nORDER BY "+generator.getOrderBy()) : "\nORDER BY " + identifier + " DESC";
//		orderBy = !orderBy.contains(identifier) ? (orderBy + ", " + identifier + " DESC") : orderBy;
		String getIds = selectDistinctRootIdFrom + join + where + orderBy;
//		System.out.println("getIds:\n" + getIds);
		
		String selectDistinctRoot = "SELECT DISTINCT " + alias;
		String selectDistinctRootFrom = selectDistinctRootIdFrom.replace(selectDistinctRootId, selectDistinctRoot);
		String getEntities = selectDistinctRootFrom + ("\nWHERE " + identifier + " IN (:ids)\n" + orderBy);
//		System.out.println("getEntities:\n" + getEntities);
		
		Map<String, Object> params = generator.getParams();
		
		long startTime = System.currentTimeMillis();
		
		List<T> results = Collections.emptyList();
		int totalCount = 0;
		Iterator<Long> itr = s.createQuery(getTotalCount).setProperties(params).iterate();
		if(itr!=null && itr.hasNext()){
			totalCount = itr.next().intValue();
		}
		
		PageNavigator pn = new PageNavigator(totalCount, countPerPage);
		pn.setCurrentPage(currentPage);
		pageNavigator = pn;
		
		List<String> ids = 
			s
			.createQuery(getIds)
			.setProperties(params)
			.setMaxResults(pn.getCountPerPage())
			.setFirstResult(pn.countFirstResultIndex())
			.list();
		if(ids.isEmpty()){
			return results;
		}
		results = s.createQuery(getEntities).setParameterList("ids", ids).list();
		
		long endTime = System.currentTimeMillis();
		System.out.println("executeQueryPageable, spent sec.: " + ((endTime-startTime)/1000));
		return results;		
		
	}

	@Override
	public List<T> executeQueryList() {
		return executeSession(s->{
			return executeQueryList(s);
		});
	}
	
	@Override
	public List<T> executeQueryList(Session s) {
		QueryGenerator q = toQueryGenerator();
		String sql = q.toCompleteStr();
		Map<String, Object> params = q.getParams();
		return s.createQuery(sql).setProperties(params).list();
	}	
	@Override
	public int getCurrentPage() {
		return currentPage;
	}
	@Override
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	@Override
	public int getCountPerPage() {
		return countPerPage;
	}
	@Override
	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}
	@Override
	public PageNavigator getPageNavigator() {
		return pageNavigator;
	}
	/**
	 * abstracting session execute boilerplate code to avoid duplicate
	 * @param execution
	 * @return
	 */
	protected List<T> executeSession(Function<Session, List<T>> execution){
		Session s = null;
		List<T> resultset = Collections.emptyList();
		try{
			s = sessionFactory.openSession();
			resultset = execution.apply(s);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			s.close();
		}
		return resultset;
	}

}
