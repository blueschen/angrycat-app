package com.angrycat.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="shr_defaultserial")
public class DefaultSerial {
	private String id;
	private String sep0;
	private String dateSep0;
	private String sep1;
	private String dateSep1;
	private String sep2;
	private String dateSep2;
	private String sep3;
	private String no;
	private String sep4;
	private String resetNoField;
	private String resetNoFieldLastValue;
	private long resetNoTo;
	private String note;
	@Id
	@Column(name="id")
	@GenericGenerator(name="angrycat_defaultserial_id", strategy = "assigned")
	@GeneratedValue(generator="angrycat_defaultserial_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="sep0")
	public String getSep0() {
		return sep0;
	}
	public void setSep0(String sep0) {
		this.sep0 = sep0;
	}
	@Column(name="dateSep0")
	public String getDateSep0() {
		return dateSep0;
	}
	public void setDateSep0(String dateSep0) {
		this.dateSep0 = dateSep0;
	}
	@Column(name="sep1")
	public String getSep1() {
		return sep1;
	}
	public void setSep1(String sep1) {
		this.sep1 = sep1;
	}
	@Column(name="dateSep1")
	public String getDateSep1() {
		return dateSep1;
	}
	public void setDateSep1(String dateSep1) {
		this.dateSep1 = dateSep1;
	}
	@Column(name="sep2")
	public String getSep2() {
		return sep2;
	}
	public void setSep2(String sep2) {
		this.sep2 = sep2;
	}
	@Column(name="dateSep2")
	public String getDateSep2() {
		return dateSep2;
	}
	public void setDateSep2(String dateSep2) {
		this.dateSep2 = dateSep2;
	}
	@Column(name="sep3")
	public String getSep3() {
		return sep3;
	}
	public void setSep3(String sep3) {
		this.sep3 = sep3;
	}
	@Column(name="no")
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	@Column(name="sep4")
	public String getSep4() {
		return sep4;
	}
	public void setSep4(String sep4) {
		this.sep4 = sep4;
	}
	@Column(name="resetNoField")
	public String getResetNoField() {
		return resetNoField;
	}
	public void setResetNoField(String resetNoField) {
		this.resetNoField = resetNoField;
	}
	@Column(name="resetNoFieldLV")
	public String getResetNoFieldLastValue() {
		return resetNoFieldLastValue;
	}
	public void setResetNoFieldLastValue(String resetNoFieldLastValue) {
		this.resetNoFieldLastValue = resetNoFieldLastValue;
	}
	@Column(name="resetNoTo")
	public long getResetNoTo() {
		return resetNoTo;
	}
	public void setResetNoTo(long resetNoTo) {
		this.resetNoTo = resetNoTo;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
