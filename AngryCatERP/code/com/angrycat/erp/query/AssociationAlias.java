package com.angrycat.erp.query;
/**
 * @author JERRY LIN
 *
 */
public class AssociationAlias extends Alias {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3116181195641128074L;
	
	private String associationPath;
	private String on;
	public AssociationAlias(String alias, String associationPath) {
		super(alias);
		this.associationPath = associationPath;
	}
	public String getAssociationPath() {
		return associationPath;
	}
	public void setAssociationPath(String associationPath) {
		this.associationPath = associationPath;
	}
	public String getOn() {
		return on;
	}
	public void setOn(String on) {
		this.on = on;
	}

}
