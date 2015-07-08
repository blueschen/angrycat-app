package com.angrycat.erp.format;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;

import com.angrycat.erp.model.Member;


public class FormatListFactory {
	
	private static final MultiKeyMap map = new MultiKeyMap();
	
	static{
		map.put(Member.class, FormatUse.DELETE_LOG, ofMemberForDeleteLog());	
	}
		
	public static List<ObjectFormat> findFormatList(Class<?> clz, FormatUse use){
		List<ObjectFormat> list = (List<ObjectFormat>)map.get(clz, use);
		return list == null ? Collections.emptyList() : list;
	}
	
	public static List<ObjectFormat> ofMemberForDeleteLog(){
		List<ObjectFormat> f = new LinkedList<>();
		
		f.add(new PropertyFormat("姓名", "name"));
		f.add(new PropertyFormat("FB暱稱", "fbNickname"));
		f.add(new PropertyFormat("身分證字號", "idNo"));		
		f.add(new PropertyFormat("是否為VIP", "important"));
		f.add(new PropertyFormat("性別", "gender"));
		f.add(new PropertyFormat("生日", "birthday"));
		f.add(new PropertyFormat("電子信箱", "email"));
		f.add(new PropertyFormat("電話", "mobile"));
		f.add(new PropertyFormat("郵遞區號", "postalCode"));
		f.add(new PropertyFormat("地址", "address"));
		f.add(new PropertyFormat("轉VIP日期", "toVipDate"));
		f.add(new PropertyFormat("備註", "note"));
		
		return f;
	}
}
