package com.angrycat.erp.log;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.format.FormatList;
import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.model.DataChangeLog;
import com.angrycat.erp.model.DataChangeLogDetail;
import com.angrycat.erp.security.User;
import com.angrycat.erp.security.extend.UserInfo;

@Component
@Scope("prototype")
public class DataChangeLogger {
	private static final User DEFAULT_USER;
	static{
		DEFAULT_USER = new User();
		DEFAULT_USER.setUserId("visitor");
		DEFAULT_USER.setInfo(new UserInfo());
		DEFAULT_USER.getInfo().setName("遊客");
	}
	@Autowired
	private LocalSessionFactoryBean sf;
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public void log(String docId, String docType, Object oldObject, Object newObject, FormatList formatList, Session s, ActionType action){
		log(user, docId, docType, oldObject, newObject, formatList, null, s, action);
	}
	
	public void log(User user, String docId, String docType, Object oldObject, Object newObject, FormatList formatList, String note, Session s, ActionType action){
		if(user == null){
			user = DEFAULT_USER; 
		}
		DataChangeLog log = new DataChangeLog();
		log.setDocId(docId);
		log.setDocType(docType);
		String docTitleConfig = formatList.getDocTitle();
		if(StringUtils.isNotBlank(docTitleConfig)){
			Object existed = getObjectExisted(oldObject, newObject);
			String docTitle = formatDocTitle(existed, docTitleConfig);
			if(StringUtils.isNotBlank(docTitle)){
				log.setDocTitle(docTitle);
			}
		}
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
	
	/**
	 * 格式化輸出docTitle，為了涵蓋各種情況，允許傳入多個以逗號隔開指定欄位組成docTitle，並以底線隔開。
	 * 
	 * @param obj
	 * @param docTitleConfig
	 * @return
	 */
	private String formatDocTitle(Object obj, String docTitleConfig){
		String docTitle = null;
		if(docTitleConfig.contains(",")){
			String[] docTitleConfigs = docTitleConfig.split(",");
			List<String> docTitleSplits = new ArrayList<>();
			for(String config : docTitleConfigs){
				String field = getFormattedString(obj, config);
				if(StringUtils.isNotBlank(field)){
					docTitleSplits.add(field);
				}
			}
			if(!docTitleSplits.isEmpty()){
				docTitle = StringUtils.join(docTitleSplits.toArray(), "_");
			}
		}else{
			String field = getFormattedString(obj, docTitleConfig);
			docTitle = field;
		}
		return docTitle;
	}
	
	private String getFormattedString(Object obj, String prop){
		Object target = CommonUtil.getProperty(obj, prop);
		String field = null;
		if(target != null){
			Class<?> clz = target.getClass();
				try{
					DateFormat df1 = DatetimeUtil.DF_yyyyMMdd_DASHED;
					DateFormat df2 = DatetimeUtil.DF_yyyyMMdd_DASHED_EXTEND_TO_SEC;
					if(clz == String.class){
						String val = (String)target;
						val = val.trim();
						val = val.replace("\n", "");
						val = val.replace("\r", "");
						val = val.replace("\t", "");
						field = val;
					}else if(clz == Date.class){
						field = df1.format((Date)target);
					}else if(clz == Timestamp.class){
						long timeoffset = ((Timestamp)target).getTime();
						field = df2.format(new Date(timeoffset));
					}else if(target instanceof Number){
						field = ((Number)target).toString();
					}
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			
		}
		if(StringUtils.isNotBlank(field)){
			return field;
		}
		return null;
	}
	
	private String toDefaultIfNull(String val){
		return StringUtils.defaultIfBlank(val, "--");
	}
	
	public void logAdd(Object newObject, Session s){
		FormatList formatList = FormatListFactory.forLog(newObject);
		logAction(ActionType.ADD, null, newObject, s, formatList);
	}
	
	public void logUpdate(Object oldObject, Object newObject, Session s){
		FormatList formatList = FormatListFactory.forUpdateLog(oldObject, newObject);
		logAction(ActionType.UPDATE, oldObject, newObject, s, formatList);
	}
	
	public void logDelete(Object oldObject, Session s){
		FormatList formatList = FormatListFactory.forLog(oldObject);
		logAction(ActionType.DELETE, oldObject, null, s, formatList);
	}
	
	public void logDelete(Object oldObject, Session s, User user){
		setUser(user);
		logDelete(oldObject, s);
	}
	
	public void logAction(ActionType action, Object oldObject, Object newObject, Session s, FormatList formatList){
		Object example = getObjectExisted(oldObject, newObject);
		String id = sf.getObject().getClassMetadata(example.getClass()).getIdentifierPropertyName();
		String idVal = (String)CommonUtil.getProperty(example, id);
		log(idVal, example.getClass().getName(), oldObject, newObject, formatList, s, action);
	}
	private Object getObjectExisted(Object oldObject, Object newObject){
		Object existed = oldObject != null ? oldObject : newObject;
		return existed;
	}
	
	private static String removeReturn(String val){
		if(val == null || StringUtils.isBlank(val)){
			return null;
		}
		return StringUtils.replace(val, "\r\n", "\n");
	}
}
