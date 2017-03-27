package com.angrycat.erp.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import org.junit.Test;

import com.angrycat.erp.BaseTestCase;
import com.angrycat.erp.service.TimeService;

public class AmericanGroupBuyTest extends BaseTestCase {
	public static final String NO201703 = "AGB201703";
	public static AmericanGroupBuy new201703Instance(){
		AmericanGroupBuy agb = new AmericanGroupBuy();
		agb.setActivity("2017-03美國團");
		agb.setNo("AGB201703");
		agb.setDeadline(Date.valueOf(LocalDate.of(2017, 3, 31)));
		agb.setQualifyTotalAmtThreshold(new BigDecimal("125"));
		agb.setWaitTotalAmtThreshold(new BigDecimal("80"));
		agb.setGiftValAmtUSD(new BigDecimal("65"));
		agb.setMultiplier(new BigDecimal("1.1"));
		agb.setRate(new BigDecimal("33"));
		agb.setServiceChargeNTD(new BigDecimal("800"));
		return agb;
	}
	// 如果要調整資料庫主機位置，在BaseTestCase做變更
	@Test
	public void insert(){
		executor.executeTransaction(s->{
			String no = NO201703;
			AmericanGroupBuy agb = (AmericanGroupBuy)s.createQuery("FROM " + AmericanGroupBuy.class.getName() + " a WHERE a.no = :no").setString("no", no).uniqueResult();
			if(agb == null){
				agb = new201703Instance();
				s.save(agb);
				s.flush();
			}
		});
	}
	// 如果要調整資料庫主機位置，在BaseTestCase做變更
	@Test
	public void insertSalesNoGenerator(){
		executor.executeTransaction(s->{
			DefaultSerial ds = (DefaultSerial)s.get(DefaultSerial.class, AmericanGroupBuyOrderForm.SALESNO_GENERATOR_ID);
			if(ds == null){
				ds = new DefaultSerial();
				ds.setId(AmericanGroupBuyOrderForm.SALESNO_GENERATOR_ID);
				ds.setNo("0000");
				ds.setResetNoTo(1);
				s.save(ds);
				s.flush();
			}
		});
	}
	@Test
	public void isOrderFormDisabled(){
		AmericanGroupBuy agb = new AmericanGroupBuy();
		System.out.println(agb.isOrderFormDisabled());
		
		agb.setDeadline(new TimeService().atStartOfToday());
		System.out.println(agb.isOrderFormDisabled());
		
		agb.setDeadline(Date.valueOf(LocalDate.of(2017, 3, 8)));
		System.out.println(agb.isOrderFormDisabled());
		
		agb.setDeadline(Date.valueOf(LocalDate.of(2017, 3, 10)));
		System.out.println(agb.isOrderFormDisabled());
	}
}
