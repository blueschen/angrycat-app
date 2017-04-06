package com.angrycat.erp.web.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.common.CommonUtil;
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
		config.put("transferTo", "中國信託");
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
}
