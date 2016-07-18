package com.angrycat.erp.service.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.ModelPropertyService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ModelPropertyServiceTests {
	@Autowired
	private ModelPropertyService modelPropertyService;
	@Test
	public void getProductTotalStockQtyTypes(){
		Class<?>clz = modelPropertyService.getModelPropertyTypes().get(Product.class).get("totalStockQty");
		System.out.println(clz);
	}
}
