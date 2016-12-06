package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		add2.setModelId("BGL00108");
		SalesDetail add3 = new SalesDetail();
		add3.setModelId("AMV00207");
		sds.add(add1);
		sds.add(add2);
		sds.add(add3);
		
		SalesDetail update1 = new SalesDetail();
		update1.setId("20160311-114304897-ZnpHH");
		update1.setModelId("AMV014");
		SalesDetail update2 = new SalesDetail();
		update2.setId("20160311-114304901-kDzfR");
		update2.setModelId("CA002");
		SalesDetail update3 = new SalesDetail();
		update3.setId("20160311-114304905-NRPtL");
		update3.setModelId("BGL00108");
		
		sds.add(update1);
		sds.add(update2);
		sds.add(update3);
		
		sfw.executeSession(s->{
			List<Product> products = serv.findProducts(sds, s);
			products.stream().forEachOrdered(p->{
				System.out.println(p.getModelId());
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
		List<String> shipStatuses = Arrays.asList(待出貨, 集貨中, 調貨中, 待補貨, 已出貨);
		shipStatuses.forEach(newStatus->{
			int stockChanged = serv.getStockChanged(ACTION_NEW, null, newStatus);
			assertEquals(-1, stockChanged);
		});
		
		shipStatuses.forEach(status->{
			int stockChanged = serv.getStockChanged(ACTION_DELETE, null, status);
			assertEquals(1, stockChanged);
		});
		assertEquals(0, serv.getStockChanged(ACTION_DELETE, null, 作廢));
				
		List<String> allStatuses = new ArrayList<>(shipStatuses);
		Collections.addAll(allStatuses, 作廢);
		List<String> copyStatuses = new ArrayList<>(allStatuses);
		allStatuses.forEach(oldStatus->{
			copyStatuses.forEach(newStatus->{
				int stockChanged = serv.getStockChanged(ACTION_UPDATE, oldStatus, newStatus);
				if(oldStatus != newStatus){
					if(oldStatus != null && oldStatus.contains(作廢)){
						assertEquals(-1, stockChanged);
					}else if(newStatus != null && newStatus.contains(作廢)){
						assertEquals(1, stockChanged);
					}
				}else{
					assertEquals(0, stockChanged);
				}
				
			});
		});
		
	}
}
