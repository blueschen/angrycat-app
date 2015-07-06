package com.angrycat.erp.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="shr_datachangelog")
public class DataChangeLog {
	
	@Transient
	public static final String ACTION_ADD = "add"; 
	@Transient
	public static final String ACTION_UPDATE = "update"; 
	@Transient
	public static final String ACTION_DELETE = "delete"; 
	@Transient
	public static final String ACTION_QUERY = "query"; 
		
	private String id;
	private String docId;
	private String docType;
	private Timestamp logTime;
	private String userId;
	private String userName;
	private List<DataChangeLogDetail> details = new ArrayList<>();
	private String note;
	@Id
	@Column(name="id")
	@GenericGenerator(name="datachangelog_id", strategy = "com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="datachangelog_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="docId")
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	@Column(name="docType")
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	@Column(name="logTime")
	public Timestamp getLogTime() {
		return logTime;
	}
	public void setLogTime(Timestamp logTime) {
		this.logTime = logTime;
	}
	@Column(name="userId")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Column(name="userName")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@ElementCollection
	@CollectionTable(
		name="shr_datachangelogdetail",
		joinColumns=@JoinColumn(name="dataChangeLogId"))
	@OrderColumn(name="idx")
	public List<DataChangeLogDetail> getDetails() {
		return details;
	}
	public void setDetails(List<DataChangeLogDetail> details) {
		this.details = details;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
