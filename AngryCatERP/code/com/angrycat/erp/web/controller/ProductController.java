package com.angrycat.erp.web.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.excel.ProductExcelExporter;
import com.angrycat.erp.initialize.StartupWebAppInitializer;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.ProductCategory;
import com.angrycat.erp.service.KendoUiService;
import com.angrycat.erp.service.ProductCategoryQueryService;
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
	@Autowired
	private ProductCategoryQueryService productCategoryQueryService;
	@Autowired
	private ProductExcelExporter productExcelExporter;
	@Autowired
	private Environment env;
	
	@Override
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}
	@Override
	@Autowired
	public <ProductKendoUiService extends KendoUiService<Product, Product>>void setKendoUiGridService(@Qualifier("productKendoUiService") ProductKendoUiService productKendoUiService) {
		kendoUiGridService = productKendoUiService;
	}
	@SuppressWarnings("unchecked")
	@Override
	ProductExcelExporter getExcelExporter() {
		return productExcelExporter;
	}
	@RequestMapping(
			value="/downloadImage/{modelId}",
			method=RequestMethod.GET)
	public void downloadImage(@PathVariable("modelId")String modelId, HttpServletResponse res){
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
	public @ResponseBody ConditionConfig<Product> queryConditional(@RequestBody ConditionConfig<Product> conditionConfig){
		long start = System.currentTimeMillis();
		ConditionConfig<Product> cc = kendoUiGridService.executeQueryPageable(conditionConfig);
		
//		String parent = StartupWebAppInitializer.getUploadRoot();
//		images.clear();
//		cc.getResults().forEach(p->{
//			String modelId = p.getModelId();
//			String imgDir = p.getImgDir();
//			String path = parent + imgDir;
//			File f = new File(path);
//			if(f.exists()){
//				images.put(modelId, f);
//			}
//		});
		String path = env.getProperty("syno.img.path.prefix");
		images.clear();
		cc.getResults().forEach(p->{
			String modelId = p.getModelId();
			String imgDir = path + modelId + ".jpg";
			File f = new File(imgDir);
			if(f.exists()){
				images.put(modelId, f);
			}
		});
		long end = System.currentTimeMillis();
		System.out.println(moduleName+ ".queryConditional() takes time: " + (end-start) + " ms");
		return cc;
	}
	@RequestMapping(value="/queryProductCategoryAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<ProductCategory> queryProductCategoryAutocomplete(@RequestBody ConditionConfig<ProductCategory> conditionConfig){
		ConditionConfig<ProductCategory> result = productCategoryQueryService.findTargetPageable(conditionConfig);
		return result;
	}
}
