package com.angrycat.erp.test;

import org.junit.Test;
public class TypeCastTests {
	@Test
	public void longToInt(){
		long num1 = 100L;
		int num2 = (int)num1;
		System.out.println("num2:" + num2);
		double num3 = 25.552;
		int num4 = (int)num3; // 無條件捨去
		System.out.println("num4:" + num4);
	}
}
