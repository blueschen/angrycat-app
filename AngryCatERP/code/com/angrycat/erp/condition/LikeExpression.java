package com.angrycat.erp.condition;

/**
 * @author JERRY LIN
 *
 */
public class LikeExpression extends SimpleExpression {

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
	@Override
	public Object getFormattedValue(){
		return matchMode.transformer().apply(this);
	}

}
