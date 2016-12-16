package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="purchasebilldetail")
public class PurchaseBillDetail {
	private String id;
	private String purchaseBillId;
	private String modelId;
	private String name;
	private String nameEng;
	private int count;
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_purchasebilldetail_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_purchasebilldetail_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="purchaseBillId")
	public String getPurchaseBillId() {
		return purchaseBillId;
	}
	public void setPurchaseBillId(String purchaseBillId) {
		this.purchaseBillId = purchaseBillId;
	}
	@Column(name="modelId")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
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
	@Column(name="count")
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
