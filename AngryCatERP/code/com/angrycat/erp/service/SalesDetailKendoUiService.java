package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
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
	private ProductKendoUiService productKendoUiService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	public static final String ACTION_NEW = "新增";
	static final String ACTION_UPDATE = "修改";
	static final String ACTION_DELETE = "刪除";
	static final String STATUS_INVALID = "作廢";
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		List<?> results = super.deleteByIds(ids);
		List<SalesDetail> targets = (List<SalesDetail>)results;
		Session s = sfw.currentSession();
		List<Product> products = findProducts(targets, s);
		s.clear(); // 之後如果再度查詢，應該要拿資料庫而非記憶體當中的，所以清掉記憶體佔存的products；這個動作可以確保之後異動記錄抓到新舊資料差異。
		List<Product> productsUpdated = new ArrayList<>();
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				SalesDetail sd = targets.get(i);
				Product p = products.get(i);
				if(null == p){
					return;
				}
				int stockChanged = updateStock(ACTION_DELETE, sd, p, null);
				if(stockChanged != 0){
					productsUpdated.add(p);
				}
			});
		productKendoUiService.batchSaveOrMerge(productsUpdated, null, s);
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
		List<Product> products = findProducts(targets, s);
		// 銷售明細先儲存，是為了得到id
		List<SalesDetail> details = super.batchSaveOrMerge(targets, before);
		List<Product> productsUpdated = new ArrayList<>();
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				Product p = products.get(i);
				if(null == p){
					return;
				}
				SalesDetail sd = targets.get(i);
				SalesDetail oldDetail = oldDetailMap.get(sd.getId());
				String action = null;
				String oldSaleStatus = null;
				if(oldDetail==null){
					action = ACTION_NEW;
				}else{
					action = ACTION_UPDATE;
					oldSaleStatus = oldDetail.getSaleStatus();
				}
				int stockChanged = updateStock(action, sd, p, oldSaleStatus);
				if(stockChanged != 0){// 不一定每個銷售明細對應的商品庫存都有異動，有異動才更新
					productsUpdated.add(p);// TODO 要考量針對同一商品連續異動庫存的狀況
				}
			});
		
		productKendoUiService.batchSaveOrMerge(productsUpdated, null, s);
		return details;
	}
	/**
	 * 以型號查詢與銷售明細順序一致的商品<br>
	 * 銷售明細有可能不會對應到商品<br>
	 * 或者其提供的型號沒有查到商品<br>
	 * 在上述兩種情況下<br>
	 * 會以null值呈現
	 * @param targets
	 * @param s
	 * @return
	 */
	public List<Product> findProducts(List<SalesDetail> targets, Session s){
		String queryProduct = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		// 此處要確保記憶體中銷售明細和產品順序的一致性，所以用HQL只能一個一個查
		List<Product> products = 
			targets
				.stream()
				.map(sd->{
					String modelId = sd.getModelId();
					if(StringUtils.isBlank(modelId)){// 沒有提供型號，視作無效商品
						return null;
					}
					// 查到一筆或者沒有結果
					return (Product)s.createQuery(queryProduct).setString("modelId", modelId).uniqueResult();
					})
				.collect(Collectors.toList());
		return products;		
	}
	/**
	 * 如果商品需要更動庫存<br>
	 * 改變庫存數並註記<br>
	 * 之後回傳更動庫存數<br>
	 * 1代表庫存加1<br>
	 * -1代表庫存扣1<br>
	 * 0代表庫存不變
	 * @param action
	 * @param sd
	 * @param p
	 * @param oldStatus
	 * @return
	 */
	public int updateStock(String action, SalesDetail sd, Product p, String oldStatus){
		int stock = 0;
		String newStatus = sd.getSaleStatus();
		String saleId = sd.getId();
		
		stock = getStockChanged(action, oldStatus, newStatus);
		p.setTotalStockQty(p.getTotalStockQty()+stock);
		if(SalesDetail.SALE_POINT_ESLITE_TAOBAO.equals(sd.getSalePoint())){
			p.setTaobaoStockQty(p.getTaobaoStockQty()+stock);
			if(stock > 0){
				p.setWarning(ProductKendoUiService.ADD_TAOBAO + stock);
			}else if(stock < 0){
				p.setWarning(ProductKendoUiService.SUBTRACT_TAOBAO + (-stock));
			}
		}else{
			if(stock > 0){
				p.setWarning(ProductKendoUiService.ADD_TOTAL + stock);
			}else if(stock < 0){
				p.setWarning(ProductKendoUiService.SUBTRACT_TOTAL + (-stock));
			}
		}
		
		if(stock != 0){
			p.setTotalStockChangeNote(ProductKendoUiService.genTotalStockChangeNote(action, "銷售明細"+saleId, stock));
		}
		return stock;
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
