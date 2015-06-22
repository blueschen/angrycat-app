package com.angrycat.erp.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.angrycat.erp.condition.ConditionConfigurable;
import com.angrycat.erp.condition.Junction;
import com.angrycat.erp.condition.Order;
import com.angrycat.erp.condition.SimpleExpression;
/**
 * 
 * @author JERRY LIN
 *
 */
public class QueryConfig implements QueryConfigurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6924389172456723909L;

	private List<String> aliases = new ArrayList<>();
	private Map<String, FromAlias> froms = new LinkedHashMap<>();
	private Map<String, AssociationAlias> associations = new LinkedHashMap<>();
	private List<String> selects = new ArrayList<>();
	private List<ConditionConfigurable> wheres = new ArrayList<>();
	private List<Order> orders = new ArrayList<>();
	private Map<String, SimpleExpression> simpleExpressions = new LinkedHashMap<>();
	private Map<String, Object> requestParams = new LinkedHashMap<>();
	private final Map<String, Predicate<QueryConfigurable>> filterStrategies = new LinkedHashMap<>();
	
	private String addAlias(String alias){
		Objects.requireNonNull(alias);
		alias = alias.trim();
		if("".equals(alias)){
			throw new RuntimeException("alias requires not empty!!");
		}
		if(aliases.contains(alias)){
			throw new RuntimeException("alias has duplicated!!");
		}
		aliases.add(alias);
		return alias;
	}
	
	private void addSimpleExpression(SimpleExpression simpleExpression){
		if(this.simpleExpressions.keySet().contains(simpleExpression.getId())){
			throw new RuntimeException("SimpleExpression id has duplicated!!");
		}
		this.simpleExpressions.put(simpleExpression.getId(), simpleExpression);
	}
	
	private void addSimpleExpressionIfMatched(ConditionConfigurable condition){
		if(condition instanceof SimpleExpression){
			SimpleExpression simpleExpression = (SimpleExpression)condition;
			addSimpleExpression(simpleExpression);
		}else if(condition instanceof Junction){
			Junction junction = (Junction)condition;
			junction.getCriterionList()
				.stream()
				.forEach(cri->{
					addSimpleExpressionIfMatched(cri);
				});
			
		}
	}
	
	private void requireAliasExisted(ConditionConfigurable condition){
		if(condition instanceof SimpleExpression){
			SimpleExpression simpleExpression = (SimpleExpression)condition;
			String propertyName = simpleExpression.getPropertyName();
			if(propertyName.contains(".")){
				String alias = propertyName.substring(0, propertyName.indexOf("."));
				if(!aliases.contains(alias)){
					throw new RuntimeException("SimpleExpression id ["+alias+"] not existed!!");
				}
			}
		}else if(condition instanceof Junction){
			Junction junction = (Junction)condition;
			junction.getCriterionList()
				.stream()
				.forEach(cri->{
					requireAliasExisted(cri);
				});
			
		}
	}
	
	private void addFilterStrategy(String id, Predicate<QueryConfigurable> filterStrategy){
		if(filterStrategies.containsKey(id)){
			throw new RuntimeException("filterStrategy id[" + id + "] has duplicated!!");
		}
		filterStrategies.put(id, filterStrategy);
	}
	
	@Override
	public QueryConfigurable createFromAlias(String target, String alias) {
		alias = addAlias(alias);
		FromAlias from = new FromAlias(alias);
		from.setTarget(target);
		froms.put(alias, from);
		return this;
	}

	@Override
	public QueryConfigurable createAssociationAlias(String associationPath,
			String alias, String on) {
		alias = addAlias(alias);
		AssociationAlias association = new AssociationAlias(alias, associationPath);
		association.setOn(on);
		associations.put(alias, association);
		return this;
	}

	@Override
	public QueryConfigurable createAssociationAliasFilterable(String associationPath,
			String alias, String on, Predicate<QueryConfigurable> filterStrategy) {
		createAssociationAlias(associationPath, alias, on);		
		addFilterStrategy(associations.get(alias).toString(), filterStrategy);
		return this;
	}
	
	@Override
	public QueryConfigurable addSelect(String target) {
		selects.add(target);
		return this;
	}

	@Override
	public QueryConfigurable addWhere(ConditionConfigurable condition) {
		requireAliasExisted(condition);
		wheres.add(condition);
		addSimpleExpressionIfMatched(condition);
		return this;
	}
	
	@Override
	public QueryConfigurable addWhereFilterable(
			ConditionConfigurable condition,
			Predicate<QueryConfigurable> filterStrategy) {
		addWhere(condition);
		addFilterStrategy(condition.toString(), filterStrategy);
		return this;
	}

	@Override
	public QueryConfigurable addOrder(Order order) {
		orders.add(order);
		return this;
	}

	@Override
	public Map<String, SimpleExpression> getSimpleExpressions() {
		return this.simpleExpressions;
	}

	@Override
	public QueryGenerator toQueryGenerator() {
		QueryGenerator gen = new QueryGenerator();
		
		List<String> fromTargets = new ArrayList<>();
		List<String> joinTargets = new ArrayList<>();
		
		// select
		gen.setSelect(StringUtils.join(this.selects, ", "));
		
		// from
		froms.forEach((alias, from)->{			
			fromTargets.add(from.getTarget() + " " + alias);
		});
		gen.setFrom(StringUtils.join(fromTargets, ", "));
		
		//association or join
		associations.forEach((alias, association)->{
			if(!filterStrategies.containsKey(association.toString())
			|| !filterStrategies.get(association.toString()).test(this)){
				joinTargets.add(" " + association.getAssociationPath() + " " + alias + (association.getOn()!=null?(" "+association.getOn()):""));
			}
		});
		gen.setJoin(StringUtils.join(joinTargets, "\n"));
		
		// where
		gen.setWhere(genWhere());
		
		// order by
		List<String> orderTypes = 
			orders.stream()
			.map(o->{return o.toString();})
			.collect(Collectors.toList());
		gen.setOrderBy(StringUtils.join(orderTypes, ", "));
		
		// root entity alias
		String rootAlias = froms.entrySet().iterator().next().getKey();
		gen.setRootAlias(rootAlias);
		
		// named parameters
		gen.setParams(genNamedParams());
		
		return gen;
	}
	
	private String genWhere(){
		final StringBuffer sb = new StringBuffer();
		Iterator<ConditionConfigurable> itr = wheres.iterator();
		List<ConditionConfigurable> notFilteredConds = new ArrayList<>(); 
		while(itr.hasNext()){
			ConditionConfigurable target = itr.next();
			if(!filterStrategies.containsKey(target.toString())
			|| !filterStrategies.get(target.toString()).test(this)){
				if(target instanceof SimpleExpression){
					SimpleExpression s = (SimpleExpression)target;
					if(!s.isIgnored()){
						notFilteredConds.add(s);
					}
				}else{
					notFilteredConds.add(target);
				}
			}
		}
		
		itr = notFilteredConds.iterator();
		while(itr.hasNext()){
			ConditionConfigurable target = itr.next();
			sb.append(target.toSqlString());
			if(itr.hasNext()){
				sb.append(" ").append("AND").append(" ");
			}
		}
		String where = sb.toString();
//		where = where.replace("\n AND 1=1", "").replace(" AND 1=1", "").replace(" OR 1=1", "");
		return where;
	}
	
	private Map<String, Object> genNamedParams(){
		
				
		List<SimpleExpression> list = 
		simpleExpressions.values()
			.stream()
			.filter(s->!s.isIgnored())
			.collect(Collectors.toList());
		Map<String, Object> namedParams = new HashMap<>();
		System.out.println("list count: " + list.size());
		list.forEach(s->{
			System.out.println("s.getId(): " + s.getId() + ", s.getFormattedValue(): " + s.getFormattedValue());
			namedParams.put(s.getId(), s.getFormattedValue());
		});
//				list
//				.stream()
//				.collect(Collectors.toMap(SimpleExpression::getId, s->s.getFormattedValue()));
		return namedParams;
	}

	@Override
	public void addRequestParam(String name, Object value) {
		requestParams.put(name, value);
	}

	@Override
	public Object getRequestParam(String name) {
		return requestParams.get(name);
	}
	
	@Override
	public void addOrderAfterClear(Order order) {
		orders.clear();
		addOrder(order);
	}
}
