package com.angrycat.erp.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.ProductKendoUiService.ProductStockReport;
import com.angrycat.erp.service.magento.MagentoBaseService;

import static com.angrycat.erp.service.ProductKendoUiService.*;

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
	@Test
	public void strSplit(){
		String addTaobao = "taobao_+_1";
		
		String[] intentions = addTaobao.split("_");
		String type = intentions[0];
		String add = intentions[1];
		int count = Integer.parseInt(intentions[2]);
		
		assertEquals("taobao", type);
		assertEquals("+", add);
		assertEquals(1, count);
		
		String subtractTaobao = "taobao_-_3";
		intentions = subtractTaobao.split("_");
		type = intentions[0];
		add = intentions[1];
		count = Integer.parseInt(intentions[2]);
		
		assertEquals("taobao", type);
		assertEquals("-", add);
		assertEquals(3, count);
	}
	@Test
	public void adjustProdStock_Taobao(){
		List<Product> targets = new ArrayList<>();
		List<Product> olds = new ArrayList<>();
		
		Product n1 = new Product();
		n1.setId("001");
		n1.setTotalStockQty(12);
		n1.setTaobaoStockQty(10); // old value should be 8
		n1.setWarning(ADD_TAOBAO+"2");
		targets.add(n1);
		
		Product o1 = new Product();
		o1.setId("001");
		o1.setTotalStockQty(10);
		o1.setTaobaoStockQty(9); // but in db old value is 9		
		olds.add(o1);
		
		Product n2 = new Product();
		n2.setId("002");
		n2.setTotalStockQty(12); // old value should be 12
		n2.setTaobaoStockQty(10);
		n2.setWarning(ADD_TAOBAO+"3");
		targets.add(n2);
		
		Product o2 = new Product();
		o2.setId("002");
		o2.setTotalStockQty(11); //  but in db old value is 11
		o2.setTaobaoStockQty(7);		
		olds.add(o2);
		
		Product n3 = new Product();
		n3.setId("003");
		n3.setTotalStockQty(12);
		n3.setTaobaoStockQty(10); // old value should be 9
		n3.setWarning(SUBTRACT_TAOBAO+"1");
		targets.add(n3);
		
		Product o3 = new Product();
		o3.setId("003");
		o3.setTotalStockQty(12);
		o3.setTaobaoStockQty(8); //  but in db old value is 8		
		olds.add(o3);
		
		Product n4 = new Product();
		n4.setId("004");
		n4.setTotalStockQty(12); // old value should be 12
		n4.setTaobaoStockQty(10);
		n4.setWarning(SUBTRACT_TAOBAO+"2");
		targets.add(n4);
		
		Product o4 = new Product();
		o4.setId("004");
		o4.setTotalStockQty(13); //  but in db old value is 13
		o4.setTaobaoStockQty(12);		
		olds.add(o4);
		
		Product n5 = new Product();
		n5.setId("005");
		n5.setTotalStockQty(9);
		n5.setTaobaoStockQty(10); // taobao new value should be less than or equal to total new value
		n5.setWarning(ADD_TAOBAO+"2");
		targets.add(n5);
		
		Product o5 = new Product();
		o5.setId("005");
		o5.setTotalStockQty(9);
		o5.setTaobaoStockQty(8);		
		olds.add(o5);
		
		Product n6 = new Product();
		n6.setId("006");
		n6.setTotalStockQty(4);
		n6.setTaobaoStockQty(0);
		n6.setWarning(SUBTRACT_TAOBAO+"5"); // this implies taobao old value greater than total, theoretically impossible
		targets.add(n6);
		
		Product o6 = new Product();
		o6.setId("006");
		o6.setTotalStockQty(4);
		o6.setTaobaoStockQty(5);		
		olds.add(o6);
		
		Product n7 = new Product();
		n7.setId("007");
		n7.setTotalStockQty(6);
		n7.setTaobaoStockQty(0);
		n7.setWarning(SUBTRACT_TAOBAO+"5");
		targets.add(n7);
		
		Product o7 = new Product();
		o7.setId("007");
		o7.setTotalStockQty(6);
		o7.setTaobaoStockQty(5);		
		olds.add(o7);
		
		Map<String, Product> needToModifyStock = 
			targets.stream()
				.filter(p->isStockRelated(p.getWarning()) && StringUtils.isNotBlank(p.getId()))
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		
		List<Product> filterOut = productKendoUiService.adjustProdStock(targets, needToModifyStock, olds);
		
		int i = -1;
		assertEquals(1, targets.size());
		assertEquals(6, filterOut.size());
		assertEquals("增加001淘寶庫存2:淘寶庫存已先被異動", filterOut.get(++i).getWarning());
		assertEquals("增加002淘寶庫存3:總庫存已先被異動", filterOut.get(++i).getWarning());
		assertEquals("減去003淘寶庫存1:淘寶庫存已先被異動", filterOut.get(++i).getWarning());
		assertEquals("減去004淘寶庫存2:總庫存已先被異動", filterOut.get(++i).getWarning());
		assertEquals("增加005淘寶庫存2:淘寶庫存已大於總庫存", filterOut.get(++i).getWarning());
		assertEquals("減去006淘寶庫存5:總庫存跟著連動會小於0", filterOut.get(++i).getWarning());
		assertEquals("007", targets.get(0).getId());
		assertEquals(1, targets.get(0).getTotalStockQty());
	}
	@Test
	public void adjustProdStock_Total(){
		List<Product> targets = new ArrayList<>();
		List<Product> olds = new ArrayList<>();
		
		Product n1 = new Product();
		n1.setId("001");
		n1.setTotalStockQty(12); // old value should be 10
		n1.setTaobaoStockQty(3);
		n1.setWarning(ADD_TOTAL+"2");
		targets.add(n1);
		
		Product o1 = new Product();
		o1.setId("001");
		o1.setTotalStockQty(9);
		o1.setTaobaoStockQty(3); // but in db old value is 9		
		olds.add(o1);
		
		Product n2 = new Product();
		n2.setId("002");
		n2.setTotalStockQty(4); // total new value should be greater than or equal to taobao
		n2.setTaobaoStockQty(5);
		n2.setWarning(SUBTRACT_TOTAL+"3");
		targets.add(n2);
		
		Product o2 = new Product();
		o2.setId("002");
		o2.setTotalStockQty(7);
		o2.setTaobaoStockQty(5);		
		olds.add(o2);
		
		Product n3 = new Product();
		n3.setId("003");
		n3.setTotalStockQty(9);
		n3.setTaobaoStockQty(5);
		n3.setWarning(ADD_TOTAL+"5");
		targets.add(n3);
		
		Product o3 = new Product();
		o3.setId("003");
		o3.setTotalStockQty(4);
		o3.setTaobaoStockQty(5);		
		olds.add(o3);
		
		Product n4 = new Product();
		n4.setId("004");
		n4.setTotalStockQty(5);
		n4.setTaobaoStockQty(5);
		n4.setWarning(SUBTRACT_TOTAL+"4");
		targets.add(n4);
		
		Product o4 = new Product();
		o4.setId("004");
		o4.setTotalStockQty(9);
		o4.setTaobaoStockQty(5);		
		olds.add(o4);
		
		Map<String, Product> needToModifyStock = 
			targets.stream()
				.filter(p->isStockRelated(p.getWarning()) && StringUtils.isNotBlank(p.getId()))
				.collect(Collectors.toMap(Product::getId, Function.identity()));
			
		List<Product> filterOut = productKendoUiService.adjustProdStock(targets, needToModifyStock, olds);
		
		int i = -1;
		assertEquals(2, targets.size());
		assertEquals(2, filterOut.size());
		assertEquals("增加001總庫存2:總庫存已先被異動", filterOut.get(++i).getWarning());
		assertEquals("減去002總庫存3:總庫存已小於淘寶庫存", filterOut.get(++i).getWarning());
		assertEquals("003", targets.get(0).getId());
		assertEquals(9, targets.get(0).getTotalStockQty());
		assertEquals("004", targets.get(1).getId());
		assertEquals(5, targets.get(1).getTotalStockQty());
	}
	
	@Test
	public void printJson(){
		List<Product> targets = new ArrayList<>();
		
		Product n1 = new Product();
		n1.setId("001");
		n1.setTotalStockQty(12);
		n1.setTaobaoStockQty(3);
		n1.setWarning(ADD_TOTAL+"2");
		targets.add(n1);
		
		Product n2 = new Product();
		n2.setId("002");
		n2.setTotalStockQty(4);
		n2.setTaobaoStockQty(5);
		n2.setWarning(SUBTRACT_TOTAL+"3");
		targets.add(n2);
		
		Map<String, Product> needToModifyStock = 
			targets.stream()
				.filter(p->isStockRelated(p.getWarning()) && StringUtils.isNotBlank(p.getId()))
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		
		String json = ProductKendoUiService.printJson(needToModifyStock);
		System.out.println(json);
	}
}
