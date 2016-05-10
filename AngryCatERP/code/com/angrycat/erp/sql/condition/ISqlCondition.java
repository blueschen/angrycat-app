package com.angrycat.erp.sql.condition;

import com.angrycat.erp.sql.ISqlNode;
import com.angrycat.erp.sql.condition.SqlCondition.Junction;
/**
 * Sql Condition Base Interface
 * @author JerryLin
 *
 */
public interface ISqlCondition extends ISqlNode {
	ISqlCondition junction(Junction junction);
	public Junction getJunction();
	public ISqlCondition groupMark(String groupMark);
	public String getGroupMark();
}
