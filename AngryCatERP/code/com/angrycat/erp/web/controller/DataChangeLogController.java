package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putStr;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.ConditionFactory.putTimestampEnd;
import static com.angrycat.erp.condition.ConditionFactory.putTimestampStart;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.excel.NotImplementedExcelExporter;
import com.angrycat.erp.model.DataChangeLog;

@Controller
@Scope("session")
@RequestMapping(value="/datachangelog")
public class DataChangeLogController extends BaseQueryController<DataChangeLog, DataChangeLog>{
	private static final long serialVersionUID = -7447959203751296318L;
	
	@Autowired
	private NotImplementedExcelExporter notImplementedExcelExporter;
	
	@Override
	@PostConstruct
	public void init(){
		super.init();
		
		queryBaseService
			.addWhere(putStr("p.docType = :pDocType"))
			.addWhere(putStr("p.docId = :pDocId"))
			.addWhere(putTimestampStart("p.logTime >= :pLogTimeStart"))
			.addWhere(putTimestampEnd("p.logTime <= :pLogTimeEnd"))
			.addWhere(putStr("p.action = :pAction"))
			.addWhere(putStrCaseInsensitive("p.userName LIKE :pUserName", MatchMode.ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.userId LIKE :pUserId", MatchMode.ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.docTitle LIKE :pDocTitle", MatchMode.ANYWHERE))
			;
	}
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(HttpServletRequest request, Model model){
		String docType = request.getParameter("docType");
		String docId = request.getParameter("docId");
		queryBaseService.getSimpleExpressions().get("pDocType").setValue(docType);
		queryBaseService.getSimpleExpressions().get("pDocId").setValue(docId);

		return super.list(request, model);
	}

	@Override
	Class<DataChangeLog> getRoot() {
		return DataChangeLog.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	NotImplementedExcelExporter getExcelExporter() {
		return notImplementedExcelExporter;
	}
}
