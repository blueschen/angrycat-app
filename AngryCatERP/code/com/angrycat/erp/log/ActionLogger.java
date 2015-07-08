package com.angrycat.erp.log;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.FormatUse;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.security.User;

public class ActionLogger {
	public static void logToFile(List<ObjectFormat> formats, User user, String loggerName, Object... objs){
		Object obj = objs[0];
		
		List<String> values = 
				formats.stream()
				.filter(of->of.getValue(obj)!=null)
				.filter(of->(!(of.getValue(obj) instanceof String)) || !((String)of.getValue(obj).trim()).equals(""))
				.map(of->(getGenericValueExpression(of, obj)))
				.collect(Collectors.toList());
		String action = findActionFrom(loggerName);
		String userId = user != null ? user.getUserId() : "NotProvided";
		String msg = "{} By: {}\nitem: {}\ninfo: {}";
		String info = StringUtils.join(values, "|");
		Logger logger = LogManager.getLogger(loggerName);
		
		logger.info(
			msg, 
			action, 
			userId, 
			obj.getClass().getSimpleName(), 
			info);
	}
	
	public static <T>void logDelete(T obj, User user){
		List<ObjectFormat> formats = FormatListFactory.findFormatList(obj.getClass(), FormatUse.DELETE_LOG);
		logToFile(formats, user, "com.angrycat.erp.crud.delete", obj);
	}
	
	private static String getGenericValueExpression(ObjectFormat of, Object obj){
		String name = of.getName(); 
		String val = name + ":" + of.getValue(obj);
		return val;
	}
	
	private static String findActionFrom(String loggerName){
		int beginIndex = loggerName.lastIndexOf(".")+1;
		int endIndex = loggerName.length();
		String action = loggerName.substring(beginIndex, endIndex);
		return action;
	}
}
