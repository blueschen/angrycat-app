package com.angrycat.erp.condition;

import java.io.Serializable;
/**
 * 
 * @author JERRY LIN
 *
 */
public interface ConditionConfigurable extends Serializable {
	public String toSqlString();
}
