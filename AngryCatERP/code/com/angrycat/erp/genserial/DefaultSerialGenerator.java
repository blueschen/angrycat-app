package com.angrycat.erp.genserial;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.model.DefaultSerial;

public class DefaultSerialGenerator extends SerialGenerator<String, Session> {
	private String id;
	private SessionFactory sessionFactory;
	public DefaultSerialGenerator(String id){
		setId(id);
	}
	public DefaultSerialGenerator(String id, SessionFactory sessionFactory){
		this(id);
		setSessionFactory(sessionFactory);
	}
	@Override
	public String getId() {
		return id;
	}
	public DefaultSerialGenerator setId(String id){
		this.id = id;
		return this;
	}
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Override
	public String getNext(){
		Session s = null;
		Transaction tx = null;
		try{
			s = sessionFactory.openSession();
			tx = s.beginTransaction();
			String nextNo = getNext(s);
			tx.commit();
			return nextNo;
		}finally{
			if(tx.isActive()){
				tx.rollback();
			}
			s.close();
		}
	}
	@Override
	public String getNext(Session s){
		DefaultSerial ds = (DefaultSerial)s.load(DefaultSerial.class, this.id);
		String nextNo = getNext(ds);
		s.flush();
		return nextNo;
	}
	public String getNext(DefaultSerial ds){
		StringBuffer sb = new StringBuffer();
		Calendar c = Calendar.getInstance();
		if(StringUtils.isNotBlank(ds.getSep0())){
			sb.append(ds.getSep0());
		}
		if(StringUtils.isNotBlank(ds.getDateSep0())){
			sb.append(formatDate(ds.getDateSep0(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep1())){
			sb.append(ds.getSep1());
		}
		if(StringUtils.isNotBlank(ds.getDateSep1())){
			sb.append(formatDate(ds.getDateSep1(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep2())){
			sb.append(ds.getSep2());
		}
		if(StringUtils.isNotBlank(ds.getDateSep2())){
			sb.append(formatDate(ds.getDateSep2(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep3())){
			sb.append(ds.getSep3());
		}
		String resetNoField = ds.getResetNoField();
		if(StringUtils.isNotBlank(resetNoField)){
			String v = CommonUtil.getPropertyVal(ds, resetNoField);
			String dateString = formatDate(v, c);
			if(StringUtils.isNotBlank(ds.getResetNoFieldLastValue())
			&& !dateString.equals(ds.getResetNoFieldLastValue())){
				ds.setNo(formatNo(ds.getResetNoTo(), ds.getNo().length()));
			}
			ds.setResetNoFieldLastValue(dateString);
		}
		if(StringUtils.isNotBlank(ds.getNo())){
			sb.append(ds.getNo());
			ds.setNo(incrementNo(ds.getNo()));
		}
		if(StringUtils.isNotBlank(ds.getSep4())){
			sb.append(ds.getSep4());
		}
		return sb.toString();
	
	}
	protected String formatDate(String dateSep, Calendar c){
		// ROC代表民國年
		// ROC3代表民國年，並且總是有三位數，不足左邊補零
		if("ROC".equals(dateSep) || "ROC3".equals(dateSep)){
			int year = c.get(Calendar.YEAR) - 1911;
			if("ROC".equals(dateSep)){
				return String.valueOf(year);
			}
			if(year < 100){
				return "0" + String.valueOf(year);
			}
			return String.valueOf(year);
		}
		SimpleDateFormat df = new SimpleDateFormat(dateSep);
		return df.format(c.getTime());
	}
}
