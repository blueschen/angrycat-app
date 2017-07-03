package com.angrycat.erp.service;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.ProductKendoUiService.ProductStockReport;
import com.angrycat.erp.service.magento.MagentoBaseService;

import static org.junit.Assert.*;

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
//		report.printToConsole();
		System.out.println(report.toHtml());
	}
	@Test
	public void testNegativeFormat(){
		String trTmp1 = "<tr><td>%s</td><td>%s</td><td>%s</td></tr>";
		String f1 = String.format(trTmp1, "AMV00601", -1+"", 0+"");
		assertEquals("<tr><td>AMV00601</td><td>-1</td><td>0</td></tr>", f1);
		
		String trTmp2 = "<tr><td>%s</td><td>%o</td><td>%o</td></tr>";
		String f2 = String.format(trTmp2, "AMV00601", -1, 0);
		assertEquals("<tr><td>AMV00601</td><td>37777777777</td><td>0</td></tr>", f2);
	}
	@Test
	public void sendStockReportHTML(){
		productKendoUiService.getMagentoProductService().setBaseUrl(MagentoBaseService.LOCALHOST_BASE_URL);
		productKendoUiService.getMagentoProductService().setDebug(true);
		ProductStockReport report = productKendoUiService.generateStockReport();
		productKendoUiService.sendHTMLToAdmin("測試:sendStockReportHTML", report.toHtml());
	}
	@Test
	public void sendToAdmin(){
		productKendoUiService.sendToAdmin("ProductKendoUiService.sendToAdmin", "測試內容");
	}
	@Test
	public void sendHTMLToAdmin(){
		productKendoUiService.sendHTMLToAdmin("ProductKendoUiService.sendHTMLToAdmin", "<span style='color:red;'>Happy New Year</span>");
	}
	@Test
	public void updateStockIfMagentoIsMore(){
		productKendoUiService.getMagentoProductService().setBaseUrl(MagentoBaseService.LOCALHOST_BASE_URL);
		productKendoUiService.getMagentoProductService().setDebug(true);
		productKendoUiService.updateStockIfMagentoIsMore();
	}
	@Test
	public void asyncUpdateMagentoStock(){
		Product p1 = new Product();
		p1.setModelId("TT033");
		p1.setTotalStockQty(10);
		
		Product p2 = new Product();
		p2.setModelId("galaxy001");
		p2.setTotalStockQty(15);
		
		Product p3 = new Product();
		p3.setModelId("apple001");
		p3.setTotalStockQty(23);
		
		List<Product> products = Arrays.asList(p1, p2, p3);
		
		productKendoUiService.getMagentoProductService().setBaseUrl(MagentoBaseService.LOCALHOST_BASE_URL);
		productKendoUiService.getMagentoProductService().setDebug(true);
//		productKendoUiService.asyncUpdateMagentoStock(products); // test not achievable because of asynchronous
		productKendoUiService.getMagentoProductService().updateStockIfDifferentFromMagento(products);
	}
}
