package com.angrycat.erp.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
@Entity
@Table(name="salesdetail")
public class SalesDetail {
	@Transient
	public static final String SALE_POINT_FB = "FB社團";
	@Transient
	public static final String SALE_POINT_ESLITE_DUNNAN = "敦南誠品";
	@Transient
	public static final String SALE_POINT_TAOBAO = "淘寶";
	
	private String id;
	private Member member;
	
	private String salePoint;
	private String saleStatus;
	private String fbName;
	private String activity;
	private String modelId;
	private String productName;
	private double price;
	private double memberPrice;
	private String priority;
	private Date orderDate;
	private String otherNote;
	private String checkBillStatus;
	private String mobile;
	private String idNo;
	private String discountType;
	private String arrivalStatus;
	private Date shippingDate;
	private String sendMethod;
	private String note;
	
	private Date payDate;
	private String contactInfo;
	private String registrant;
	
	private String rowId;
	private String payType;
	private String payStatus;
	
	private String orderNo;
	
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_salesdetail_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_salesdetail_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(targetEntity=Member.class)
	@JoinColumn(name="memberId", columnDefinition="會員")
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
	@Column(name="salePoint", columnDefinition="銷售點")
	public String getSalePoint() {
		return salePoint;
	}
	public void setSalePoint(String salePoint) {
		this.salePoint = salePoint;
	}
	@Column(name="saleStatus", columnDefinition="狀態")
	public String getSaleStatus() {
		return saleStatus;
	}
	public void setSaleStatus(String saleStatus) {
		this.saleStatus = saleStatus;
	}
	@Column(name="fbName", columnDefinition="FB名稱/客人姓名")
	public String getFbName() {
		return fbName;
	}
	public void setFbName(String fbName) {
		this.fbName = fbName;
	}
	@Column(name="activity", columnDefinition="活動")
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	@Column(name="modelId", columnDefinition="型號")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	@Column(name="productName", columnDefinition="明細")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@Column(name="price", columnDefinition="定價")
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Column(name="memberPrice", columnDefinition="會員價(實收價格)")
	public double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(double memberPrice) {
		this.memberPrice = memberPrice;
	}
	@Column(name="priority", columnDefinition="順序")
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	@Column(name="orderDate", columnDefinition="銷售日期")
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	@Column(name="otherNote", columnDefinition="其他備註")
	public String getOtherNote() {
		return otherNote;
	}
	public void setOtherNote(String otherNote) {
		this.otherNote = otherNote;
	}
	@Column(name="checkBillStatus", columnDefinition="對帳狀態")
	public String getCheckBillStatus() {
		return checkBillStatus;
	}
	public void setCheckBillStatus(String checkBillStatus) {
		this.checkBillStatus = checkBillStatus;
	}
	@Column(name="mobile", columnDefinition="手機")
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	@Column(name="idNo", columnDefinition="身份證字號")
	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	@Column(name="discountType", columnDefinition="折扣説明")
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	@Column(name="arrivalStatus", columnDefinition="已到貨")
	public String getArrivalStatus() {
		return arrivalStatus;
	}
	public void setArrivalStatus(String arrivalStatus) {
		this.arrivalStatus = arrivalStatus;
	}
	@Column(name="shippingDate", columnDefinition="出貨日")
	public Date getShippingDate() {
		return shippingDate;
	}
	public void setShippingDate(Date shippingDate) {
		this.shippingDate = shippingDate;
	}
	@Column(name="sendMethod", columnDefinition="郵寄方式")
	public String getSendMethod() {
		return sendMethod;
	}
	public void setSendMethod(String sendMethod) {
		this.sendMethod = sendMethod;
	}
	@Column(name="note", columnDefinition="備註")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Column(name="payDate", columnDefinition="付款日期")
	public Date getPayDate() {
		return payDate;
	}
	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}
	@Column(name="contactInfo", columnDefinition="郵寄地址電話")
	public String getContactInfo() {
		return contactInfo;
	}
	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}
	@Column(name="registrant", columnDefinition="登單者")
	public String getRegistrant() {
		return registrant;
	}
	public void setRegistrant(String registrant) {
		this.registrant = registrant;
	}
	@Column(name="rowId", columnDefinition="Excel序號")
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	@Column(name="payType", columnDefinition="付款別")
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name="payStatus", columnDefinition="付款狀態")
	public String getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}
	@Column(name="orderNo", columnDefinition="訂單編號")
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
}
