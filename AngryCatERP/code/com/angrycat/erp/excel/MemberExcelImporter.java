package com.angrycat.erp.excel;

import static com.angrycat.erp.excel.ExcelColumn.Member.Facebook_姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.Ohmliy_VIP;
import static com.angrycat.erp.excel.ExcelColumn.Member.VIP延續;
import static com.angrycat.erp.excel.ExcelColumn.Member.備註;
import static com.angrycat.erp.excel.ExcelColumn.Member.出生年月日;
import static com.angrycat.erp.excel.ExcelColumn.Member.國家代碼;
import static com.angrycat.erp.excel.ExcelColumn.Member.地址;
import static com.angrycat.erp.excel.ExcelColumn.Member.室內電話;
import static com.angrycat.erp.excel.ExcelColumn.Member.性別;
import static com.angrycat.erp.excel.ExcelColumn.Member.手機電話;
import static com.angrycat.erp.excel.ExcelColumn.Member.生日使用8折優惠;
import static com.angrycat.erp.excel.ExcelColumn.Member.真實姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.身份證字號;
import static com.angrycat.erp.excel.ExcelColumn.Member.轉VIP日期;
import static com.angrycat.erp.excel.ExcelColumn.Member.郵遞區號;
import static com.angrycat.erp.excel.ExcelColumn.Member.電子信箱;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.web.controller.MemberController;

@Component
@Scope("prototype")
public class MemberExcelImporter extends ExcelImporter {
	private static final String NAME_NOT_EXISTED = "姓名不存在";
	private static final String MOBILE_DUPLICATE = "姓名和行動電話已重複";
	private static final String TEL_DUPLICATE = "姓名和室內電話已重複";
	private static final String COUNTRY_CODE_FORMAT_NOT_CORRECT = "國碼應為兩碼大寫英文字母";
	private static final List<String> MSG_KEYS = Arrays.asList(
		NAME_NOT_EXISTED, 
		MOBILE_DUPLICATE, 
		TEL_DUPLICATE, 
		COUNTRY_CODE_FORMAT_NOT_CORRECT);
	
	private static final int VIP_MAX_YEAR = 2;
	@Autowired
	private MemberVipDiscount discount;
	
	@Override
	protected boolean processRow(Row row, Session s, int sheetIdx, int readableRowNum, Map<String, Integer> msg){
		String VIP			= parseStrVal(row, Ohmliy_VIP);
		Date vipUsed		= parseSqlDateVal(row, 生日使用8折優惠);
		String fbNickname	= parseStrVal(row, Facebook_姓名);
		String name			= parseStrVal(row, 真實姓名);
		String gender		= parseStrVal(row, 性別);
		String idNo			= parseStrVal(row, 身份證字號);
		Date birthday		= parseSqlDateVal(row, 出生年月日);
		String email		= parseStrVal(row, 電子信箱);
		String mobile		= parseNumericOrStr(row, 手機電話);
		String tel			= parseNumericOrStr(row, 室內電話);
		String postalCode	= parseNumericOrStr(row, 郵遞區號);
		String address		= parseStrVal(row, 地址);
		Date toVipDate		= parseSqlDateVal(row, 轉VIP日期);
		String note			= parseStrVal(row, 備註);
		String vipYear		= parseNumericOrStr(row, VIP延續);
		String countryCode	= parseStrVal(row, 國家代碼);
		String clientId		= null;
		
		if(StringUtils.isNotBlank(countryCode) && !Pattern.matches("[A-Z]{2}", countryCode)){
			msg.put("國碼應為兩碼大寫英文字母"+readableRowNum, readableRowNum);
			return false;
		}else{
			if(StringUtils.isBlank(countryCode)){
				countryCode = "TW";
			}
			clientId = MemberController.genNextClientId(s, countryCode);
		}
		if(StringUtils.isBlank(name)){
			msg.put("姓名不存在"+readableRowNum, readableRowNum);
			return false;
		}
		if(StringUtils.isBlank(mobile) && StringUtils.isBlank(tel)){
			tel = "00000";
		}				
		
		if(StringUtils.isNotBlank(mobile)){
			Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.name = :name AND m.mobile = :mobile").setString("name", name).setString("mobile", mobile).uniqueResult();
			int count = num.intValue();
			if(count > 0){
				msg.put("姓名和行動電話已重複"+readableRowNum, readableRowNum);
				return false;
			}
		}
		if(StringUtils.isNotBlank(tel)){
			Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.name = :name AND m.tel = :tel").setString("name", name).setString("tel", tel).uniqueResult();
			int count = num.intValue();
			if(count > 0){
				msg.put("姓名和室內電話已重複"+readableRowNum, readableRowNum);
				return false;
			}
		}
		
		Member m = new Member();
		if(StringUtils.isNotBlank(VIP)){
			m.setImportant("VIP".equals(VIP) || "R-VIP".equals(VIP) || VIP.contains("bloger"));
		}
		m.setFbNickname(fbNickname);
		m.setName(name);
		m.setGender("男".equals(gender) ? Member.GENDER_MALE : Member.GENDER_FEMALE);
		if(StringUtils.isNoneBlank(idNo)){
			idNo = idNo.toUpperCase();
			m.setIdNo(idNo);
		}
		m.setBirthday(birthday);
		m.setEmail(email);
		m.setMobile(mobile);
		m.setTel(tel);
		m.setPostalCode(postalCode);
		m.setAddress(address);
		m.setToVipDate(toVipDate);
		m.setNote(note);
		m.setClientId(clientId);
						
		s.save(m);
		
		if(m.getBirthday()!=null && m.getToVipDate()!=null){
			int vipEffectiveYearCount = 0;
			if(StringUtils.isNumeric(vipYear) || StringUtils.isBlank(vipYear)){
				vipEffectiveYearCount = 1;
			}else{
				vipEffectiveYearCount = Integer.parseInt(vipYear);
				if(vipEffectiveYearCount > VIP_MAX_YEAR){
					vipEffectiveYearCount = VIP_MAX_YEAR;
				}
			}
			discount.setBatchStartDate(m.getToVipDate());
			discount.setAddCount(vipEffectiveYearCount);
			discount.applyRule(m);
			m.setImportant(true);
			if(vipUsed != null && m.getVipDiscountDetails().size() > 0){
				m.getVipDiscountDetails().get(0).setDiscountUseDate(vipUsed);
			}
		}
		s.save(m);
		return true;
	}
	
	@Override
	protected List<String> msgKeys(){
		return MSG_KEYS;
	}
}
