package com.angrycat.erp.service.magento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.JsonNodeWrapper;

@Service
@Scope("prototype")
public class MagentoOrderService extends MagentoBaseService {
	private static final long serialVersionUID = -1221976777524113169L;
	public MagentoOrderService(){
		setModule("angrycatorder");
		setController("api");
	}
	@PostConstruct
	public void init(){
		if(linodeDomain == null){
			linodeDomain = env.getProperty("linode.host.domain") + "magento/index.php";
		}
		setBaseUrl(linodeDomain);
	}
	/*
	 * Magento提供查詢訂單的API，可回傳相當多欄位，以下是比較重要且共通的舉例
	 * increment_id		:訂單編號
	 * customer_id		:客戶ID
	 * subtotal			:小計金額
	 * grand_total		:總計金額
	 * status			:狀態
	 * state			:狀態
	 * customer_email	:客戶Email
	 * 
	 * */
	
	// ref. http://devdocs.magento.com/guides/m1x/api/soap/sales/salesOrder/sales_order.info.html
	/**
	 * 以訂單號批次取回訂單資訊(包含訂單商品項目)<br>
	 * 這組API可取得詳盡的訂單項目<br>
	 * 如果找不到他會回傳錯誤報告id，但http status code是503(service unavailable)<br>
	 * 如果去找對應的report，裡面會提示找不到: Mage_Api_Model_Resource_Abstract->_fault('not_exists')
	 * @param ids
	 * @param fields: 如果為null，則取回所有欄位；不建議提供null值，因為欄位數目太多，透過網路傳輸效能不佳
	 * @return
	 */
	public String listOrderInfosByIncreIds(List<String> ids, List<String> fields){
		Map<String, List<String>> config = new LinkedHashMap<>();
		config.put("ids", ids);
		// 可以設定要取回的欄位
		config.put("fields", fields);
		
		String result = connect("listOrderInfosByIncreIds", config);
		return result;
	}
	/**
	 * 以訂單號批次取回訂單資訊<br>
	 * 此組API跟listOrderInfosByIncreIds不一樣在於他不會去取訂單細項，理論上效能更好<br>
	 * 如果找不到他會回傳空陣列(http status code 200)，在判別上比較直觀
	 * @param ids
	 * @param fields 如果為null，則取回所有欄位；不建議提供null值，因為欄位數目太多，透過網路傳輸效能不佳
	 * @return
	 */
	public JsonNodeWrapper listOrdersByIncreIds(List<String> ids, List<String> fields){
		Map<String, String> values = new LinkedHashMap<>();
		values.put("key", "in");
		values.put("value", StringUtils.join(ids, ","));
		
		Map<String, Object> cond1 = new LinkedHashMap<>();
		cond1.put("key", "increment_id");
		cond1.put("value", values);
		
		List<Map<String, Object>> conds = new ArrayList<>();
		conds.add(cond1);
		
		Map<String, List<Map<String, Object>>> filters = new LinkedHashMap<>();
		filters.put("complex_filter", conds);
		
		JsonNodeWrapper result = request("listOrdersByFilters", filters, fields);
		return result;
	}
	public boolean areOrdersExisted(String increIds){
		if(StringUtils.isBlank(increIds)){
			return false;
		}
		List<String> ids = Arrays.asList(increIds.split(",")).stream().map(s->StringUtils.trim(s)).collect(Collectors.toList());
		JsonNodeWrapper r = listOrdersByIncreIds(ids, Arrays.asList("increment_id"));
		return r.getFound().size() == ids.size();
	}
}
