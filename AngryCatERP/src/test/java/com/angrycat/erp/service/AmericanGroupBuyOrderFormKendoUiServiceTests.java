package com.angrycat.erp.service;

import java.util.List;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;
import com.angrycat.erp.service.AmericanGroupBuyCalculationService.AmericanGroupBuyCalculation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class AmericanGroupBuyOrderFormKendoUiServiceTests {
	private static final Logger LOG = Logger.getLogger(AmericanGroupBuyOrderFormKendoUiServiceTests.class.getName());
	@Autowired
	private AmericanGroupBuyOrderFormKendoUiService serv;
	
	private List<AmericanGroupBuyOrderForm> mock(){
		List<AmericanGroupBuyOrderForm> orders = AmericanGroupBuyCalculationServiceTest.include("12", "33", "1.15", "8.9", "0.21");
		AmericanGroupBuyCalculation cal = serv.getAmericanGroupBuyCalculationService().setOrders(orders).calculate();
		AmericanGroupBuyOrderForm o1 = orders.get(0);
		o1.setSalesType("正取");
		o1.setProductName("aaa");
		o1.setModelId("aaa001");
		o1.setSalesNo("AGB0001");
		o1.setEmail("angrycat.it.jerrylin@gmail.com");
		o1.setTotalAmtNTD(cal.getTotalAmtNTD().intValue());
		AmericanGroupBuyOrderForm o2 = orders.get(1);
		o2.setSalesType("正取");
		o2.setProductName("bbb");
		o2.setModelId("bbb001");
		o2.setSalesNo("AGB0001");
		AmericanGroupBuyOrderForm o3 = orders.get(2);
		o3.setSalesType("備取");
		o3.setProductName("ccc");
		o3.setModelId("ccc001");
		o3.setSalesNo("AGB0001");
		AmericanGroupBuyOrderForm o4 = orders.get(3);
		o4.setSalesType("備取");
		o4.setProductName("ddd");
		o4.setModelId("ddd001");
		o4.setSalesNo("AGB0001");
		AmericanGroupBuyOrderForm o5 = orders.get(4);
		o5.setSalesType("贈品");
		o5.setProductName("eee");
		o5.setModelId("eee001");
		o5.setSalesNo("AGB0001");
		o5.setSize("17cm");
		
		return orders;
	}
	@Test
	public void sendMail(){
		List<AmericanGroupBuyOrderForm> orders = mock();
		serv.sendEmail(orders);
	}
	@Test
	public void genMailContent(){
		List<AmericanGroupBuyOrderForm> orders = mock();
		String content = serv.genMailContent(orders);
		LOG.info(content);
	}
}
