package com.angrycat.erp.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.service.ParameterCrudService;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping("/parameter")
@Scope("session")
public class ParameterController {

	
	private static final String PARAMATER_LIST = "parameterList";
	
	@Autowired
	@Qualifier("parameterCrudService")
	private ParameterCrudService parameterCrudService;
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String showConditions(){
		return PARAMATER_LIST;
	}
	
	@RequestMapping(value="/getConditionConfig", 
			method=RequestMethod.GET,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ConditionConfig<Parameter> getConditionConfig(){
		List<Parameter> results = parameterCrudService.executeQueryPageable();
		ConditionConfig<Parameter> cc = parameterCrudService.getConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	@RequestMapping(value="/getConditionConfig",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Parameter> parseJson(@RequestBody ConditionConfig<Parameter> conditionConfig){
		Map<String, Object> c = conditionConfig.getConds();
		c.forEach((k,v)->{
			System.out.println(k + ": " + (v!=null ? v.getClass() : ""));
		});
		return parameterCrudService.executeQueryPageable(conditionConfig);
	}

}
