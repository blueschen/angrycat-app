package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.propertyDesc;
import static com.angrycat.erp.condition.ConditionFactory.putStr;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.service.QueryBaseService;

@Controller
@RequestMapping(value="/datadeletelog")
@Scope("session")
public class DataDeleteLogController extends DataChangeLogController {
	@Override
	@PostConstruct
	public void init(){
		super.init();
		QueryBaseService<DataChangeLog, DataChangeLog> queryListService = getQueryListService();
		queryListService
			.createFromAlias(DataChangeLog.class.getName(), "d")
			.addWhere(propertyDesc("p.docId = d.docId"))
			.addWhere(putStr("d.action = :dAction", "DELETE"));
	}
	@Override
	String getModule(){
		return "datadeletelog";
	}
}
