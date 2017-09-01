package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putStr;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;
/**
 * 
 * @author JerryLin
 *
 * @param <T> 查詢標的，對應SELECT...SQL語法
 * @param <R> 查詢來源，對應FROM...SQL語法
 */
public abstract class BaseQueryController<T, R> implements Serializable {
	private static final long serialVersionUID = 3114068155803613052L;

	@Autowired
	@Qualifier("queryBaseService")
	protected QueryBaseService<T, R> queryBaseService; 
	
	@Autowired
	@Qualifier("queryBaseService")
	protected QueryBaseService<T, R> findTargetService; 
	/**
	 * 取得對應資料夾路徑的模組名稱。
	 * 預設模組名稱是entity的simple name，而且全為小寫字母。
	 * 小寫字母是為了符合資料夾的命名慣例。
	 * @return
	 */
	String getModule(){
		String root = getRoot().getSimpleName();
		String moduleName = root.toLowerCase();
		return moduleName;
	}
	/**
	 * 首字母小寫的駝峰式模組名稱
	 * 預設是entity的simple name，將第一個字母改成小寫
	 * 這符合Java變數命名慣例
	 * @return
	 */
	String getLowerCamelCaseModuleName(){
		String root = getRoot().getSimpleName();
		String firstLetter = root.substring(0, 1);
		String remaining = root.substring(1, root.length());
		String moduleName = firstLetter.toLowerCase() + remaining;
		return moduleName;
	}
	/**
	 * 取得要處理的entity
	 * @return
	 */
	abstract Class<R> getRoot();
	/**
	 * 在controller bean建立之後，進行額外的初始化操作。
	 * 現階段(2016-01-27)最主要的初始化工作，就是設定查詢條件，並且找到對應session的使用者
	 */
	@PostConstruct
	public void init(){
		queryBaseService.setRootAndInitDefault(getRoot());		
		findTargetService
			.createFromAlias(getRoot().getName(), "p")
			.addWhere(putStr("p.id = :pId"))
			;
	}
	
	/**
	 * 設定moduleName這個屬性，這個屬性可讓前端用來組合模組路徑
	 * @param model
	 */
	void addUrlPrefixAsModuleName(Model model){
		String urlPrefix = getModule();
		model.addAttribute("moduleName", urlPrefix);
	}
	
	/**
	 * 將條件設定(及查詢結果)手動轉成json string。
	 * 這個方法是為了因應ajax所需回應而設。
	 * @param obj
	 * @return
	 */
	String conditionConfigToJsonStr(Object obj){
		String result = CommonUtil.parseToJson(obj);
		return result;
	}
	/**
	 * 如果找到對應的entity，在傳到前端之前可執行額外動作
	 */
	void preForwardToView(T t){}
	
	@RequestMapping(value="/view/{id}",
			method=RequestMethod.GET)
	public String view(@PathVariable("id")String id, Model model){
		findTargetService.getSimpleExpressions().get("pId").setValue(id);
		List<T> targets = findTargetService.executeQueryList();
		T target = null;
		String moduleName = getModule();
		String urlPrefix = moduleName;
		if(!targets.isEmpty()){
			target = targets.get(0);
		}else{
			return urlPrefix + "/list"; // 如果在查詢和導頁之間，資料被刪掉了，就導回查詢
		}
		if(target!=null){
			preForwardToView(target);
		}
		String result = CommonUtil.parseToJson(target);
		addUrlPrefixAsModuleName(model);
		model.addAttribute(getLowerCamelCaseModuleName(), result);
		return urlPrefix + "/view";
	}
	
	/**
	 * 導頁至特定模組查詢頁
	 */
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(HttpServletRequest request, Model model){
		String urlPrefix = getModule();
		addUrlPrefixAsModuleName(model);
		return urlPrefix + "/list";
	}
	
	/**
	 * 只清除前端UI的查詢條件，無法跟後端seesion狀態一致。
	 * 比較完整的方式，是先清掉後端查詢條件，再以此狀態與前端同步。
	 */
	@RequestMapping(value="/resetConditions", 
			method=RequestMethod.GET,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ConditionConfig<T> resetConditions(){
		ConditionConfig<T> cc = queryBaseService.resetConditions();
		return cc;
	}
	
	/**
	 * 以預設分頁功能查詢全部資料，
	 * 所以不會回傳所有資料。
	 */
	@RequestMapping(value="/queryAll",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<T> queryAll(){
		ConditionConfig<T> cc = queryBaseService.genCondtitionsAfterExecuteQueryPageable();
		return cc;
	}
	/**
	 * 分頁條件查詢
	 */
	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<T> queryConditional(@RequestBody ConditionConfig<T> conditionConfig){
		ConditionConfig<T> cc = queryBaseService.executeQueryPageable(conditionConfig);
		return cc;
	}
	/**
	 * 查詢下載Excel之前，先把頁面的條件傳到後端，所以要複製查詢條件
	 * @param conditionConfig
	 * @return
	 */
	@RequestMapping(value="/copyCondition", method=RequestMethod.POST, produces={"application/xml", "application/json"})
	public @ResponseBody Map<String, String> copyCondition(@RequestBody ConditionConfig<T> conditionConfig){
		queryBaseService.copyConditionConfig(conditionConfig);
		return Collections.emptyMap();
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
		File tempFile = getExcelExporter().normal(queryBaseService);
		
		try(FileInputStream fis = new FileInputStream(tempFile);){
			writeExcelToResponse(response, fis, getDownloadExcelName());
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
	
	public static String getMimeType(String fileName){
		String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
		String mimeType = "application/octet-stream";
		if("xls".equals(extension)){
			mimeType = "application/xls";
		}else if("xlsx".equals(extension)){
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}
		return mimeType;
	}
	public static void writeExcelToResponse(HttpServletResponse response, FileInputStream fis, String fileName) throws Throwable{
		response.setContentType(getMimeType(fileName));
		response.setHeader("Pragma", "");
		response.setHeader("cache-control", "");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		
		ServletOutputStream sos = response.getOutputStream();
		IOUtils.copy(fis, sos);
		sos.close();
	}
	
	public static void main(String[]args){
		String root = SalesDetail.class.getSimpleName();
		String firstLetter = root.substring(0, 1);
		String remaining = root.substring(1, root.length());
		String moduleName = firstLetter.toLowerCase() + remaining;
		System.out.println(moduleName);
	}
}
