package com.angrycat.erp.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.service.KendoUiService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

public abstract class KendoUiGridController<T, R> implements Serializable{
	private static final long serialVersionUID = 6941560925887626505L;
	@Autowired
	KendoUiService<T, R> kendoUiGridService;	
	@Autowired
	private SessionFactoryWrapper sfw;
	private Class<T> rootType;
	String moduleName;
	
	@PostConstruct
	void init(){
		rootType = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
		kendoUiGridService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(rootType, "p");
		
		RequestMapping rm = AnnotationUtils.findAnnotation(this.getClass(), RequestMapping.class);
		String[] modulePaths = rm.value();
		moduleName = modulePaths[0].substring(1);
	}
	
	public Class<T> getRootType(){
		return rootType;
	}
	
	@RequestMapping(value="/list", method={RequestMethod.POST, RequestMethod.GET})
	public String list(HttpServletRequest request, Model model){
		request.setAttribute("moduleName", moduleName);
		String listPath = moduleName + "/list";
		return listPath;
	}
	
	String conditionConfigToJsonStr(Object cc){
		String json = kendoUiGridService.conditionConfigToJsonStr(cc);
		return json;
	}

	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryConditional(@RequestBody ConditionConfig<T> conditionConfig){
		ConditionConfig<T> cc = kendoUiGridService.executeQueryPageable(conditionConfig);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}	
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<T> batchSaveOrMerge(@RequestBody List<T> salesDetails){
		return kendoUiGridService.batchSaveOrMerge(salesDetails);
	}
	
	@RequestMapping(value="/deleteByIds",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteByIds(@RequestBody List<String> ids){
		List<?> deletedItems = kendoUiGridService.deleteByIds(ids);
		return conditionConfigToJsonStr(deletedItems);
	}
	
	abstract <E extends ExcelExporter<T>>E getExcelExporter();
	/**
	 * 取得下載Excel的名稱
	 * @return
	 */
	String getDownloadExcelName(){
		return "download.xlsx";
	}
	/**
	 * 下載/匯出Excel，這個動作在此被視為一種查詢。步驟是先查詢、接著轉成Excel，最後是下載
	 * @param response
	 */
	@RequestMapping(value="/downloadExcel", method={RequestMethod.POST, RequestMethod.GET})
	public void downloadExcel(HttpServletResponse response){
		HttpSession session = WebUtils.currentSession();
		Object kendoData = session.getAttribute(moduleName + "KendoDataPojo");
		ConditionConfig<T> conditionConfig = new ConditionConfig<T>();
		Map<String, Object> conds = new LinkedHashMap<>();
		conditionConfig.setConds(conds);
		if(kendoData != null){
			conds.put(KendoUiService.KENDO_UI_DATA, kendoData);
			
		}
		File tempFile = getExcelExporter().normal(kendoUiGridService.executeQueryScrollable(conditionConfig));
		
		try(FileInputStream fis = new FileInputStream(tempFile);){
			BaseQueryController.writeExcelToResponse(response, fis, getDownloadExcelName());
		}catch(Throwable t){
			throw new RuntimeException(t);
		}finally{
			try{
				FileUtils.forceDelete(tempFile);
			}catch(Throwable t){
				throw new RuntimeException(t);
			}
		}
	}
}