package com.angrycat.erp.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.SalesDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ConfigClassApplicationContextTests extends AbstractTransactionalJUnit4SpringContextTests{
	@Resource
	private SessionFactoryWrapper sfw;
	
	@Test
	public void countParameterRow(){
		int count = countRowsInTable("shr_parameter");
		int expected = 33;
		assertEquals(expected, count);
	}
	
	@Test
	public void executeFindResults(){
		List<SalesDetail> details = 
		sfw.executeFindResults(s->{
			String queryHql = "SELECT DISTINCT p FROM " + SalesDetail.class.getName() + " p";
			List<SalesDetail> results = s.createQuery(queryHql).list();
			return results;
		});
		int count = details.size();
		int expected = 6901;
		assertEquals(expected, count);
	}
}
