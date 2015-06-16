package com.angrycat.erp.query;

import java.util.List;
/**
 * 
 * @author JERRY LIN
 *
 * @param <T>
 */
public interface QueryExecutable<T> extends QueryConfigurable {

	public List<T> executeQueryPageable();
	public List<T> executeQueryList();
	public int getCurrentPage();
	public void setCurrentPage(int currentPage);
	public int getCountPerPage();
	public void setCountPerPage(int countPerPage);
	public PageNavigator getPageNavigator();

}
