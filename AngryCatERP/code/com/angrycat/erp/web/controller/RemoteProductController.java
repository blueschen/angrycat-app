package com.angrycat.erp.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value="/remoteproduct")
@Scope("request")
public class RemoteProductController {
	@Autowired
	private LoginController loginController;
	
	@RequestMapping(
		value="/acceptData",
		method={RequestMethod.GET, RequestMethod.POST})
	public void acceptData(
		@RequestParam("apiUser")String apiUser,
		@RequestParam("apiKey")String apiKey,
		@RequestParam("data")String data,
		Model model){
		loginController.login(apiUser, apiKey, model);
		if(model.containsAttribute(LoginController.LOGIN_ERR_MSG)){
			return;
		}
		
	}
}
