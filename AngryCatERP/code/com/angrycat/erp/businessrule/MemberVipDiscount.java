package com.angrycat.erp.businessrule;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

@Component
@Scope("prototype")
public class MemberVipDiscount implements Serializable {
	private static final long serialVersionUID = 854300118955063805L;
	
	public void applyRule(Member member){
		addVipDetials(member);
	}
	
	private void addVipDetials(Member member){
		int addVipYearCount = 0;
		Date toVipDate = member.getToVipDate();
		Date birthday = member.getBirthday();
		
		Calendar toVip = DateUtils.toCalendar(toVipDate);
		int toVipYear = toVip.get(Calendar.YEAR);
		int toVipMonth = toVip.get(Calendar.MONTH);
		
		Calendar birth = DateUtils.toCalendar(birthday);
		int birthMonth = birth.get(Calendar.MONTH);		
		
		Date firstEffectiveStart = null;
		Date firstEffectiveEnd = null;
		
		if(isInVipEffectiveDur(member)){
			addVipYearCount = member.getVipEffectiveYearCount() - member.getVipDiscountDetails().size();
			firstEffectiveStart = addOneDayToFirstMinute(member.getToVipEndDate());
			firstEffectiveEnd = addOneYearToLastMinute(firstEffectiveStart);						
		}else{
			addVipYearCount = member.getVipEffectiveYearCount();
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
			detail.setToVipDate(toVipDate);
			
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
		if(m.getToVipDate() == null || m.getToVipEndDate() == null || m.getVipEffectiveYearCount() == 0){
			return false;
		}
		Date current = new Date(System.currentTimeMillis());
		if(current.compareTo(m.getToVipDate()) < 0 || current.compareTo(m.getToVipEndDate()) > 0){
			return false;
		}
		return true;
	}

	
	/**
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	private Date getLastDateOfMonth(int year, int month){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		return getLastMinuteOfDay(year, month, lastDay);
	}
	public static void testGetLastDateOfMonth(){
		MemberVipDiscount m = getInstance();
		System.out.println("last day: " + m.getLastDateOfMonth(2016, 1));
	}
	/**
	 * retrieve Date, and hour, minute, second, millisec all are set to zero
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	private static Date getFirstMinuteOfDay(int year, int month, int day){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	private static Date getFirstMinuteOfDay(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	private static Calendar getFirstMinute(Calendar c){
		c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
		return c;
	}
	private static void testGetFirstMinute(){
		Calendar c = Calendar.getInstance();
		c = getFirstMinute(c);
		System.out.println(c.getTime());
	}
	private static Date getLastMinuteOfDay(int year, int month, int day){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c = getLastMinute(c);
		return new Date(c.getTimeInMillis());
	}
	private static Calendar getLastMinute(Calendar c){
		c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
		return c;
	}
	private static void testGetLastMinute(){
		Calendar c = Calendar.getInstance();
		c = getLastMinute(c);
		System.out.println(c.getTime());
	}
	public static void main(String[]args){
		testAddOneYearToLastMinute();
	}
	private Date addOneDayToFirstMinute(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c = getFirstMinute(c);
		return new Date(c.getTimeInMillis());
	}
	public static void testAddOneDayToFirstMinute(){
		MemberVipDiscount m = getInstance();
		Date today = new Date(System.currentTimeMillis());
		System.out.println("add One day: " + m.addOneDayToFirstMinute(today));
	}
	/**
	 * ex: 2015-09-10~2015-09-09
	 * @param c
	 * @return
	 */
	private Date addOneYearToLastMinute(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.YEAR, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		c = getLastMinute(c);
		Date oneYearAfter = new Date(c.getTime().getTime());
		return oneYearAfter;
	}
	private static void testAddOneYearToLastMinute(){
		MemberVipDiscount m = getInstance();
		Date today = new Date(System.currentTimeMillis());
		System.out.println("today: " + today);
		System.out.println("one year after: " + m.addOneYearToLastMinute(today));
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2016);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 29);
		Date time = new Date(c.getTimeInMillis());
		System.out.println("test time: " + time);
		System.out.println("one year after: " + m.addOneYearToLastMinute(time));
	}
	private static void testApplyVipEffectiveDurGenRule(){
		Member m = new Member();
		m.setBirthday(getFirstMinuteOfDay(1977, 1, 20));
		m.setToVipDate(getFirstMinuteOfDay(2014, 3, 30));
		m.setVipEffectiveYearCount(1);
		
		MemberVipDiscount memberVipDiscount = new MemberVipDiscount();
		memberVipDiscount.applyRule(m);
		System.out.println("birthday: " + m.getBirthday());
		
		System.out.println("toVipDate: " + m.getToVipDate());
		System.out.println("vipEffectiveYearCount: " + m.getVipEffectiveYearCount());
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
