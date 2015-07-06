package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DataChangeLogDetail {
	private String fieldName;
	private String originalContent;
	private String changedContent;
	public DataChangeLogDetail(){}
	public DataChangeLogDetail(String fieldName, String originalContent, String changedContent){
		this.fieldName = fieldName;
		this.originalContent = originalContent;
		this.changedContent = changedContent;
	}
	@Column(name="fieldName")
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	@Column(name="originalContent")
	public String getOriginalContent() {
		return originalContent;
	}
	public void setOriginalContent(String originalContent) {
		this.originalContent = originalContent;
	}
	@Column(name="changedContent")
	public String getChangedContent() {
		return changedContent;
	}
	public void setChangedContent(String changedContent) {
		this.changedContent = changedContent;
	}
}
