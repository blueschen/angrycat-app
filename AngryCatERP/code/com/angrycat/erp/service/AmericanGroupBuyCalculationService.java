package com.angrycat.erp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.AmericanGroupBuy;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;

@Service
@Scope("prototype")
public class AmericanGroupBuyCalculationService {
	private AmericanGroupBuy americanGroupBuy;
	private List<AmericanGroupBuyOrderForm> orders;
	
	public AmericanGroupBuy getAmericanGroupBuy() {
		return americanGroupBuy;
	}
	public AmericanGroupBuyCalculationService setAmericanGroupBuy(AmericanGroupBuy americanGroupBuy) {
		this.americanGroupBuy = americanGroupBuy;
		return this;
	}
	public List<AmericanGroupBuyOrderForm> getOrders() {
		return orders;
	}
	public AmericanGroupBuyCalculationService setOrders(List<AmericanGroupBuyOrderForm> orders) {
		this.orders = orders;
		return this;
	}

	public AmericanGroupBuyCalculation calculate(){
		int maxScale = orders.stream().map(r->r.getProductAmtUSD().scale()).max(Integer::compareTo).get();
		BigDecimal giftMadeUpDiff = BigDecimal.ZERO; // 贈品價差要補的金額
		// 一批訂單頂多有一筆是贈品
		Optional<BigDecimal> foundDiff = orders.stream().filter(r->"贈品".equals(r.getSalesType())).map(r->r.getProductAmtUSD()).findFirst();
		if(foundDiff.isPresent()){
			giftMadeUpDiff = foundDiff.get();
		}
		// 小計不包含備取
		BigDecimal subAmtUSD = orders.stream().filter(r->StringUtils.isNotBlank(r.getSalesType()) && !r.getSalesType().contains("備取")).map(r->r.getProductAmtUSD()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(maxScale, BigDecimal.ROUND_CEILING);
		BigDecimal discountUSDThreshold = americanGroupBuy.getDiscountUSDThreshold();
		// 如果小計包含贈品差價的部分，要先扣除再行比較
		BigDecimal discountUSD = subAmtUSD.subtract(giftMadeUpDiff).compareTo(discountUSDThreshold) >= 0 ? americanGroupBuy.getDiscountUSD() : BigDecimal.ZERO;
		BigDecimal multiplier = americanGroupBuy.getMultiplier();
		BigDecimal rate = americanGroupBuy.getRate();
		BigDecimal serviceChargeNTD = americanGroupBuy.getServiceChargeNTD();
		
		BigDecimal totalAmtNTD = subAmtUSD.subtract(discountUSD).multiply(multiplier).multiply(rate).add(serviceChargeNTD).setScale(0, BigDecimal.ROUND_CEILING);
		
		AmericanGroupBuyCalculation c = new AmericanGroupBuyCalculation();
		c.subAmtUSD           = subAmtUSD;
		c.discountUSDThreshold= discountUSDThreshold;
		c.discountUSD         = discountUSD;
		c.multiplier          = multiplier;
		c.rate                = rate;
		c.serviceChargeNTD    = serviceChargeNTD;
		c.totalAmtNTD         = totalAmtNTD;
		return c;
	}
	/**
	 * 計算結果，所有數字如有小數，都取到最後一位非零數值，譬如: 1.2000 => 1.2 或者 30.000 => 30
	 * @return
	 */
	public AmericanGroupBuyCalculation calculateScaleToLastNonZero(){
		AmericanGroupBuyCalculation c = calculate();
		c.subAmtUSD           = scaleToLastNonZero(c.subAmtUSD);
		c.discountUSDThreshold= scaleToLastNonZero(c.discountUSDThreshold);
		c.discountUSD         = scaleToLastNonZero(c.discountUSD);
		c.multiplier          = scaleToLastNonZero(c.multiplier);
		c.rate                = scaleToLastNonZero(c.rate);
		c.serviceChargeNTD    = scaleToLastNonZero(c.serviceChargeNTD);
		c.totalAmtNTD         = scaleToLastNonZero(c.totalAmtNTD);
		return c;
	}
	public static BigDecimal scaleToLastNonZero(BigDecimal input){
		String n = input.toString();
		int scale = input.scale();
		if(scale == 0){
			return input.setScale(0);
		}
		int lastNonZero = scale;
		int len = n.length();
		for(int i = 0; i < scale; i++){
			String d = n.substring(len-1, len);
			if(Integer.parseInt(d) > 0){
				lastNonZero = lastNonZero - i;
				break;
			}
			len--;
		}
		len = n.length();
		if(lastNonZero == scale && Integer.parseInt(n.substring(len-1, len)) == 0){
			lastNonZero = 0;
		}
		BigDecimal scaled = input.setScale(lastNonZero);
		return scaled;
	}
	public static class AmericanGroupBuyCalculation {
		BigDecimal subAmtUSD;
		BigDecimal discountUSDThreshold;
		BigDecimal discountUSD;
		BigDecimal multiplier;
		BigDecimal rate;
		BigDecimal serviceChargeNTD;
		BigDecimal totalAmtNTD;
		public BigDecimal getSubAmtUSD() {
			return subAmtUSD;
		}
		public BigDecimal getDiscountUSDThreshold() {
			return discountUSDThreshold;
		}
		public BigDecimal getDiscountUSD() {
			return discountUSD;
		}
		public BigDecimal getMultiplier() {
			return multiplier;
		}
		public BigDecimal getRate() {
			return rate;
		}
		public BigDecimal getServiceChargeNTD() {
			return serviceChargeNTD;
		}
		public BigDecimal getTotalAmtNTD() {
			return totalAmtNTD;
		}
	}
}
