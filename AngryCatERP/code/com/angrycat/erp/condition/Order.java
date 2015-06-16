package com.angrycat.erp.condition;

import java.io.Serializable;
/**
 * @author JERRY LIN
 *
 */
public class Order implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2818134520629473301L;
	
	private String propertyName;
	private boolean ascending;
	public Order(String propertyName, boolean ascending){
		this.propertyName = propertyName;
		this.ascending = ascending;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public boolean isAscending() {
		return ascending;
	}
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	
	public static Order ascending(String propertyName){
		Order order = new Order(propertyName, true);
		return order;
	}
	public static Order descending(String propertyName){
		Order order = new Order(propertyName, false);
		return order;
	}
	public static Order translate(String content){
		if(content==null || content.trim().equals("")){
			return null;
		}
		content = content.trim().toLowerCase();
		if(content.contains("asc")){
			return ascending(content.replace("asc", "").trim());
		}
		return descending(content.replace("desc", "").trim());
	}
	public String toString(){
		return propertyName + (ascending ? " ASC" : " DESC");
	}

}
