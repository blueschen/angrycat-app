package com.angrycat.erp.condition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimestampStartExpression extends SimpleExpression {
	private static final long serialVersionUID = -2529248288494538954L;
	static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DateFormat dateFormat = DEFAULT_FORMAT;
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
			str += " 00:00:00";
			try {
				returnVal = dateFormat.parse(str);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return returnVal;
	}
}
