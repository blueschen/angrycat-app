package com.angrycat.erp.web.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.service.CrudBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping(value="/member")
@Scope("session")
public class MemberController {
	@Autowired
	@Qualifier("crudBaseService")
	private CrudBaseService<Member, Member> memberCrudService;
	
	@Autowired
	private ExcelImporter excelImporter;
	
	@PostConstruct
	public void init(){
		memberCrudService.setRootAndInitDefault(Member.class);

		String rootAliasWith = CrudBaseService.DEFAULT_ROOT_ALIAS + ".";
		memberCrudService
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"name LIKE :pName", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putInt(rootAliasWith+"gender=:pGender"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday >= :pBirthdayStart"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday <= :pBirthdayEnd"))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"idNo LIKE :pIdNo", MatchMode.START))
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"fbNickname LIKE :pFbNickname", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"mobile LIKE :pMobile", MatchMode.START))
			.addWhere(ConditionFactory.putBoolean(rootAliasWith+"important = :pImportant"))
		;
		memberCrudService.setUser(WebUtils.getSessionUser());
	}
	
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
		ConditionConfig<Member> cc = memberCrudService.executeQueryPageableAndGenCondtitions();
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
	@RequestMapping(value="/view/{id}",
			method=RequestMethod.GET)
	public String view(@PathVariable("id")String id, Model model){
		Member member = memberCrudService.findById(id);
		String result = CommonUtil.parseToJson(member);
		model.addAttribute("member", result);
		return "member/view";
	}
	@RequestMapping(value="/save",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody Member saveOrMerge(@RequestBody Member member){
		return memberCrudService.saveOrMerge(member);
	}
	
	@RequestMapping(
			value="/uploadExcel", 
			method=RequestMethod.POST, 
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Member> uploadExcel(
		@RequestPart("uploadExcelFile") byte[] uploadExcelFile){
		Map<String, String> msg = excelImporter.persist(uploadExcelFile);
		ConditionConfig<Member> cc = memberCrudService.executeQueryPageableAndGenCondtitions();
		cc.getMsgs().clear();
		cc.getMsgs().putAll(msg);
		return cc;
	}
	
}
