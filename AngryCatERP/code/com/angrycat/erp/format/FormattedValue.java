package com.angrycat.erp.format;

public class FormattedValue {
	private String oldVal;
	private String newVal;
	private String name;
	public FormattedValue(String oldVal, String newVal, String name){
		this.oldVal = oldVal;
		this.newVal = newVal;
		this.name = name;
	}
	public String getOldVal() {
		return oldVal;
	}
	public String getNewVal() {
		return newVal;
	}
	public String getName() {
		return name;
	}
}
