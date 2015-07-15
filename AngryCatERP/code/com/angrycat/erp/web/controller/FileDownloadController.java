package com.angrycat.erp.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Scope("request")
@RequestMapping(value="/download")
public class FileDownloadController {
	
	private static final int BUFFER_SIZE = 4096;
	
	@RequestMapping(value="/file", method=RequestMethod.GET)
	public void downloadFile(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestParam("name") String name,
		@RequestParam("id") String id) throws IOException{
		
		String filePath = "";
		
		ServletContext context = request.getServletContext();
		String appPath = context.getRealPath("");
		System.out.println("appPath: " + appPath);
		
		String fullPath = appPath + filePath;
		File downloadFile = new File(fullPath);
		FileInputStream fis = new FileInputStream(downloadFile);
		
		String mimeType = context.getMimeType(fullPath);
		if(mimeType == null){
			mimeType = "application/octet-stream";
		}
		System.out.println("MimeType: " + mimeType);
		
		response.setContentType(mimeType);
		response.setContentLengthLong(downloadFile.length());
		
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);
		
		OutputStream os = response.getOutputStream();
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;
		
		while((bytesRead = fis.read(buffer)) != -1){
			os.write(buffer, 0, bytesRead);
		}
		
		fis.close();
		os.close();
	}
	
}
