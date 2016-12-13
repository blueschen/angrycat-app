package com.angrycat.erp.component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 將JsonNode包裝起來，<br>
 * 提供更直覺、友善、易於維護的操作介面。<br>
 * 關鍵類別: JsonNode, ObjectMapper<br>
 * ref. http://fasterxml.github.io/jackson-databind/javadoc/2.2.0/
 * @author JerryLin
 *
 */
@Component
@Scope(value="prototype")
@Lazy(value=true)
public class JsonNodeWrapper {
	private JsonNode root;
	private List<JsonNode> found = new LinkedList<>();
	public JsonNodeWrapper(String source){
		ObjectMapper om = new ObjectMapper();
		try{
			root = om.readTree(source);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 過濾出所有的Object Node；<br>
	 * 如果遇到Array Node，<br>
	 * 針對他的元素檢查是否為Object Node。<br>
	 * JsonNode如果為Object Node，<br>
	 * 則代表是屬性值的集合。<br>
	 * @return
	 */
	public JsonNodeWrapper filterObjectNode(){
		found.clear();
		filterObjectNode(root);
		return this;
	}
	private void filterObjectNode(JsonNode node){
		if(node.isObject()){
			found.add(node);
		}else if(node.isArray()){
			node.forEach(n->{
				filterObjectNode(n);
			});
		}
	}
	public <T>List<T> toList(Function<JsonNode, T> exe){
		return found.stream().map(exe).collect(Collectors.toList());
	} 
	public void consume(Consumer<JsonNode> consumer){
		found.stream().forEachOrdered(consumer);
	}
	public JsonNodeWrapper filter(Predicate<JsonNode> tester){
		found = found.stream().filter(tester).collect(Collectors.toList());
		return this;
	}
	public JsonNode getRoot(){
		return root;
	}
	public List<JsonNode> getFound(){
		return found;
	}
	/**
	 * 列印出Object Node上所有的屬性值<br>
	 * 目的在觀察資料結構和除錯
	 * @param n
	 */
	public static void printObjectNodeValues(JsonNode n){
		if(!n.isObject()){
			return;
		}
		Iterator<String> fieldNames = n.fieldNames();
		while(fieldNames.hasNext()){
			String fieldName = fieldNames.next();
			JsonNode valueNode = n.get(fieldName);
			Object val = getVal(valueNode);
			System.out.println(fieldName + ":" + val);
		}
	}
	private static Object getVal(JsonNode valueNode){
		Object val = null;
		if(valueNode.isTextual()){
			val = valueNode.textValue();
		}else if(valueNode.isBoolean()){
			val = valueNode.booleanValue();
		}else if(valueNode.isDouble()){
			val = valueNode.doubleValue();
		}else if(valueNode.isFloat()){
			val = valueNode.floatValue();
		}else if(valueNode.isInt()){
			val = valueNode.intValue();
		}else if(valueNode.isLong()){
			val = valueNode.longValue();
		}else if(valueNode.isShort()){
			val = valueNode.shortValue();
		}else if(valueNode.isBigInteger()){
			val = valueNode.bigIntegerValue();
		}else if(valueNode.isBigDecimal()){
			//??
		}
		return val;
	}
}
