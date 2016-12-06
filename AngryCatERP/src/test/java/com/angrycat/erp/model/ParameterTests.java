package com.angrycat.erp.model;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Parameter;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ParameterTests {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Test
	public void testLocaleNamesOrderBy(){
		sfw.executeSession(s->{
			String q = "SELECT p FROM " + Parameter.class.getName() + " p ORDER BY p.localeNames['discount'] DESC";
			List<Parameter> list = s.createQuery(q).list();
			list.forEach(p->{
				System.out.println(p.getLocaleNames().get("discount"));
			});
		});
	}
}
