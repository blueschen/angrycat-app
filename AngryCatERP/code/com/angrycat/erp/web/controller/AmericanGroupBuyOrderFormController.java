package com.angrycat.erp.web.controller;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.AmericanGroupBuyOrderFormExcelExporter;
import com.angrycat.erp.model.AmericanGroupBuy;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;
import com.angrycat.erp.service.AmericanGroupBuyOrderFormKendoUiService;
@Controller
@RequestMapping(value="/americangroupbuyorderform")
@Scope("session")
public class AmericanGroupBuyOrderFormController extends
		KendoUiGridController<AmericanGroupBuyOrderForm, AmericanGroupBuyOrderForm> {
	private static final long serialVersionUID = -3644268782484537105L;

	@Autowired
	private AmericanGroupBuyOrderFormKendoUiService americanGroupBuyOrderFormKendoUiService;
	@Autowired
	private AmericanGroupBuyOrderFormExcelExporter excelExporter;
	
	private String replyUri;
	
	@SuppressWarnings("unchecked")
	@Override
	AmericanGroupBuyOrderFormExcelExporter getExcelExporter() {
		return excelExporter;
	}

	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(Model model){
		AmericanGroupBuy americanGroupBuy = americanGroupBuyOrderFormKendoUiService.getAmericanGroupBuy();
		if(americanGroupBuy != null){
			model.addAttribute("americanGroupBuy", CommonUtil.parseToJson(americanGroupBuy));
			model.addAttribute("isOrderFormDisabled", americanGroupBuy.isOrderFormDisabled());
			model.addAttribute("replyUri", getReplyUri());
		}
		return moduleName + "/view";
	}
	@Override
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<AmericanGroupBuyOrderForm> batchSaveOrMerge(@RequestBody List<AmericanGroupBuyOrderForm> models){
		if(models.isEmpty()){
			return models;
		}
		List<AmericanGroupBuyOrderForm> results = americanGroupBuyOrderFormKendoUiService.batchSaveOrMerge(models, beforeSaveOrMerge());

		String replyUri = getReplyUri();
		CompletableFuture.runAsync(()->{
			americanGroupBuyOrderFormKendoUiService.sendEmail(models, replyUri);
		}).exceptionally((e)->{
			americanGroupBuyOrderFormKendoUiService.getMailService().to(JERRY).content("寄送美國團訂單失敗:\n"+e).sendSimple();
			return null;
		});

		return results;
	}
	private String getReplyUri(){
		if(StringUtils.isBlank(replyUri)){
			// 匯款回條網址
			UriComponents ucb = 
				ServletUriComponentsBuilder
					.fromCurrentContextPath()
					.pathSegment("transferreply/addAmericanGroupBuy")
					.build();
			replyUri = ucb.toUriString();
		}
		return replyUri;
	}

}
