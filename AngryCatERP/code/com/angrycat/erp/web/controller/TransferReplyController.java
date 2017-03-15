package com.angrycat.erp.web.controller;

import java.util.HashMap;
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
	
	@SuppressWarnings("unchecked")
	@Override
	ExcelExporter<TransferReply> getExcelExporter() {
		return excelExporter;
	}
	@RequestMapping(value="/addPandora",
			method=RequestMethod.GET)
	public String addPandora(Model model){
		model.addAttribute("isAmericanGroupBuy", false);
		return moduleName + "/pandoraView";
	}
	@RequestMapping(value="/addAmericanGroupBuy",
			method=RequestMethod.GET)
	public String addAmericanGroupBuy(
		@RequestParam(required=false) String fbNickname,
		@RequestParam(required=false) String mobile,
		@RequestParam(required=false) String salesNo,
		Model model){
		model.addAttribute("isAmericanGroupBuy", true);
		if(StringUtils.isNotBlank(fbNickname)
		|| StringUtils.isNotBlank(salesNo)
		|| StringUtils.isNotBlank(mobile)){
			Map<String, String> user = new HashMap<>();
			user.put("fbNickname", fbNickname);
			user.put("mobile", mobile);
			user.put("salesNo", salesNo);
			model.addAttribute("user", CommonUtil.parseToJson(user));
		}
		return moduleName + "/pandoraView";
	}
	@RequestMapping(value="/salesNoNotExisted/{salesNo}", method=RequestMethod.GET)
	public @ResponseBody Map<String, Boolean> salesNoNotExisted(@PathVariable("salesNo") String salesNo){
		return serv.salesNoNotExisted(salesNo);
	}
}
