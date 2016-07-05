package com.angrycat.erp.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.model.Parameter;
@Controller
@RequestMapping(value="/parameter2")
@Scope("session")
public class Parameter2Controller extends
		KendoUiGridController<Parameter, Parameter> {
	private static final long serialVersionUID = -5262768095922252056L;
	private static final Map<String, String> filterFieldConverter;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("parameterCategory", "parameterCategory.name");
	}
	@Override
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}
	@Override
	<E extends ExcelExporter<Parameter>> E getExcelExporter() {
		// TODO Auto-generated method stub
		return null;
	}

}
