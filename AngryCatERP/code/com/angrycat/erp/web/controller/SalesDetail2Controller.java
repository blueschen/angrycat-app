package com.angrycat.erp.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.SalesDetailExcelExporter;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.service.MemberQueryService;
import com.angrycat.erp.service.ProductQueryService;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping("/salesdetail2")
@Scope("session")
public class SalesDetail2Controller extends
		KendoUiGridController<SalesDetail, SalesDetail> {
	private static final long serialVersionUID = -7852973782246983654L;
	
	private static final Map<String, String> filterFieldConverter;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("member", "member.name");
	}	

	@Autowired
	private SalesDetailExcelExporter salesDetailExcelExporter;
	@Autowired
	private MemberQueryService memberQueryService;
	@Autowired
	private ProductQueryService productQueryService;
	
	@Override
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}
	
	@Override
	String conditionConfigToJsonStr(Object cc){
		String json = CommonUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}
	
	@RequestMapping(value="/queryMemberAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryMemberAutocomplete(@RequestBody ConditionConfig<Member> conditionConfig){
		String result = memberQueryService.findTargetPageable(conditionConfig);
		return result;
	}
	
	@RequestMapping(value="/queryProductAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryProductAutocomplete(@RequestBody ConditionConfig<Product> conditionConfig){
		String result = productQueryService.findTargetPageable(conditionConfig);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	SalesDetailExcelExporter getExcelExporter() {
		return salesDetailExcelExporter;
	}	
}
