package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.SalesDetail;
@Service
@Scope("prototype")
public class SalesDetailKendoUiService extends
		KendoUiService<SalesDetail, SalesDetail> {
	private static final long serialVersionUID = 2968402826378838925L;
	@Autowired
	private KendoUiService<Product, Product> productService;
	@Autowired
	private SessionFactoryWrapper sfw;
	private static BinaryOperator<SalesDetail> mergeSalesDetailFunction = 
		new BinaryOperator<SalesDetail>(){
			@Override
			public SalesDetail apply(SalesDetail u, SalesDetail v) {
				throw new IllegalStateException(String.format("Duplicate keys %s", u));
			}};
	private static BinaryOperator<String> mergeModelIdFunction = 
		new BinaryOperator<String>(){
			@Override
			public String apply(String u, String v) {
				throw new IllegalStateException(String.format("Duplicate keys %s", u));
			}};			
	private static Supplier<LinkedHashMap<Integer, SalesDetail>> mapSalesDetailSupplier =
		new Supplier<LinkedHashMap<Integer, SalesDetail>>(){
			@Override
			public LinkedHashMap<Integer, SalesDetail> get() {
				return new LinkedHashMap<Integer, SalesDetail>();
			}};
	private static Supplier<LinkedHashMap<Integer, String>> mapModelIdSupplier =
		new Supplier<LinkedHashMap<Integer, String>>(){
			@Override
			public LinkedHashMap<Integer, String> get() {
				return new LinkedHashMap<Integer, String>();
			}};
	
	static final String ACTION_NEW = "新增";
	static final String ACTION_UPDATE = "修改";
	static final String ACTION_DELETE = "刪除";
	static final String STATUS_INVALID = "作廢";
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		List<?> results = super.deleteByIds(ids);
		List<SalesDetail> targets = (List<SalesDetail>)results;
		List<Product> products = findProducts(targets, sfw.currentSession());
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				SalesDetail sd = targets.get(i);
				Product p = products.get(i);
				updateStock(ACTION_DELETE, sd, p, null);
			});
		productService.batchSaveOrMerge(products, null);
		return results;
	}
	@Override
	@Transactional
	public List<SalesDetail> batchSaveOrMerge(List<SalesDetail> targets, BiFunction<SalesDetail, Session, SalesDetail> before){
		List<String> updateIds = targets.stream().filter(d->StringUtils.isNotBlank(d.getId())).map(d->d.getId()).collect(Collectors.toList());
		
		Session s = sfw.currentSession();
		String querySalesDetail = "SELECT p FROM " + SalesDetail.class.getName() + " p WHERE p.id IN (:ids)";
		List<SalesDetail> oldDetails = updateIds.isEmpty() ? Collections.emptyList() : s.createQuery(querySalesDetail).setParameterList("ids", updateIds).list();
		Map<String, SalesDetail> oldDetailMap = 
			oldDetails.stream().map(d->{
				s.evict(d);// 轉換的時候，順帶脫離session
				return d;
			}).collect(Collectors.toMap(d->d.getId(), Function.identity()));
		
		List<Product> products = findProducts(targets, sfw.currentSession());
		// 銷售明細先儲存，是為了得到id
		List<SalesDetail> details = super.batchSaveOrMerge(targets, before);
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				SalesDetail sd = targets.get(i);
				Product p = products.get(i);
				SalesDetail oldDetail = oldDetailMap.get(sd.getId());
				String action = null;
				String oldSaleStatus = null;
				if(oldDetail==null){
					action = ACTION_NEW;
				}else{
					action = ACTION_UPDATE;
					oldSaleStatus = oldDetail.getSaleStatus();
				}
				updateStock(action, sd, p, oldSaleStatus);
			});	
		productService.batchSaveOrMerge(products, null);
		return details;
	}
	public List<Product> findProducts(List<SalesDetail> targets, Session s){
		List<String> modelIds =
			targets.stream().map(t->t.getModelId()).collect(Collectors.toList());
//		Map<Integer, SalesDetail> targetAsMap = 
//			IntStream.range(0, targets.size())
//				.boxed()
//				.collect(Collectors.toMap(
//					i->i, 
//					i->targets.get(i),
//					mergeSalesDetailFunction,
//					mapSalesDetailSupplier)); // 用LinkedHashMap確保順序一致
//		Map<Integer, String> targetModelIds = 
//			targetAsMap.entrySet().stream()
//				.collect(Collectors.toMap(
//					p->p.getKey(), 
//					p->p.getValue().getModelId(),
//					mergeModelIdFunction,
//					mapModelIdSupplier)); // 用LinkedHashMap確保順序一致
					
		String queryProduct = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId IN (:modelIds)";
		List<Product> products = s.createQuery(queryProduct).setParameterList("modelIds", modelIds).list();
		return products;		
	}
	public Product updateStock(String action, SalesDetail sd, Product p, String oldStatus){
		int stock = 0;
		String newStatus = sd.getSaleStatus();
		String saleId = sd.getId();
		
		stock = getStockChanged(action, oldStatus, newStatus);
		
		List<String> template = new ArrayList<>();
		template.add(action);
		template.add("銷售明細"+saleId);
		p.setTotalStockQty(p.getTotalStockQty()+stock);
		if(stock==1){
			template.add("總庫存+1");
		}
		if(stock==-1){
			template.add("總庫存-1");
		}
		if(stock != 0){
			p.setTotalStockChangeNote("產生自:"+StringUtils.join(template, "_"));
		}
		return p;
	}
	int getStockChanged(String action, String oldStatus, String newStatus){
		int stockChanged = 0;
		if(action.equals(ACTION_NEW)){
			if(null == newStatus){
				throw new IllegalArgumentException(ACTION_NEW + " 的時候，應提供狀態");
			}else if(null != newStatus && newStatus.contains(STATUS_INVALID)){
				throw new IllegalArgumentException(ACTION_NEW + " 的時候，" + "銷售狀態不應為: " + STATUS_INVALID);
			}
			stockChanged = -1;
		}else if(action.equals(ACTION_DELETE) && null != newStatus && !newStatus.contains(STATUS_INVALID)){
			stockChanged = 1;
		}else if(action.equals(ACTION_UPDATE) && oldStatus != newStatus){
			if(null != oldStatus && oldStatus.contains(STATUS_INVALID)){
				stockChanged = -1;
			}else if(null != newStatus && newStatus.contains(STATUS_INVALID)){
				stockChanged = 1;
			}
		}
		return stockChanged;
	}
}
