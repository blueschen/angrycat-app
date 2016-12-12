package com.angrycat.erp.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.model.Product;
@Service
@Scope("prototype")
@Qualifier("productKendoUiService")
public class ProductKendoUiService extends KendoUiService<Product, Product> {
	private static final long serialVersionUID = 9088514750485237337L;
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		List<?> results = super.deleteByIds(ids);
		// TODO 與Magento庫存非同步連動
		mockAsyncRequest();
		return results;
	}
	
	@Override
	@Transactional
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before){
		List<Product> results = super.batchSaveOrMerge(targets, before);
		// TODO 與Magento庫存非同步連動
		mockAsyncRequest();
		return results;
	}
	/**
	 * 測試非同步
	 */
	private void mockAsyncRequest(){
		CompletableFuture.supplyAsync(()->{
			try{
				Thread.sleep(10000);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			return "mock async request successful";
		}).exceptionally((e)->{
			return "executing mockAsyncRequest errors:\n" + e;
		})
		.thenAccept(retMsg->{
			System.out.println("mockAsyncRequest:\n" + retMsg);
		});
		System.out.println("main thread keeps executing");
	}
}
