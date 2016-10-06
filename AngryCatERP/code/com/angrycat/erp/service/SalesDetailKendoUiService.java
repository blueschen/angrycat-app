package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Arrays;
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
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		List<?> results = super.deleteByIds(ids);
		System.out.println("deleteByIds results is: " + results.getClass());
		List<SalesDetail> targets = (List<SalesDetail>)results;
		List<Product> products = findProducts(targets, sfw.currentSession());
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				SalesDetail sd = targets.get(i);
				Product p = products.get(i);
				updateStock("刪除", sd, p, null);
			});
		productService.batchSaveOrMerge(products, null);
		return results;
	}
	@Override
	@Transactional
	public List<SalesDetail> batchSaveOrMerge(List<SalesDetail> targets, BiFunction<SalesDetail, Session, SalesDetail> before){
		List<String> updateIds = targets.stream().filter(d->StringUtils.isNotBlank(d.getId())).map(d->d.getId()).collect(Collectors.toList());
		System.out.println("batchSaveOrMerge targets is: " + targets.getClass());
		
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
					action = "新增";
				}else{
					action = "修改";
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
					
		String queryProduct = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		List<Product> products = modelIds.stream().map(modelId->(Product)s.createQuery(queryProduct).setString("modelId", modelId).uniqueResult()).collect(Collectors.toList());
		return products;		
	}
	private int getSaleStatusCode(String saleStatus){
		int code = Integer.parseInt(saleStatus.substring(0, 2));
		return code;
	}
	public Product updateStock(String action, SalesDetail sd, Product p, String oldStatus){
		Product stock = null;
		String saleStatus = sd.getSaleStatus();
		String saleId = sd.getId();
		if(action.equals("新增")){
			stock = getAddedStock(saleStatus);
		}
		if(action.equals("修改")){
			stock = getExistedStock(oldStatus, saleStatus);
		}
		if(action.equals("刪除")){
			stock = getDeletedStock(saleStatus);
		}
		List<String> template = new ArrayList<>();
		template.add(action);
		template.add("銷售明細"+saleId);
		int totalStockQty = stock.getTotalStockQty();
		int notShipStockQty = stock.getNotShipStockQty();
		p.setTotalStockQty(p.getTotalStockQty()+totalStockQty);
		p.setNotShipStockQty(p.getNotShipStockQty()+notShipStockQty);
		if(totalStockQty==1){
			template.add("總庫存+1");
		}
		if(totalStockQty==-1){
			template.add("總庫存-1");
		}
		if(notShipStockQty==1){
			template.add("未出貨+1");
		}
		if(notShipStockQty==-1){
			template.add("未出貨-1");
		}
		p.setTotalStockChangeNote("產生自:"+StringUtils.join(template, "_"));
		return p;
	}
	public Product getDeletedStock(String saleStatus){
		if(StringUtils.isBlank(saleStatus)){
			throw new IllegalArgumentException("銷售狀態為必填");
		}
		int code = getSaleStatusCode(saleStatus);
		Product p = new Product();
		if(code < 40){
			p.setNotShipStockQty(-1);
		}else if(code == 40){
			// do nothing
		}else if(code == 99){
			p.setTotalStockQty(1);
		}
		return p;
	}
	public Product getAddedStock(String saleStatus){
		if(StringUtils.isBlank(saleStatus)){
			throw new IllegalArgumentException("銷售狀態為必填");
		}
		int code = getSaleStatusCode(saleStatus);
		Product p = new Product();
		if(code < 40){
			p.setNotShipStockQty(1);
		}else if(code == 40){
			// do nothing
		}else if(code == 99){
			p.setTotalStockQty(-1);
		}
		return p;
	}
	public Product getExistedStock(String oldSaleStatus, String newSaleStatus){
		if(StringUtils.isBlank(oldSaleStatus) && StringUtils.isBlank(newSaleStatus)){
			throw new IllegalArgumentException("新舊銷售狀態皆為空值，不是正常參數");
		}
		//10. 待出貨
		//20. 集貨中
		//30. 調貨中
		//40. 待補貨
		//99. 已出貨
		int oldCode = getSaleStatusCode(oldSaleStatus);
		int newCode = getSaleStatusCode(newSaleStatus);
		Product p = new Product();
		if((oldCode < 50 && newCode < 50)
		|| (oldCode == 99 && newCode == 99)){
			// do nothing
		}else if(oldCode == 99 && newCode == 40){
			p.setTotalStockQty(1);
		}else if(oldCode == 99 && newCode < 40){
			p.setTotalStockQty(1);
			p.setNotShipStockQty(1);
		}else if(oldCode == 40 && newCode == 99){
			p.setTotalStockQty(-1);
		}else if(oldCode < 40 && newCode == 99){
			p.setTotalStockQty(-1);
			p.setNotShipStockQty(-1);
		}
		return p;
	}
}
