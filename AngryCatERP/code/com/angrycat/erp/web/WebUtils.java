package com.angrycat.erp.web;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.angrycat.erp.security.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class WebUtils {
	public static final String SESSION_USER = "sessionUser";
	public static HttpSession currentSession(){
		HttpSession hs = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
		return hs;
	}
	public static User getSessionUser(){
		User user = (User)currentSession().getAttribute(SESSION_USER);
		return user;
	}
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
}
