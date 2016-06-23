package com.angrycat.erp.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.functors.DefaultEquator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.angrycat.erp.model.ExamItem;
public class ApacheCommonsTests {
	
	
	private List<ExamItem> items1(){
		ExamItem e1 = new ExamItem();
		e1.setId("e1");
		ExamItem e2 = new ExamItem();
		e2.setId("e2");
		ExamItem e3 = new ExamItem();
		e3.setId("e3");
		
		List<ExamItem> items1 = new LinkedList<>();
		items1.add(null);
		items1.add(e2);
		items1.add(e3);
		return items1;
	}
	private List<ExamItem> items2(){
		ExamItem e2 = new ExamItem();
		e2.setId("e2");
		ExamItem e3 = new ExamItem();
		e3.setId("e3");
		ExamItem e4 = new ExamItem();
		e4.setId("e4");
		
		List<ExamItem> items2 = new LinkedList<>();
		items2.add(e2);
		items2.add(e3);
		items2.add(null);
		return items2;
	}
	private Equator<Object> idEquator(){
		return new Equator<Object>(){
			private String getId(Object obj){
				String id = null;
				try{
					String ID = "id";
					id = (String)PropertyUtils.getProperty(obj, ID);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
				return id;
			}
			@Override
			public boolean equate(Object t1, Object t2) {
				if(t1 == null
				|| t2 == null){
					return false;
				}
				String t1_id = getId(t1);
				String t2_id = getId(t2);
				if(StringUtils.isBlank(t1_id)
				|| StringUtils.isBlank(t2_id)
				|| !t1_id.equals(t2_id)){
					return false;
				}
				return true;
			}
			@Override
			public int hash(Object t) {
				if(t == null){
					return DefaultEquator.HASHCODE_NULL;
				}
				String id = getId(t);
				if(StringUtils.isBlank(id)){
					return 0;
				}
				return id.hashCode();
			}
		};
	}
	@Test
	public void retainAll(){
		List<ExamItem> items1 = items1();
		List<ExamItem> items2 = items2();
		Collection<ExamItem> retained = 
			CollectionUtils.retainAll(
				items1, 
				items2, 
				idEquator());
	
		assertEquals("Retained Collection size should be 2", 2, retained.size());
		List<String> ids = retained.stream().map(e->e.getId()).collect(Collectors.toList());
		assertTrue("ids should contain e2", ids.contains("e2"));
		assertTrue("ids should contain e3", ids.contains("e3"));
		assertTrue("items1 is not the same as retained", items1 != retained);
	}
	@Test
	public void removeAll(){
		List<ExamItem> items1 = items1();
		List<ExamItem> items2 = items2();
		Collection<ExamItem> removed = 
			CollectionUtils.removeAll(
				items1, 
				items2, 
				idEquator());
	
		assertEquals("Removed Collection size should be 1", 1, removed.size());
		assertTrue("remaining element should be null", removed.contains(null));
		assertTrue("items1 is not the same as removed", items1 != removed);
	}	
}
