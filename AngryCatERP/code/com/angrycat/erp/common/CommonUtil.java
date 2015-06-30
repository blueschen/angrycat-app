package com.angrycat.erp.common;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class CommonUtil {
	public static String parseToJson(Object object){
		String json = "";
		try{
			ObjectMapper om = new ObjectMapper();
			json = om.writeValueAsString(object);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public static <T>T getProxyTargetObject(Object proxy){
		T t = null;
		try{
			if(AopUtils.isJdkDynamicProxy(proxy)){
				t = (T)((Advised)proxy).getTargetSource().getTarget();
			}else{
				t = (T)proxy;
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return t;
	}
}
