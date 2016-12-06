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
import com.angrycat.erp.model.SalesDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class SalesDetailTests {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Test
	public void testInClause(){
		sfw.executeSession(s->{
			String querySalesDetail = "SELECT p FROM " + SalesDetail.class.getName() + " p WHERE p.id IN (:ids)";
			List<SalesDetail> oldDetails = s.createQuery(querySalesDetail).setParameterList("ids", Arrays.asList()).list();
		});
	}
}
