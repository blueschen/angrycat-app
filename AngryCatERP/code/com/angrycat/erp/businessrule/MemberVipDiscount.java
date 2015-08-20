package com.angrycat.erp.businessrule;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.angrycat.erp.common.DatetimeUtil.*;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

@Component
@Scope("prototype")
public class MemberVipDiscount implements Serializable {
	private static final long serialVersionUID = 854300118955063805L;
	
	private Date today;
	private int addCount = 1;
	public Date getToday(){return this.today;}
	public void setToday(Date today){this.today = today;}
	public int getAddCount(){return this.addCount;}
	public void setAddCount(int addCount){this.addCount = addCount;}
	public Date getTodayOrNew(){
		Date current = today;
		if(current == null){
			current = new Date(System.currentTimeMillis());
		}
		return current;
	}
	
	public void applyRule(Member member){
		addVipDetials(member);
	}
	
	private void addVipDetials(Member member){
		int addVipYearCount = getAddCount();
		Date birthday = member.getBirthday();

		Calendar birth = DateUtils.toCalendar(birthday);
		int birthMonth = birth.get(Calendar.MONTH);		
		
		Date firstEffectiveStart = null;
		Date firstEffectiveEnd = null;
		
		if(isInVipEffectiveDur(member)){
			firstEffectiveStart = addOneDayToFirstMinute(member.getToVipEndDate());
			firstEffectiveEnd = addOneYearToLastMinute(firstEffectiveStart);						
		}else{
			member.setToVipDate(getTodayOrNew()); // 如果不在VIP有效期間內，VIP有效起日應重設為當日
			member.setImportant(true);
			Date toVipDate = member.getToVipDate();
			Calendar toVip = DateUtils.toCalendar(toVipDate);
			int toVipYear = toVip.get(Calendar.YEAR);
			int toVipMonth = toVip.get(Calendar.MONTH);
			
			firstEffectiveStart = getFirstMinuteOfDay(toVipDate);
			
			if(toVipMonth == birthMonth){
				firstEffectiveEnd = addOneYearToLastMinute(toVipDate);
			}else if(toVipMonth < birthMonth){
				firstEffectiveEnd = getLastDateOfMonth(toVipYear, birthMonth);
			}else{// toVipMonth > birthMonth
				firstEffectiveEnd = getLastDateOfMonth(toVipYear+1, birthMonth);
			}
		}
		
		Date currentEffectiveEnd = firstEffectiveEnd;
		for(int i = 0; i < addVipYearCount; i++){
			VipDiscountDetail detail = new VipDiscountDetail();
			detail.setMemberId(member.getId());
			detail.setMemberIdNo(member.getIdNo());
			detail.setToVipDate(member.getToVipDate());
			
			if(i == 0){
				detail.setEffectiveStart(firstEffectiveStart);
				detail.setEffectiveEnd(firstEffectiveEnd);
			}else{
				Date nextEffectiveStart = addOneDayToFirstMinute(currentEffectiveEnd);
				Date nextEffectiveEnd = addOneYearToLastMinute(nextEffectiveStart);
				detail.setEffectiveStart(nextEffectiveStart);
				detail.setEffectiveEnd(nextEffectiveEnd);
				currentEffectiveEnd = nextEffectiveEnd;
			}
			member.getVipDiscountDetails().add(detail);
		}
		member.setToVipEndDate(currentEffectiveEnd);
	}
	
	private boolean isInVipEffectiveDur(Member m){
		if(m.getToVipDate() == null || m.getToVipEndDate() == null){
			return false;
		}
		Date current = getTodayOrNew();
		if(current.compareTo(m.getToVipEndDate()) > 0){
			return false;
		}
		return true;
	}
	private static void testApplyVipEffectiveDurGenRule(){
		Member m = new Member();
		m.setBirthday(getFirstMinuteOfDay(1977, 1, 20));
		m.setToVipDate(getFirstMinuteOfDay(2014, 3, 30));
		
		MemberVipDiscount memberVipDiscount = new MemberVipDiscount();
		memberVipDiscount.applyRule(m);
		System.out.println("birthday: " + m.getBirthday());
		
		System.out.println("toVipDate: " + m.getToVipDate());
		System.out.println("toVipEndDate: " + m.getToVipEndDate());
		m.getVipDiscountDetails().forEach(v->{
			System.out.println("effective start: " + v.getEffectiveStart());
			System.out.println("effective end: " + v.getEffectiveEnd());
		});
	}
	public static MemberVipDiscount getInstance(){
		MemberVipDiscount memberVipDiscount = new MemberVipDiscount();
		return memberVipDiscount;
	}
}
