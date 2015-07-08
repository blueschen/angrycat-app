package com.angrycat.erp.format;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FormatList extends LinkedList<ObjectFormat> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2362468710467586043L;
	
	public FormatList addElement(ObjectFormat e){
		this.add(e);
		return this;
	}
	


}
