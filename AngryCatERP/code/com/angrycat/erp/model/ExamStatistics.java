package com.angrycat.erp.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.angrycat.erp.security.User;

@Entity
@Table(name="examstatistics")
public class ExamStatistics {
	private String id;
	private Date examDate;
	private int maxScore;
	private int avgScore;
	private int examCount;
	private User examinee;
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_exam_statistics_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_exam_statistics_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="examDate")
	public Date getExamDate() {
		return examDate;
	}
	public void setExamDate(Date examDate) {
		this.examDate = examDate;
	}
	@Column(name="maxScore")
	public int getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}
	@Column(name="avgScore")
	public int getAvgScore() {
		return avgScore;
	}
	public void setAvgScore(int avgScore) {
		this.avgScore = avgScore;
	}
	@Column(name="examCount")
	public int getExamCount() {
		return examCount;
	}
	public void setExamCount(int examCount) {
		this.examCount = examCount;
	}
	@ManyToOne(targetEntity=User.class)
	@JoinColumn(name="examinee")
	public User getExaminee() {
		return examinee;
	}
	public void setExaminee(User examinee) {
		this.examinee = examinee;
	}
}
