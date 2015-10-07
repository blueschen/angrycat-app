package com.angrycat.erp.businessrule;

import static com.angrycat.erp.common.DatetimeUtil.addOneDayToFirstMinute;
import static com.angrycat.erp.common.DatetimeUtil.addOneYearToLastMinute;
import static com.angrycat.erp.common.DatetimeUtil.getFirstMinuteOfDay;
import static com.angrycat.erp.common.DatetimeUtil.getLastDateOfMonth;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

@Component
@Scope("prototype")
public class MemberVipDiscount_20150930 implements Serializable {
	private static final long serialVersionUID = 854300118955063805L;
	
	private Date today;
	private int addCount = 1;
	private boolean toVipDateReset = true; // 如果為歷史資料，必須以原來的狀態新增，應當設為false；如果為新增資料，則須考量續會或不續會的問題，應當設為true
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
	public boolean isToVipDateReset() {
		return toVipDateReset;
	}
	public void setToVipDateReset(boolean toVipDateReset) {
		this.toVipDateReset = toVipDateReset;
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
			if(toVipDateReset){
				member.setToVipDate(getTodayOrNew()); // 如果不在VIP有效期間內，VIP有效起日應重設為當日
			}
			member.setImportant(true);
			Date toVipDate = member.getToVipDate();
			Calendar toVip = DateUtils.toCalendar(toVipDate);
			int toVipYear = toVip.get(Calendar.YEAR);
			int toVipMonth = toVip.get(Calendar.MONTH);
			
			firstEffectiveStart = getFirstMinuteOfDay(toVipDate);
			if(toVipMonth == birthMonth){
				firstEffectiveEnd = addOneYearToLastMinute(toVipDate);
			}else if(toVipMonth < birthMonth){
				firstEffectiveEnd = getLastDateOfMonth(toVipYear, birthMonth+1);
			}else{// toVipMonth > birthMonth
				firstEffectiveEnd = getLastDateOfMonth(toVipYear+1, birthMonth+1);
			}
		}
		
		if(!(member.getVipDiscountDetails() instanceof LinkedList)){
			List<VipDiscountDetail> details = member.getVipDiscountDetails();
			member.setVipDiscountDetails(new LinkedList<>());
			details.forEach(d->{
				member.getVipDiscountDetails().add(d);
			});
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
			((LinkedList)member.getVipDiscountDetails()).addFirst(detail);
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
	public static MemberVipDiscount_20150930 getInstance(){
		MemberVipDiscount_20150930 memberVipDiscount = new MemberVipDiscount_20150930();
		return memberVipDiscount;
	}
}
