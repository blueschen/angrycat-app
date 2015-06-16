package com.angrycat.erp.condition;
/**
 * 
 * @author JERRY LIN
 *
 */
public class PropertyDescExpression implements ConditionConfigurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4300403988153005303L;

	private String propertyDesc;
	public String getPropertyDesc() {
		return propertyDesc;
	}
	public void setPropertyDesc(String propertyDesc) {
		this.propertyDesc = propertyDesc;
	}
	@Override
	public String toSqlString() {
		return propertyDesc;
	}

}
