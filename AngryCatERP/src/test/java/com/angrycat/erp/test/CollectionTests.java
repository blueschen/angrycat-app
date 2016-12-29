package com.angrycat.erp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.angrycat.erp.model.SalesDetail;
public class CollectionTests {
	@Test
	public void testContainsAll(){
		List<String> l1 = Arrays.asList("t1", "t2");
		List<String> l2 = Arrays.asList("t1", "t2");
		assertEquals(l1.size(), l2.size());
		assertTrue(l1.containsAll(l2));
		
		List<String> l3 = Arrays.asList("t2", "t1");
		assertEquals(l1.size(), l3.size());
		assertTrue(l1.containsAll(l3));
	}
	@Test
	public void testMapImpl(){
		List<SalesDetail> l = new ArrayList<SalesDetail>();
		SalesDetail sd1 = new SalesDetail();
		sd1.setId("sd1");
		l.add(sd1);
		SalesDetail sd2 = new SalesDetail();
		sd2.setId("sd2");
		l.add(sd2);
		Map<String, SalesDetail> map = l.stream().collect(Collectors.toMap(SalesDetail::getId, Function.identity()));
		System.out.println(map.getClass());// default implemented with HashMap
		
		LinkedHashMap<String, String> m2 = new LinkedHashMap<>();
		m2.put("A", "1");
		m2.put("B", "2");
		m2.put("C", "3");
		System.out.println(m2.values().getClass());
	}
	@Test
	public void retainAll(){
		List<String> l1 = new ArrayList<>(Arrays.asList("a","b","c","d"));
		List<String> l2 = new ArrayList<>(Arrays.asList("b","c","d", "e", "f"));
		l1.retainAll(l2);
		System.out.println(l1);
		System.out.println(l2);
	}
}
