package com.angrycat.erp.format;

import java.util.LinkedList;
/**
 * 格式化設定的集合
 * @author JerryLin
 *
 */
public class FormatList extends LinkedList<ObjectFormat> {
	private static final long serialVersionUID = -2362468710467586043L;
	private String docTitle;
	private static final FormatList EMPTY_LIST= new FormatList();
	public String getDocTitle() {
		return docTitle;
	}
	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}
	public static FormatList emptyList(){
		return EMPTY_LIST;
	}
}
