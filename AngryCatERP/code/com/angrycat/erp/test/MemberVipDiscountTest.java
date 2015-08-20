package com.angrycat.erp.test;

import static com.angrycat.erp.common.DatetimeUtil.getFirstMinuteOfDay;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;
import com.angrycat.erp.service.CrudBaseService;

public class MemberVipDiscountTest extends BaseTest {
	public static void main(String[]args){
		testMemberAndDiscountDetailSaveAtTheSameTime();
	}
	// unexpected results!!
	public static void testQueryMemberVipDiscount(){
		executeSession((s, acac)->{
			CrudBaseService<Member, Member> cbs = (CrudBaseService<Member, Member>)acac.getBean("crudBaseService");
			cbs.setRoot(Member.class);
			cbs.createFromAlias(Member.class.getName(), "p")
				.addSelect("DISTINCT(p)")
				.createAssociationAlias("left join p.vipDiscountDetails", "details", null)
				.addWhere(ConditionFactory.propertyDesc("details.toVipDate = p.toVipDate"));
			System.out.println(cbs.toQueryGenerator().toCompleteStr());
			
			List<Member> list = cbs.executeQueryList(s);
			list.stream().forEach(m->{
				System.out.println("m id: " + m.getId() + ", toVipDate: " + m.getToVipDate());
				m.getVipDiscountDetails().stream().forEach(d->{
					System.out.println("d id: " + d.getId() + ", toVipDate: " + d.getToVipDate());
				});
			});
		});
	}
	
	public static void testInsertMemberVipDiscountWithVipStartGreaterThanBirthMonth(){
		executeSession((s, acac)->{
						
			Member m = new Member();
			m.setName("vipStartGreaterThanBirthMonth");
			m.setBirthday(getFirstMinuteOfDay(1988, 1, 12));
			s.save(m);
			s.flush();
			
			MemberVipDiscount mvd = acac.getBean(MemberVipDiscount.class);
			mvd.setToday(getFirstMinuteOfDay(2015, 8, 20));
			mvd.setAddCount(3);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());			
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2018, 3, 20));
			mvd.setAddCount(1);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2019, 5, 3));
			mvd.setAddCount(2);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
		});
	}
	
	public static void testInsertMemberVipDiscountWithVipStartLessThanBirthMonth(){
		executeSession((s, acac)->{
						
			Member m = new Member();
			m.setName("vipStartLessThanBirthMonth");
			m.setBirthday(getFirstMinuteOfDay(1988, 8, 12));
			s.save(m);
			s.flush();
			
			MemberVipDiscount mvd = acac.getBean(MemberVipDiscount.class);
			mvd.setToday(getFirstMinuteOfDay(2015, 2, 20));
			mvd.setAddCount(3);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());			
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2018, 6, 20));
			mvd.setAddCount(1);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2019, 1, 3));
			mvd.setAddCount(2);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
		});
	}
	
	public static void testInsertMemberVipDiscountWithVipStartEqulasToBirthMonth(){
		executeSession((s, acac)->{
						
			Member m = new Member();
			m.setName("vipStartEqulasToBirthMonth");
			m.setBirthday(getFirstMinuteOfDay(1988, 6, 12));
			s.save(m);
			s.flush();
			
			MemberVipDiscount mvd = acac.getBean(MemberVipDiscount.class);
			mvd.setToday(getFirstMinuteOfDay(2015, 6, 20));
			mvd.setAddCount(3);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());			
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2018, 6, 3));
			mvd.setAddCount(1);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
			
			mvd.setToday(getFirstMinuteOfDay(2019, 6, 15));
			mvd.setAddCount(2);
			mvd.applyRule(m);
//			multilinePrint(m);
//			multilinePrint(m.getVipDiscountDetails());
			s.update(m);
			s.flush();
		});
	}
	
	
	public static void testMemberAndDiscountDetailSaveAtTheSameTime(){
		executeSession((s, acac)->{
			Member m = new Member();
			m.setName("testMemberAndDiscountDetailSaveAtTheSameTime2");
			m.setBirthday(getFirstMinuteOfDay(1988, 3, 28));
			MemberVipDiscount mvd = acac.getBean(MemberVipDiscount.class);
			mvd.setToday(getFirstMinuteOfDay(2015, 6, 20));
			mvd.setAddCount(3);
			mvd.applyRule(m);
			
			Set<VipDiscountDetail> detail = m.getVipDiscountDetails();
			m.setVipDiscountDetails(new LinkedHashSet<VipDiscountDetail>());
			
			s.save(m);
			s.flush();
			
			detail.stream().forEach(d->{
				d.setMemberId(m.getId());
				
			});
			m.getVipDiscountDetails().addAll(detail);
			s.saveOrUpdate(m);
			s.flush();
			
			
		});
	}
}
