package com.angrycat.erp.service.magento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.JsonNodeWrapper;

@Service
@Scope("prototype")
public class MagentoProductService extends MagentoBaseService {
	private static final long serialVersionUID = 8322835412340989148L;
	public MagentoProductService(){
		setBaseUrl(LOCALHOST_BASE_URL);
		setModule("angrycatproduct");
		setController("api");
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
}
