package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.magento.MagentoProductService;

@Service
@Scope("prototype")
@Qualifier("prroductStockService")
public class ProductStockService {
	@Autowired
	private MagentoProductService magentoProductService;
	@Autowired
	private MailService mailService;
	
	@Async
	public void asyncUpdateMagentoStock(List<Product> targets){
		List<Product> cs = new ArrayList<>();
		List<String> modelIds = new ArrayList<>();
		for(Product t : targets){
			String modelId = t.getModelId();
			if(modelIds.contains(modelId)){
				continue;
			}
			modelIds.add(modelId);
			Product c = new Product();
			cs.add(c);
			
			c.setModelId(modelId);
			c.setTotalStockQty(t.getTotalStockQty());
			c.setTotalStockChangeNote(t.getTotalStockChangeNote());
		}
		try{
			magentoProductService.updateStockIfDifferentFromMagento(cs);
		}catch(Exception e){
			mailService.subject("ProductStockService asyncUpdateMagentoStock Errors")
			.content("error:\n" + e)
			.sendSimple();
		}
		System.out.println("ProductStockService asyncUpdateMagentoStock end...");	
	}
}
