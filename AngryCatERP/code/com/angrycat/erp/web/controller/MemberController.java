package com.angrycat.erp.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.service.MemberCrudService;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping(value="/member")
@Scope("session")
public class MemberController {
	@Autowired
	@Qualifier("memberCrudService")
	private MemberCrudService memberCrudService;
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(){
		return "member/list";
	}
	
	@RequestMapping(value="/queryAll", 
			method=RequestMethod.GET,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ConditionConfig<Member> queryAll(){
		List<Member> results = memberCrudService.executeQueryPageable();
		ConditionConfig<Member> cc = memberCrudService.getConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	@RequestMapping(value="/queryCondtional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Member> queryCondtional(@RequestBody ConditionConfig<Member> conditionConfig){
		return memberCrudService.executeQueryPageable(conditionConfig);
	}
	
	@RequestMapping(value="/deleteItems",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Member> deleteItems(@RequestBody List<String> ids){
		return memberCrudService.executeQueryPageableAfterDelete(ids);
	}
	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(){
		return "member/view";
	}
	@RequestMapping(value="/save",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody Member saveOrMerge(@RequestBody Member member){
		return memberCrudService.saveOrMerge(member);
	}
	
}
