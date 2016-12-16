package com.angrycat.erp.test;
import org.junit.Test;
public class OverrideMethodTests {
	static class Parent{
		public void m1(){
			System.out.println("This is Parent m1");
		}
		public void m2(){
			m1();
			System.out.println("This is Parent m2");
		}
	}
	static class Child extends Parent{
		@Override
		public void m1(){
			int i = -2;
			System.out.println("This is Child m1:" + i);
		}
	}
	@Test
	public void execute(){
		Child c = new Child();
		c.m2();
	}
}
