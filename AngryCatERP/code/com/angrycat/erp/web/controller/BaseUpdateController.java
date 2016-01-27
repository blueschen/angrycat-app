package com.angrycat.erp.web.controller;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;





import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;





import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;





import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.log.DataChangeLogger;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

public abstract class BaseUpdateController<T, R> extends
		BaseQueryController<T, R> {
	private static final long serialVersionUID = -5216635470444696023L;
	
	@Autowired
	private DataChangeLogger dataChangeLogger;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Override
	@PostConstruct
	public void init(){
		super.init();
		User currentUser = WebUtils.getSessionUser();
		dataChangeLogger.setUser(currentUser);
	}
	/**
	 * 新增導頁，讓使用者可以key in資料
	 */
	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(Model model){
		String urlPrefix = getModule();
		addUrlPrefixAsModuleName(model);
		return urlPrefix + "/view";
	}
	
	@RequestMapping(value="/save",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody T saveOrMerge(@RequestBody T target){
		sfw.executeSaveOrUpdate(s->{
			T oldSnapshot = null;
			String id = null;
			try{
				id = (String)PropertyUtils.getProperty(target, "id");
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			if(StringUtils.isBlank(id)){// add
				s.save(target);
				s.flush();
			}else{// update
				QueryBaseService<T, R> findTargetService =  getFindTargetService();
				findTargetService.getSimpleExpressions().get("pId").setValue(id);
				List<T> targets = findTargetService.executeQueryList(s);
				
				if(!targets.isEmpty()){
					oldSnapshot = targets.get(0);// old data detached
					s.evict(oldSnapshot);
				}
			}
			s.saveOrUpdate(target);// update member, or add or update detail
			s.flush();
			if(oldSnapshot == null){
				dataChangeLogger.logAdd(target, s);
			}else{
				dataChangeLogger.logUpdate(oldSnapshot, target, s);
			}
			s.flush();
		});
		return target;
	}
	
	@RequestMapping(value="/deleteItems",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteItems(@RequestBody List<String> ids){
		ConditionConfig<T> cc = getQueryBaseService().executeQueryPageableAfterDelete(ids);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}
	
	abstract <I extends ExcelImporter>I getExcelImporter();
	
	@RequestMapping(
			value="/uploadExcel", 
			method=RequestMethod.POST, 
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String uploadExcel(
		@RequestPart("uploadExcelFile") byte[] uploadExcelFile){
		Map<String, String> msg = getExcelImporter().persist(uploadExcelFile, dataChangeLogger);
		ConditionConfig<T> cc = getQueryBaseService().genCondtitionsAfterExecuteQueryPageable();
		cc.getMsgs().clear();
		cc.getMsgs().putAll(msg);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}
	
	/**
	 * 取得範本來源名稱
	 * @return
	 */
	abstract String getTemplateFrom();
	/**
	 * 取得範本匯出名稱
	 * @return
	 */
	String getDownloadTemplateName(){
		return "template.xlsx";
	}
	
	/**
	 * 下載範本，下載範本通常是為了匯入，所以放在BaseUpdateController中
	 * @param response
	 */
	@RequestMapping(value="/downloadTemplate", method={RequestMethod.GET, RequestMethod.POST})
	public void downloadTemplate(HttpServletResponse response){
		String filePath = WebUtils.getWebRootFile(getTemplateFrom());
		try(FileInputStream fis = new FileInputStream(filePath);){
			writeExcelToResponse(response, fis, getDownloadTemplateName());
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}


}
