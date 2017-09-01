package com.angrycat.erp.web.controller;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	public void uploadShippingRawData(HttpServletResponse response, @RequestPart("uploadTarget") byte[] rawData) throws Exception{
		byte[] outputData = ShippingDetailsProcessor.renderXlsx(rawData); // TODO considering the possibility of not clearing up tmp file
		
		String fileName = "details.xlsx";
		response.setContentType(BaseQueryController.getMimeType(fileName));
		response.setHeader("Pragma", "");
		response.setHeader("cache-control", "");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		
		ServletOutputStream sos = response.getOutputStream();
		sos.write(outputData);
		sos.close();
	}
	private void blockNotAuthorized(HttpServletRequest request){
		String remoteAddr = request.getRemoteAddr();
		String remoteIP = request.getHeader("X-FORWARDED-FOR");
		System.out.println("remoteAddr: " + remoteAddr + ", remoteIP: " + remoteIP);
	}
}
