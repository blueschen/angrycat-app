package com.angrycat.erp.service.magento;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.angrycat.erp.initialize.config.RootConfig;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class MagentoOrderServiceTests {
	private static final Logger LOG = Logger.getLogger(MagentoOrderServiceTests.class.getName());
	@Autowired
	private MagentoOrderService serv;
	
	@Before
	public void init(){
		serv.setDebug(true);
		serv.setBaseUrl(MagentoBaseService.SERVER_LOCAL_BASE_URL);
	}
	
	@Test
	public void accessErrReportId(){
		String t = "report.php?id=1159657232&amp;";
		Pattern p = Pattern.compile("report.php\\?id=(\\d+)");
		Matcher m = p.matcher(t);
		while(m.find()){
			String g = m.group(1);
			LOG.info(g);
		}
	}
	
	@Test
	public void listOrderInfosByIncreIds(){
		List<String> ids = Arrays.asList("100000095");
		List<String> fields = Arrays.asList("increment_id", "customer_id", "subtotal", "grand_total", "status", "state", "customer_email");
		String r = serv.listOrderInfosByIncreIds(ids, fields);
	}
	@Test
	public void listOrderInfosByIncreIdsForNotFound(){
		List<String> ids = Arrays.asList("ddddssss");
		List<String> fields = Arrays.asList("increment_id", "customer_id", "subtotal", "grand_total", "status", "state", "customer_email");
		String r = serv.listOrderInfosByIncreIds(ids, fields);
	}
	@Test
	public void listOrdersByIncreIds(){
		List<String> ids = Arrays.asList("100000095");
		List<String> fields = Arrays.asList("order_id");
		JsonNodeWrapper r = serv.listOrdersByIncreIds(ids, fields);
		r.consume(n->JsonNodeWrapper.printObjectNodeValues(n));
	}
	@Test
	public void listOrdersByIncreIdsForNotFound(){
		List<String> ids = Arrays.asList("dddsssd");
		List<String> fields = Arrays.asList("increment_id");
		JsonNodeWrapper r = serv.listOrdersByIncreIds(ids, fields);
		r.consume(n->JsonNodeWrapper.printObjectNodeValues(n));
	}
	@Test
	public void listOrdersByIncreIdsMulti(){
		List<String> ids = Arrays.asList("100000095", "100000094");
		List<String> fields = Arrays.asList("increment_id", "order_id");
		JsonNodeWrapper r = serv.listOrdersByIncreIds(ids, fields);
		r.consume(n->JsonNodeWrapper.printObjectNodeValues(n));
	}
	@Test
	public void listOrdersByIncreIdsPartial(){
		List<String> ids = Arrays.asList("100000095", "dddsssd");
		List<String> fields = Arrays.asList("increment_id", "order_id");
		JsonNodeWrapper r = serv.listOrdersByIncreIds(ids, fields);
		r.consume(n->JsonNodeWrapper.printObjectNodeValues(n));
	}
	@Test
	public void areOrdersExisted(){
		String ids = "100000095, 100000094";
		boolean existed = serv.areOrdersExisted(ids);
		assertEquals(true, existed);
		
		ids = "100000095,dddsssd";
		existed = serv.areOrdersExisted(ids);
		assertEquals(false, existed);
	}
}
