package com.angrycat.erp.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.TransferReplyExcelExporter;
import com.angrycat.erp.model.TransferReply;

@Controller
@RequestMapping(value="/transferreply")
@Scope("session")
public class TransferReplyController extends
		KendoUiGridController<TransferReply, TransferReply> {
	private static final long serialVersionUID = 4669720322134606887L;
	@Autowired
	private TransferReplyExcelExporter excelExporter;
	
	@SuppressWarnings("unchecked")
	@Override
	ExcelExporter<TransferReply> getExcelExporter() {
		return excelExporter;
	}
	@RequestMapping(value="/addPandora",
			method=RequestMethod.GET)
	public String addPandora(Model model){
		return moduleName + "/pandoraView";
	}
}
