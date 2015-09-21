package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.propertyDesc;
import static com.angrycat.erp.condition.ConditionFactory.putFixedStr;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.condition.Order;
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
		
		Order orderByDocId = new Order("p.docId", false);
		Order orderByUserId = new Order("p.userId", false);
		
		QueryBaseService<DataChangeLog, DataChangeLog> queryListService = getQueryListService();
		queryListService
			.createFromAlias(DataChangeLog.class.getName(), "d")
			.addWhere(propertyDesc("p.docId = d.docId"))
			.addWhere(putFixedStr("d.action = :dAction", "DELETE"))
			.addOrder(orderByDocId)
			.addOrder(orderByUserId)
			;
	}
	@Override
	String getModule(){
		return "datadeletelog";
	}
}
