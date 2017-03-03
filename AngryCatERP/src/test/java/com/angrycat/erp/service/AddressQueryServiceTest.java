package com.angrycat.erp.service;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AddressQueryServiceTest {
	private static final Logger LOG = Logger.getLogger(AddressQueryServiceTest.class);
	@Test
	public void readXml(){
		//URL http://download.post.gov.tw/post/download/Xml_10510.xml
		String localFile = "C:\\Users\\JerryLin\\Desktop\\Xml_10510.xml";
		try{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		    Document doc = docBuilder.parse (new File(localFile));

		    // normalize text representation
//		    doc.getDocumentElement().normalize();
		    System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

		    NodeList infos = doc.getDocumentElement().getChildNodes();
		    int totalCount = infos.getLength();
		    Set<String> citySet = new LinkedHashSet<>();
		    Set<String> areaSet = new LinkedHashSet<>();
		    Set<String> roadSet = new LinkedHashSet<>();
		    Set<String> otherSet = new LinkedHashSet<>();
		    for(int i = 0; i < totalCount; i++){
		    	Node info = infos.item(i);
		    	
		    	// 欄位1是 郵遞區號
		    	// 欄位4是 縣市 + 鄉鎮市區
		    	// 欄位2是 路(街)名或鄉里名稱
		    	// 欄位3是其他道路資訊
		    	
		    	// 其他道路資訊有幾種表達:

		    	if(info.getNodeType() == Node.ELEMENT_NODE){
		    		Element e = Element.class.cast(info);
		    		String postCode = e.getElementsByTagName("欄位1").item(0).getChildNodes().item(0).getNodeValue();
		    		String cityArea = e.getElementsByTagName("欄位4").item(0).getChildNodes().item(0).getNodeValue();
		    		String road = e.getElementsByTagName("欄位2").item(0).getChildNodes().item(0).getNodeValue();
		    		String others = e.getElementsByTagName("欄位3").item(0).getChildNodes().item(0).getNodeValue();
		    				    		
		    		int splitPos = cityArea.indexOf("縣"); // 縣要先判斷，因為會有宜蘭縣宜蘭市
		    		splitPos = splitPos >= 0 ? splitPos : cityArea.indexOf("市");
		    		String city = "";
		    		String area = "";
		    		if(splitPos >= 0){
		    			city = cityArea.substring(0, splitPos+1);
			    		area = cityArea.substring(splitPos+1, cityArea.length());
		    		}else{ // 如果中間不含縣或市，可能是釣魚臺釣魚臺
		    			city = cityArea.substring(0, 3);
			    		area = cityArea.substring(3, cityArea.length());
		    		}		    		
		    		assertTrue(city.length() > 0);
		    		assertTrue(area.length() > 0);
		    		citySet.add(city);
		    		areaSet.add(area);
		    		roadSet.add(road);
		    		otherSet.add(others);
		    	}
		    }
		    
		    for(String city : citySet){
		    	System.out.println(city);
		    }
		    for(String area : areaSet){
		    	System.out.println(area);
		    }
		    for(String road : roadSet){
		    	System.out.println(road);
		    }
		    for(String other : otherSet){
		    	System.out.println(other);
		    }
		    LOG.info("縣市共"+citySet.size()+"筆");
		    LOG.info("鄉鎮市區共"+areaSet.size()+"筆");
		    LOG.info("道路共"+roadSet.size()+"筆");
		    LOG.info("其他道路資訊共"+otherSet.size()+"筆");
		    LOG.info("共"+totalCount+"筆");
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Test
	public void sendPost(){
		String requestUrl = "http://www.post.gov.tw/post/internet/Postal/index.jsp?ID=208";
		String data = "city=新北市&cityArea=板橋區";
		String result = "";
		
		byte[] postData = data.getBytes(StandardCharsets.UTF_8);
		int len = postData.length;
		boolean isOk = false;
		try{
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("charset", "utf-8");
//			connection.setRequestProperty("Conetent-Length", Integer.toString(len));
//			connection.setRequestProperty("Conetent-Type", "application/x-www-form-urlencoded");
			connection.setUseCaches(false);
			
			boolean redirect = false;

			// normally, 3xx is redirect
			int status = connection.getResponseCode();
			System.out.println("status: " + status);
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
			
			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = connection.getHeaderField("Location");

				// get the cookie if need, for login
				String cookies = connection.getHeaderField("Set-Cookie");

				// open the new connnection again
				connection = (HttpURLConnection) new URL(newUrl).openConnection();
				connection.setRequestProperty("Cookie", cookies);
				connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				connection.addRequestProperty("User-Agent", "Mozilla");

				System.out.println("Redirect to URL : " + newUrl);

			}
//			try(BufferedOutputStream bos = new BufferedOutputStream(new DataOutputStream(connection.getOutputStream()));){
//				bos.write(postData);
//				bos.flush();
//			}
			int responseCode = connection.getResponseCode();
			LOG.info("responseCode: "+responseCode);
			// 回應碼在(包含)200~(不包含)400之間，都算成功
			isOk = (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_BAD_REQUEST);
			try(BufferedReader br = new BufferedReader(new InputStreamReader((isOk ? connection.getInputStream() : connection.getErrorStream())));){
				String lineResult = null;
				Pattern p = Pattern.compile("http://www\\.post\\.gov\\.tw/post/internet/Postal/index\\.jsp\\?ID=(\\d+)");
				
				while((lineResult = br.readLine())!=null){
					Matcher m = p.matcher(lineResult);
					while(m.find()){
						String id = m.group(1);
						System.out.println("id is " + id);
					}
					
					result+= lineResult;
				}
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		LOG.info(result);
	} 
}	
