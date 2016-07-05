package com.angrycat.erp.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;
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
	@Test
	public void sortCollection(){
		sfw.executeSession(s->{
			String q = "SELECT p FROM " + Exam.class.getName() + " p LEFT JOIN FETCH p.items WHERE p.id = :id";
			Exam exam = (Exam)s.createQuery(q).setString("id", "20160704-102524204-mmzTq").uniqueResult();
			System.out.println("原題序");
			exam.getItems().forEach(ei->{
				System.out.println(ei.getSequence());
			});
			Collections.sort(exam.getItems(), new Comparator<ExamItem>(){
				@Override
				public int compare(ExamItem o1, ExamItem o2) {
					return o1.getSequence()-o2.getSequence();
				}
			});
			System.out.println("新題序");
			exam.getItems().forEach(ei->{
				System.out.println(ei.getSequence());
			});
		});
	}
}
