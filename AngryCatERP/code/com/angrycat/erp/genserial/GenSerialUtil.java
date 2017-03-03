package com.angrycat.erp.genserial;

import java.util.HashMap;
import java.util.Map;

public class GenSerialUtil {
	private static Map<String, SerialGenerator<String, ?>> generators = new HashMap<>();
	public static synchronized void addGenerator(SerialGenerator<String, ?> generator){
		if(generator.getId() == null){
			throw new RuntimeException("generator id can't be null");
		}
		if(generators.containsKey(generator.getId())){
			throw new RuntimeException("duplicate generator id[" + generator.getId() + "]");
		}
		generators.put(generator.getId(), generator);
	}
	public static synchronized String getNext(String id)throws Throwable{
		SerialGenerator<String, ?> generator = generators.get(id);
		if(generator == null){
			throw new RuntimeException("SerialGenerator id not found[" + id + "]");
		}
		return generator.getNext();
	}
	public static synchronized <S>String getNext(String id, S s){
		SerialGenerator<String, S> generator = (SerialGenerator<String, S>)generators.get(id);
		if(generator == null){
			throw new RuntimeException("SerialGenerator id not found[" + id + "]");
		}
		return generator.getNext(s);
	}
}
