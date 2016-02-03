package com.angrycat.erp.service.http;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.function.ConsumerThrowable;

@Service
@Scope("prototype")
public class HttpService {
	private final static String USER_AGENT = "Mozilla/5.0";
	
	public void sendPost(String requestUrl, ConsumerThrowable<BufferedInputStream> action)throws Throwable{
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		
		int responseCode = connection.getResponseCode();
		
		if(responseCode == HttpURLConnection.HTTP_OK){
			try(BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());){
				action.accept(bis);
			}
		}else{
			System.out.println("failed responseCode: " + responseCode + ", requestUrl: " + requestUrl);
		}
		connection.disconnect();
	}
}
