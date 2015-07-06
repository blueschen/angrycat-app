package com.angrycat.erp.log;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.Session;

import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.model.DataChangeLogDetail;
import com.angrycat.erp.security.User;

public class DataChangeLogger {
	
	public static void log(User user, String docId, String docType, Object oldObject, Object newObject, List<ObjectFormat> formatList, Session s){
		log(user, docId, docType, oldObject, newObject, formatList, null, s);
	}
	
	public static void log(User user, String docId, String docType, Object oldObject, Object newObject, List<ObjectFormat> formatList, String note, Session s){
		DataChangeLog log = new DataChangeLog();
		log.setDocId(docId);
		log.setDocType(docType);
		log.setLogTime(new Timestamp(System.currentTimeMillis()));
		log.setUserId(user.getUserId());
		log.setUserName(user.getInfo().getName());
		log.setNote(note);
		formatList.stream().forEach(of->{
			String oldVal = removeReturn(of.getValue(oldObject));
			String newVal = removeReturn(of.getValue(newObject));
			if(!new EqualsBuilder().append(oldVal, newVal).isEquals()){
				log.getDetails().add(new DataChangeLogDetail(of.getName(), oldVal, newVal));
			}
		});
		if(!log.getDetails().isEmpty()){
			s.save(log);
		}
	}
	
	private static String removeReturn(String val){
		if(val == null){
			return val;
		}
		return StringUtils.replace(val, "\r\n", "\n");
	}
}
