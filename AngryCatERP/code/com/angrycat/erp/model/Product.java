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
	private int taobaoStockQty;
	
	private String totalStockChangeNote;
	private String warning;
	
	private Double priceAsRMB;
	private String mainCategory;
	
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
		if(modelId == null){
			return null;
		}
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
	@Column(name="taobaoStockQty", columnDefinition="淘寶庫存")
	public int getTaobaoStockQty() {
		return taobaoStockQty;
	}
	public void setTaobaoStockQty(int taobaoStockQty) {
		this.taobaoStockQty = taobaoStockQty;
	}
	@Column(name="totalStockChangeNote", columnDefinition="總庫存修改備註")
	public String getTotalStockChangeNote() {
		return totalStockChangeNote;
	}
	public void setTotalStockChangeNote(String totalStockChangeNote) {
		this.totalStockChangeNote = totalStockChangeNote;
	}
	public String getWarning() {
		return warning;
	}
	public void setWarning(String warning) {
		this.warning = warning;
	}
	@Column(name="priceAsRMB", columnDefinition="人民幣售價")
	public Double getPriceAsRMB() {
		return priceAsRMB;
	}
	public void setPriceAsRMB(Double priceAsRMB) {
		this.priceAsRMB = priceAsRMB;
	}
	@Column(name="mainCategory", columnDefinition="主分類")
	public String getMainCategory() {
		return mainCategory;
	}
	public void setMainCategory(String mainCategory) {
		this.mainCategory = mainCategory;
	}
}
