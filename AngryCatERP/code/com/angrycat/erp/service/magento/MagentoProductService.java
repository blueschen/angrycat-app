package com.angrycat.erp.service.magento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class MagentoProductService extends MagentoBaseService {
	private static final long serialVersionUID = 8322835412340989148L;
	public MagentoProductService(){
		setModule("angrycatproduct");
		setController("api");
	}
	public String listAllProductsResponse()throws Throwable{
		String result = connect("listAllProductsResponse");
		return result;
	}
	public String listProductsBySku(String...sku)throws Throwable{
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
	public String listInventoryById(String...id)throws Throwable{
		String result = connect("listInventoryByIds", (Object[])id);
		return result;
	}
	public String listAllInventory()throws Throwable{
		String result = connect("listAllInventory");
		return result;
	}
}
