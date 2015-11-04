package com.angrycat.erp.onepos.vo;

import java.util.ArrayList;
import java.util.List;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_GIFT;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_ACT;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_PDIS;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.BRAND_ID;
/**
 * OnePos需要折抵(價格為負值或0)的項目，如果是固定折抵譬如禮券，價格就會直接設定負數值；如果是比例折扣就是一般打折，價格就會是0，由專櫃手動計算在銷售項目上填入負值
 * @author JerryLin
 *
 */
public class OnePosDiscountItem {
	private String id;
	private String name;
	private String categoryId = CAT_GIFT;
	private String barCode;
	private String scannableBarCode;
	private String price;
	private String brandId = BRAND_ID;
	private boolean fixed; // 是否為固定金額折抵項目，若否，則為按比例也就一般說的打折。打折的情況價格應該為0，因為沒有固定金額，只能讓專櫃人員計算後帶入負值
	
	public OnePosDiscountItem(String id, String name, String categoryId, String price){
		this.id = id;
		this.name = name;
		this.categoryId = categoryId;
		this.price = price;
		
		this.barCode = id;
		this.scannableBarCode = "*" + id +"*";
	}
	public OnePosDiscountItem(String id, String name, String categoryId, String price, boolean fixed){
		this(id, name, categoryId, price);
		this.fixed = fixed;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public String getScannableBarCode() {
		return scannableBarCode;
	}
	public void setScannableBarCode(String scannableBarCode) {
		this.scannableBarCode = scannableBarCode;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public boolean isFixed() {
		return fixed;
	}
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	public static List<OnePosDiscountItem> getDefaultItems(){
		List<OnePosDiscountItem> items = new ArrayList<>();
		// 固定折抵金額--禮券
		items.add(new OnePosDiscountItem("CITIBANK100",			"花旗禮卷100元",			CAT_GIFT, 	"-100",		true));
		items.add(new OnePosDiscountItem("CTBC100",				"中信託禮卷100元", 		CAT_GIFT, 	"-100", 	true));
		items.add(new OnePosDiscountItem("ESLITE100",			"誠品禮卷100元", 			CAT_GIFT, 	"-100",		true));
		items.add(new OnePosDiscountItem("ESLITE150",			"誠品禮卷150元", 			CAT_GIFT, 	"-150",		true));
		items.add(new OnePosDiscountItem("ESLITE200", 			"誠品禮卷200元",			CAT_GIFT, 	"-200", 	true));
		items.add(new OnePosDiscountItem("ESLITE250", 			"誠品禮卷250元",			CAT_GIFT, 	"-250", 	true));
		items.add(new OnePosDiscountItem("ESLITE500", 			"誠品禮卷500元", 			CAT_GIFT, 	"-500", 	true));
		items.add(new OnePosDiscountItem("OHM200",				"OHM禮卷200元", 			CAT_GIFT, 	"-200", 	true));
		items.add(new OnePosDiscountItem("OHM300",				"OHM禮卷300元", 			CAT_GIFT, 	"-300", 	true));
		
		// 固定折抵金額--活動
		items.add(new OnePosDiscountItem("20151031WHTPREMIUM",	"10/31前買五墜送一手鍊", 	CAT_ACT,	"-2200",	true)); // 20151031表示是這個日期以前的活動，WHT是要贈送的手鍊型號前面開頭碼，PREMIUM代表贈品
		items.add(new OnePosDiscountItem("20151111WHB088DIS",	"第二波周年慶折扣款墜子", 	CAT_ACT,	"-289",		true)); // 20151111表示活動結束時間，WHB088是折扣產品的型號，DIS代表折扣
		
		// 浮動/比例折扣金額--折扣
		items.add(new OnePosDiscountItem("MEMBERVIPBIRTH001",	"會員VIP生日折扣", 		CAT_PDIS, 	"0", 		false));
		return items;
	}
}
