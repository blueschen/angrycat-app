package com.angrycat.erp.web.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.initialize.StartupWebAppInitializer;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@Scope("session")
@RequestMapping(value="/product")
public class ProductController extends KendoUiGridController<Product, Product> {
	private static final long serialVersionUID = -738495252675656191L;
	private static final Map<String, String> filterFieldConverter;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("productCategory", "productCategory.code");
	}

	private Map<String, File> images = new LinkedHashMap<>();
	
	@Override
	<E extends ExcelExporter<Product>> E getExcelExporter() {
		// TODO Auto-generated method stub
		return null;
	}
	@RequestMapping(
			value="/downloadImage/{modelId}",
			method=RequestMethod.GET)
	public void downloadImage(@PathVariable("modelId")String modelId, HttpServletResponse res){
		System.out.println("img modelId:" + modelId);
		File f = images.get(modelId);
		if(f == null){
			return;
		}
		TestController.downloadImage(f, res);
	}
	@Override
	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryConditional(@RequestBody ConditionConfig<Product> conditionConfig){
		long start = System.currentTimeMillis();
		ConditionConfig<Product> cc = kendoUiGridService.executeQueryPageable(conditionConfig);
		
		String parent = StartupWebAppInitializer.getUploadRoot();
		images.clear();
		cc.getResults().forEach(p->{
			String modelId = p.getModelId();
			String imgDir = p.getImgDir();
			String path = parent + imgDir;
			File f = new File(path);
			if(f.exists()){
				images.put(modelId, f);
			}
		});
		
		String result = conditionConfigToJsonStr(cc);
		long end = System.currentTimeMillis();
		System.out.println(moduleName+ ".queryConditional() takes time: " + (end-start) + " ms");
		return result;
	}	
}
