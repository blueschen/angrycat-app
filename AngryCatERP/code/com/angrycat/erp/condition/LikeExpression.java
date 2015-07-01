package com.angrycat.erp.condition;

/**
 * @author JERRY LIN
 *
 */
public class LikeExpression extends SimpleExpression {
	private boolean caseInsensitive;

	/**
	 * 
	 */
	private static final long serialVersionUID = -2803281004332709504L;
	
	private MatchMode matchMode;

	public MatchMode getMatchMode() {
		return matchMode;
	}
	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}
	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}
	@Override
	public Object getFormattedValue(){
		String temp = (String)getValue();
		if(caseInsensitive){
			temp = temp.toLowerCase();
		}
		return matchMode.transformer(temp);
	}
	@Override
	public String toSqlString() {
		String tempProperty = getPropertyName();
		if(caseInsensitive){
			tempProperty = "lower("+tempProperty+")";
		}
		return tempProperty + " " + getOperator() + " (:" + getId() + ")";
	}
}
