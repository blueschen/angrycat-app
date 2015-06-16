package com.angrycat.erp.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * 
 * @author JERRY LIN
 *
 */
public class Junction implements ConditionConfigurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7718999213355552689L;

	private List<ConditionConfigurable> conditionList = new ArrayList<>();
	private String nature;
	
	public Junction(String nature){
		this.nature = nature;
	}
	public Junction add(ConditionConfigurable condition){
		this.conditionList.add(condition);
		return this;
	}
	public Junction addAll(ConditionConfigurable... conditions){
		this.conditionList.addAll(Arrays.asList(conditions));
		return this;
	}
	public List<ConditionConfigurable> getCriterionList(){
		return this.conditionList;
	}
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	@Override
	public String toSqlString() {
		List<ConditionConfigurable> notFilteredConds = new ArrayList<>();
		Iterator<ConditionConfigurable> itr = conditionList.iterator();
		while(itr.hasNext()){
			ConditionConfigurable c = itr.next();
			if(c instanceof SimpleExpression){
				SimpleExpression s = (SimpleExpression)c;
				if(!s.isIgnored()){
					notFilteredConds.add(s);
				}
			}else if(c instanceof Junction && !"1=1".equals(c.toSqlString())){
				notFilteredConds.add(c);
			}else{
				notFilteredConds.add(c);
			}
		}
		if(notFilteredConds.size() == 0){
			return "1=1";
		}
		
		final StringBuffer sb = new StringBuffer().append("\n(");
		itr = notFilteredConds.iterator();
		while(itr.hasNext()){
			sb.append(itr.next().toSqlString());
			if(itr.hasNext()){
				sb.append(" ")
					.append(nature)
					.append(" ");
			}
		}
		String content = sb.append(")").toString();
		return content;
	}

}
