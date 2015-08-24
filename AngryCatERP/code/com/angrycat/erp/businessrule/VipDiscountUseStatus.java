package com.angrycat.erp.businessrule;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

@Component
@Scope("prototype")
public class VipDiscountUseStatus implements Serializable{
	private static final long serialVersionUID = 4139654319192465971L;
	
	private Date today;
	public Date getToday(){return this.today;}
	public void setToday(Date today){this.today = today;}
	public Date getTodayOrNew(){
		Date current = today;
		if(current == null){
			current = new Date(System.currentTimeMillis());
		}
		return current;
	}

	private void applyRule(VipDiscountDetail d, Date birthday){
		d.setAvailable(false);
		Date today = getTodayOrNew();
		int birthMonth = getMonth(birthday);
		
		if(d.getDiscountUseDate() != null){
			d.setUseStatus("已用過");
		}else if(today.compareTo(d.getEffectiveEnd()) > 0){
			d.setUseStatus("已過期");//超過有效期間
		}else if(today.compareTo(d.getEffectiveStart()) < 0){
			d.setUseStatus("尚未到有效期限");//還沒到有效期間
		}else if(birthMonth != getMonth(today)){
			d.setUseStatus("尚未到可用期間");//還沒到生日月份
		}else{
			d.setUseStatus("可使用");
			d.setAvailable(true);
		}
	}
	
	public void applyRule(Member member){
		List<VipDiscountDetail> details = member.getVipDiscountDetails();
		details.forEach(d->{
			applyRule(d, member.getBirthday());
		});
	}
	
	private static int getMonth(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MONTH);
	}
}
