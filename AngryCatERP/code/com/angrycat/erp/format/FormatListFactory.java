package com.angrycat.erp.format;

import java.util.List;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;


public class FormatListFactory {
	
	private static FormatList ofMemberBase(String dateFormat){
		FormatList f = new FormatList();
		f.setDocTitle("name");
		
		PropertyFormat toVipDate = new PropertyFormat("轉VIP起始日", "toVipDate");
		toVipDate.setDateFormat(dateFormat);
		PropertyFormat birthday = new PropertyFormat("出生年月日", "birthday");
		birthday.setDateFormat(dateFormat);
		
		f.add(toVipDate);
		f.add(new BooleanPropertyFormat("Ohmliy VIP", "important", new String[]{"VIP", null}));
		f.add(new PropertyFormat("Facebook 姓名（中文/英文)", "fbNickname"));
		f.add(new PropertyFormat("真實姓名", "name"));
		f.add(new PropertyFormat("性別", "gender"){
			public String getValue(Object obj){
				int val = (int)super.getPropertyObject(obj);
				if(val==0){
					return "男";
				}else if(val==1){
					return "女";
				}
				return null;
			}
		});
		f.add(new PropertyFormat("身分證字號", "idNo"));		
		f.add(birthday);
		f.add(new PropertyFormat("電子信箱", "email"));
		f.add(new PropertyFormat("手機電話", "mobile"));
		f.add(new PropertyFormat("室內電話", "tel"));
		f.add(new PropertyFormat("郵遞區號", "postalCode"));
		f.add(new PropertyFormat("地址", "address"));		
		f.add(new PropertyFormat("備註", "note"));
		f.add(new PropertyFormat("客戶編號", "clientId"));
		
		return f;
	}
	
	public static FormatList ofMemberForExcelExport(){
		FormatList f = ofMemberBase("yyyy-MM-dd");
		return f;
	}
	
	private static FormatList ofMemberMaster(){
		FormatList f = ofMemberBase("yyyy-MM-dd");
		f.add(new PropertyFormat("轉VIP到期日", "toVipEndDate"));
		return f;
	}
	
	private static FormatList ofMemberDetails(int vipCount){
		FormatList list = ofMemberMaster();
		for(int i = 0; i < vipCount; i++){
			String subject = "VIP紀錄"+(i+1)+"_";
			String field = "vipDiscountDetails["+i+"].";
			list.add(new DetailPropertyFormat(subject + "有效起始日", field+"effectiveStart"));
			list.add(new DetailPropertyFormat(subject + "有效結束日", field+"effectiveEnd"));
			list.add(new DetailPropertyFormat(subject + "折扣使用日期", field+"discountUseDate"));
			list.add(new DetailPropertyFormat(subject + "轉VIP日", field+"toVipDate"));
		}
		return list;
	}
	/**
	 * 新增、刪除記錄用
	 * @param obj
	 * @return
	 */
	public static <T>FormatList forLog(T obj){
		FormatList formats = FormatList.emptyList();
		if(obj.getClass() == Member.class){
			Member m = (Member)obj;
			int size = m.getVipDiscountDetails().size();
			formats = ofMemberDetails(size);
		}
		return formats;
	}
	/**
	 * 修改紀錄用
	 * 
	 * 
	 * @param oldObj
	 * @param newObj
	 * @return
	 */
	public static <T>FormatList forUpdateLog(T oldObj, T newObj){
		FormatList formats = FormatList.emptyList();
		if(oldObj.getClass() == Member.class){
			Member oldOne = (Member)oldObj;
			List<VipDiscountDetail> oldDetails = oldOne.getVipDiscountDetails();
			int oldSize = oldDetails.size();
			
			Member newOne = (Member)newObj;
			List<VipDiscountDetail> newDetails = newOne.getVipDiscountDetails();
			int newSize = newDetails.size();
			
			int maxSize = Math.max(oldSize, newSize);
			formats = ofMemberDetails(maxSize);
		}
		return formats;
	}
}
