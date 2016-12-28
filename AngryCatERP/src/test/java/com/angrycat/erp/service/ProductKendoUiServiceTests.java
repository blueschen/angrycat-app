package com.angrycat.erp.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.service.ProductKendoUiService.ProductStockReport;
import com.angrycat.erp.service.magento.MagentoBaseService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ProductKendoUiServiceTests {
	@Autowired
	private ProductKendoUiService productKendoUiService;
	
	@Test
	public void generateStockReport(){
		productKendoUiService.getMagentoProductService().setBaseUrl(MagentoBaseService.INTRANET_BASE_URL);
		productKendoUiService.getMagentoProductService().setDebug(true);
		ProductStockReport report = productKendoUiService.generateStockReport();
		report.printToConsole();
	}
}
