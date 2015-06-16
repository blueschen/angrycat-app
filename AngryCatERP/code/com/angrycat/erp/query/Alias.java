package com.angrycat.erp.query;

import java.io.Serializable;
/**
 * @author JERRY LIN
 *
 */
public class Alias implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3427959788895634961L;

	private String alias;

	public Alias(String alias){
		this.alias = alias;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
