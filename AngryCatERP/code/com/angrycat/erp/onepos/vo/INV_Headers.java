package com.angrycat.erp.onepos.vo;

import java.math.BigDecimal;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

/**
 * OnePos銷售單
 * @author JerryLin
 *
 */
public class INV_Headers {
	private String docId;		// 銷售單號
	private String status;
	private String docType;
	private String siteId;		// 銷售點位置
	private String storageId;	// 扣庫存位置
	private String tillId;		
	private String salesId;		// 營業員(可變更)
	private String billId;		// 買單客戶(指定客戶或預設--0000)
	private String billName;	// 買單客戶姓名
	private String billAddress; // 買單客戶地址
	private String shipAddress;
	private String reference;
	private String payDue;
	private String payTerms;
	private String posRemark;
	private String remark;
	private String invCurrency;	// 使用幣別
	private int count;
	private float net;
	private String discountType;
	private float discountRate;
	private float discount;
	private float priceTotal;		// 應付總額
	private float tax1;
	private float tax2;
	private float grandTotal;
	private double memberPoints;	// 會員點數
	private String dateCFM;
	private String dateNEW;
	private String cfmBy;		// 確認者，跟營業員一致
	private String newBy;		// 開單者(以登錄帳號為準，無法變更)
	private String officeUse;
	private String logDate;		// 登錄日期
	private String logTime;		// 登錄時間
	private double dendJobId;
	private double paid;		// 實付金額
	private double outstanding;
	private boolean taxInPrice;
	private boolean tax1InUse;
	private String tax1Name;
	private BigDecimal tax1Rate;
	private boolean tax2InUse;
	private String tax2Name;
	private BigDecimal tax2Rate;
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	public String getTillId() {
		return tillId;
	}
	public void setTillId(String tillId) {
		this.tillId = tillId;
	}
	public String getSalesId() {
		return salesId;
	}
	public void setSalesId(String salesId) {
		this.salesId = salesId;
	}
	public String getBillId() {
		return billId;
	}
	public void setBillId(String billId) {
		this.billId = billId;
	}
	public String getBillName() {
		return billName;
	}
	public void setBillName(String billName) {
		this.billName = billName;
	}
	public String getBillAddress() {
		return billAddress;
	}
	public void setBillAddress(String billAddress) {
		this.billAddress = billAddress;
	}
	public String getShipAddress() {
		return shipAddress;
	}
	public void setShipAddress(String shipAddress) {
		this.shipAddress = shipAddress;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getPayDue() {
		return payDue;
	}
	public void setPayDue(String payDue) {
		this.payDue = payDue;
	}
	public String getPayTerms() {
		return payTerms;
	}
	public void setPayTerms(String payTerms) {
		this.payTerms = payTerms;
	}
	public String getPosRemark() {
		return posRemark;
	}
	public void setPosRemark(String posRemark) {
		this.posRemark = posRemark;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getInvCurrency() {
		return invCurrency;
	}
	public void setInvCurrency(String invCurrency) {
		this.invCurrency = invCurrency;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public float getNet() {
		return net;
	}
	public void setNet(float net) {
		this.net = net;
	}
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	public float getDiscountRate() {
		return discountRate;
	}
	public void setDiscountRate(float discountRate) {
		this.discountRate = discountRate;
	}
	public float getDiscount() {
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	public float getPriceTotal() {
		return priceTotal;
	}
	public void setPriceTotal(float priceTotal) {
		this.priceTotal = priceTotal;
	}
	public float getTax1() {
		return tax1;
	}
	public void setTax1(float tax1) {
		this.tax1 = tax1;
	}
	public float getTax2() {
		return tax2;
	}
	public void setTax2(float tax2) {
		this.tax2 = tax2;
	}
	public float getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(float grandTotal) {
		this.grandTotal = grandTotal;
	}
	public double getMemberPoints() {
		return memberPoints;
	}
	public void setMemberPoints(double memberPoints) {
		this.memberPoints = memberPoints;
	}
	public String getDateCFM() {
		return dateCFM;
	}
	public void setDateCFM(String dateCFM) {
		this.dateCFM = dateCFM;
	}
	public String getDateNEW() {
		return dateNEW;
	}
	public void setDateNEW(String dateNEW) {
		this.dateNEW = dateNEW;
	}
	public String getCfmBy() {
		return cfmBy;
	}
	public void setCfmBy(String cfmBy) {
		this.cfmBy = cfmBy;
	}
	public String getNewBy() {
		return newBy;
	}
	public void setNewBy(String newBy) {
		this.newBy = newBy;
	}
	public String getOfficeUse() {
		return officeUse;
	}
	public void setOfficeUse(String officeUse) {
		this.officeUse = officeUse;
	}
	public String getLogDate() {
		return logDate;
	}
	public void setLogDate(String logDate) {
		this.logDate = logDate;
	}
	public String getLogTime() {
		return logTime;
	}
	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}
	public double getDendJobId() {
		return dendJobId;
	}
	public void setDendJobId(double dendJobId) {
		this.dendJobId = dendJobId;
	}
	public double getPaid() {
		return paid;
	}
	public void setPaid(double paid) {
		this.paid = paid;
	}
	public double getOutstanding() {
		return outstanding;
	}
	public void setOutstanding(double outstanding) {
		this.outstanding = outstanding;
	}
	public boolean isTaxInPrice() {
		return taxInPrice;
	}
	public void setTaxInPrice(boolean taxInPrice) {
		this.taxInPrice = taxInPrice;
	}
	public boolean isTax1InUse() {
		return tax1InUse;
	}
	public void setTax1InUse(boolean tax1InUse) {
		this.tax1InUse = tax1InUse;
	}
	public String getTax1Name() {
		return tax1Name;
	}
	public void setTax1Name(String tax1Name) {
		this.tax1Name = tax1Name;
	}
	public BigDecimal getTax1Rate() {
		return tax1Rate;
	}
	public void setTax1Rate(BigDecimal tax1Rate) {
		this.tax1Rate = tax1Rate;
	}
	public boolean isTax2InUse() {
		return tax2InUse;
	}
	public void setTax2InUse(boolean tax2InUse) {
		this.tax2InUse = tax2InUse;
	}
	public String getTax2Name() {
		return tax2Name;
	}
	public void setTax2Name(String tax2Name) {
		this.tax2Name = tax2Name;
	}
	public BigDecimal getTax2Rate() {
		return tax2Rate;
	}
	public void setTax2Rate(BigDecimal tax2Rate) {
		this.tax2Rate = tax2Rate;
	}
	public static INV_Headers toVo(Row row){
		INV_Headers salesOrder = new INV_Headers();

		salesOrder.setDocId((String)row.get("DocID"));
		salesOrder.setStatus((String)row.get("Status"));
		salesOrder.setDocType((String)row.get("DocType"));
		salesOrder.setSiteId((String)row.get("SiteID"));
		salesOrder.setStorageId((String)row.get("StorageID"));
		salesOrder.setTillId((String)row.get("TillID"));
		salesOrder.setSalesId((String)row.get("SalesID"));
		salesOrder.setBillId((String)row.get("BillID"));
		salesOrder.setBillName((String)row.get("BillName"));
		salesOrder.setBillAddress((String)row.get("BillAddress"));
		salesOrder.setShipAddress((String)row.get("ShipAddress"));
		salesOrder.setReference((String)row.get("Reference"));
		salesOrder.setPayDue((String)row.get("PayDue"));
		salesOrder.setPayTerms((String)row.get("PayTerms"));
		salesOrder.setPosRemark((String)row.get("POSRemark"));
		salesOrder.setRemark((String)row.get("Remark"));
		salesOrder.setInvCurrency((String)row.get("InvCurrency"));
		salesOrder.setCount((int)row.get("Count"));
		salesOrder.setNet((float)row.get("Net"));
		salesOrder.setDiscountType((String)row.get("DiscountType"));
		salesOrder.setDiscountRate((float)row.get("DiscountRate"));
		salesOrder.setDiscount((float)row.get("Discount"));
		salesOrder.setPriceTotal((float)row.get("PriceTotal"));
		salesOrder.setTax1((float)row.get("Tax1"));
		salesOrder.setTax2((float)row.get("Tax2"));
		salesOrder.setGrandTotal((float)row.get("GrandTotal"));
		salesOrder.setMemberPoints((double)row.get("MemberPoints"));
		salesOrder.setDateCFM((String)row.get("DateCFM"));
		salesOrder.setDateNEW((String)row.get("DateNEW"));
		salesOrder.setCfmBy((String)row.get("CFMBy"));
		salesOrder.setNewBy((String)row.get("NEWBy"));
		salesOrder.setOfficeUse((String)row.get("OfficeUse"));
		salesOrder.setLogDate((String)row.get("logdate"));
		salesOrder.setLogTime((String)row.get("logtime"));
		salesOrder.setDendJobId((double)row.get("DENDJobID"));
		salesOrder.setPaid((double)row.get("Paid"));
		salesOrder.setOutstanding((double)row.get("Outstanding"));
		salesOrder.setTaxInPrice((boolean)row.get("TaxInPrice"));
		salesOrder.setTax1InUse((boolean)row.get("Tax1InUse"));
		salesOrder.setTax1Name((String)row.get("Tax1Name"));
		salesOrder.setTax1Rate((BigDecimal)row.get("Tax1Rate"));
		salesOrder.setTax2InUse((boolean)row.get("Tax2InUse"));
		salesOrder.setTax2Name((String)row.get("Tax2Name"));
		salesOrder.setTax2Rate((BigDecimal)row.get("Tax2Rate"));
		
		return salesOrder;
	}
	
	public static void keyPrintf(Table table){
		try{
			String sGap = "%-15s";
			String dGap = "%-15d";
			String fGap = "%-15.2f";
			System.out.println(String.format(sGap+sGap+sGap+sGap+sGap+sGap+sGap+sGap, "DocID", "NEWBy", "PayDue", "Paid", "logdate", "logtime", "BillID", "BillName"));
			table.forEach(row->{
				INV_Headers h = toVo(row);
				System.out.println(String.format(sGap+sGap+sGap+fGap+sGap+sGap+sGap+sGap, 
									h.getDocId(),
									h.getNewBy(),
									h.getPayDue(),
									h.getPaid(),
									h.getLogDate(),
									h.getLogTime(),
									h.getBillId(),
									h.getBillName()));
				
			});
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
}
