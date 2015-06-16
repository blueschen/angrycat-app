package com.angrycat.erp.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
/**
 * @author JERRY LIN
 *
 */
public class QueryGenerator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1362448226556637517L;
	

	private String select;
	private String from;
	private String join;
	private String where;
	private String orderBy;
	private String groupBy;
	private String having;
	private String rootAlias;
	private Map<String, Object> params = Collections.emptyMap();
	public String getSelect() {
		return select;
	}
	public void setSelect(String select) {
		this.select = select;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getJoin() {
		return join;
	}
	public void setJoin(String join) {
		this.join = join;
	}
	public String getWhere() {
		return where;
	}
	public void setWhere(String where) {
		this.where = where;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getHaving() {
		return having;
	}
	public void setHaving(String having) {
		this.having = having;
	}
	public String getRootAlias() {
		return rootAlias;
	}
	public void setRootAlias(String rootAlias) {
		this.rootAlias = rootAlias;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public String toCompleteStr(){
		List<String> contents = new ArrayList<>();
		if(StringUtils.isNotBlank(select)){
			contents.add("SELECT " + select);
		}
		if(StringUtils.isNotBlank(from)){
			contents.add("FROM " + from);
		}
		if(StringUtils.isNotBlank(join)){
			contents.add(join);
		}
		if(StringUtils.isNotBlank(where)){
			contents.add("WHERE " + where);
		}
		if(StringUtils.isNotBlank(orderBy)){
			contents.add("ORDER BY " + orderBy);
		}
		if(StringUtils.isNotBlank(groupBy)){
			contents.add("GROUP BY " + groupBy);
		}
		if(StringUtils.isNotBlank(having)){
			contents.add("HAVING " + having);
		}
		if(contents.isEmpty()){
			return "";
		}
		return StringUtils.join(contents, "\n");
	}


}
