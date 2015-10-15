package com.angrycat.erp.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.angrycat.erp.security.User;

public final class WebUtils {
	public static final String SESSION_USER = "sessionUser";
	
	public static ServletContext currentServletContext(){
		ServletContext context = currentRequest().getServletContext();
		return context;
	}
	public static HttpServletRequest currentRequest(){
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		return request;
	}
	public static HttpSession currentSession(){
		HttpSession hs = currentRequest().getSession();
		return hs;
	}
	public static User getSessionUser(){
		User user = null;
		HttpSession hs = currentSession();
		if(hs != null){
			user = (User)hs.getAttribute(SESSION_USER);
		}
		return user;
	}
	public static String getWebRootDir(){
		String root = WebUtils.currentServletContext().getRealPath("/");
		return root;
	}
	public static String getWebRootFile(String fileName){
		String root = getWebRootDir();
		String filePath = root + File.separator + fileName; 
		return filePath;
	}
}
