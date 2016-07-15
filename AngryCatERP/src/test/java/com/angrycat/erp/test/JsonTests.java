package com.angrycat.erp.test;
import static com.angrycat.erp.common.CommonUtil.processSingleNode;

import java.io.File;
import java.util.Iterator;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class JsonTests {
	@Test
	public void fromProductsFile(){
		ObjectMapper om = new ObjectMapper();
		String path = "E:\\tmp\\products.txt";
		try{
			JsonNode root = om.readTree(new File(path));
			processSingleNode(root, (f,n)->{
				System.out.println(f);
			});
			String countField = "count";
			String summaryField = "summary";
			String warnField = "warn";
			
			JsonNode countValNode = root.get(countField);
			JsonNode summaryNode = root.get(summaryField);
			JsonNode warnNode = root.get(warnField);
			
			System.out.println("count: " + countValNode.asInt());
			Iterator<String> modelIds = summaryNode.fieldNames();
			
			String skuField = "sku";
			String nameEngField = "nameEng";
			String stockField = "stock";
			String retailPriceField = "retailPrice";
			String sheetNameField = "sheetName";
			String idxField = "idx";
			String groupPriceField = "groupPrice";
			String counterPriceField = "counterPrice";
			String productNameField = "productName";
			
			int count = 0;
			while(modelIds.hasNext()){
				count++;
				String modelId = modelIds.next();
				JsonNode product = summaryNode.get(modelId);
				Iterator<String>infoFields = product.fieldNames();
				while(infoFields.hasNext()){
					String infoField = infoFields.next();
					JsonNode info = product.get(infoField);
					if(info.isInt()){
						info.asInt();
					}else if(info.isTextual()){
						info.asText();
					}
					if(modelId.equals("TT059")){
						System.out.println(infoField + ":" + (info.isInt() ? info.asInt() : info.asText()));
					}
				}
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
}
