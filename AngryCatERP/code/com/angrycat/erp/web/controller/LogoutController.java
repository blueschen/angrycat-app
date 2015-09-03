package com.angrycat.erp.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.angrycat.erp.web.WebUtils;

@Controller
@Scope("request")
public class LogoutController {
	@RequestMapping(value="/logout", method={RequestMethod.GET, RequestMethod.POST})
	public String logout(HttpServletRequest request){
		request.getSession().removeAttribute(WebUtils.SESSION_USER);
		return "forward:" + LoginController.LOGIN_PATH;
	}
}
