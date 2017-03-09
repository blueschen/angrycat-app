package com.angrycat.erp.genserial;

import java.io.Serializable;
import java.text.NumberFormat;

public abstract class SerialGenerator<PK extends Serializable & Comparable<PK>, S> {
	public abstract PK getId();
	public abstract String getNext();
	public abstract String getNext(S s);
	
	public static String incrementNo(String no){
		long num = Long.parseLong(no) + 1l;
		int len = no.length();
		return formatNo(num, len);
	}
	public static String formatNo(long num, int len){
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMaximumIntegerDigits(len);
		nf.setMinimumIntegerDigits(len);
		nf.setGroupingUsed(false);
		String no = nf.format(num);
		return no;
	}
	
}
