package com.angrycat.erp.excel;

import java.util.ArrayList;
import java.util.List;
import static com.angrycat.erp.excel.OnePosInitialExcelAccessor.CAT_GIFT;

public class OnePosDiscountItem {
	private String id;
	private String name;
	private String categoryId = CAT_GIFT;
	private String barCode;
	private String scannableBarCode;
	private String price;
	private String brandId = OnePosInitialExcelAccessor.BRAND_ID;
	
	public OnePosDiscountItem(String id, String name, String categoryId, String price){
		this.id = id;
		this.name = name;
		this.categoryId = categoryId;
		this.price = price;
		
		this.barCode = id;
		this.scannableBarCode = "*" + id +"*";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public String getScannableBarCode() {
		return scannableBarCode;
	}
	public void setScannableBarCode(String scannableBarCode) {
		this.scannableBarCode = scannableBarCode;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public static List<OnePosDiscountItem> getDefaultItems(){
		List<OnePosDiscountItem> items = new ArrayList<>();
		items.add(new OnePosDiscountItem("CITIBANK100",	"花旗禮卷100元",	CAT_GIFT, "-100"));
		items.add(new OnePosDiscountItem("CTBC100",		"中信託禮卷100元", CAT_GIFT, "-100"));
		items.add(new OnePosDiscountItem("ESLITE100",	"誠品禮卷100元", 	CAT_GIFT, "-100"));
		items.add(new OnePosDiscountItem("ESLITE200", 	"誠品禮卷200元",	CAT_GIFT, "-200"));
		items.add(new OnePosDiscountItem("ESLITE500", 	"誠品禮卷500元", 	CAT_GIFT, "-500"));
		items.add(new OnePosDiscountItem("OHM200",		"OHM禮卷200元", 	CAT_GIFT, "-200"));
		items.add(new OnePosDiscountItem("OHM300",		"OHM禮卷300元", 	CAT_GIFT, "-300"));
		return items;
	}
}
