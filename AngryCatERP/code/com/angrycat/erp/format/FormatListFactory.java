package com.angrycat.erp.format;

import java.util.List;

import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.PurchaseBill;
import com.angrycat.erp.model.PurchaseBillDetail;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.model.VipDiscountDetail;


public class FormatListFactory {
	private static FormatList ofPurchaseBillDetails(int detailCount){
		FormatList list = ofPurchaseBase("yyyy-MM-dd");
		for(int i = 0; i < detailCount; i++){
			String subject = "進貨單紀錄"+(i+1)+"_";
			String field = "purchaseBillDetails["+i+"].";
			list.add(new DetailPropertyFormat(subject + "型號", field+"modelId"));
			list.add(new DetailPropertyFormat(subject + "名稱", field+"name"));
			list.add(new DetailPropertyFormat(subject + "英文名稱", field+"nameEng"));
			list.add(new DetailPropertyFormat(subject + "數量", field+"count"){});
		}
		return list;
	}
	
	private static FormatList ofPurchaseBase(String dateFormat){
		FormatList f = new FormatList();
		f.setDocTitle("no");
		
		PropertyFormat arriveDate = new PropertyFormat("到貨日", "arriveDate");
		arriveDate.setDateFormat(dateFormat);
		PropertyFormat stockDate = new PropertyFormat("入庫日", "stockDate");
		stockDate.setDateFormat(dateFormat);
		
		f.add(new PropertyFormat("單號", "no"));
		f.add(arriveDate);
		f.add(stockDate);
		f.add(new PropertyFormat("備註", "note"));
		
		return f;
	}
	
	private static FormatList ofProductBase(String dateFormat){
		FormatList f = new FormatList();
		f.setDocTitle("modelId");
		
		f.add(new PropertyFormat("型號", "modelId"));
		f.add(new PropertyFormat("定價", "suggestedRetailPrice"));
		f.add(new PropertyFormat("中文名字", "name"));
		f.add(new PropertyFormat("英文名字", "nameEng"));
		f.add(new PropertyFormat("系列名", "seriesName"));
		f.add(new PropertyFormat("條碼號", "barcode"));
		f.add(new PropertyFormat("商品類別", "productCategory.code"));
		f.add(new PropertyFormat("總庫存", "totalStockQty"));
		f.add(new PropertyFormat("總庫存修改備註", "totalStockChangeNote"));
		
		return f;
	}
	
	public static FormatList ofProducForExcelExport(){
		FormatList f = ofProductBase("yyyy-MM-dd");
		return f;
	}
	
	private static FormatList ofSalesBase(String dateFormat){
		FormatList f = new FormatList();
		f.setDocTitle("rowId");
		
		PropertyFormat orderDate = new PropertyFormat("銷售日", "orderDate");
		orderDate.setDateFormat(dateFormat);
		PropertyFormat shippingDate = new PropertyFormat("出貨日", "shippingDate");
		shippingDate.setDateFormat(dateFormat);
		PropertyFormat payDate = new PropertyFormat("付款日", "payDate");
		payDate.setDateFormat(dateFormat);
		
		f.add(new PropertyFormat("狀態", "saleStatus"));
		f.add(new PropertyFormat("姓名", "fbName"));
		f.add(new PropertyFormat("會員資料", "member.name"));
		f.add(new PropertyFormat("銷售點", "salePoint"));
		f.add(new PropertyFormat("型號", "modelId"));
		f.add(new PropertyFormat("明細", "productName"));
		f.add(new PropertyFormat("定價", "price"));
		f.add(new PropertyFormat("實收", "memberPrice"));
		f.add(new PropertyFormat("折扣別", "discountType"));
		f.add(orderDate);
		f.add(payDate);
		f.add(new PropertyFormat("付款別", "payType"));
		f.add(new PropertyFormat("付款狀態", "payStatus"));
		f.add(shippingDate);
		f.add(new PropertyFormat("郵寄方式", "sendMethod"));
		f.add(new PropertyFormat("登單者", "registrant"));
		f.add(new PropertyFormat("備註", "note"));
		f.add(new PropertyFormat("手機", "mobile"));
		f.add(new PropertyFormat("身分證", "idNo"));
		
		f.add(new PropertyFormat("對帳狀態", "checkBillStatus"));
		f.add(new PropertyFormat("已到貨", "arrivalStatus"));
		f.add(new PropertyFormat("郵寄地址電話", "contactInfo"));
		f.add(new PropertyFormat("活動", "activity"));
		f.add(new PropertyFormat("順序", "priority"));
		f.add(new PropertyFormat("其他備註", "otherNote"));
		
		return f;
	}
	
	private static FormatList ofExamBase(String dateFormat){
		FormatList f = new FormatList();
		f.setDocTitle("description");
		
		f.add(new PropertyFormat("題目", "description"));
		f.add(new PropertyFormat("類別", "category"));
		f.add(new PropertyFormat("提示", "hint"));
		PropertyFormat createDate = new PropertyFormat("新增題庫日", "createDate");
		createDate.setDateFormat(dateFormat);
		f.add(createDate);
		
		return f;
	}
	
	public static FormatList ofSalesDetailForExcelExport(){
		FormatList f = ofSalesBase("yyyy-MM-dd");
		return f;
	}
	
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
			list.add(new DetailPropertyFormat(subject + "轉VIP日", field+"toVipDate"){});
		}
		return list;
	}
	
	private static FormatList ofExamItems(int count){
		FormatList list = ofExamBase("yyyy-MM-dd");
		for(int i = 0; i < count; i++){
			String subject = "題項"+(i+1)+"_";
			String field = "items["+i+"].";
			list.add(new DetailPropertyFormat(subject + "順序", field+"sequence"));
			list.add(new DetailPropertyFormat(subject + "描述", field+"description"));
			list.add(new DetailPropertyFormat(subject + "正確答案", field+"correct"));
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
		Class<?> clz = obj.getClass();
		if(clz == Member.class){
			Member m = (Member)obj;
			int size = m.getVipDiscountDetails().size();
			formats = ofMemberDetails(size);
		}else if(clz == SalesDetail.class){
			formats = ofSalesDetailForExcelExport();
		}else if(clz == Product.class){
			formats = ofProducForExcelExport();
		}else if(clz == Exam.class){
			Exam e = (Exam)obj;
			int size = e.getItems().size();
			formats = ofExamItems(size);
		}else if(clz == PurchaseBill.class){
			PurchaseBill p = (PurchaseBill)obj;
			int size = p.getPurchaseBillDetails().size();
			formats = ofPurchaseBillDetails(size);
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
		Class<?> clz = oldObj.getClass();
		if(clz == Member.class){
			Member oldOne = (Member)oldObj;
			List<VipDiscountDetail> oldDetails = oldOne.getVipDiscountDetails();
			int oldSize = oldDetails.size();
			
			Member newOne = (Member)newObj;
			List<VipDiscountDetail> newDetails = newOne.getVipDiscountDetails();
			int newSize = newDetails.size();
			
			int maxSize = Math.max(oldSize, newSize);
			formats = ofMemberDetails(maxSize);
		}else if(clz == SalesDetail.class){
			formats = ofSalesDetailForExcelExport();
		}else if(clz == Product.class){
			formats = ofProducForExcelExport();
		}else if(clz == Exam.class){
			Exam oldOne = (Exam)oldObj;
			List<ExamItem> oldItems = oldOne.getItems();
			int oldSize = oldItems.size();
			Exam newOne = (Exam)newObj;
			List<ExamItem> newItems = newOne.getItems();
			int newSize = newItems.size();
			int maxSize = Math.max(oldSize, newSize);
			formats = ofExamItems(maxSize);
		}else if(clz == PurchaseBill.class){
			PurchaseBill oldOne = (PurchaseBill)oldObj;
			List<PurchaseBillDetail> oldDetails = oldOne.getPurchaseBillDetails();
			int oldSize = oldDetails.size();
			
			PurchaseBill newOne = (PurchaseBill)newObj;
			List<PurchaseBillDetail> newDetails = newOne.getPurchaseBillDetails();
			int newSize = newDetails.size();
			
			int maxSize = Math.max(oldSize, newSize);
			formats = ofPurchaseBillDetails(maxSize);
		}
		return formats;
	}
}
