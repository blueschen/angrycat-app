package com.angrycat.erp.format;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.angrycat.erp.format.FormattedValue;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

import static org.junit.Assert.*;

public class ComplexDetailPropertyFormatTests {
	
	@Test
	public void getValues(){
		Date d1 = Date.valueOf(LocalDate.of(2001, 12, 1));
		Date d2 = Date.valueOf(LocalDate.of(2003, 11, 2));
		Date d3 = Date.valueOf(LocalDate.of(2007, 7, 14));
		Date d4 = Date.valueOf(LocalDate.of(2008, 3, 2));
		Date d5 = Date.valueOf(LocalDate.of(2015, 10, 9));
		
		VipDiscountDetail v1_1 = new VipDiscountDetail();
		v1_1.setId("v1");
		v1_1.setEffectiveStart(d2);
		v1_1.setEffectiveEnd(d5);
		
		VipDiscountDetail v2_1 = new VipDiscountDetail();
		v2_1.setId("v2");
		v2_1.setEffectiveStart(d1);
		v2_1.setEffectiveEnd(d3);
		
		VipDiscountDetail v3_1 = new VipDiscountDetail();
		v3_1.setId("v3");
		v3_1.setEffectiveStart(d3);
		v3_1.setEffectiveEnd(d4);
		
		VipDiscountDetail v4_1 = new VipDiscountDetail();
		v4_1.setId("v4");
		v4_1.setEffectiveStart(d1);
		v4_1.setEffectiveEnd(d2);
		
		VipDiscountDetail v5_1 = new VipDiscountDetail();
		v5_1.setId("v5");
		v5_1.setEffectiveStart(d2);
		v5_1.setEffectiveEnd(d5);
		
		List<VipDiscountDetail> oldDetails = new ArrayList<>();
		oldDetails.add(v1_1);
		oldDetails.add(v2_1);
		oldDetails.add(v3_1);
		oldDetails.add(v4_1);
		oldDetails.add(v5_1);
		Member m1 = new Member();
		m1.setVipDiscountDetails(oldDetails);
		
		VipDiscountDetail v2_2 = new VipDiscountDetail();
		v2_2.setId("v2");
		v2_2.setEffectiveStart(d2);
		v2_2.setEffectiveEnd(d3);
		
		VipDiscountDetail v3_2 = new VipDiscountDetail();
		v3_2.setId("v3");
		v3_2.setEffectiveStart(null);
		v3_2.setEffectiveEnd(d4);
		
		VipDiscountDetail v4_2 = new VipDiscountDetail();
		v4_2.setId("v4");
		v4_2.setEffectiveStart(d1);
		v4_2.setEffectiveEnd(d4);
		
		VipDiscountDetail v6_2 = new VipDiscountDetail();
		v6_2.setId("v6");
		v6_2.setEffectiveStart(d3);
		v6_2.setEffectiveEnd(d4);
		
		VipDiscountDetail v7_2 = new VipDiscountDetail();
		v7_2.setId("v7");
		v7_2.setEffectiveStart(d4);
		v7_2.setEffectiveEnd(d5);
		
		List<VipDiscountDetail> newDetails = new ArrayList<>();
		newDetails.add(v2_2);
		newDetails.add(v3_2);
		newDetails.add(v4_2);
		newDetails.add(v6_2);
		newDetails.add(v7_2);
		Member m2 = new Member();
		m2.setVipDiscountDetails(newDetails);
		
		String dateFormat = "yyyy-MM-dd";
		PropertyFormat effectiveStart = new PropertyFormat("有效起日{{id}}", "effectiveStart");
		effectiveStart.setDateFormat(dateFormat);
		PropertyFormat effectiveEnd = new PropertyFormat("有效迄日{{id}}", "effectiveEnd");
		effectiveEnd.setDateFormat(dateFormat);
		FormatList formatList = new FormatList();
		formatList.add(effectiveStart);
		formatList.add(effectiveEnd);
		
		ComplexDetailPropertyFormat format = new ComplexDetailPropertyFormat("上層名稱", "vipDiscountDetails", formatList);
		format.init(m1, m2);
		
		List<String> added = format.getAdded();
		List<String> updated = format.getUpdated();
		List<String> deleted = format.getDeleted();
		
		assertEquals(Arrays.asList("v6", "v7"), added);
		assertEquals(Arrays.asList("v2", "v3", "v4"), updated);
		assertEquals(Arrays.asList("v1", "v5"), deleted);
//		System.out.println("added:" + added);
//		System.out.println("updated:" + updated);
//		System.out.println("deleted:" + deleted);
		
		List<FormattedValue> values = format.getValues();
		values.forEach(v->{
			System.out.println("name: " + v.getName() + ", oldValue: " + v.getOldVal() + ", newValue: " + v.getNewVal());
		});
	}
}
