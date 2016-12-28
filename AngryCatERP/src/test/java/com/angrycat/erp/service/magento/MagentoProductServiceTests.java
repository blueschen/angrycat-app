package com.angrycat.erp.service.magento;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.ProductKendoUiService;
import com.angrycat.erp.service.magento.MagentoProductService.StockInfo;
import com.fasterxml.jackson.databind.JsonNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class MagentoProductServiceTests {
	@Autowired
	private MagentoProductService serv;
	@Autowired
	private BeanFactory beanFactory;
	@Before
	public void init(){
		serv.setBaseUrl(MagentoProductService.SERVER_LOCAL_BASE_URL);
		serv.setDebug(true);
	}
	private List<Product> mockProducts(){
		List<Product> products = new ArrayList<>();
		
//		Product p1 = new Product();
//		p1.setModelId("asus001");
//		p1.setTotalStockQty(1);
//		products.add(p1);
//		Product p2 = new Product();
//		p2.setModelId("acer001");
//		p2.setTotalStockQty(3);
//		products.add(p2);
		Product p3 = new Product();
		p3.setModelId("apple001");
		p3.setTotalStockQty(16);
		products.add(p3);
		Product p4 = new Product();
		p4.setModelId("galaxy001");
		p4.setTotalStockQty(1);
		products.add(p4);
//		Product p5 = new Product();
//		p5.setModelId("TT072(AT)");
//		p5.setTotalStockQty(11);
//		products.add(p5);
//		Product p6 = new Product();
//		p6.setModelId("TT016 (C)");
//		p6.setTotalStockQty(1);
//		products.add(p6);
//		Product p7 = new Product();
//		p7.setModelId("TT033");
//		p7.setTotalStockQty(5);
//		products.add(p7);
//		
//		Product p8 = new Product();
//		p8.setModelId("XXXdd");
//		p8.setTotalStockQty(5);
//		products.add(p8);
		
		return products;
	}
	@Test
	public void compareStock(){
//		serv.setBaseUrl(MagentoProductService.INTRANET_BASE_URL);
		
		ProductKendoUiService q = beanFactory.getBean(ProductKendoUiService.class);
		List<Product> products = q.genCondtitionsAfterExecuteQueryList().getResults();
		System.out.println("db total count:" + products.size());
		
		Map<String, StockInfo> infos = serv.filterByComparingStock(products, (magentoStock, totalStock)->true); // 將同時存在於Magento和庫存表的商品全部找出
		List<StockInfo> magentoIsMore = infos.entrySet().stream().filter(entry->entry.getValue().getMagentoStockQty() > entry.getValue().getTotalStockQty()).map(entry->entry.getValue()).collect(Collectors.toList());
		List<StockInfo> totalIsMore = infos.entrySet().stream().filter(entry->entry.getValue().getTotalStockQty() > entry.getValue().getMagentoStockQty()).map(entry->entry.getValue()).collect(Collectors.toList());
		List<StockInfo> stockIsEquals = infos.entrySet().stream().filter(entry->entry.getValue().getMagentoStockQty() == entry.getValue().getTotalStockQty()).map(entry->entry.getValue()).collect(Collectors.toList());
		System.out.println("magentoIsMore count: " + magentoIsMore.size());
		magentoIsMore.stream().forEach(si->{
			System.out.println("sku: " + si.getSku() + ", magento stock: " + si.getMagentoStockQty() + ", total stock: " + si.getTotalStockQty());
		});
		System.out.println("totalIsMore count: " + totalIsMore.size());
		totalIsMore.stream().forEach(si->{
			System.out.println("sku: " + si.getSku() + ", magento stock: " + si.getMagentoStockQty() + ", total stock: " + si.getTotalStockQty());
		});
		System.out.println("stockIsEquals count: " + stockIsEquals.size());
		stockIsEquals.stream().forEach(si->{
			System.out.println("sku: " + si.getSku() + ", magento stock: " + si.getMagentoStockQty() + ", total stock: " + si.getTotalStockQty());
		});
	}
	/**
	 * 比對Magento存在但庫存表沒有的商品
	 */
	@Test
	public void findMagentoProductNotExisted(){
//		serv.setBaseUrl(MagentoProductService.INTRANET_BASE_URL);
		
		ProductKendoUiService q = beanFactory.getBean(ProductKendoUiService.class);
		List<String> ids = q.genCondtitionsAfterExecuteQueryList().getResults().stream().map(p->p.getModelId()).collect(Collectors.toList());
//		System.out.println("db total count:" + ids.size());
		
		JsonNodeWrapper jnw = serv.listAllInventory();
		List<String> notFound = 
		jnw.filter(n->{
				String sku = n.findValue("sku").textValue();
				return !ids.contains(sku);
			}).toList(n->n.findValue("sku").textValue());
		System.out.println("notFound : " + notFound);
	}
	@Test
	public void filterTotalStockIsMore(){
		List<Product> products = mockProducts();
		List<StockInfo> infos = serv.filterByComparingStock(products, (magentoStock, totalStock)->magentoStock < totalStock).entrySet().stream().map(entry->entry.getValue()).collect(Collectors.toList());
		long count = infos.stream().filter(i->i.getTotalStockQty() <= i.getMagentoStockQty()).count();
		long expectedVal = 0;
		assertEquals(expectedVal, count);
		System.out.println("filterTotalStockIsMore: " + count);
	}
	@Test
	public void filterMagentoStockIsMore(){
		List<Product> products = mockProducts();
		List<StockInfo> infos = serv.filterByComparingStock(products, (magentoStock, totalStock)->magentoStock > totalStock).entrySet().stream().map(entry->entry.getValue()).collect(Collectors.toList());
		long count = infos.stream().filter(i->i.getTotalStockQty() >= i.getMagentoStockQty()).count();
		long expectedVal = 0;
		assertEquals(expectedVal, count);
		System.out.println("filterMagentoStockIsMore: " + count);
	}
	@Test
	public void filterStockIsEquals(){
		List<Product> products = mockProducts();
		List<StockInfo> infos = serv.filterByComparingStock(products, (magentoStock, totalStock)->magentoStock == totalStock).entrySet().stream().map(entry->entry.getValue()).collect(Collectors.toList());
		long count = infos.stream().filter(i->i.getTotalStockQty() != i.getMagentoStockQty()).count();
		long expectedVal = 0;
		assertEquals(expectedVal, count);
		System.out.println("filterStockIsEquals: " + count);
	}
	@Test
	public void listAllInventory(){
		JsonNodeWrapper jnw = serv.listAllInventory();
		jnw.consume(n->{
				JsonNodeWrapper.printObjectNodeValues(n);
			});
	}
	@Test
	public void listInventoryById(){
		JsonNodeWrapper jnw = serv.listInventoryById("7", "TT033");
		jnw.consume(n->{
				JsonNodeWrapper.printObjectNodeValues(n);
			});
	}
	@Test
	public void updateStockIfMagentoIsMore(){
		List<Product> products = mockProducts();
		JsonNodeWrapper jnw = serv.updateStockIfMagentoIsMore(products);
		jnw.getFound().forEach(n->{
			JsonNodeWrapper.printObjectNodeValues(n);
		});
	}
	@Test
	public void updateStockIfDifferentFromMagento(){
		List<Product> products = mockProducts();
		JsonNodeWrapper jnw = serv.updateStockIfDifferentFromMagento(products);
		jnw.getFound().forEach(n->{
			JsonNodeWrapper.printObjectNodeValues(n);
		});
	}
	@Test
	public void updateInventoryByProductId(){
		Map<String, Object> params = new LinkedHashMap<>();
//		params.put("2", 3);
//		params.put("4", 0);
		JsonNodeWrapper jnw = serv.updateInventoryByProductId(params);
		jnw.consume(n->{
				JsonNodeWrapper.printObjectNodeValues(n);
			});
	}
	@Test
	public void checkQtyType(){
		JsonNodeWrapper jnw = serv.listInventoryById("7");
		JsonNode valNode = jnw.getFound().get(0).findValue("qty");
		assertTrue(valNode.isTextual());
		String qty = valNode.textValue();
		System.out.println(Double.valueOf(qty).intValue());
		System.out.println(Float.valueOf(qty));
	}
	@Test
	public void filter(){
		JsonNodeWrapper jnw = serv.listAllInventory();
		List<JsonNode> filter1 = new ArrayList<>(jnw.getFound());
		List<JsonNode> filter2 = jnw
			.filter(n->n.findPath("sku").textValue().contains("TT"))
			.getFound();
		int expectedVal = 4;
		assertEquals(expectedVal, filter2.size());
		System.out.println("filter1 size: " + filter1.size() + ", filter2 size: " + filter2.size());
	}
	@Test
	public void checkIntClass(){
		int i = 3;
		Object r = i;
		assertTrue(r.getClass() == Integer.class);
		assertFalse(r.getClass() == int.class);
	}
}
