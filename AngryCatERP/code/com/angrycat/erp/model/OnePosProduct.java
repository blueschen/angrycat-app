package com.angrycat.erp.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name="Products")
public class OnePosProduct {
	// 數字(雙精準數):double or Double
	// 數字(長整數):int or Integer 
	// 數字(小數點):BigDecimal 
	// 是/否:boolean or Boolean 選擇boolean或Boolean相對的readMethod不一樣，boolean是isXxx，Boolean是getXxx
	private String productId;
	private String productName;
	private String productDescription;
	private String categoryId;
	private String itemTypeId;
	private String productOtherId;
	private String productUnit;
	private double price = 0.0;
	private double priceVIP = 0.0;
//	private int cost;
//	private String productAcdCode;
//	private String productAccCode;
	private String productRemark;
	private String productAlert;
	private int productDisabled = 0;
	private int productDeleted = 0;
	private String dateCreated;
	private String dateModified;
	private String brandId;
	private String vendorId;
	private String vendorCode;
	private int reorderQty;
	private String modelNo;
	private int allowOverSale = 0;
	private double averageCost = 0.0;
	private BigDecimal price3 = BigDecimal.ZERO;
	private BigDecimal price4 = BigDecimal.ZERO;
	private BigDecimal price5 = BigDecimal.ZERO;
	private String web;
	private boolean tax1InUse = Boolean.FALSE;
	private boolean tax2InUse = Boolean.FALSE;
	private Timestamp displayByDate;
	private BigDecimal eShopQty = BigDecimal.ZERO;
	@Column(name="productid", columnDefinition="產品編號")
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	@Column(name="productname", columnDefinition="產品名稱")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@Column(name="productdescription", columnDefinition="產品描述")
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	@Column(name="categoryid", columnDefinition="產品類別")
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	@Column(name="itemtypeid", columnDefinition="項目性質")
	public String getItemTypeId() {
		return itemTypeId;
	}
	public void setItemTypeId(String itemTypeId) {
		this.itemTypeId = itemTypeId;
	}
	@Column(name="productotherid", columnDefinition="條碼")
	public String getProductOtherId() {
		return productOtherId;
	}
	public void setProductOtherId(String productOtherId) {
		this.productOtherId = productOtherId;
	}
	@Column(name="productunit", columnDefinition="單位")
	public String getProductUnit() {
		return productUnit;
	}
	public void setProductUnit(String productUnit) {
		this.productUnit = productUnit;
	}
	@Column(name="price", columnDefinition="基本Price")
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Column(name="priceVIP", columnDefinition="VIP價格")
	public double getPriceVIP() {
		return priceVIP;
	}
	public void setPriceVIP(double priceVIP) {
		this.priceVIP = priceVIP;
	}
	@Column(name="productremark", columnDefinition="備註")
	public String getProductRemark() {
		return productRemark;
	}
	public void setProductRemark(String productRemark) {
		this.productRemark = productRemark;
	}
	@Column(name="productalert", columnDefinition="POS零售彈出提示")
	public String getProductAlert() {
		return productAlert;
	}
	public void setProductAlert(String productAlert) {
		this.productAlert = productAlert;
	}
	@Column(name="productdisabled", columnDefinition="已失效")
	public int getProductDisabled() {
		return productDisabled;
	}
	public void setProductDisabled(int productDisabled) {
		this.productDisabled = productDisabled;
	}
	@Column(name="productdeleted", columnDefinition="已刪除")
	public int getProductDeleted() {
		return productDeleted;
	}
	public void setProductDeleted(int productDeleted) {
		this.productDeleted = productDeleted;
	}
	@Column(name="datecreated", columnDefinition="建立日期")
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	@Column(name="datemodified", columnDefinition="修改日期")
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	@Column(name="brandid", columnDefinition="品牌")
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	@Column(name="vendorid", columnDefinition="供應商")
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	@Column(name="vendorcode", columnDefinition="供應商來貨編號")
	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}
	@Column(name="reorderqty", columnDefinition="重訂數量")
	public int getReorderQty() {
		return reorderQty;
	}
	public void setReorderQty(int reorderQty) {
		this.reorderQty = reorderQty;
	}
	@Column(name="modelno", columnDefinition="型號")
	public String getModelNo() {
		return modelNo;
	}
	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}
	@Column(name="allowoversale", columnDefinition="庫存不足也可銷售")
	public int getAllowOverSale() {
		return allowOverSale;
	}
	public void setAllowOverSale(int allowOverSale) {
		this.allowOverSale = allowOverSale;
	}
	@Column(name="AverageCost", columnDefinition="平均成本")
	public double getAverageCost() {
		return averageCost;
	}
	public void setAverageCost(double averageCost) {
		this.averageCost = averageCost;
	}
	@Column(name="price3", columnDefinition="price3")
	public BigDecimal getPrice3() {
		return price3;
	}
	public void setPrice3(BigDecimal price3) {
		this.price3 = price3;
	}
	@Column(name="price4", columnDefinition="price4")
	public BigDecimal getPrice4() {
		return price4;
	}
	public void setPrice4(BigDecimal price4) {
		this.price4 = price4;
	}
	@Column(name="price5", columnDefinition="price5")
	public BigDecimal getPrice5() {
		return price5;
	}
	public void setPrice5(BigDecimal price5) {
		this.price5 = price5;
	}
	@Column(name="web", columnDefinition="網站")
	public String getWeb() {
		return web;
	}
	public void setWeb(String web) {
		this.web = web;
	}
	@Column(name="Tax1InUse", columnDefinition="稅項一")
	public boolean isTax1InUse() {
		return tax1InUse;
	}
	public void setTax1InUse(boolean tax1InUse) {
		this.tax1InUse = tax1InUse;
	}
	@Column(name="Tax2InUse", columnDefinition="稅向二")
	public boolean isTax2InUse() {
		return tax2InUse;
	}
	public void setTax2InUse(boolean tax2InUse) {
		this.tax2InUse = tax2InUse;
	}
	@Column(name="DisplayByDate", columnDefinition="陳列日期至")
	public Timestamp getDisplayByDate() {
		return displayByDate;
	}
	public void setDisplayByDate(Timestamp displayByDate) {
		this.displayByDate = displayByDate;
	}
	@Column(name="eShopQty", columnDefinition="網店備用數量")
	public BigDecimal geteShopQty() {
		return eShopQty;
	}
	public void seteShopQty(BigDecimal eShopQty) {
		this.eShopQty = eShopQty;
	}
}
