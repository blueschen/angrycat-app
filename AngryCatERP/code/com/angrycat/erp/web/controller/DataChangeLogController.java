package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putStr;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.ConditionFactory.putTimestampEnd;
import static com.angrycat.erp.condition.ConditionFactory.putTimestampStart;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@Scope("session")
@RequestMapping(value="/datachangelog")
public class DataChangeLogController {
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<DataChangeLog, DataChangeLog> queryListService;
	
	@PostConstruct
	public void init(){
		queryListService.setRootAndInitDefault(DataChangeLog.class);
		
		queryListService
			.addWhere(putStr("p.docType = :pDocType"))
			.addWhere(putStr("p.docId = :pDocId"))
			.addWhere(putTimestampStart("p.logTime >= :pLogTimeStart"))
			.addWhere(putTimestampEnd("p.logTime <= :pLogTimeEnd"))
			.addWhere(putStr("p.action = :pAction"))
			.addWhere(putStrCaseInsensitive("p.userName LIKE :pUserName", MatchMode.ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.userId LIKE :pUserId", MatchMode.ANYWHERE))
			;
		
		User currentUser = WebUtils.getSessionUser();
		queryListService.setUser(currentUser);
	}
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(@RequestParam("docType")String docType, @RequestParam("docId")String docId){
		queryListService.getSimpleExpressions().get("pDocType").setValue(docType);
		queryListService.getSimpleExpressions().get("pDocId").setValue(docId);
		return "datachangelog/list";
	}
	
	@RequestMapping(value="/queryAll",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<DataChangeLog> queryAll(){
		ConditionConfig<DataChangeLog> cc = queryListService.genCondtitionsAfterExecuteQueryPageable();
		return cc;
	}
	
	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<DataChangeLog> queryConditional(@RequestBody ConditionConfig<DataChangeLog> conditionConfig){
		ConditionConfig<DataChangeLog> cc = queryListService.executeQueryPageable(conditionConfig);
		return cc;
	}
}
