package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.AT_OHM;
import static com.angrycat.erp.common.EmailContact.JERRY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.security.User;
import com.angrycat.erp.web.WebUtils;
@Service
@Scope("prototype")
public class SalesDetailKendoUiService extends
		KendoUiService<SalesDetail, SalesDetail> {
	private static final long serialVersionUID = 2968402826378838925L;
	@Autowired
	private ProductKendoUiService productKendoUiService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
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
		List<String> msgs = new ArrayList<>();
		IntStream.range(0, products.size())
			.boxed()
			.forEachOrdered(i->{
				SalesDetail sd = targets.get(i);
				Product p = products.get(i);
				if(null == p){
					return;
				}
				int stockChanged = updateStock(ACTION_DELETE, sd, p, null);
				if(StringUtils.isNotBlank(p.getWarning())){
					msgs.add(p.getWarning());
				}
				if(stockChanged != 0){
					productsUpdated.add(p);
					
					// 刪除的時候，只有兩種情況: 加回庫存或者不異動庫存
					// 由於錯誤只可能發生在減庫存的情況，所以理論上此處並不需要檢核
					// 但還是保留程式
					if(msgs.size() > 0){
						String errMsg = "<h4>刪除銷售明細時修改庫存狀態有誤:</h4>";
						errMsg += msgs.stream().map(m->"<h4>" + m + "</h4>").collect(Collectors.joining());
						throw new RuntimeException(errMsg);
					}
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
		
		Map<String, SalesDetail> oldDetailMap = new HashMap<>();
		for(SalesDetail oldDetail : oldDetails){
			s.evict(oldDetail);// 轉換的時候，順帶脫離session
			oldDetailMap.put(oldDetail.getId(), oldDetail);
		}
		
		List<Product> products = findProducts(targets, s);
		
		// 銷售明細先儲存，是為了得到id
		List<SalesDetail> details = super.batchSaveOrMerge(targets, before);
		
		List<Product> productsUpdated = new ArrayList<>();
		List<String> msgs = new ArrayList<>();
		for(int i = 0; i < products.size(); i++){
			Product p = products.get(i);
			if(null == p){
				continue;
			}
			SalesDetail sd = details.get(i);
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
			if(StringUtils.isNotBlank(p.getWarning()) && !p.getWarning().contains("淘寶庫存已大於總庫存")){
				msgs.add(p.getWarning());
				
				if(msgs.size() > 0){ // 原本是收集所有錯誤之後一次丟出，現在改為一發現有誤，立刻針對該筆庫存丟出錯誤，此舉是為了簡化連動庫存處理的複雜性
					String errMsg = "<h4>異動銷售明細時修改庫存狀態有誤:</h4>";
					errMsg += msgs.stream().map(m->"<h4>" + m + "</h4>").collect(Collectors.joining());
					throw new RuntimeException(errMsg);
				}
			}
			if(stockChanged != 0){// 不一定每個銷售明細對應的商品庫存都有異動，有異動才更新
				productsUpdated.add(p);
			}
		}
		
		productKendoUiService.batchSaveOrMerge(productsUpdated, null, s);
		return details;
	}
	/**
	 * 以型號查詢與銷售明細順序一致的商品。<br>
	 * 銷售明細有可能不會對應到商品，<br>
	 * 或者其提供的型號沒有查到商品，<br>
	 * 在上述兩種情況下，<br>
	 * 會以null值呈現商品。<br>
	 * 相同商品的參照應一致，<br>
	 * 這可以確保針對同一商品多次修改庫存結果符合預期
	 * @param targets
	 * @param s
	 * @return
	 */
	public List<Product> findProducts(List<SalesDetail> targets, Session s){
		List<String> modelIds = new ArrayList<>();
		for(SalesDetail sd : targets){
			String modelId = sd.getModelId();
			if(modelId != null){
				modelId = modelId.trim();
			}
			modelIds.add(modelId); // 如果有null也要保留
		}
		
		Set<String> existedIds = new HashSet<>(modelIds);
		existedIds.remove(null);
		existedIds.remove("");
		
		if(existedIds.isEmpty()){
			return Collections.emptyList();
		}
		
		String q = "SELECT DISTINCT p FROM " + Product.class.getName() + " p WHERE p.modelId IN (:ids)";
		List<Product> founds = s.createQuery(q).setParameterList("ids", existedIds).list();
		Map<String, Product> mapProd = new HashMap<>();
		for(Product f : founds){
			s.evict(f);
			mapProd.put(f.getModelId(), f);
		}
		
		List<Product> prods = new ArrayList<>();
		for(String modelId : modelIds){
			Product prod = null;
			if(StringUtils.isNotBlank(modelId)){
				prod = mapProd.get(modelId);
			}
			prods.add(prod);
		}
		
		return prods;
		
//		String queryProduct = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
//		// 此處要確保記憶體中銷售明細和產品順序的一致性，所以用HQL只能一個一個查
//		List<Product> products = 
//			targets
//				.stream()
//				.map(sd->{
//					String modelId = sd.getModelId();
//					if(StringUtils.isBlank(modelId)){// 沒有提供型號，視作無效商品
//						return null;
//					}
//					// 查到一筆或者沒有結果
//					return (Product)s.createQuery(queryProduct).setString("modelId", modelId).uniqueResult();
//					})
//				.collect(Collectors.toList());
//		return products;		
	}
	/**
	 * 如果商品需要更動庫存<br>
	 * 改變庫存數並註記<br>
	 * 之後回傳更動庫存數<br>
	 * 1代表庫存加1<br>
	 * -1代表庫存扣1<br>
	 * 0代表庫存不變<br>
	 * 另一個重點在於依據修改庫存的狀態提供不同的訊息
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
		if(stock == 0){
			return stock;
		}
		
		p.setTotalStockQty(p.getTotalStockQty()+stock);
		String modelId = StringUtils.isNotBlank(p.getModelId()) ? p.getModelId() : p.getId();
		String msg = null;
		
		String stockMsg = modelId;
		if(SalesDetail.SALE_POINT_TAOBAO.equals(sd.getSalePoint())){
			stockMsg += "淘寶(總)";			
		}else{
			stockMsg += "總";
		}
		stockMsg += "庫存";
		String stockType = stockMsg;
		if(stock > 0){
			stockMsg += ("+" + stock);
		}else{
			stockMsg += ("-" + (-stock));
		}
		
		if(SalesDetail.SALE_POINT_TAOBAO.equals(sd.getSalePoint())){ // 淘寶庫存
			p.setTaobaoStockQty(p.getTaobaoStockQty()+stock);
			if(stock < 0){ // 減淘寶庫存需要檢核
				if(p.getTaobaoStockQty() < 0){
					msg = stockMsg + ":淘寶庫存會小於0";
				}
				if(p.getTotalStockQty() < 0){
					if(p.getTaobaoStockQty() < 0){
						msg += ",總庫存跟著連動會小於0";
					}else{
						msg = stockMsg + ":總庫存跟著連動會小於0";
					}
				}
			}
		}else{ // 總庫存
			if(stock < 0){ // 減總庫存需要檢核
				if(p.getTotalStockQty() < 0){
					msg = stockMsg + ":總庫存會小於0";
				}else if(p.getTotalStockQty() < p.getTaobaoStockQty()){
					msg = stockMsg + ":淘寶庫存已大於總庫存";
					
					// 2018-12-12喵娘需求: 於銷售時，總庫存小於淘寶庫存，要去扣淘寶，而非阻擋總庫存
					p.setTaobaoStockQty(p.getTaobaoStockQty()-1);
					SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
					simpleMailMessage.setFrom(JERRY);
					User u = WebUtils.getSessionUser();
					if(u != null){
						String userId = u.getUserId();
						// 一般使用者帳號與信箱相符，但管理者不在此限，這段程式碼是為了兼容測試和正式環境，上線穩定後也可考慮移除
						if("jerry".equals(userId) || "root".equals(userId)){
							userId = "jerrylin";
						}
						simpleMailMessage.setTo(new String[]{userId+AT_OHM});
					}else{
						simpleMailMessage.setTo(new String[]{JERRY});
					}
					simpleMailMessage.setText(msg+"系統自動扣掉淘寶庫存，請自行於淘寶同步");
					simpleMailMessage.setSubject(simpleMailMessage.getText());
					mailSender.send(simpleMailMessage);
				}
			}
		}
		
		String msgTitle = "銷售明細" + saleId;
		
		if(StringUtils.isNotBlank(msg)){
			if(ACTION_NEW.equals(action)){
				msgTitle = msgTitle.replace(saleId, ""); // 這邊的錯誤會被丟出，所以新增時產生的id也會被rollback，為了讓前端取得錯誤訊息不要混淆，這裡移除id
			}
			msg = msgTitle + "_" + msg;
			p.setWarning(msg);// 這種格式就不會觸動Product模組內建檢核異動庫存機制，畢竟銷售明細處理庫存的方式不一樣
		}
		
		String currentNote = ProductKendoUiService.genTotalStockChangeNote(action, msgTitle, stock, stockType);
		p.setTotalStockChangeNote(currentNote+(msg != null && msg.contains("淘寶庫存已大於總庫存") ? "(淘寶扣1)" : ""));
		return stock;
	}
	
	int getStockChanged(String action, String oldStatus, String newStatus){
		/* 	
		 * 如果新增時允許銷售狀態為null，會衍生幾種狀態：
		 * 	新增時銷售狀態可為null，庫存不動
		 * 	修改時如果舊的銷售狀態不為null：
		 * 		無法改為null(目前頁面的設計如此)
		 * 		可選「作廢」加回庫存，庫存加1
		 * 	修改時銷售狀態可從null改成其他新狀態：
		 * 		改成作廢，庫存不動
		 * 		改成其他狀態，庫存減1
		 * 	刪除時狀態可能是null或非null：
		 * 		若為null，庫存不動
		 * 		若為作廢，庫存不動
		 * 		若為其他狀態，庫存加1
		 */
		if(action.equals(ACTION_NEW)){
			if(null == newStatus || newStatus.contains(STATUS_INVALID)){
				//throw new IllegalArgumentException(ACTION_NEW + " 的時候，應提供狀態");
				//throw new IllegalArgumentException(ACTION_NEW + " 的時候，" + "銷售狀態不應為: " + STATUS_INVALID);
				return 0;
			}
			return -1;
		}
		if(action.equals(ACTION_DELETE)){
			if(null == newStatus || newStatus.contains(STATUS_INVALID)){
				return 0;
			}
			return 1;
		}
		if(action.equals(ACTION_UPDATE) && oldStatus != newStatus){
			if((oldStatus == null && !newStatus.contains(STATUS_INVALID)) 
			|| (oldStatus != null && oldStatus.contains(STATUS_INVALID))){
				return -1;
			}
			if(oldStatus != null && newStatus.contains(STATUS_INVALID)){
				return 1;
			}
		}
		return 0;
	}
}
