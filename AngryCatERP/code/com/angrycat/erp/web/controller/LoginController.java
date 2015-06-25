package com.angrycat.erp.web.controller;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.CrudBaseService;
import com.angrycat.erp.web.WebUtils;

@Controller
@Scope("request")
public class LoginController {
	@Autowired
	@Qualifier("crudBaseService")
	private CrudBaseService<User, User> loginCrudService;
	
	@RequestMapping(value="/login", method={RequestMethod.GET})
	public String login(
		@RequestParam("userId")String userId,
		@RequestParam("password")String password,
		Model model){
		
		String loginPath = "/login.jsp";
		if(StringUtils.isBlank(userId)
		|| StringUtils.isBlank(password)){
			model.addAttribute("loginErrMsg", "帳號或密碼不正確");
			model.addAttribute("user", WebUtils.parseToJson(new User(userId, password)));
			return "forward:" + loginPath;
		}
		
		loginCrudService.setRootAndInitDefault(User.class);		
		String rootAliasWith = CrudBaseService.DEFAULT_ROOT_ALIAS + ".";
		loginCrudService
			.addWhere(ConditionFactory.putStr(rootAliasWith+"userId=:pUserId", userId))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"password=:pPassword", password))
		;
		List<User> users = loginCrudService.executeQueryList();
		if(!users.isEmpty()){
			WebUtils.currentSession().setAttribute(WebUtils.SESSION_USER, users.get(0));
			return "redirect:/member/list";
		}
		model.addAttribute("loginErrMsg", "帳號不存在");
		model.addAttribute("user", WebUtils.parseToJson(new User(userId, password)));
		return "forward:" + loginPath;
	}
	
	
}
