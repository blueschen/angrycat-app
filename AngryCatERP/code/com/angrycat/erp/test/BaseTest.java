package com.angrycat.erp.test;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.ModuleConfig;
import com.angrycat.erp.model.SalesDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;



public class BaseTest {
	
	public static void executeApplicationContext(Consumer<AnnotationConfigApplicationContext> logic){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		logic.accept(acac);
		acac.close();
	}
	
	protected static void executeSession(BiConsumer<Session, AnnotationConfigApplicationContext>c){
		executeApplicationContext(acac->{
			Session s = null;
			Transaction tx = null;
			try{
				s = acac.getBean(LocalSessionFactoryBean.class).getObject().openSession();
				tx = s.beginTransaction();
				c.accept(s, acac);
				tx.commit();
			}catch(Throwable e){
				tx.rollback();
				e.printStackTrace();
			}finally{
				
				if(s !=null && s.isOpen()){
					s.close();
				}
			}
		});
	}
	
	protected static void multilinePrint(Object obj){
		System.out.println(ReflectionToStringBuilder.toString(obj, ToStringStyle.MULTI_LINE_STYLE));
	}
	
	protected static void multilinePrint(List<?> list){
		list.stream().forEach(d->{
			multilinePrint(d);
		});
	}
	
	private static void testSubstr(){
		String t1 = "rettt.xls";
		String t2 = "eettrrggd.xlsx";
		System.out.println(t1.lastIndexOf("."));
		System.out.println(t1.length());
		System.out.println(t1.substring(t1.lastIndexOf(".")+1, t1.length()));
		
	}
	
	private static void testSystemProperty(){
		String root = System.getProperty("catalina.home");
		System.out.println(root);
	}
	
	private static void testNestedProperty(){
		Member m = new Member();
		m.setName("memberName");
		SalesDetail s = new SalesDetail();
		s.setMember(m);
		s.setFbName("fbName");
		
		String propName1 = "member.name";
		String propName2 = "fbName";
		try{
			System.out.println(PropertyUtils.getNestedProperty(s, propName1));
			System.out.println(PropertyUtils.getNestedProperty(s, propName2));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testStrLength(){
		String t = "Bearonthemoon@daum.net";
		System.out.println(t.length());
	}
	
	private static void testArgs(String...args){
		System.out.println(args.length);
	}
	
	private static void testFromJsonStrToObj(){
		executeApplicationContext(acac->{
			SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
			sfw.executeSession(s->{
				List<ModuleConfig> configs = s.createQuery("SELECT DISTINCT p FROM " + ModuleConfig.class.getName() + " p").list();
				configs.forEach(c->{
					ObjectMapper om = new ObjectMapper();
					try {
						Map<?, ?> result = om.readValue(c.getJson(), Map.class);
						result.forEach((k,v)->{
							System.out.println(k);
							System.out.println(v);
						});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
		});
	}
	
	/**
	 * 結論:透過JsonNode無法自動轉換日期格式成Java Date
	 */
	private static void testJsonNode(){
		executeApplicationContext(acac->{
			
			String sample = "{\"page\":1,\"filter\":{\"logic\":\"and\",\"filters\":[{\"operator\":\"gte\",\"value\":\"2016-04-26T00:00:00.000Z\",\"field\":\"orderDate\"}]}}";
			try{
				ObjectMapper om = new ObjectMapper();
				om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ"));
				JsonNode node = om.readValue(sample, JsonNode.class);										
				JsonNode element = node.findValue("filters");
				System.out.println("root node: " + node.getClass().getName() + ", isValueNode: " + node.isValueNode()); // ObjectNode
				System.out.println("filters node: " + element.getClass().getName() + ", isValueNode: " + element.isValueNode()); // ArrayNode
				
				ArrayNode array = (ArrayNode)element;
				Iterator<JsonNode> iterator = array.elements();
				while(iterator.hasNext()){
					JsonNode n = iterator.next();
					JsonNodeType type = n.getNodeType();
					System.out.println(type);
					System.out.println(n.isValueNode());
					Iterator<String> fieldNames = n.fieldNames();
					while(fieldNames.hasNext()){
						String fieldName = fieldNames.next();
						JsonNode field = n.get(fieldName);
						System.out.println(fieldName + ":"+field.asText() + ", isValueNode: " + field.isValueNode() + ", type: " + field.getNodeType());
						if("field".equals(fieldName) && "orderDate".equals(field.asText())){
							System.out.println("isInt: " + field.isInt());
							System.out.println("isDouble: " + field.isDouble());
							System.out.println("isFloat: " + field.isFloat());
							System.out.println("isFloatingPointNumber: " + field.isFloatingPointNumber());
							System.out.println("isLong: " + field.isLong());
							System.out.println("isTextual: " + field.isTextual());
						}
					}
				}
				
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			
		});
	}
	
	public static void main(String[]args){
		testJsonNode();
	}

	
	
	
}
