package com.angrycat.erp.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import com.angrycat.erp.excel.ShippingDetailsProcessor;

@Controller
@Scope("session")
@RequestMapping(value="/shipping")
public class ShippingController {
	
	@RequestMapping(
		value="/index",
		method=RequestMethod.GET
	)
	public String index(HttpServletRequest request){
		return "shipping/index";
	}
	@RequestMapping(
		value="/uploadShippingRawData",
		method=RequestMethod.POST,
		produces={"application/xml", "application/json"},
		headers="Accept=*/*"
	)
	public void uploadShippingRawData(
		HttpServletResponse response,
		@RequestParam("shippingDate") String shippingDate,
		@RequestPart("uploadTargetFixed") byte[] uploadTargetFixed,
		@RequestPart("uploadTargetSpecified") byte[] uploadTargetSpecified) throws Exception{
		
		Map<String, Object> options = Collections.emptyMap();
		if(shippingDate != null && !"".equals(shippingDate.trim())){
			options = new HashMap<>();
			options.put("shippingDate", shippingDate.trim());
		}
		
		List<String> warnings = new ArrayList<>();
		byte[] outputData = uploadTargetFixed != null && uploadTargetFixed.length > 0 
			? ShippingDetailsProcessor.renderXlsx(uploadTargetFixed, options, warnings)
			: ShippingDetailsProcessor.renderAgeteXlsx(uploadTargetSpecified, options, warnings); // TODO considering the possibility of not clearing up tmp file
		
		if(!warnings.isEmpty()){
			System.out.println("warnings:" + warnings);
			throw new RuntimeException(warnings.stream().map(w->"<h4>"+w+"</h4>").collect(Collectors.joining()));
		}	
			
		String fileName = "details.xlsx";
		response.setContentType(BaseQueryController.getMimeType(fileName));
		response.setHeader("Pragma", "");
		response.setHeader("cache-control", "");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		
		ServletOutputStream sos = response.getOutputStream();
		sos.write(outputData);
		sos.close();
	}
}
