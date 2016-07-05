package com.angrycat.erp.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.service.RandomExamService;

@Controller
@RequestMapping(value="admin")
@Scope("session")
public class AdminController {
	@Autowired
	private SessionFactoryWrapper sfw;
	@RequestMapping(value="/index",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public String index(Model model){
		Map<String, Object> settings = new LinkedHashMap<>();
		Parameter testCount = RandomExamService.findTestCount(sfw);
		settings.put("testCount", testCount);
		model.addAttribute("settings", CommonUtil.parseToJson(settings));
		return "admin/index";
	}
}
