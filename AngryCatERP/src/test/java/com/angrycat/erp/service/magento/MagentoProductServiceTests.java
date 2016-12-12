package com.angrycat.erp.service.magento;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.angrycat.erp.initialize.config.RootConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class MagentoProductServiceTests {
	@Autowired
	private MagentoProductService serv;
	@Before
	public void init(){
		serv.setBaseUrl(MagentoProductService.LOCALHOST_BASE_URL);
	}
	@Test
	public void listAllInventory(){
		JsonNodeWrapper jnw = serv.listAllInventory();
		jnw.filterObjectNode()
			.consume(n->{
				JsonNodeWrapper.printObjectNodeValues(n);
			});
	}
	@Test
	public void listInventoryById(){
		JsonNodeWrapper jnw = serv.listInventoryById("7", "TT033");
		jnw.filterObjectNode()
			.consume(n->{
				JsonNodeWrapper.printObjectNodeValues(n);
			});
	}
}
