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
@Table(name="americangroupbuy")
public class AmericanGroupBuy {
	private String id;
	private String activity;
	private String no;
	private Date deadline;
	
	private BigDecimal qualifyTotalAmtThreshold;
	private BigDecimal giftValAmtUSD;
	private BigDecimal multiplier;
	private BigDecimal rate;
	private BigDecimal serviceChargeNTD;
	
	private BigDecimal waitTotalAmtThreshold;
	
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_americangroupby_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_americangroupby_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="activity", columnDefinition="活動名稱")
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	@Column(name="no", columnDefinition="編號")
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	@Column(name="deadline", columnDefinition="截止時間")
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	@Column(name="giftValAmtUSD", columnDefinition="贈品價值金額(美金)")
	public BigDecimal getGiftValAmtUSD() {
		return giftValAmtUSD;
	}
	public void setGiftValAmtUSD(BigDecimal giftValAmtUSD) {
		this.giftValAmtUSD = giftValAmtUSD;
	}
	@Column(name="multiplier", columnDefinition="乘數")
	public BigDecimal getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(BigDecimal multiplier) {
		this.multiplier = multiplier;
	}
	@Column(name="rate", columnDefinition="美金對台幣匯率")
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	@Column(name="serviceChargeNTD", columnDefinition="代購服務費(台幣)")
	public BigDecimal getServiceChargeNTD() {
		return serviceChargeNTD;
	}
	public void setServiceChargeNTD(BigDecimal serviceChargeNTD) {
		this.serviceChargeNTD = serviceChargeNTD;
	}
	@Column(name="qualifyTotalAmtThreshold", columnDefinition="正取總額門檻(美金)")
	public BigDecimal getQualifyTotalAmtThreshold() {
		return qualifyTotalAmtThreshold;
	}
	public void setQualifyTotalAmtThreshold(BigDecimal qualifyTotalAmtThreshold) {
		this.qualifyTotalAmtThreshold = qualifyTotalAmtThreshold;
	}
	@Column(name="waitTotalAmtThreshold", columnDefinition="備取總額門檻(美金)")
	public BigDecimal getWaitTotalAmtThreshold() {
		return waitTotalAmtThreshold;
	}
	public void setWaitTotalAmtThreshold(BigDecimal waitTotalAmtThreshold) {
		this.waitTotalAmtThreshold = waitTotalAmtThreshold;
	}
	/**
	 * 訂單是否被關閉<br>
	 * 以deadline的值判斷，若deadline為null或者大於今日則關閉訂單功能
	 * @return
	 */
	@Transient
	public boolean isOrderFormDisabled(){
		return deadline == null || deadline.compareTo(Date.valueOf(LocalDate.now())) < 0;
	}
}
