package com.angrycat.erp.test;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

public class CompletableFutureTests {
	@Test
	public void execute(){
		CompletableFuture<String> receiver = 
				CompletableFuture.supplyAsync(this::genMsg);
		receiver.thenAcceptAsync(this::printMsg1);
		receiver.thenAcceptAsync(this::printMsg2);
		receiver.thenAcceptAsync(this::printMsg3);
	}
	
	private String genMsg(){
		return "this is msg generated...";
	}
	
	private void printMsg1(String msg){
		System.out.println("this is printMsg1...received msg: " + msg);
	}
	private void printMsg2(String msg){
		System.out.println("this is printMsg2...received msg: " + msg);
	}
	private void printMsg3(String msg){
		System.out.println("this is printMsg3...received msg: " + msg);
	}
	
	@Test
	public void processException(){
		CompletableFuture.supplyAsync(()->genException())
			.exceptionally((r)->{
				System.out.println(r);
				return null;
			});
		System.out.println("process end...");
	}
	private String genException(){
		throw new RuntimeException("This is manually exception");
	}
}
