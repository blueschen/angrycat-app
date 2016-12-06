package com.angrycat.erp.model;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ProductTests {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Test
	public void testInClauseOrder(){
		sfw.executeSession(s->{
			String queryProducts = "SELECT p.id FROM " + Product.class.getName() + " p WHERE p.modelId IN (:modelIds)";
			List<String> modelIds = Arrays.asList("WHB069","CA002","BGL00108","AMV014","APY201","AMV00207","AMV014","WHB069");
			List<String> products = s.createQuery(queryProducts).setParameterList("modelIds", modelIds).list();
			System.out.println("size:"+products.size());
			products.stream().forEachOrdered(p->{
				System.out.println(p);
			});
		});
	}
}
