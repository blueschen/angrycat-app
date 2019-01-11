package com.angrycat.erp.service.magento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.angrycat.erp.model.Product;

@Service
@Scope("prototype")
public class MagentoProductService extends MagentoBaseService {
	private static final long serialVersionUID = 8322835412340989148L;
	public MagentoProductService(){
		setModule("angrycatproduct");
		setController("api");
	}
	@PostConstruct
	public void init(){
		if(linodeDomain == null){
			linodeDomain = env.getProperty("linode.host.domain");
		}
		setBaseUrl(linodeDomain);
	}
	public String listAllProductsResponse(){
		String result = connect("listAllProductsResponse");
		return result;
	}
	public String listProductsBySku(String...sku){
		Map<String, String> values = new LinkedHashMap<>();
		values.put("key", "in");
		values.put("value", StringUtils.join(sku, ","));
		
		Map<String, Object> cond1 = new LinkedHashMap<>();
		cond1.put("key", "sku");
		cond1.put("value", values);
		
		List<Map<String, Object>> conds = new ArrayList<>();
		conds.add(cond1);
		
		Map<String, List<Map<String, Object>>> filters = new LinkedHashMap<>();
		filters.put("complex_filter", conds);
		
		String result = connect("listProductsByFilters", filters);
		return result;
	}
	private String getIsInStock(Object stock){
		if(stock == null || (stock.getClass() != Integer.class)){
			throw new RuntimeException("stock should be int");
		}
		int qty = Integer.class.cast(stock);
		return qty > 0 ? "1" : "0";
	}
	/**
	 * 跟Magento庫存比較
	 * 過濾出不同比較基準的結果
	 * @param products
	 * @param compareStock: the first argument is stock quantity from Magento; the second from the memory
	 * @return
	 */
	public Map<String, StockInfo> filterByComparingStock(List<Product> products, BiPredicate<Integer, Integer> compareStock){
		Map<String, Product> map = products.stream().collect(Collectors.toMap(Product::getModelId, Function.identity()));
		JsonNodeWrapper jnw = listInventoryById(map.keySet().toArray(new String[map.size()]));
		if(jnw == null){
			return Collections.emptyMap();
		}
		if(isDebug()){
			System.out.println("listInventoryById magento found count: " + jnw.getFound().size());
		}
		Map<String, StockInfo> infos = jnw
			.toMap(k->{
				String modelId = k.findValue("sku").textValue();
				return modelId;
			},
			v->{
				int qty = Double.valueOf(v.findValue("qty").textValue()).intValue();
				String modelId = v.findValue("sku").textValue();
				Product p = map.get(modelId);
				StockInfo si = new StockInfo(modelId, qty, p.getTotalStockQty());
				return si;
			})
			.entrySet()
			.stream()
			.filter(entry->compareStock.test(entry.getValue().getMagentoStockQty(), entry.getValue().getTotalStockQty()))
			.collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue()));
		return infos;
	}
	/**
	 * 根據資料庫ID或型號找到商品庫存<br>
	 * 資料結構範例如下:<br>
	 * [{"product_id":"7","sku":"TT016 (C)","qty":"996.0000","is_in_stock":"1"}]
	 * @param id
	 * @return
	 */
	public JsonNodeWrapper listInventoryById(String...id){
		JsonNodeWrapper result = request("listInventoryByIds", (Object[])id);
		return result;
	}
	/**
	 * 取得所有商品庫存<br>
	 * 資料結構範例如下:<br>
	 * [{"product_id":"1","sku":"asus001","qty":"14.0000","is_in_stock":"1"}]
	 * @return
	 */
	public JsonNodeWrapper listAllInventory(){
		JsonNodeWrapper result = request("listAllInventory");
		return result;
	}
	/**
	 * 根據比較條件更新Magenot庫存:<br>
	 * 先去查詢Magenot商品，<br>
	 * 針對符合篩選條件的項目，<br>
	 * 以庫存表更新Magento
	 * @param products
	 * @param compareStock
	 * @return
	 */
	private JsonNodeWrapper updateStockAfterCompare(List<Product> products, BiPredicate<Integer, Integer> compareStock){
		Map<String, Object> params = 
				filterByComparingStock(products, compareStock)
				.entrySet()
				.stream()
				.collect(
					Collectors.toMap(
						entry->entry.getValue().getSku(), 
						entry->entry.getValue().getTotalStockQty()));
			JsonNodeWrapper result = updateInventoryByProductId(params);
			return result;
	}
	/**
	 * 如果Magento跟庫存表不一樣，<br>
	 * 用庫存表的庫存更改他，<br>
	 * 讓Magenot庫存與庫存表一致<br>
	 * @param products
	 * @return
	 */
	public JsonNodeWrapper updateStockIfDifferentFromMagento(List<Product> products){
		JsonNodeWrapper result = updateStockAfterCompare(products, (magentoStock, totalStock)-> magentoStock != totalStock);
		return result;
	}
	/**
	 * 如果Magento庫存較多，<br>
	 * 用庫存表的庫存更改他，<br>
	 * 讓Magenot庫存與庫存表一致<br>
	 * @param products
	 * @return
	 */
	public JsonNodeWrapper updateStockIfMagentoIsMore(List<Product> products){
		JsonNodeWrapper result = updateStockAfterCompare(products, (magentoStock, totalStock)-> magentoStock > totalStock);
		return result;
	}
	/**
	 * 批次修改Magento庫存數量及有無庫存flag<br>
	 * 傳入參數資料結構範例如下:<br>
	 * [{"productId":"2","updateData":{"qty":3,"is_in_stock":"1"}},{"productId":"4","updateData":{"qty":0,"is_in_stock":"0"}}]<br>
	 * 成功回傳值資料結構如下:<br>
	 * [{"2":true},{"4":true}]
	 * @param params
	 * @return
	 */
	public JsonNodeWrapper updateInventoryByProductId(Map<String, Object> params){
		JsonNodeWrapper result = null;
		if(params.size()==0){
			result = new JsonNodeWrapper("{\"status\":\"Params is empty\"}");
			return result;
		}
		List<Object> args = new LinkedList<>();
		for(Map.Entry<String, Object> p : params.entrySet()){
			Map<String, Object> updateData = new LinkedHashMap<>();
			updateData.put("qty", p.getValue());
			updateData.put("is_in_stock", getIsInStock(p.getValue()));
			Map<String, Object> update = new LinkedHashMap<>();
			String productId = p.getKey();
			update.put("productId", productId);
			update.put("updateData", updateData);
			
			args.add(update);
		}
		result = request("updateInventoryByProductId", args.toArray());
		return result;
	}
	/**
	 * Magento購物網站及庫存表庫存資訊
	 * @author JerryLin
	 *
	 */
	public static class StockInfo{
		private String sku;
		private int magentoStockQty;
		private int totalStockQty;
		public StockInfo(String sku, int magentoStockQty, int totalStockQty){
			this.sku = sku;
			this.magentoStockQty = magentoStockQty;
			this.totalStockQty = totalStockQty;
		}
		public String getSku() {
			return sku;
		}
		public int getMagentoStockQty() {
			return magentoStockQty;
		}
		public int getTotalStockQty() {
			return totalStockQty;
		}
	}
}
