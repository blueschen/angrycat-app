package com.angrycat.erp.test.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
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
	public void getExistedStock(){
		String _10 = "10. 待出貨";
		String _20 = "20. 集貨中";
		String _30 = "30. 調貨中";
		String _40 = "40. 待補貨";
		String _99 = "99. 已出貨";
		
		List<String> waitings = Arrays.asList(_10, _20, _30);
		waitings.forEach(w->{
			Product p1 = serv.getExistedStock(w, _99);
			System.out.println(w + "->" + _99);
			System.out.println(ReflectionToStringBuilder.toString(p1, ToStringStyle.MULTI_LINE_STYLE));
		});
		
		
		
		
		
	}
}
