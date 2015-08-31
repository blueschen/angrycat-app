package com.angrycat.erp.log;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.model.DataChangeLogDetail;
import com.angrycat.erp.security.User;

@Component
@Scope("prototype")
public class DataChangeLogger {
	@Autowired
	private LocalSessionFactoryBean sf;
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public void log(String docId, String docType, Object oldObject, Object newObject, List<ObjectFormat> formatList, Session s, ActionType action){
		log(user, docId, docType, oldObject, newObject, formatList, null, s, action);
	}
	
	public void log(User user, String docId, String docType, Object oldObject, Object newObject, List<ObjectFormat> formatList, String note, Session s, ActionType action){
		DataChangeLog log = new DataChangeLog();
		log.setDocId(docId);
		log.setDocType(docType);
		log.setLogTime(new Timestamp(System.currentTimeMillis()));
		log.setUserId(user.getUserId());
		log.setUserName(user.getInfo().getName());
		log.setNote(note);
		log.setAction(action.name());
		formatList.stream().forEach(of->{
			String oldVal = oldObject != null ? removeReturn(of.getValue(oldObject)) : null;
			String newVal = newObject != null ? removeReturn(of.getValue(newObject)) : null;
			if(!new EqualsBuilder().append(oldVal, newVal).isEquals()){
				log.getDetails().add(new DataChangeLogDetail(of.getName(), toDefaultIfNull(oldVal), toDefaultIfNull(newVal)));
			}
		});
		if(!log.getDetails().isEmpty()){
			s.save(log);
		}
	}
	
	private String toDefaultIfNull(String val){
		return StringUtils.defaultIfBlank(val, "--");
	}
	
	public void logAdd(Object newObject, Session s){
		List<ObjectFormat> formatList = FormatListFactory.forLog(newObject);
		logAction(ActionType.ADD, null, newObject, s, formatList);
	}
	
	public void logUpdate(Object oldObject, Object newObject, Session s){
		List<ObjectFormat> formatList = FormatListFactory.forUpdateLog(oldObject, newObject);
		logAction(ActionType.UPDATE, oldObject, newObject, s, formatList);
	}
	
	public void logDelete(Object oldObject, Session s){
		List<ObjectFormat> formatList = FormatListFactory.forLog(oldObject);
		logAction(ActionType.DELETE, oldObject, null, s, formatList);
	}
	
	public void logDelete(Object oldObject, Session s, User user){
		setUser(user);
		logDelete(oldObject, s);
	}
	
	public void logAction(ActionType action, Object oldObject, Object newObject, Session s, List<ObjectFormat> formatList){
		Object example = oldObject != null ? oldObject : newObject;
		String id = sf.getObject().getClassMetadata(example.getClass()).getIdentifierPropertyName();
		try {
			String idVal = (String)PropertyUtils.getProperty(example, id);
			log(idVal, example.getClass().getName(), oldObject, newObject, formatList, s, action);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String removeReturn(String val){
		if(val == null || StringUtils.isBlank(val)){
			return null;
		}
		return StringUtils.replace(val, "\r\n", "\n");
	}
}
