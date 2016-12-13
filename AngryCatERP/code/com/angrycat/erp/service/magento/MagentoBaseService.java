package com.angrycat.erp.service.magento;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class MagentoBaseService implements Serializable{
	private static final long serialVersionUID = 2360391978352669173L;

	@Autowired
	private Environment env;
	@Autowired
	BeanFactory beanFactory;
	boolean debug;
	
	static final String INTRANET_BASE_URL = "http://192.168.1.15/magento/index.php";
	static final String LOCALHOST_BASE_URL = "http://localhost/magento/index.php";
	private String baseUrl = LOCALHOST_BASE_URL;
	private String controller = "";
	private String module = "";
	public void setBaseUrl(String baseUrl){
		this.baseUrl = baseUrl;
	}
	public void setModule(String module){
		this.module = module;
	}
	public void setController(String controller){
		this.controller = controller;
	}
	String getRequestUrl(String action){
		String requestUrl = baseUrl + "/" + module + "/" + controller + "/" + action;
		return requestUrl;
	}
	public String connect(String action, Object...args){
		String rquestUrl = getRequestUrl(action);
		String user = env.getProperty("magento.api.user");
		String key = env.getProperty("magento.api.key");
		String data = "apiUser=" + user + "&apiKey=" + key + "&";
//		System.out.println("requestUrl:" + rquestUrl);
		String result = "";
		
		try{
			if(args!=null && args.length>0){
				ObjectMapper om = new ObjectMapper();
				String writeData = om.writeValueAsString(args);
				data += ("data=" + writeData);
				if(debug){
					System.out.println("post data:\n" + writeData);
				}
			}
			byte[] postData = data.getBytes(StandardCharsets.UTF_8);
			int len = postData.length;
			boolean isOk = false;
			URL url = new URL(rquestUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Conetent-Length", Integer.toString(len));
			connection.setUseCaches(false);
			
			try(BufferedOutputStream bos = new BufferedOutputStream(new DataOutputStream(connection.getOutputStream()));){
				bos.write(postData);
				bos.flush();
			}
			int responseCode = connection.getResponseCode();
			// 回應碼在(包含)200~(不包含)400之間，都算成功
			isOk = (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_BAD_REQUEST);
			try(BufferedReader br = new BufferedReader(new InputStreamReader((isOk ? connection.getInputStream() : connection.getErrorStream())));){
				String lineResult = null;
				while((lineResult = br.readLine())!=null){
					result+= lineResult;
				}
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return result;
	}
	public JsonNodeWrapper request(String url, Object...args){
		String json = connect(url, args);
		if(debug){
			System.out.println("return data:\n" + json);
		}
		JsonNodeWrapper jnw = beanFactory.getBean(JsonNodeWrapper.class, json);
		return jnw;
	}
}
