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
	
	@Id
	@Column(name="id")
	@GenericGenerator(name="angrycat_product_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_product_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="productCategory")
	public ProductCategory getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
	}
	@Column(name="modelId")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	@Column(name="suggestedRetailPrice")
	public double getSuggestedRetailPrice() {
		return suggestedRetailPrice;
	}
	public void setSuggestedRetailPrice(double suggestedRetailPrice) {
		this.suggestedRetailPrice = suggestedRetailPrice;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="nameEng")
	public String getNameEng() {
		return nameEng;
	}
	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}
	@Column(name="seriesName")
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	@Column(name="barcode")
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
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
}
