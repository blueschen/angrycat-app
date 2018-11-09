package com.angrycat.erp.web.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.SalesDetailExcelExporter;
import com.angrycat.erp.excel.SalesDetailExcelImporter;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.service.MemberQueryService;
import com.angrycat.erp.service.ProductKendoUiService;
import com.angrycat.erp.service.SalesDetailKendoUiService;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping("/salesdetail2")
@Scope("session")
public class SalesDetail2Controller extends
		KendoUiGridController<SalesDetail, SalesDetail> {
	private static final long serialVersionUID = -7852973782246983654L;
	
	private static final Map<String, String> filterFieldConverter;
	private static final List<String> parameterCatNames = 
		Arrays.asList(
			"銷售狀態",
			"銷售點",
			"折扣別",
			"付款別",
			"付款狀態",
			"郵寄方式"
		);
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("member", "member.name");
	}	

	@Autowired
	private SalesDetailExcelExporter salesDetailExcelExporter;
	@Autowired
	private MemberQueryService memberQueryService;
	@Autowired
	private ProductKendoUiService productKendoUiService;
	@Autowired
	private SalesDetailKendoUiService salesDetailKendoUiService;
	
	@Override
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}	
	@RequestMapping(value="/queryMemberAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Member> queryMemberAutocomplete(@RequestBody ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> result = memberQueryService.findTargetPageable(conditionConfig);
		return result;
	}
	
	@RequestMapping(value="/queryProductAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Product> queryProductAutocomplete(@RequestBody ConditionConfig<Product> conditionConfig){
		ConditionConfig<Product> result = productKendoUiService.findTargetPageable(conditionConfig);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	SalesDetailExcelExporter getExcelExporter() {
		return salesDetailExcelExporter;
	}
	
	@Override
	List<String> getParameterCatNames(){
		return parameterCatNames;
	}

	@Override
	BiFunction<SalesDetail, Session, SalesDetail> beforeSaveOrMerge(){
		return new BiFunction<SalesDetail, Session, SalesDetail>(){
			@Override
			public SalesDetail apply(SalesDetail sd, Session s) {
				if(StringUtils.isNotBlank(sd.getId())){
					return sd;
				}
				SalesDetailExcelImporter.findMember(sd, s);
				return sd;
			}
		};
	}
	@Override
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<SalesDetail> batchSaveOrMerge(@RequestBody List<SalesDetail> models){
		// 一筆一筆做為交易單位處理；如果透過batchSaveOrMerge直接處理models的話，代表整批當作交易單位
		/*
		for(SalesDetail m : models){
			salesDetailKendoUiService.batchSaveOrMerge(Arrays.asList(m), beforeSaveOrMerge());
		}*/
		salesDetailKendoUiService.batchSaveOrMerge(models, beforeSaveOrMerge());
		
		return models;
	}
	@Override
	@RequestMapping(value="/deleteByIds",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<?> deleteByIds(@RequestBody List<String> ids){
		List<?> deletedItems = salesDetailKendoUiService.deleteByIds(ids);
		return deletedItems;
	}
	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(HttpServletRequest request, Model model){
		request.setAttribute("moduleName", moduleName);
		request.setAttribute(moduleName + "Parameters", CommonUtil.parseToJson(kendoUiGridService.listParameters(getParameterCatNames())));
		return moduleName + "/view";
	}
}
