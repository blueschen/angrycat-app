package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="product")
public class Product {
	private String id;
	private ProductCategory productCategory;
	private String modelId;
	private double suggestedRetailPrice;
	private String name;
	private String nameEng;
	private String seriesName;
	private String barcode;
	private String imgDir;
	
	private int totalStockQty;
	private int officeStockQty;					
	private int drawerStockQty;					
	private int showcaseStockQty;
	private int notShipStockQty;
	private int drawerInZhongheStockQty;
	private int showcaseInZhongheStockQty;
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_product_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_product_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="productCategory", columnDefinition="商品類別")
	public ProductCategory getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
	}
	@Column(name="modelId", columnDefinition="型號")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	@Column(name="suggestedRetailPrice", columnDefinition="定價")
	public double getSuggestedRetailPrice() {
		return suggestedRetailPrice;
	}
	public void setSuggestedRetailPrice(double suggestedRetailPrice) {
		this.suggestedRetailPrice = suggestedRetailPrice;
	}
	@Column(name="name", columnDefinition="中文名字")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="nameEng", columnDefinition="英文名字")
	public String getNameEng() {
		return nameEng;
	}
	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}
	@Column(name="seriesName", columnDefinition="系列名")
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	@Column(name="barcode", columnDefinition="條碼號")
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	/**
	 * 取得調整過後的型號，因為Towntalk的型號似乎是自編，所以會有一些不常見的編碼情況。
	 * 這種編碼情況，可能會導致後續各式資料處理發生非預期的例外。
	 * 譬如OnePos不接受括弧作為他的產品編號(因為我們以型號作為他的產品編號)，
	 * 而且原始資料也有空白、重複等情況。
	 * @return
	 */
	@Transient
	public String getModelIdAdjusted(){
		String modelIdAdjusted = modelId.replace(" ", "");
		if(modelIdAdjusted.contains("(")){
			int startIdx = modelIdAdjusted.indexOf("(");
			int endIdx = modelIdAdjusted.indexOf(")");
			String content = modelIdAdjusted.substring(startIdx+1, endIdx);
			String preContent = modelIdAdjusted.substring(0, startIdx);
			if(content.equals(preContent)){
				modelIdAdjusted = preContent;
			}else{
				modelIdAdjusted = preContent + "_" + content;
			}
		}
		return modelIdAdjusted;
	}
	@Column(name="imgDir", columnDefinition="圖片位置")
	public String getImgDir() {
		return imgDir;
	}
	public void setImgDir(String imgDir) {
		this.imgDir = imgDir;
	}
	@Column(name="totalStockQty", columnDefinition="總庫存")
	public int getTotalStockQty() {
		return totalStockQty;
	}
	public void setTotalStockQty(int totalStockQty) {
		this.totalStockQty = totalStockQty;
	}
	@Column(name="officeStockQty", columnDefinition="辦公室庫存")
	public int getOfficeStockQty() {
		return officeStockQty;
	}
	public void setOfficeStockQty(int officeStockQty) {
		this.officeStockQty = officeStockQty;
	}
	@Column(name="drawerStockQty", columnDefinition="專櫃抽屜")
	public int getDrawerStockQty() {
		return drawerStockQty;
	}
	public void setDrawerStockQty(int drawerStockQty) {
		this.drawerStockQty = drawerStockQty;
	}
	@Column(name="showcaseStockQty", columnDefinition="展示櫃")
	public int getShowcaseStockQty() {
		return showcaseStockQty;
	}
	public void setShowcaseStockQty(int showcaseStockQty) {
		this.showcaseStockQty = showcaseStockQty;
	}
	@Column(name="notShipStockQty", columnDefinition="未出貨")
	public int getNotShipStockQty() {
		return notShipStockQty;
	}
	public void setNotShipStockQty(int notShipStockQty) {
		this.notShipStockQty = notShipStockQty;
	}
	@Column(name="drawerInZhongheStockQty", columnDefinition="中和庫存")
	public int getDrawerInZhongheStockQty() {
		return drawerInZhongheStockQty;
	}
	public void setDrawerInZhongheStockQty(int drawerInZhongheStockQty) {
		this.drawerInZhongheStockQty = drawerInZhongheStockQty;
	}
	@Column(name="showcaseInZhongheStockQty", columnDefinition="中和展示")
	public int getShowcaseInZhongheStockQty() {
		return showcaseInZhongheStockQty;
	}
	public void setShowcaseInZhongheStockQty(int showcaseInZhongheStockQty) {
		this.showcaseInZhongheStockQty = showcaseInZhongheStockQty;
	}
}
