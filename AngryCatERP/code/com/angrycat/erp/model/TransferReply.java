package com.angrycat.erp.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
@Entity
@Table(name="transferreply")
public class TransferReply {
	private String id;
	private String brand;
	private String fbNickname;
	private String name;
	private String mobile;
	private String tel;
	private String postalCode;
	private String address;
	private Date transferDate;
	private String transferAccountCheck;
	private int transferAmount;
	private String productDetails;
	private String note;
//	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private Date createDate = new Date(System.currentTimeMillis());
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_transferreply_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_transferreply_id")
	public String getId() {
		return id;
	}
	public void setId(String id){
		this.id = id;
	}
	@Column(name="brand", columnDefinition="品牌")
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	@Column(name="fbNickname", columnDefinition="FB顯示名稱")
	public String getFbNickname() {
		return fbNickname;
	}
	public void setFbNickname(String fbNickname) {
		this.fbNickname = fbNickname;
	}
	@Column(name="name", columnDefinition="真實姓名")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="mobile", columnDefinition="手機號碼")
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	@Column(name="tel", columnDefinition="備用聯絡電話")
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	@Column(name="postalCode", columnDefinition="郵遞區號")
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	@Column(name="address", columnDefinition="掛號收件地址")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Column(name="transferDate", columnDefinition="匯款日期")
	public Date getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(Date transferDate) {
		this.transferDate = transferDate;
	}
	@Column(name="transferAccountCheck", columnDefinition="匯款帳號後5碼")
	public String getTransferAccountCheck() {
		return transferAccountCheck;
	}
	public void setTransferAccountCheck(String transferAccountCheck) {
		this.transferAccountCheck = transferAccountCheck;
	}
	@Column(name="transferAmount", columnDefinition="匯款金額")
	public int getTransferAmount() {
		return transferAmount;
	}
	public void setTransferAmount(int transferAmount) {
		this.transferAmount = transferAmount;
	}
	@Column(name="productDetails", columnDefinition="購買明細")
	public String getProductDetails() {
		return productDetails;
	}
	public void setProductDetails(String productDetails) {
		this.productDetails = productDetails;
	}
	@Column(name="note", columnDefinition="其他備註")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Column(name="createDate", columnDefinition="填單時間")
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}