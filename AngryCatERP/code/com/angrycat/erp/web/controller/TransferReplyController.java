package com.angrycat.erp.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.CBCTBankTransferCSVProcessor;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.TransferReplyExcelExporter;
import com.angrycat.erp.model.TransferReply;
import com.angrycat.erp.service.AmericanGroupBuyOrderFormKendoUiService;
import com.angrycat.erp.service.magento.MagentoOrderService;

@Controller
@RequestMapping(value="/transferreply")
@Scope("session")
public class TransferReplyController extends
		KendoUiGridController<TransferReply, TransferReply> {
	private static final long serialVersionUID = 4669720322134606887L;
	@Autowired
	private TransferReplyExcelExporter excelExporter;
	@Autowired @Lazy
	private AmericanGroupBuyOrderFormKendoUiService serv;
	@Autowired
	private MagentoOrderService magentoOrderService;
	@Autowired
	private CBCTBankTransferCSVProcessor csvProcessor;
	
	@SuppressWarnings("unchecked")
	@Override
	ExcelExporter<TransferReply> getExcelExporter() {
		return excelExporter;
	}
	@RequestMapping(value="/addPandora",
			method=RequestMethod.GET)
	public String addPandora(Model model){
		return moduleView();
	}
	@RequestMapping(value="/addAmericanGroupBuy",
			method=RequestMethod.GET)
	public String addAmericanGroupBuy(
		@RequestParam(required=false) String fbNickname,
		@RequestParam(required=false) String mobile,
		@RequestParam(required=false) String salesNo,
		Model model){
		Map<String, String> config = new LinkedHashMap<>();
		config.put("brand", "Pandora");
//		config.put("activity", "美國團");
		model.addAttribute("config", CommonUtil.parseToJson(config));
		if(StringUtils.isNotBlank(fbNickname)
		|| StringUtils.isNotBlank(salesNo)
		|| StringUtils.isNotBlank(mobile)){
			Map<String, String> user = new HashMap<>();
			user.put("fbNickname", fbNickname);
			user.put("mobile", mobile);
			user.put("salesNo", salesNo);
			model.addAttribute("user", CommonUtil.parseToJson(user));
		}
		return moduleView();
	}
	@RequestMapping(value="/addOHMStore",
			method=RequestMethod.GET)
	public String addOHMStore(Model model){
		Map<String, String> config = new LinkedHashMap<>();
		config.put("brand", "OHM Beads");
		config.put("salePoint", "OHM商店");
		model.addAttribute("config", CommonUtil.parseToJson(config));
		return moduleView();
	}
	public Map<String, Boolean> magentoSalesNoNotExisted(String salesNo){
		Map<String, Boolean> results = new HashMap<>();
		boolean existed = magentoOrderService.areOrdersExisted(salesNo);
		results.put("isValid", existed);
		return results;
	}
	@RequestMapping(value="/salesNoNotExisted/{salesNo}/{salePoint}", method=RequestMethod.GET)
	public @ResponseBody Map<String, Boolean> salesNoNotExisted(@PathVariable("salesNo") String salesNo, @PathVariable("salePoint") String salePoint){
		if("OHM商店".equals(salePoint)){
			return magentoSalesNoNotExisted(salesNo);
		}
		return serv.salesNoNotExisted(salesNo);
	}
	private String moduleView(){
		return moduleName + "/view";
	}
	private static String stackTraceString(Throwable e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	@RequestMapping(
		value="/uploadCsv",
		method=RequestMethod.POST,
		produces={"application/xml", "application/json"},
		headers="Accept=*/*"
	)
	public @ResponseBody Map<String, Object> uploadCsv(@RequestPart("csv") MultipartFile csv){ // 這裡也可以使用byte[]
		Map<String, Object> msg = new LinkedHashMap<>();
		if(csv == null){
			msg.put("data", "not found");
			return msg;
		}
		try{
			Map<String, String> importMsg = csvProcessor.importBytes(csv.getBytes()).updateTranferReplies();
			String kendoDataJson = (String)kendoUiGridService.getCurrentHttpSession().getAttribute(moduleName+"KendoData");
			msg.put("data", "success");
			msg.put("lastKendoData", kendoDataJson);
			msg.put("importMsg", importMsg);
		}catch(Throwable e){
			msg.put("data", "runtime err:\n" + stackTraceString(e));
		}finally{
			if(csv != null){
				try {
					csv.getInputStream().close();
				} catch (IOException e) {
					msg.put("data", "runtime err:\n" + stackTraceString(e));
				}
			}
		}
		return msg;
	}
}
