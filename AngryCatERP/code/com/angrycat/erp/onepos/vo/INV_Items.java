package com.angrycat.erp.onepos.vo;

import java.math.BigDecimal;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
/**
 * OnePos銷售項目
 * @author JerryLin
 *
 */
public class INV_Items {
	private int rowId;		// 自動增加ID
	private String docId;		// 銷售單號
	private String productId;	// 產品編號
	private String description;	// 產品名稱
	private float qty;			// 銷售數量
	private float returnedQty;	// 
	private float price;			// 單項售價
	private float discount;		// 折扣
	private float subTotal;		// 小計(某項目的結算額)
	private String itemType;	// 產品性質(I:庫存/N:非庫存)
	private double unitCost;		// 單位成本
	private String logDate;		// 登錄日期
	private String logTime;		// 登錄時間
	private boolean tax1InUse;
	private String tax1Name;
	private BigDecimal tax1Rate;
	private int tax1Amount;
	private boolean tax2InUse;
	private String tax2Name;
	private BigDecimal tax2Rate;
	private int tax2Amount;
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public float getQty() {
		return qty;
	}
	public void setQty(float qty) {
		this.qty = qty;
	}
	public float getReturnedQty() {
		return returnedQty;
	}
	public void setReturnedQty(float returnedQty) {
		this.returnedQty = returnedQty;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public float getDiscount() {
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	public float getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(float subTotal) {
		this.subTotal = subTotal;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public double getUnitCost() {
		return unitCost;
	}
	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
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
	public int getTax1Amount() {
		return tax1Amount;
	}
	public void setTax1Amount(int tax1Amount) {
		this.tax1Amount = tax1Amount;
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
	public int getTax2Amount() {
		return tax2Amount;
	}
	public void setTax2Amount(int tax2Amount) {
		this.tax2Amount = tax2Amount;
	}
	public static INV_Items toVo(Row row){
		INV_Items item = new INV_Items();
		
		item.setRowId((int)row.get("rowid"));
		item.setDocId((String)row.get("DocID"));
		item.setProductId((String)row.get("ProductID"));
		item.setDescription((String)row.get("Description"));
		item.setQty((float)row.get("Qty"));
		item.setReturnedQty((float)row.get("ReturnedQty"));
		item.setPrice((float)row.get("Price"));
		item.setDiscount((float)row.get("Discount"));
		item.setSubTotal((float)row.get("SubTotal"));
		item.setItemType((String)row.get("ItemType"));
		item.setUnitCost((double)row.get("UnitCost"));
		item.setLogDate((String)row.get("logdate"));
		item.setLogTime((String)row.get("logtime"));
		item.setTax1InUse((boolean)row.get("Tax1InUse"));
		item.setTax1Name((String)row.get("Tax1Name"));
		item.setTax1Rate((BigDecimal)row.get("Tax1Rate"));
		item.setTax2InUse((boolean)row.get("Tax2InUse"));
		item.setTax2Name((String)row.get("Tax2Name"));
		item.setTax2Rate((BigDecimal)row.get("Tax2Rate"));
		
		return item;
	}
	
	public static void keyPrintf(Table table){
		try{
			String sGap = "%-18s";
			String dGap = "%-15d";
			String fGap = "%-18.2f";
			System.out.println(String.format(sGap+sGap+sGap+sGap+sGap+sGap+sGap+sGap, "DocID", "ProductID", "Qty", "Price", "Discount", "SubTotal", "logdate", "logtime"));
			table.forEach(row->{
				INV_Items h = toVo(row);
				System.out.println(String.format(sGap+sGap+fGap+fGap+fGap+fGap+sGap+sGap, 
									h.getDocId(),
									h.getProductId(),
									h.getQty(),
									h.getPrice(),
									h.getDiscount(),
									h.getSubTotal(),
									h.getLogDate(),
									h.getLogTime()));
				
			});
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
}
