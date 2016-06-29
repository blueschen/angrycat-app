package com.angrycat.erp.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegularExpressionTests {
	@Test
	public void replaceUpperORLowerCase(){
		String t1 = "p.name ASC";
		String pattern = "(asc)|(ASC)";
		Pattern p = Pattern.compile(pattern);
		Matcher m1 = p.matcher(t1);
		while(m1.find()){
			String first = m1.group(1);
			String second = m1.group(2);
			assertEquals(null, first);
			assertEquals("ASC", second);
			System.out.println("first:" + first + ", second:" + second);
		}
		
		String t2 = "p.hint asc";
		Matcher m2 = p.matcher(t2);
		while(m2.find()){
			String first = m2.group(1);
			String second = m2.group(2);
			assertEquals("asc", first);
			assertEquals(null, second);
			System.out.println("first:" + first + ", second:" + second);
		}
		
		assertEquals("p.name ", t1.replaceAll(pattern, ""));
		assertEquals("p.hint ", t2.replaceAll(pattern, ""));
	}
}
