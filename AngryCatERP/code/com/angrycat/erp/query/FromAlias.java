package com.angrycat.erp.query;
/**
 * @author JERRY LIN
 *
 */
public class FromAlias extends Alias {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4004468306693464863L;

	public FromAlias(String alias) {
		super(alias);
	}
	private String target;

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
}
