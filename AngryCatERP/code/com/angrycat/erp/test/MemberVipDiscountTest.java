package com.angrycat.erp.test;

import static com.angrycat.erp.common.DatetimeUtil.getFirstMinuteOfDay;

import java.util.LinkedList;
import java.util.List;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;
import com.angrycat.erp.service.QueryBaseService;

public class MemberVipDiscountTest extends BaseTest {
	public static void testQueryMemberVipDiscountLazyFetch(){
		executeSession((s, acac)->{
			QueryBaseService<Member, Member> cbs = (QueryBaseService<Member, Member>)acac.getBean("queryBaseService");
			cbs
				.createFromAlias(Member.class.getName(), "p")
				.addSelect("DISTINCT(p)")
				.createAssociationAlias("left join fetch p.vipDiscountDetails", "details", null)
				.addWhere(ConditionFactory.putStr("p.id = :pId"));
			
			cbs.getSimpleExpressions().get("pId").setValue("20150820-164113515-pRMin");
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
	// unexpected results!!
	public static void testQueryMemberVipDiscount(){
		executeSession((s, acac)->{
			QueryBaseService<Member, Member> cbs = (QueryBaseService<Member, Member>)acac.getBean("queryBaseService");
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
			
			List<VipDiscountDetail> detail = m.getVipDiscountDetails();
			m.setVipDiscountDetails(new LinkedList<VipDiscountDetail>());
			
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
	
	public static void testQueryDetailSort(){
		executeSession((s, acac)->{
			List<Member> members = s.createQuery("SELECT DISTINCT(m) FROM " + Member.class.getName() + " m join m.vipDiscountDetails details WHERE size(details) > 3").list();
			members.stream().forEach(m->{
				System.out.println("member id: " + m.getId());
				m.getVipDiscountDetails().stream().forEach(d->{
					System.out.println("d id: " + d.getId());
				});
			});
		});
	}
	
	private static void testVipDiscountRule(){
		Member m = new Member();
		m.setBirthday(DatetimeUtil.getFirstMinuteOfDay(1988, 11, 12));
		m.setToVipDate(DatetimeUtil.getFirstMinuteOfDay(2015, 8, 31));
		System.out.println("birthday:" + m.getBirthday());
		System.out.println("toVipDate:" + m.getToVipDate());
		
		MemberVipDiscount mvd = new MemberVipDiscount();
		mvd.applyRule(m);
		
		m.getVipDiscountDetails().forEach(d->{
			System.out.println("start: " + d.getEffectiveStart() + ", end: " + d.getEffectiveEnd());
		});
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
	public static void main(String[]args){
		testApplyVipEffectiveDurGenRule();
	}
}
