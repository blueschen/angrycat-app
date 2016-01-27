package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putStr;
import static com.angrycat.erp.condition.MatchMode.EXACT;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.excel.SalesDetailExcelExporter;
import com.angrycat.erp.excel.SalesDetailExcelImporter;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.service.QueryBaseService;
@Controller
@RequestMapping(value="/salesdetail")
@Scope("session")
public class SalesDetailController extends BaseUpdateController<SalesDetail, SalesDetail> {
	private static final long serialVersionUID = -3399258014454138856L;
	
	@Autowired
	private SalesDetailExcelExporter salesDetailExcelExporter;
	@Autowired
	private SalesDetailExcelImporter salesDetailExcelImporter;
	
	@Override
	@PostConstruct
	public void init(){
		super.init();
		
		QueryBaseService<SalesDetail, SalesDetail> queryBaseService = getQueryBaseService();
		queryBaseService
			.addWhere(putStr("p.salePoint LIKE :pSalePoint", EXACT))
			.addWhere(putStr("p.saleStatus LIKE :pSaleStatus", EXACT))
			;
	}

	@Override
	Class<SalesDetail> getRoot() {
		return SalesDetail.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	SalesDetailExcelExporter getExcelExporter() {
		return salesDetailExcelExporter;
	}

	@SuppressWarnings("unchecked")
	@Override
	SalesDetailExcelImporter getExcelImporter() {
		return salesDetailExcelImporter;
	}

	@Override
	String getTemplateFrom() {
		return "salesdetail_sample.xlsx";
	}

}
