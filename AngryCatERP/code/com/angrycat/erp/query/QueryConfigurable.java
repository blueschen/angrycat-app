package com.angrycat.erp.query;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Predicate;

import com.angrycat.erp.condition.ConditionConfigurable;
import com.angrycat.erp.condition.Order;
import com.angrycat.erp.condition.SimpleExpression;
/**
 * configure conditions sync or async;<br>
 * adjust condition expression according to parameter value;<br>
 * export sql(hql or jpql) splittable, including named parameters
 * @author JERRY LIN
 *
 */
public interface QueryConfigurable extends Serializable {
	public QueryConfigurable createFromAlias(String target, String alias);
	public QueryConfigurable createAssociationAlias(String associationPath, String alias, String on);
	public QueryConfigurable createAssociationAliasFilterable(String associationPath, String alias, String on, Predicate<QueryConfigurable> filterStrategy);
	public QueryConfigurable addSelect(String target);
	public QueryConfigurable addWhere(ConditionConfigurable condition);
	public QueryConfigurable addWhereFilterable(ConditionConfigurable condition, Predicate<QueryConfigurable> filterStrategy);
//	public Criteria addGroupBy(String target);
//	public Criteria addHaving(Criterion criterion);
	public QueryConfigurable addOrder(Order order);
	public void addOrderAfterClear(Order order);
	
	public Map<String, SimpleExpression> getSimpleExpressions();
	public QueryGenerator toQueryGenerator();
	
	public void addRequestParam(String name, Object value);
	public Object getRequestParam(String name);
	
}
