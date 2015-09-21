package com.angrycat.erp.condition;

import java.util.Collection;

/**
 * any condition mapping to a (set of) parameter value
 * @author JERRY LIN
 *
 */
public class SimpleExpression implements ConditionConfigurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2920983608372973805L;
	
	private String id;
	private String propertyName;
	private String operator;
	private Object value;
	private Class<?> type;
	private boolean fixed;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isIgnored(){
		Object val = getValue();
		if(null == val){
			return true;
		}
		if(val instanceof String && val.toString().trim().equals("")){
			return true;
		}
		if(val instanceof Collection && ((Collection<?>)val).isEmpty()){
			return true;
		}
		if(val instanceof Object[] && ((Object[])val).length == 0){
			return true;
		}
			return false;
	}
	
	public Object getFormattedValue(){
		Class<?> type = getType();
		Object v = getValue();
		Object returnVal = null;
		if(v.getClass() == type){
			returnVal = v;
		}else if(v instanceof String){
			String strVal = (String)v;
			if(type == Integer.class){
				returnVal = Integer.valueOf(strVal);
			}else if(type == Float.class){
				returnVal = Float.valueOf(strVal);
			}else{
				returnVal = strVal;
			}
		}else{
			returnVal = v;
		}

		return returnVal;
	}
	
	@Override
	public String toSqlString() {
		return getPropertyName() + " " + getOperator() + " (:" + getId() + ")";
	}

}
