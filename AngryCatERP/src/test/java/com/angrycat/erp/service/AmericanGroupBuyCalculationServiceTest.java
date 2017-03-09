package com.angrycat.erp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.angrycat.erp.BaseTestCase;
import com.angrycat.erp.model.AmericanGroupBuy;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;
import com.angrycat.erp.model.AmericanGroupBuyTest;
import com.angrycat.erp.service.AmericanGroupBuyCalculationService.AmericanGroupBuyCalculation;


public class AmericanGroupBuyCalculationServiceTest extends BaseTestCase {
	private static final Logger LOG = Logger.getLogger(AmericanGroupBuyCalculationServiceTest.class.getName());
	
	private AmericanGroupBuy americanGroupBuy;
	private AmericanGroupBuyCalculationService serv;
	@Before
	public void init() throws Throwable{
		super.init();
		americanGroupBuy = AmericanGroupBuyTest.new201703Instance();
		serv = new AmericanGroupBuyCalculationService();
		serv.setAmericanGroupBuy(americanGroupBuy);
	}
	public static List<AmericanGroupBuyOrderForm> incrementAmtTo(int last){
		List<AmericanGroupBuyOrderForm> l = new ArrayList<>();
		last++; 
		for(int i = 0; i < last; i++){
			AmericanGroupBuyOrderForm a = new AmericanGroupBuyOrderForm();
			a.setProductAmtUSD(new BigDecimal(i));
			l.add(a);
		}
		return l;
	}
	public static List<AmericanGroupBuyOrderForm> include(double...amts){
		List<AmericanGroupBuyOrderForm> l = new ArrayList<>();
		int size = amts.length; 
		for(int i = 0; i < size; i++){
			double amt = amts[i];
			AmericanGroupBuyOrderForm a = new AmericanGroupBuyOrderForm();
			a.setProductAmtUSD(new BigDecimal(amt));
			l.add(a);
		}
		return l;
	}
	public static List<AmericanGroupBuyOrderForm> include(String...amts){
		List<AmericanGroupBuyOrderForm> l = new ArrayList<>();
		int size = amts.length; 
		for(int i = 0; i < size; i++){
			String amt = amts[i];
			AmericanGroupBuyOrderForm a = new AmericanGroupBuyOrderForm();
			a.setProductAmtUSD(new BigDecimal(amt));
			l.add(a);
		}
		return l;
	}
	@Test
	public void calculateInt(){
		List<AmericanGroupBuyOrderForm> orders = incrementAmtTo(10);
		serv.setOrders(orders);
		AmericanGroupBuyCalculation cal = serv.calculate();
		LOG.info(ReflectionToStringBuilder.toString(cal, ToStringStyle.MULTI_LINE_STYLE));
	}
	@Test
	public void calculateDouble(){
		// 使用double初始化BigDecimal，精確度會跟著帶入
		List<AmericanGroupBuyOrderForm> orders = include(10.18, 11.2, 20, 19.07, 120.45);
		serv.setOrders(orders);
		AmericanGroupBuyCalculation cal = serv.calculate();
		LOG.info(ReflectionToStringBuilder.toString(cal, ToStringStyle.MULTI_LINE_STYLE));
	}
	@Test
	public void maxBigDecimal(){
		List<AmericanGroupBuyOrderForm> orders = include(10.18, 11.2, 20, 19.07, 120.45);
		BigDecimal max = orders.stream().map(t->t.getProductAmtUSD()).max(BigDecimal::compareTo).get();
		LOG.info(max);
	}
	@Test
	public void maxScaleAfterCalculate(){
		BigDecimal n1 = new BigDecimal("1.11");
		LOG.info("n1 scale" + n1.scale());
		BigDecimal n2 = new BigDecimal("2.567");
		LOG.info("n2 scale" + n2.scale());
		BigDecimal n3 = new BigDecimal("2.2");
		LOG.info("n3 scale" + n3.scale());
		BigDecimal n4 = new BigDecimal("0.56");
		LOG.info("n4 scale" + n4.scale());
		
		BigDecimal r = n1.add(n2).multiply(n3).divide(n4, 2, RoundingMode.HALF_UP); // 除法要指名換算的精確度，否則可能發生錯誤
		LOG.info("r" + r.toEngineeringString()+ ", r scale" + r.scale());
	}
	@Test
	public void ceilingBigDecimal(){
		BigDecimal d = new BigDecimal("0.330").setScale(2, BigDecimal.ROUND_CEILING);
		LOG.info(d.toString());
	}
	@Test
	public void calculateDiscountUSD(){
		List<AmericanGroupBuyOrderForm> orders = include("11", "1.98", "4.0", "124.94", "7.98", "0.1");
		orders.get(orders.size()-1).setSalesType("贈品"); // 設定最後一個是贈品
		serv.setOrders(orders);
		AmericanGroupBuyCalculation cal = serv.calculate();
		LOG.info(ReflectionToStringBuilder.toString(cal, ToStringStyle.MULTI_LINE_STYLE));
	}
	@Test
	public void precision(){
		BigDecimal n1 = new BigDecimal("1.23");
		LOG.info(n1.precision());
	}
	@Test
	public void scaleToLastNonZero(){
		BigDecimal d = new BigDecimal("1.2300");
		d = AmericanGroupBuyCalculationService.scaleToLastNonZero(d);
		String expected = "1.23";
		assertEquals(expected, d.toString());
		
		d = new BigDecimal("0.1112");
		d = AmericanGroupBuyCalculationService.scaleToLastNonZero(d);
		expected = "0.1112";
		assertEquals(expected, d.toString());
		
		d = new BigDecimal("12");
		d = AmericanGroupBuyCalculationService.scaleToLastNonZero(d);
		expected = "12";
		assertEquals(expected, d.toString());
		
		d = new BigDecimal("30.000");
		d = AmericanGroupBuyCalculationService.scaleToLastNonZero(d);
		expected = "30";
		assertEquals(expected, d.toString());
	}
}
