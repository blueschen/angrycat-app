package com.angrycat.erp.service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.SalesDetail;

@Service
@Scope("singleton")
public class ModelPropertyService {
	@Autowired
	private LocalSessionFactoryBean sessionFactory;
	private Map<Class<?>, Map<String, Class<?>>> modelPropertyTypes = new LinkedHashMap<>();
	private Map<Class<?>, Map<String, String>> modelPropertyDefinitions = new LinkedHashMap<>();
	
	@PostConstruct
	public void init(){
		for(Map.Entry<String, ClassMetadata> c : sessionFactory.getObject().getAllClassMetadata().entrySet()){
			Class<?> mappingClz = c.getValue().getMappedClass();
			Map<String, Class<?>> typeMapping = new LinkedHashMap<>();
			Map<String, String> definitionMapping = new LinkedHashMap<>();
			getPropertyTypeMapping(mappingClz,typeMapping, definitionMapping, null, 2);
			modelPropertyTypes.put(mappingClz, typeMapping);
			modelPropertyDefinitions.put(mappingClz, definitionMapping);
		}
	}
	
	public Map<Class<?>, Map<String, Class<?>>> getModelPropertyTypes(){
		return this.modelPropertyTypes;
	}
	
	public static Object getNestedProperty(Object obj, String propertyName){
		if(obj == null || StringUtils.isBlank(propertyName)){
			return null;
		}
		String[] prop = propertyName.split("\\.");
		Object propVal = null;
		try{
			propVal = PropertyUtils.getNestedProperty(obj, prop[0]);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		if(prop.length == 1){
			return propVal;
		}else{
			String[] remaining = Arrays.copyOfRange(prop, 1, prop.length);
			return getNestedProperty(propVal, StringUtils.join(remaining, "."));
		}
	}
	
	private static Class<?> getFirstGenericType(Class<?> clz, String propName){
		Class<?> parameterGenericType = null;
		try {
			Field field = clz.getDeclaredField(propName);
			Type genericType = field.getGenericType();
			if(genericType != null && genericType instanceof ParameterizedType){
				ParameterizedType type = (ParameterizedType)genericType;
				if(type != null){
					Type[] args = type.getActualTypeArguments();
					parameterGenericType = (Class<?>)args[0];
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return parameterGenericType;
	}
	
	private static void getPropertyTypeMapping(Class<?> target, Map<String, Class<?>> typeMapping,  Map<String, String> definitionMapping, String parentPath, int level){
		if(StringUtils.isBlank(parentPath)){
			parentPath = "";
		}
		if(parentPath.split("\\.").length == level){
			return;
		}
		List<Class<?>> internals = 
			Arrays.asList(
				String.class,
				Boolean.class,
				Integer.class,
				Double.class,
				Float.class,
				Boolean.class,
				java.util.Date.class,
				java.sql.Date.class,
				java.sql.Timestamp.class);
		try{
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(target);
			for(PropertyDescriptor p : ps){
				String name = p.getName();
				Class<?> type = p.getPropertyType();
				Method method = p.getReadMethod();
				if(type == java.lang.Class.class){
					continue;
				}
				if(Collection.class.isAssignableFrom(type)){
					if(List.class.isAssignableFrom(type)){
						name += "[]";
					}else{
						continue;
					}
				}
				if(Map.class.isAssignableFrom(type)){
					name += "{}";
				}
				String currentPath = StringUtils.isBlank(parentPath) ? name : (parentPath + "." + name);
				typeMapping.put(currentPath, type);
				definitionMapping.put(currentPath, getMethodDefinition(method));
				
				if(name.contains("{}")){
					continue;
				}
				
				if(name.contains("[]")){
					Class<?> parameterizedGenericType = getFirstGenericType(target, name.replace("[]", ""));
					getPropertyTypeMapping(parameterizedGenericType, typeMapping, definitionMapping, currentPath, level);
					continue;
				}
				
				if(!type.isPrimitive() && !internals.contains(type)){
					getPropertyTypeMapping(type, typeMapping, definitionMapping, currentPath, level);
				}
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static String getMethodDefinition(Method method){
		String def = "";
		Column[] columns = method.getAnnotationsByType(Column.class);
		if(columns.length != 0){
			def = columns[0].columnDefinition();
		}
		JoinColumn[] joinColumns = method.getAnnotationsByType(JoinColumn.class);
		if(StringUtils.isBlank(def) && joinColumns.length != 0){
			def = joinColumns[0].columnDefinition();
		}
		return def;
	}
	
	private static void testGetFirstGenericType(){
		List<Member> list = new ArrayList<>();
		getFirstGenericType(Member.class, "vipDiscountDetails");
	}
	
	private static void testIsList(){
		System.out.println(List.class.isAssignableFrom(ArrayList.class));
	}
	
	private static void testGetPropertyTypeMapping(){
		Map<String, Class<?>> typeMapping = new LinkedHashMap<>();
		Map<String, String> definitionMapping = new LinkedHashMap<>();
		getPropertyTypeMapping(SalesDetail.class, typeMapping, definitionMapping, "", 2);
//		for(Map.Entry<String, Class<?>> c : container.entrySet()){
//			System.out.println("key: " + c.getKey() + ", val: " + c.getValue());
//		}
	}
	
	private static void testGetNestedProperty(){
		Member m = new Member();
		m.setName("memberName");
		m.setFbNickname("fbNickname");
		SalesDetail s = new SalesDetail();
//		s.setMember(m);
		
		String propName = "member.name";
		System.out.println(getNestedProperty(s, propName));
	}
	
	private static void testGetMethodAnnotationValue(){
		PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(SalesDetail.class);
		for(PropertyDescriptor p : ps){
			Method method = p.getReadMethod();
			if(method.getName().equals("getId")){
				Column[] columns = method.getAnnotationsByType(Column.class);
				if(columns.length != 0){
					System.out.println(columns[0].columnDefinition());
				}else{
					JoinColumn[] joinColumns = method.getAnnotationsByType(JoinColumn.class);
					System.out.println(joinColumns[0].columnDefinition());
				}
			}
		}
		
	}
	
	public static void main(String[]args){
		testGetMethodAnnotationValue();
	}
}
