package com.angrycat.erp.format;

import java.util.Collection;

import org.apache.commons.beanutils.PropertyUtils;

public class DetailPropertyFormat extends PropertyFormat {

	public DetailPropertyFormat(String name, String property) {
		super(name, property);
	}
	@Override
	public Object getPropertyObject(Object obj){
		try{
			String property = getProperty();
			String masterExpression = getMasterExpression(property);
			Object master = PropertyUtils.getProperty(obj, masterExpression);
			if(master==null || !(master instanceof Collection)){
				return null;
			}
			Collection<?> c = Collection.class.cast(master);
			int size = c.size();
			int index = getDetailPropertyIndex(property);
			if(index >= size){
				return null;
			}
			return PropertyUtils.getProperty(obj, property);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	static String getMasterExpression(String property){
		String master = property.substring(0, property.indexOf("["));
		return master;
	}
	static int getDetailPropertyIndex(String property){
		String index = property.substring(property.indexOf("[")+1, property.indexOf("]"));
		return Integer.parseInt(index);
	}
}
