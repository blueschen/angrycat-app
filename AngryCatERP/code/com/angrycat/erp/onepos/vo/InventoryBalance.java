package com.angrycat.erp.onepos.vo;

import com.healthmarketscience.jackcess.Row;

/**
 * 庫存
 * @author JerryLin
 */
public class InventoryBalance {
	private String invSiteId;		// 銷售點位置
	private String invStorageId;	// 庫存位置
	private String invProductId;	// 庫存產品
	private int invQty;				// 庫存量
	private String invDate;			// 庫存變更日期
	private String invTime;			// 庫存變更時間
	public String getInvSiteId() {
		return invSiteId;
	}
	public void setInvSiteId(String invSiteId) {
		this.invSiteId = invSiteId;
	}
	public String getInvStorageId() {
		return invStorageId;
	}
	public void setInvStorageId(String invStorageId) {
		this.invStorageId = invStorageId;
	}
	public String getInvProductId() {
		return invProductId;
	}
	public void setInvProductId(String invProductId) {
		this.invProductId = invProductId;
	}
	public int getInvQty() {
		return invQty;
	}
	public void setInvQty(int invQty) {
		this.invQty = invQty;
	}
	public String getInvDate() {
		return invDate;
	}
	public void setInvDate(String invDate) {
		this.invDate = invDate;
	}
	public String getInvTime() {
		return invTime;
	}
	public void setInvTime(String invTime) {
		this.invTime = invTime;
	}
	public static InventoryBalance toVo(Row row){
		InventoryBalance inv = new InventoryBalance();
		inv.setInvSiteId((String)row.get("invSiteID"));
		inv.setInvStorageId((String)row.get("invStorageID"));
		inv.setInvProductId((String)row.get("invProductID"));
		inv.setInvQty((int)row.get("invQty"));
		inv.setInvDate((String)row.get("invDate"));
		inv.setInvTime((String)row.get("invTime"));
		return inv;
	}
}
