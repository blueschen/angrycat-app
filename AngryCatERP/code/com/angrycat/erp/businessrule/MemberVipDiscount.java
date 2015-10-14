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
public class MemberVipDiscount implements Serializable {
	private static final long serialVersionUID = 854300118955063805L;
	
	private Date today;
	private int addCount = 1;
	private Date batchStartDate;
	public Date getToday(){return this.today;}
	public void setToday(Date today){this.today = today;}
	public int getAddCount(){return this.addCount;}
	public void setAddCount(int addCount){this.addCount = addCount;}
	public Date getBatchStartDate() {return batchStartDate;}
	public void setBatchStartDate(Date batchStartDate) {this.batchStartDate = batchStartDate;}
	/**
	 * 取得人工設定的當日，如果沒有就回傳系統當日
	 * @return
	 */
	private Date specifyOrNewToday(){
		Date current = today;
		if(current == null){
			current = new Date(System.currentTimeMillis());
		}
		return current;
	}
	/**
	 * 取得此次計算VIP起始日，如果沒有指定，就取人工輸入的當日，前面兩者都沒有，就傳回系統當日。
	 * 理論上，VIP起始日不可早於系統當日，但前端使用者在新增資料時，需要能夠調整VIP起始日。
	 * 人工輸入的當日是為了測試用途，用以了解在不同的時間點，前端資料會如何變化。
	 * @return
	 */
	private Date accessVIPStartDate(){
		if(batchStartDate != null){
			return batchStartDate;
		}
		return specifyOrNewToday();
	}
	public void applyRule(Member member){
		addVipDetials(member);
	}
	
	private void addVipDetials(Member member){
		int addVipYearCount = getAddCount();
		Date birthday = member.getBirthday();

		Calendar birth = DateUtils.toCalendar(birthday);
		int birthMonth = birth.get(Calendar.MONTH);		

		// 先計算出第一筆VIP有效起迄日
		Date firstEffectiveStart = null;
		Date firstEffectiveEnd = null;

		if(isInVipEffectiveDur(member)){
			firstEffectiveStart = addOneDayToFirstMinute(member.getToVipEndDate());
			firstEffectiveEnd = addOneYearToLastMinute(firstEffectiveStart);						
		}else{
			member.setToVipDate(accessVIPStartDate());
			member.setImportant(true);
			Date toVipDate = member.getToVipDate();

			firstEffectiveStart = getFirstMinuteOfDay(toVipDate);
			firstEffectiveEnd = addOneYearToLastMinute(toVipDate);
			
			// 計算VIP起始日不以生日為基準，所以將下列程式碼註解起來
//			Calendar toVip = DateUtils.toCalendar(toVipDate);
//			int toVipYear = toVip.get(Calendar.YEAR);
//			int toVipMonth = toVip.get(Calendar.MONTH);
//			if(toVipMonth == birthMonth){
//				firstEffectiveEnd = addOneYearToLastMinute(toVipDate);
//			}else if(toVipMonth < birthMonth){// 轉VIP的時候，尚未到當年生日
//				firstEffectiveEnd = getLastDateOfMonth(toVipYear, birthMonth+1);
//			}else{// 轉VIP的時候，當年生日已經過了
//				firstEffectiveEnd = getLastDateOfMonth(toVipYear+1, birthMonth+1);
//			}
		}
		
		if(!(member.getVipDiscountDetails() instanceof LinkedList)){// 如果不是型別LinkedList，則手動轉換
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
			detail.setToVipDate(member.getToVipDate()); // 同一批明細的VIP起日都一樣
			
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
			((LinkedList)member.getVipDiscountDetails()).addFirst(detail); // 最新一筆插到最前面
		}
		member.setToVipEndDate(currentEffectiveEnd); // 同一批最後一筆VIP紀錄的結束日，就是真正的VIP有效截止日，將這筆資料回寫會員主表
	}
	
	/**
	 * VIP起始日，是否在VIP有效期間內。
	 * 以程式的角度，VIP起始日有指定日期、人工當日、系統當日。
	 * 這裡考慮兩種情況，一是新增、一是續會。
	 * @param m
	 * @return
	 */
	private boolean isInVipEffectiveDur(Member m){
		if(m.getToVipDate() == null || m.getToVipEndDate() == null){
			return false;
		}
		Date batchStartDate = accessVIPStartDate();
		if(batchStartDate.compareTo(m.getToVipEndDate()) > 0){ // VIP起始日晚於有效期限，代表可以使用這個日期做為計算起始日
			return false;
		}
		return true;
	}
	public static MemberVipDiscount getInstance(){
		MemberVipDiscount memberVipDiscount = new MemberVipDiscount();
		return memberVipDiscount;
	}
}
