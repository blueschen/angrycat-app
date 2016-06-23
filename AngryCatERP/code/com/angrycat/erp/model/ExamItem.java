package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="examitem")
public class ExamItem {
	private String id;
	private int sequence;
	private String description;
	private boolean correct;
	private String examId;
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_exam_item_id",strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_exam_item_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="sequence", columnDefinition="順序")
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	@Column(name="description", columnDefinition="描述")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="correct", columnDefinition="正確答案")
	public boolean isCorrect() {
		return correct;
	}
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}
	@Column(name="examId", columnDefinition="題目ID")
	public String getExamId(){
		return examId;
	}
	public void setExamId(String examId){
		this.examId = examId;
	}
}
