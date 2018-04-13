package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.service.SalesDetailKendoUiService;

import static org.junit.Assert.*;
import static com.angrycat.erp.service.SalesDetailKendoUiService.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class SalesDetailKendoUiServiceTests {
	@Autowired
	private SalesDetailKendoUiService serv;
	@Autowired
	private SessionFactoryWrapper sfw;
	@Test
	public void findProducts(){
		List<SalesDetail> sds = new ArrayList<SalesDetail>();
		SalesDetail add1 = new SalesDetail();
		add1.setModelId("WHB069");
		SalesDetail add2 = new SalesDetail();
		add2.setModelId("WHB069");
		SalesDetail add3 = new SalesDetail();
		add3.setModelId("AMV00207");
		sds.add(add1);
		sds.add(add2);
		sds.add(add3);
		
		SalesDetail update1 = new SalesDetail();
		update1.setId("20160311-114304897-ZnpHH");
		update1.setModelId(null);
		SalesDetail update2 = new SalesDetail();
		update2.setId("20160311-114304901-kDzfR");
		update2.setModelId("  ");
		SalesDetail update3 = new SalesDetail();
		update3.setId("20160311-114304905-NRPtL");
		update3.setModelId("BGL00108");
		
		sds.add(update1);
		sds.add(update2);
		sds.add(update3);
		
		System.out.println("original product modelIds:");
		for(SalesDetail sd : sds){
			String modelId = sd.getModelId();
			System.out.println(modelId);
		}
		System.out.println("==================");
		
		System.out.println("found products:");
		sfw.executeSession(s->{
			List<Product> products = serv.findProducts(sds, s);
			products.stream().forEachOrdered(p->{
				if(p == null){
					System.out.println(p);
				}else{
					System.out.println(p.getModelId() + ":" + p);
				}
			});
		});
	}
	@Test
	public void compareStr(){
		String a = null;
		String b = "作廢";
		String c = "作廢";
		assertFalse(a == b);
		assertTrue(b == c);
	}
	static final String 待出貨 = "待出貨";
	static final String 集貨中 = "集貨中";
	static final String 調貨中 = "調貨中";
	static final String 待補貨 = "待補貨";
	static final String 已出貨 = "已出貨";
	static final String 作廢 = STATUS_INVALID;
	@Test
	public void getStockChanged(){
		List<String> salesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		for(String newStatus : salesStatuses){
			int stockChanged = serv.getStockChanged(ACTION_NEW, null, newStatus);
			assertEquals(-1, stockChanged); // 只要是新增銷售明細，所有狀態皆需減1庫存，不含作廢及null
		}
		
		List<String> otherStatuses = Arrays.asList(作廢, null);
		for(String newStatus : otherStatuses){
			int stockChanged = serv.getStockChanged(ACTION_NEW, null, newStatus);
			assertEquals(0, stockChanged); // 只要是新增銷售明細，狀態為作廢及null，庫存不動
		}
		
		for(String newStatus : salesStatuses){
			int stockChanged = serv.getStockChanged(ACTION_DELETE, null, newStatus);
			assertEquals(1, stockChanged); // 只要是刪除銷售明細，所有狀態皆需加1庫存，不含作廢
		}
		
		for(String newStatus : otherStatuses){
			int stockChanged = serv.getStockChanged(ACTION_DELETE, null, newStatus);
			assertEquals(0, stockChanged); // 只要是刪除銷售明細，狀態為作廢及null，庫存不動
		}
				
		List<String> newStatuses = new ArrayList<>(salesStatuses); // 所有狀態當作「新」狀態，包含作廢(不含null)
		Collections.addAll(newStatuses, 作廢);
		
		List<String> oldStatuses = new ArrayList<>(newStatuses); // 所有狀態當作「舊」狀態，包含作廢及null
		oldStatuses.add(null);
		
		for(String oldStatus : oldStatuses){
			for(String newStatus : newStatuses){
				int stockChanged = serv.getStockChanged(ACTION_UPDATE, oldStatus, newStatus);
				if(oldStatus != newStatus){// 新舊不同狀態
					if((oldStatus == null && !newStatus.contains(作廢)) // 舊狀態為null, 新狀態不是作廢，庫存減1
					|| (oldStatus != null && oldStatus.contains(作廢))){ // 舊狀態為「作廢」，庫存減1
						assertEquals(-1, stockChanged);
					}else if(oldStatus != null && newStatus.contains(作廢)){ // 新狀態為「作廢」便加庫存1
						assertEquals(1, stockChanged);
					}else{
						assertEquals(0, stockChanged);
					}
				}else{
					assertEquals(0, stockChanged);
				}			
			}
		}
	}
	
	@Test
	public void updateStock_update_taobao(){
		List<String> normalSalesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		
		List<String> newSalesStatuses = new ArrayList<>(normalSalesStatuses);
		newSalesStatuses.add(作廢);
		
		List<String> oldSalesStatuses = new ArrayList<>(newSalesStatuses);
		oldSalesStatuses.add(null);
		
		System.out.println("==== 更新銷售明細:淘寶庫存 ====");
		// 更新銷售明細:淘寶庫存 TODO 測試連動總庫存的錯誤是否整卻
		for(String oldStatus : oldSalesStatuses){
			for(String newStatus : newSalesStatuses){
				SalesDetail sd = new SalesDetail();
				sd.setSalePoint(SalesDetail.SALE_POINT_TAOBAO);
				sd.setId("sd001");
				sd.setSaleStatus(newStatus);
				
				Product p = new Product();
				p.setModelId("prod001");
				p.setTotalStockQty(10);
				p.setTaobaoStockQty(0);
				
				int stockChanged = serv.updateStock(ACTION_UPDATE, sd, p, oldStatus);
				if(newStatus == oldStatus){
					assertEquals(0, stockChanged);
				}else{
					String warning = p.getWarning();
					List<String> cancels = Arrays.asList(作廢, null);
					if(cancels.contains(oldStatus)){// oldStatus[作廢, null]
						if(newStatus == 作廢){
							assertEquals(0, stockChanged);
							System.out.println("oldStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動淘寶庫存: "+ stockChanged);
						}else{
							assertEquals(-1, stockChanged);
							assertEquals("減去prod001淘寶庫存1:淘寶庫存會小於0", warning);
							System.out.println("oldStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動淘寶庫存: "+ stockChanged + "("+ warning +")");
						}
					}else{
						if(!cancels.contains(newStatus)){
							assertEquals(0, stockChanged);
							System.out.println(oldStatus + "->" + newStatus + ":" + "異動淘寶庫存: "+ stockChanged);
						}else{
							assertEquals(1, stockChanged);
							System.out.println("newStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動淘寶庫存: "+ stockChanged);
						}
					}
				}
			}
		}
		System.out.println("==== 更新銷售明細:淘寶庫存 ====完");
	}
	
	@Test
	public void updateStock_update_total(){
		List<String> normalSalesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		
		List<String> newSalesStatuses = new ArrayList<>(normalSalesStatuses);
		newSalesStatuses.add(作廢);
		
		List<String> oldSalesStatuses = new ArrayList<>(newSalesStatuses);
		oldSalesStatuses.add(null);
		
		System.out.println("==== 更新銷售明細:總庫存 ====");
		// 更新銷售明細:總庫存
		for(String oldStatus : oldSalesStatuses){
			for(String newStatus : newSalesStatuses){
				SalesDetail sd = new SalesDetail();
				sd.setSalePoint(SalesDetail.SALE_POINT_ESLITE_DUNNAN);
				sd.setId("sd001");
				sd.setSaleStatus(newStatus);
				
				Product p = new Product();
				p.setModelId("prod001");
				p.setTotalStockQty(1);
				p.setTaobaoStockQty(1);
				
				int stockChanged = serv.updateStock(ACTION_UPDATE, sd, p, oldStatus);
				if(newStatus == oldStatus){
					assertEquals(0, stockChanged);
				}else{
					String warning = p.getWarning();
					List<String> cancels = Arrays.asList(作廢, null);
					if(cancels.contains(oldStatus)){// oldStatus[作廢, null]
						if(newStatus == 作廢){
							assertEquals(0, stockChanged);
							System.out.println("oldStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動總庫存: "+ stockChanged);
						}else{
							assertEquals(-1, stockChanged);
							if(p.getTotalStockQty() < 0){
								assertEquals("減去prod001總庫存1:總庫存會小於0", warning);
								System.out.println("oldStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動總庫存: "+ stockChanged + "("+ warning +")");
							}else if(p.getTotalStockQty() < p.getTaobaoStockQty()){
								assertEquals("減去prod001總庫存1:淘寶庫存已大於總庫存", warning);
								System.out.println("oldStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動總庫存: "+ stockChanged + "("+ warning +")");
							}
						}
					}else{
						if(!cancels.contains(newStatus)){
							assertEquals(0, stockChanged);
							System.out.println(oldStatus + "->" + newStatus + ":" + "異動總庫存: "+ stockChanged);
						}else{
							assertEquals(1, stockChanged);
							System.out.println("newStatus" + cancels + oldStatus + "->" + newStatus + ":" + "異動總庫存: "+ stockChanged);
						}
					}
				}
			}
		}		
		System.out.println("==== 更新銷售明細:總庫存 ====完");
	}
	
	@Test
	public void updateStock_add_taobao(){
		List<String> normalSalesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		
		List<String> newSalesStatuses = new ArrayList<>(normalSalesStatuses);
		newSalesStatuses.add(作廢);
		
		List<String> oldSalesStatuses = new ArrayList<>(newSalesStatuses);
		oldSalesStatuses.add(null);
		
		// 新增銷售明細:淘寶庫存
		System.out.println("==== 新增銷售明細:淘寶庫存 ====");
		for(String newStatus : newSalesStatuses){
			SalesDetail sd = new SalesDetail();
			sd.setSalePoint(SalesDetail.SALE_POINT_TAOBAO);
			sd.setId("sd001");
			sd.setSaleStatus(newStatus);
				
			Product p = new Product();
			p.setModelId("prod001");
			p.setTotalStockQty(1);
			p.setTaobaoStockQty(0);
				
			int stockChanged = serv.updateStock(ACTION_NEW, sd, p, null);

			List<String> cancels = Arrays.asList(作廢, null);
			if(!cancels.contains(newStatus)){
				assertEquals(-1, stockChanged);
				String warning = p.getWarning();
				assertEquals("減去prod001淘寶庫存1:淘寶庫存會小於0", warning);
				System.out.println(cancels + newStatus + ":" + "異動淘寶庫存: "+ stockChanged + "("+warning+")");
			}else{
				assertEquals(0, stockChanged);
				System.out.println(newStatus + ":" + "異動淘寶庫存: "+ stockChanged);
			}
		}
		System.out.println("==== 新增銷售明細:淘寶庫存 ====完");	
	}
	
	@Test
	public void updateStock_add_total(){
		List<String> normalSalesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		
		List<String> newSalesStatuses = new ArrayList<>(normalSalesStatuses);
		newSalesStatuses.add(作廢);
		
		List<String> oldSalesStatuses = new ArrayList<>(newSalesStatuses);
		oldSalesStatuses.add(null);
		
		// 新增:總庫存
		System.out.println("==== 新增銷售明細:總庫存 ====");
		for(String newStatus : newSalesStatuses){
			SalesDetail sd = new SalesDetail();
			sd.setSalePoint(SalesDetail.SALE_POINT_FB);
			sd.setId("sd001");
			sd.setSaleStatus(newStatus);
				
			Product p = new Product();
			p.setModelId("prod001");
			p.setTotalStockQty(0);
			p.setTaobaoStockQty(0);
				
			int stockChanged = serv.updateStock(ACTION_NEW, sd, p, null);

			List<String> cancels = Arrays.asList(作廢, null);
			if(!cancels.contains(newStatus)){
				assertEquals(-1, stockChanged);
				String warning = p.getWarning();
				assertEquals("減去prod001總庫存1:總庫存會小於0", warning);
				System.out.println(cancels + newStatus + ":" + "異動總庫存: "+ stockChanged + "("+warning+")");
			}else{
				assertEquals(0, stockChanged);
				System.out.println(newStatus + ":" + "異動總庫存: "+ stockChanged);
			}
		}
		System.out.println("==== 新增銷售明細:總庫存 ====完");
	}
	
	// 刪除:淘寶庫存 及 刪除:總庫存理論上不會在異動庫存時出現錯誤
	
	// TODO 針對連續兩筆以上相同商品的測試
	@Test
	public void updateStock_duplicate_products(){
		List<String> normalSalesStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		
		List<String> newSalesStatuses = new ArrayList<>(normalSalesStatuses);
		newSalesStatuses.add(作廢);
		
		List<String> oldSalesStatuses = new ArrayList<>(newSalesStatuses);
		oldSalesStatuses.add(null);
		
		String oldStatus = 作廢;
		String newStatus = 待出貨;
		
		SalesDetail sd = new SalesDetail();
		sd.setSalePoint(SalesDetail.SALE_POINT_TAOBAO);
		sd.setId("sd001");
		sd.setSaleStatus(newStatus);
		
		Product p = new Product();
		p.setModelId("prod001");
		p.setTotalStockQty(10);
		p.setTaobaoStockQty(0);
		
		int stockChanged = serv.updateStock(ACTION_UPDATE, sd, p, oldStatus);
		System.out.println(p.getTaobaoStockQty());
				
		stockChanged = serv.updateStock(ACTION_UPDATE, sd, p, oldStatus);		
		System.out.println(p.getTaobaoStockQty());
	}
}
