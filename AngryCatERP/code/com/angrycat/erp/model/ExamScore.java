package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="examscore")
public class ExamScore {
	private String id;
	private int score;
	private ExamStatistics statistics;
	@Id
	@Column(name="id", columnDefinition="ID")
	@GenericGenerator(name="angrycat_exam_score_id", strategy="com.angrycat.erp.ds.TimeUID")
	@GeneratedValue(generator="angrycat_exam_score_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="score")
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	@ManyToOne(targetEntity=ExamStatistics.class)
	@JoinColumn(name="statistics")
	public ExamStatistics getStatistics() {
		return statistics;
	}
	public void setStatistics(ExamStatistics statistics) {
		this.statistics = statistics;
	}
}
