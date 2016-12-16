package com.angrycat.erp.model;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
@Entity
@Table(name="purchasebill")
public class PurchaseBill {
	private String id;
	private String no; // unique key
	private Date arriveDate;
	private Date stockDate;
	private String note;
	private List<PurchaseBillDetail> purchaseBillDetails = new LinkedList<>();
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_purchasebill_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_purchasebill_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="no")
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	@Column(name="arriveDate")
	public Date getArriveDate() {
		return arriveDate;
	}
	public void setArriveDate(Date arriveDate) {
		this.arriveDate = arriveDate;
	}
	@Column(name="stockDate")
	public Date getStockDate() {
		return stockDate;
	}
	public void setStockDate(Date stockDate) {
		this.stockDate = stockDate;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@OneToMany(fetch=FetchType.LAZY, targetEntity=PurchaseBillDetail.class, cascade=CascadeType.ALL, mappedBy="purchaseBillId", orphanRemoval=true)
	@OrderBy("id DESC")
	public List<PurchaseBillDetail> getPurchaseBillDetails() {
		return purchaseBillDetails;
	}
	public void setPurchaseBillDetails(List<PurchaseBillDetail> purchaseBillDetails) {
		this.purchaseBillDetails = purchaseBillDetails;
	}
}
