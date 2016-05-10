package com.angrycat.erp.format;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.beanutils.PropertyUtils;

import com.angrycat.erp.service.ModelPropertyService;

public class PropertyFormat implements ObjectFormat {
	private String name;
	private String property;
	private String messageFormat;
	private String dateFormat = "yyyy-MM-dd";
	private String datetimeFormat = "yyyy-MM-dd HH:mm";
	
	public PropertyFormat(String name, String property){
		this(name, property, null);
	}
	public PropertyFormat(String name, String property, String messageFormat){
		this.name = name;
		this.property = property;
		this.messageFormat = messageFormat;
	}

	@Override
	public String getValue(Object paramObject) {
		String innerGetValue = innerGetValue(paramObject);
		return getMessageFormatValue(innerGetValue);
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Object getPropertyObject(Object obj){
		try{
			return ModelPropertyService.getNestedProperty(obj, property);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	public Class<?> getPropertyType(Object obj){
		try{
			return PropertyUtils.getPropertyType(obj, property);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	public String getProperty(){
		return property;
	}
	
	protected String getMessageFormatValue(String innerGetValue){
		if(messageFormat == null){
			return innerGetValue;
		}
		return MessageFormat.format(messageFormat, new Object[]{innerGetValue});
	}
	
	private String innerGetValue(Object obj){
		Object valueObj = getPropertyObject(obj);
		if(valueObj == null){
			return "";
		}
		if(valueObj instanceof Date){
			return new SimpleDateFormat(dateFormat).format(valueObj);
		}
		if(valueObj instanceof Timestamp){
			return new SimpleDateFormat(datetimeFormat).format(valueObj);
		}
		if(valueObj instanceof Float){
			return new BigDecimal((Float)valueObj).toString();
		}
		if(valueObj instanceof Double){
			return new BigDecimal((Double)valueObj).toString();
		}
		if(valueObj instanceof Number){
			return valueObj.toString();
		}
		return valueObj.toString();
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getDatetimeFormat() {
		return datetimeFormat;
	}
	public void setDatetimeFormat(String datetimeFormat) {
		this.datetimeFormat = datetimeFormat;
	}
	

}
