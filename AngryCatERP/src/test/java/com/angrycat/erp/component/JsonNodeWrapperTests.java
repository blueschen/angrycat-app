package com.angrycat.erp.component;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeWrapperTests {
	@Test
	public void initWithString() throws Throwable{
		String json = getJsonSample();
		JsonNodeWrapper jnw = new JsonNodeWrapper(json);
		JsonNode root = jnw.getRoot(); 
		assertTrue(root.hasNonNull("pageNavigator"));
		JsonNode results = root.findPath("results");
		if(results.isArray()){
			JsonNode element = results.get(1);
			JsonNode productCategory = element.findValue("productCategory");
			if(productCategory.isObject()){
				String codeVal = productCategory.get("code").textValue();
				String expectedVal = "TIDL";
				assertEquals(expectedVal, codeVal);
				System.out.println("codeVal is " + codeVal);
			}
		}
	}
	private String getJsonSample(){
		String json = 
				"{														"
				+"   \"pageNavigator\":{                                "
				+"      \"totalCount\":1173,                            "
				+"      \"previousPage\":1,                             "
				+"      \"nextPage\":2,                                 "
				+"      \"currentPage\":1,                              "
				+"      \"totalPageCount\":235,                         "
				+"      \"countPerPage\":5                              "
				+"   },                                                 "
				+"   \"conds\":{                                        "
				+"      \"currentPage\":1,                              "
				+"      \"countPerPage\":5                              "
				+"   },                                                 "
				+"   \"results\":[                                      "
				+"      {                                               "
				+"         \"id\":\"20160204-112456936-mrCYF\",         "
				+"         \"productCategory\":{                        "
				+"            \"id\":\"20160203-120522239-AcqIY\",      "
				+"            \"code\":\"TIDL\",                        "
				+"            \"name\":null,                            "
				+"            \"description\":null                      "
				+"         },                                           "
				+"         \"modelId\":\"OHM-Tidlom\",                  "
				+"         \"suggestedRetailPrice\":8000.0,             "
				+"         \"name\":null,                               "
				+"         \"nameEng\":\"Tidlom-the skyhanger\",        "
				+"         \"seriesName\":null,                         "
				+"         \"barcode\":\"8858845942342\",               "
				+"         \"imgDir\":null,                             "
				+"         \"totalStockQty\":12,                        "
				+"         \"officeStockQty\":10,                       "
				+"         \"drawerStockQty\":2,                        "
				+"         \"showcaseStockQty\":0,                      "
				+"         \"notShipStockQty\":0                        "
				+"      },                                              "
				+"      {                                               "
				+"         \"id\":\"20160204-112456933-wHXcB\",         "
				+"         \"productCategory\":{                        "
				+"            \"id\":\"20160203-120522239-AcqIY\",      "
				+"            \"code\":\"TIDL\",                        "
				+"            \"name\":null,                            "
				+"            \"description\":null                      "
				+"         },                                           "
				+"         \"modelId\":\"AMV01802\",                    "
				+"         \"suggestedRetailPrice\":1600.0,             "
				+"         \"name\":null,                               "
				+"         \"nameEng\":\"Pink Blossom\",                "
				+"         \"seriesName\":null,                         "
				+"         \"barcode\":\"8858845942076\",               "
				+"         \"imgDir\":\"/uploads                        "
				+"/tmp/img/AMV01802.jpg\",                              "
				+"         \"totalStockQty\":0,                         "
				+"         \"officeStockQty\":0,                        "
				+"         \"drawerStockQty\":0,                        "
				+"         \"showcaseStockQty\":0,                      "
				+"         \"notShipStockQty\":0                        "
				+"      },                                              "
				+"      {                                               "
				+"         \"id\":\"20160204-112456931-RrusZ\",         "
				+"         \"productCategory\":{                        "
				+"            \"id\":\"20160203-120522239-AcqIY\",      "
				+"            \"code\":\"TIDL\",                        "
				+"            \"name\":null,                            "
				+"            \"description\":null                      "
				+"         },                                           "
				+"         \"modelId\":\"AMV01801\",                    "
				+"         \"suggestedRetailPrice\":1600.0,             "
				+"         \"name\":null,                               "
				+"         \"nameEng\":\"Country Tartan\",              "
				+"         \"seriesName\":null,                         "
				+"         \"barcode\":\"8858845942069\",               "
				+"         \"imgDir\":\"/uploads/tmp/img/AMV01801.jpg\","
				+"         \"totalStockQty\":0,                         "
				+"         \"officeStockQty\":0,                        "
				+"         \"drawerStockQty\":0,                        "
				+"         \"showcaseStockQty\":0,                      "
				+"         \"notShipStockQty\":0                        "
				+"      },                                              "
				+"      {                                               "
				+"         \"id\":\"20160204-112456929-PNexI\",         "
				+"         \"productCategory\":{                        "
				+"            \"id\":\"20160203-120522239-AcqIY\",      "
				+"            \"code\":\"TIDL\",                        "
				+"            \"name\":null,                            "
				+"            \"description\":null                      "
				+"         },                                           "
				+"         \"modelId\":\"AAA052\",                      "
				+"         \"suggestedRetailPrice\":2200.0,             "
				+"         \"name\":null,                               "
				+"         \"nameEng\":\"Jumpa\",                       "
				+"         \"seriesName\":null,                         "
				+"         \"barcode\":\"8858845942045\",               "
				+"         \"imgDir\":\"/uploads/tmp/img/AAA052.jpg\",  "
				+"         \"totalStockQty\":0,                         "
				+"         \"officeStockQty\":0,                        "
				+"         \"drawerStockQty\":0,                        "
				+"         \"showcaseStockQty\":0,                      "
				+"         \"notShipStockQty\":0                        "
				+"      },                                              "
				+"      {                                               "
				+"         \"id\":\"20160204-112456927-oHGwr\",         "
				+"         \"productCategory\":{                        "
				+"            \"id\":\"20160203-120522239-AcqIY\",      "
				+"            \"code\":\"TIDL\",                        "
				+"            \"name\":null,                            "
				+"            \"description\":null                      "
				+"         },                                           "
				+"         \"modelId\":\"AAA051\",                      "
				+"         \"suggestedRetailPrice\":2200.0,             "
				+"         \"name\":null,                               "
				+"         \"nameEng\":\"Tidlom\",                      "
				+"         \"seriesName\":null,                         "
				+"         \"barcode\":\"8858845942038\",               "
				+"         \"imgDir\":\"/uploads                        "
				+"/tmp/img/AAA051.jpg\",                                "
				+"         \"totalStockQty\":0,                         "
				+"         \"officeStockQty\":0,                        "
				+"         \"drawerStockQty\":0,                        "
				+"         \"showcaseStockQty\":0,                      "
				+"         \"notShipStockQty\":0                        "
				+"      }                                               "
				+"   ],                                                 "
				+"   \"msgs\":{											"
				+"                                                      "
				+"   }                                                  "
				+"}                                                     ";
		return json;
	}
	@Test
	public void httpGetJson() throws Throwable{
		String json = httpGet("http://192.168.1.15/test/data.json");
		System.out.println(json);
		JsonNodeWrapper jnw = new JsonNodeWrapper(json);
		assertTrue(jnw.getRoot().hasNonNull("pageNavigator"));
	}
	private String httpGet(String rquestUrl) throws Throwable{
		boolean isOk = false;
		String result = "";
		URL url = new URL(rquestUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("charset", "utf-8");
		connection.setUseCaches(false);
		
		int responseCode = connection.getResponseCode();
		// 回應碼在(包含)200~(不包含)400之間，都算成功
		isOk = (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_BAD_REQUEST);
		try(BufferedReader br = new BufferedReader(new InputStreamReader((isOk ? connection.getInputStream() : connection.getErrorStream())));){
			String lineResult = null;
			while((lineResult = br.readLine())!=null){
				result+= lineResult;
			}
		}
		return result;
	}
}
