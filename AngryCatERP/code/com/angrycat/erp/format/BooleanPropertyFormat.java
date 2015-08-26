package com.angrycat.erp.format;

public class BooleanPropertyFormat extends PropertyFormat {
	private String[] expressions;
	
	public BooleanPropertyFormat(String name, String property, String messageFormat, String[]expressions) {
		super(name, property, messageFormat);
		this.expressions = expressions;
	}
	
	public BooleanPropertyFormat(String name, String property, String[]expressions) {
		this(name, property, null, expressions);
	}
	
	public String getValue(Object obj){
		Boolean val = (Boolean)super.getPropertyObject(obj);
		if(Boolean.TRUE.equals(val)){
			return getMessageFormatValue(expressions[0]);
		}
		if(Boolean.FALSE.equals(val)){
			return getMessageFormatValue(expressions[1]);
		}
		return getMessageFormatValue(expressions[2]); 
	}

}
