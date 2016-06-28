package com.angrycat.erp.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
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
}
