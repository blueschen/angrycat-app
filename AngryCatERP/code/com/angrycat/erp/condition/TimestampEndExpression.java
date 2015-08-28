package com.angrycat.erp.condition;

import java.text.DateFormat;
import java.text.ParseException;

public class TimestampEndExpression extends SimpleExpression {
	private static final long serialVersionUID = 8854661518956501377L;
	private DateFormat dateFormat = TimestampStartExpression.DEFAULT_FORMAT;
	public DateFormat getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public Object getFormattedValue(){
		Object val = getValue();
		Class<?> type = getType();
		if(val == null){
			return null;
		}
		Object returnVal = null;
		if(type == val.getClass()){
			returnVal = val;
		}else if(val instanceof String){
			String str = (String)val;
			str += " 23:59:59";
			try {
				returnVal = dateFormat.parse(str);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return returnVal;
	}
}
