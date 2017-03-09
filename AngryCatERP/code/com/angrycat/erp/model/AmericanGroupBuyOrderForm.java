package com.angrycat.erp.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="americangroupbuyorderform")
public class AmericanGroupBuyOrderForm {
	@Transient
	public static final String SALESNO_GENERATOR_ID = "SALESNO_GENERATOR";
	
	private String id;
	// 某期美國團活動資訊
	private String activity;
	// 聯絡方式
	private String fbNickname;
//	private String name;
	private String email;
	private String mobile;
//	private String tel;
	
	//***匯款資訊****//
	// 寄送方式/地址
//	private String postalCode;
//	private String address;
	// 匯款資訊
//	private Date transferDate;
//	private String transferCheck;
//	private int transferAmount;	// TODO 匯款金額是否有可能不同於代購總金額
	
	// 個別產品資訊
	private String salesType; // 正取:qualify/備取wait/贈品gift
	private String modelId;
	private String productName;
	private BigDecimal productAmtUSD;
	private String size;
	// 整批訂單資訊
	private String salesNo; // 多個明細共用
	private int totalAmtNTD; // 代購總金額
	// 對帳資訊	
	private boolean billChecked;
	private String billCheckNote;
	
//	private String note;
	
	private Date createTime = Date.valueOf(LocalDate.now());
	
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_americangroupbuyorderform_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_americangroupbuyorderform_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="totalAmtNTD", columnDefinition="代購總金額(台幣)")
	public int getTotalAmtNTD() {
		return totalAmtNTD;
	}
	public void setTotalAmtNTD(int totalAmtNTD) {
		this.totalAmtNTD = totalAmtNTD;
	}
	@Column(name="modelId", columnDefinition="編號")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	@Column(name="productName", columnDefinition="英文名字")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@Column(name="productAmtUSD", columnDefinition="美金定價")
	public BigDecimal getProductAmtUSD() {
		return productAmtUSD;
	}
	public void setProductAmtUSD(BigDecimal productAmtUSD) {
		this.productAmtUSD = productAmtUSD;
	}
	@Column(name="billChecked", columnDefinition="是否已對帳")
	public boolean isBillChecked() {
		return billChecked;
	}
	public void setBillChecked(boolean billChecked) {
		this.billChecked = billChecked;
	}
	@Column(name="billCheckNote", columnDefinition="對帳備註")
	public String getBillCheckNote() {
		return billCheckNote;
	}
	public void setBillCheckNote(String billCheckNote) {
		this.billCheckNote = billCheckNote;
	}
	@Column(name="activity", columnDefinition="活動名稱")
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	@Column(name="salesNo", columnDefinition="單號")
	public String getSalesNo() {
		return salesNo;
	}
	public void setSalesNo(String salesNo) {
		this.salesNo = salesNo;
	}
	@Column(name="salesType", columnDefinition="銷售類型")
	public String getSalesType() {
		return salesType;
	}
	public void setSalesType(String salesType) {
		this.salesType = salesType;
	}
	@Column(name="fbNickname", columnDefinition="FB顯示名稱")
	public String getFbNickname() {
		return fbNickname;
	}
	public void setFbNickname(String fbNickname) {
		this.fbNickname = fbNickname;
	}
//	@Column(name="name", columnDefinition="真實姓名")
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
	@Column(name="email", columnDefinition="Email")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name="mobile", columnDefinition="手機")
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
//	@Column(name="tel", columnDefinition="其他聯絡電話")
//	public String getTel() {
//		return tel;
//	}
//	public void setTel(String tel) {
//		this.tel = tel;
//	}
//	@Column(name="postalCode", columnDefinition="郵遞區號")
//	public String getPostalCode() {
//		return postalCode;
//	}
//	public void setPostalCode(String postalCode) {
//		this.postalCode = postalCode;
//	}
//	@Column(name="address", columnDefinition="地址")
//	public String getAddress() {
//		return address;
//	}
//	public void setAddress(String address) {
//		this.address = address;
//	}
//	@Column(name="transferDate", columnDefinition="匯款日期")
//	public Date getTransferDate() {
//		return transferDate;
//	}
//	public void setTransferDate(Date transferDate) {
//		this.transferDate = transferDate;
//	}
//	@Column(name="transferCheck", columnDefinition="匯款帳號後五碼")
//	public String getTransferCheck() {
//		return transferCheck;
//	}
//	public void setTransferCheck(String transferCheck) {
//		this.transferCheck = transferCheck;
//	}
//	@Column(name="transferAmount", columnDefinition="匯款金額")
//	public int getTransferAmount() {
//		return transferAmount;
//	}
//	public void setTransferAmount(int transferAmount) {
//		this.transferAmount = transferAmount;
//	}
//	@Column(name="note", columnDefinition="其他備註")
//	public String getNote() {
//		return note;
//	}
//	public void setNote(String note) {
//		this.note = note;
//	}
	@Column(name="createTime", columnDefinition="填單時間")
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Column(name="size", columnDefinition="尺寸")
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
}
