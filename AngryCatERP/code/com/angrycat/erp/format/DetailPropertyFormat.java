package com.angrycat.erp.format;

import java.util.Collection;

import static com.angrycat.erp.common.CommonUtil.getPropertyVal;

public class DetailPropertyFormat extends PropertyFormat {
	public DetailPropertyFormat(String name, String property) {
		super(name, property);
	}
	@Override
	public Object getPropertyObject(Object obj){
		String property = getProperty();
		String masterExpression = getMasterExpression(property);
		Object master = getPropertyVal(obj, masterExpression);
		if(master==null || !(master instanceof Collection)){
			return null;
		}
		Collection<?> c = Collection.class.cast(master);
		int size = c.size();
		int index = getDetailPropertyIndex(property);
		if(index >= size){
			return null;
		}
		changeNameIfHinted(obj);
		return getPropertyVal(obj, property);
	}
	private void changeNameIfHinted(Object obj){
		String name = getName();
		String field = ComplexDetailPropertyFormat.findNameTemplateField(name);
		if(field != null){
			String detailExpression = getDetailExpression(getProperty());
			Object fieldVal = getPropertyVal(obj, detailExpression + "." + field);
			setName(name.replace("{{"+field+"}}", fieldVal.toString()));
		}
	}
	static String getMasterExpression(String property){
		String master = property.substring(0, property.indexOf("["));
		return master;
	}
	static String getDetailExpression(String property){
		String detail = property.substring(0, property.indexOf("]")+1);
		return detail;
	}
	static int getDetailPropertyIndex(String property){
		String index = property.substring(property.indexOf("[")+1, property.indexOf("]"));
		return Integer.parseInt(index);
	}
}
