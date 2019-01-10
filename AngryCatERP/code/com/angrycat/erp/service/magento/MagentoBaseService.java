package com.angrycat.erp.service.magento;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.JsonNodeWrapper;
import com.angrycat.erp.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
/**
 * 對應Magento Web Service服務
 * 通常的開發方式是:先開發自定義Magento Web API，再寫Java銜接
 * 如果Magento已上線，又是新開發的模組，除了要上傳模組程式碼，在magento/app/etc/modules/Angrycat_All.xml下要新增模組定義；除此之外，要到magento後台系統/進階/進階去重新選擇(沒用到的模組)儲存一次，在這邊能夠看到新模組名稱，他才能被找到
 * @author JerryLin
 *
 */
public class MagentoBaseService implements Serializable{
	private static final long serialVersionUID = 2360391978352669173L;
	private static final Pattern FIND_ERR_MSG = Pattern.compile("report.php\\?id=(\\d+)");
	private static final Logger LOG = Logger.getLogger(MagentoBaseService.class.getName());

	@Autowired
	Environment env;
	@Autowired
	BeanFactory beanFactory;
	@Autowired
	private MailService mailService;
	
	private boolean debug;
	public static final String INTRANET_BASE_URL = "http://192.168.1.2/magento/index.php";
	public static final String LOCALHOST_BASE_URL = "http://localhost/magento/index.php";
	public static final String SERVER_LOCAL_BASE_URL = "http://127.0.0.1/magento/index.php";
	String baseUrl = LOCALHOST_BASE_URL;
	String linodeDomain;
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
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
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
		if(debug){
			System.out.println("requestUrl:" + rquestUrl);
		}
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
			if(debug){
				System.out.println("http responseCode is " + responseCode);
			}
			// 回應碼在(包含)200~(不包含)400之間，都算成功
			// 503 Service Unavailable
			// TODO 如果沒有回傳200，要怎麼處理後續的程式邏輯
			isOk = (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_BAD_REQUEST);
			try(BufferedReader br = new BufferedReader(new InputStreamReader((isOk ? connection.getInputStream() : connection.getErrorStream())));){
				String lineResult = null;
				while((lineResult = br.readLine())!=null){
					result+= lineResult;
				}
			}
		}catch(Throwable e){
			LOG.info(e.toString());
			mailService.subject("MagentoBaseService.connect Err").content(e.toString()).sendSimple();
			throw new RuntimeException(e);
		}
		String errMsg = getRetErrMsg(result);
		if(debug){
			System.out.println("result string is " + result);
		}
		if(errMsg != null){
			if(debug){
				System.out.println("errMsg is " + errMsg);
			}
			return errMsg;
		}
		return result;
	}
	JsonNodeWrapper renderJson(String json){
		if(StringUtils.isNotBlank(json) && json.startsWith("<")){
			mailService
			.to(JERRY)
			.subject("renderJson error")
			.content(json)
			.sendSimple();
			return null;
		}
		JsonNodeWrapper jnw = beanFactory.getBean(JsonNodeWrapper.class, json);
		jnw.filterObjectNode();
		return jnw;
	}
	public JsonNodeWrapper request(String url, Object...args){
		String json = connect(url, args);
		if(debug){
			System.out.println("return data:\n" + json);
		}
		JsonNodeWrapper jnw = renderJson(json);
		return jnw;
	}
	
	/**
	 * Windows下Magenot可參考錯誤log位置:<br>
	 * Apache24\logs
	 * php\temp
	 * Magento\var\log
	 * Magento\var\report
	 * @param retMsg
	 * @return
	 */
	public String getRetErrMsg(String retMsg){
		String errMsg = null;
		if(!retMsg.contains("There has been an error processing your request")){
			return errMsg;
		}
		Matcher m = FIND_ERR_MSG.matcher(retMsg);
		while(m.find()){
			String id = m.group(1);
			errMsg = "Thrown Errs:\nit may be caused by 'not found', details referencing /var/report id: " + id;
		}
		if(errMsg == null){
			errMsg = "Thrown Errs:\n" + retMsg;
		}
		return errMsg;
	}
}
