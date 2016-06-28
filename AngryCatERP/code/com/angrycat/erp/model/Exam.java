package com.angrycat.erp.model;

import java.sql.Date;
import java.util.ArrayList;
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
@Table(name="exam")
public class Exam {
	private String id;
	private String description;
	private String category;
	private Date createDate;
	private String hint;
	private List<ExamItem> items = new ArrayList<>();
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_exam_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_exam_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="description", columnDefinition="題目")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="category", columnDefinition="類別")
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Column(name="createDate", columnDefinition="新增題庫日")
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@Column(name="hint", columnDefinition="提示")
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	@OneToMany(fetch=FetchType.LAZY, targetEntity=ExamItem.class, cascade=CascadeType.ALL, mappedBy="examId", orphanRemoval=true)
	@OrderBy("sequence")
	public List<ExamItem> getItems() {
		return items;
	}
	public void setItems(List<ExamItem> items) {
		this.items = items;
	}
}
