package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.SalesDetailExcelExporter;
import com.angrycat.erp.excel.SalesDetailExcelImporter;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.SalesDetail;
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
		
		queryBaseService
			.addWhere(putStrCaseInsensitive("p.salePoint LIKE :pSalePoint", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.saleStatus LIKE :pSaleStatus", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.fbName LIKE :pFbName", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.activity LIKE :pActivity", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.modelId LIKE :pModelId", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.productName LIKE :pProductName", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.checkBillStatus LIKE :pCheckBillStatus", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.discountType LIKE :pDiscountType", ANYWHERE))
			.addWhere(putSqlDate("p.orderDate >= :pOrderDateStart"))
			.addWhere(putSqlDate("p.orderDate <= :pOrderDateEnd"))
			.addWhere(putSqlDate("p.shippingDate >= :pShippingDateStart"))
			.addWhere(putSqlDate("p.shippingDate <= :pShippingDateEnd"))
			.addWhere(putSqlDate("p.payDate >= :pPayDateStart"))
			.addWhere(putSqlDate("p.payDate <= :pPayDateEnd"))			
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
	String conditionConfigToJsonStr(Object obj){
		String result = CommonUtil.parseToJson(obj, Member.class, MemberIgnoreDetail.class);
		return result;
	}

	@Override
	String getTemplateFrom() {
		return "salesdetail_sample.xlsx";
	}

}
