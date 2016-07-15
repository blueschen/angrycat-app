package com.angrycat.erp.common;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CommonUtil {
	private static final Map<String, String> DISPLAY_COUNTRY;
	static{
		DISPLAY_COUNTRY = genDisplayCountry();
	}
	
	public static String parseToJson(Object object){
		String json = parseToJson(object, null);
		return json;
	}
	
	public static String parseToJson(Object object, Class<?> mixinTarget, Class<?> mixinSource){
		Map<Class<?>, Class<?>> mixins = new HashMap<>();
		mixins.put(mixinTarget, mixinSource);
		String json = parseToJson(object, mixins);
		return json;
	}
	
	public static String parseToJson(Object object, Map<Class<?>, Class<?>> mixins){
		String json = "";
		try{
			ObjectMapper om = new ObjectMapper();
			if(mixins != null){
				mixins.forEach((target, mixin)->{
					om.addMixIn(target, mixin);
				});
			}
			json = om.writeValueAsString(object);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		return json;
	}
	
	public static Map<?, ?> parseJsonStrToMap(String json){
		Map<?, ?> result = null;
		try{
			ObjectMapper om = new ObjectMapper();
			result = om.readValue(json, Map.class);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T>T getProxyTargetObject(Object proxy){
		T t = null;
		try{
			if(AopUtils.isJdkDynamicProxy(proxy)){
				t = (T)((Advised)proxy).getTargetSource().getTarget();
			}else{
				t = (T)proxy;
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return t;
	}
	/**
	 * 取得國家代碼與對應的語言代碼，
	 * 譬如台灣國碼為TW語言為zh、美國國碼為US語言為es
	 * 但並非每個國家都有對應的語言，
	 * 這影響到程式能否以該國的語言顯示資料。
	 * @return
	 */
	private static Map<String, String> initCountryLanguage(){
		Map<String, String> results = new TreeMap<>();
		Locale[] locales = Locale.getAvailableLocales();
		for(Locale locale: locales){
			if(StringUtils.isNotBlank(locale.getDisplayCountry())){
				results.put(locale.getCountry(), locale.getLanguage());
			}
		}
		return results;
	}
	/**
	 * 取得國碼與語言的對照表
	 * 可以用該國語言顯示該國名
	 * 如果沒有對應語言的國家會略過
	 * @return
	 */
	private static Map<String, String> genDisplayCountry(){
		Map<String, String> displayCountry = new TreeMap<>();
		Map<String, String> countryLanguage = initCountryLanguage();
		String[]countries = Locale.getISOCountries();
		for(String countrycode : countries){
			Locale locale = null;
			String language = countryLanguage.get(countrycode);
			if(language == null){// 代表這個國家沒有對應的語言
				locale = new Locale("", countrycode);
			}else{
				locale = new Locale(language, countrycode);
				displayCountry.put(locale.getCountry(), locale.getDisplayCountry(locale));
			}
		}
		return displayCountry;
	}
	public static Map<String, String> getDisplayCountry(){
		return DISPLAY_COUNTRY;
	}
	private static void testGenDisplayCountry(){
		Map<String, String> displayCountry = genDisplayCountry();
		displayCountry.forEach((country, language)->{
			System.out.println("country: " + country + ", display: " + language);
		});
		System.out.println("count" + displayCountry.size());
	}
	public static Object getProperty(Object bean, String name){
		Object propertyVal = null;
		try{
			propertyVal = PropertyUtils.getProperty(bean, name);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return propertyVal;
	}
	public static void setProperty(Object bean, String name, Object val){
		try{
			PropertyUtils.setProperty(bean, name, val);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 顯示節點狀態
	 * isContainerNode == true && isArray == true 代表陣列物件
	 * isContainerNode == true && isObject == true 代表基本物件
	 * isValuNode == true 代表屬性值節點
	 * isTextual == true 代表這個屬型值節點是字串類型
	 * @param node
	 */
	public static void printStatus(JsonNode node){
		PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(node);
		Arrays.asList(ps).stream().forEach(pd->{
			Method method = pd.getReadMethod();
			String name = method.getName();
			if(name.indexOf("is") == 0){
				try{
					Object val = method.invoke(node);
					if(val != null){
//						System.out.println(name + ":" + val);
						if(val instanceof Boolean && ((boolean)val) == true){
							System.out.println(name);
						}
					}
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
		});
	}	
	public static void iterate(JsonNode node, Consumer<JsonNode> consumer){
		consumer.accept(node);
		if(node.isArray()){
			node.forEach(n->{
				iterate(n, consumer);
			});
		}
	}	
	/**
	 * 
	 * @param node
	 * @param consumer key是fielName, value是屬性值節點
	 */
	public static void iterateField(JsonNode node, BiConsumer<String, JsonNode> consumer){
		iterate(node, n->{
			processSingleNode(n, consumer);
		});
	}
	public static void processSingleNode(JsonNode n, BiConsumer<String, JsonNode> consumer){
		if(n.isContainerNode() && n.isObject()){
			Iterator<String> fieldNames = n.fieldNames();
			while(fieldNames.hasNext()){
				String fieldName = fieldNames.next();
				JsonNode fieldNode = n.get(fieldName);
				if(fieldNode != null){
					consumer.accept(fieldName, fieldNode);
				}else{
					System.out.println("fieldNode is null: " + fieldName);
				}
				
			};
		}
	}
	public static void main(String[]args){
		testGenDisplayCountry();
	}
}
