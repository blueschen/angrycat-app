package com.angrycat.erp.excel;

import static com.angrycat.erp.common.XSSFUtil.parseCellNumericOrStr;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.excel.ExcelColumn.SalesDetail.Fb;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.SalesDetail;

@Component
@Scope("prototype")
public class SalesDetailExcelImporter extends ExcelImporter {
	
	private static final List<Integer> DEFAULT_SHEET_RANGE = Arrays.asList(0, 1);
	private static final Pattern IDNO = Pattern.compile("([a-z]|[A-Z]){1}[0-9]{9}");
	
	private String fileName;
	
	public void setFileName(String fileName){this.fileName = fileName;}
	
	@Override
	protected boolean processRow(Row row, Session s, int sheetIdx, int readableRowNum, Map<String, Integer> msg){
		String salePoint		= null;
		String saleStatus		= null;
		String fbName			= null;
		String activity			= null;
		String modelId			= null;
		String productName 		= null;
		double price			= 0;
		double memberPrice  	= 0;
		String priority			= null;
		Date orderDate			= null;
		String otherNote		= null;
		String checkBillStatus	= null;
		String mobile			= null;
		String idNo				= null;
		String discountType 	= null;
		String arrivalStatus	= null;
		Date shippingDate		= null;
		String sendMethod		= null;
		String note				= null;
		
		Date payDate			= null;
		String contactInfo		= null;
		String registrant		= null;
		// "郵寄地址電話"不用匯入
		// 價格要以會員價為主，會員價即是實收價
		// 若只有一個價格，名稱雖然不叫會員價，程式應當把他歸為會員價
		if(sheetIdx == 1){
			salePoint		= SalesDetail.SALE_POINT_FB;
			saleStatus		= parseStrVal(row, 			getColumnIdxFromTitle("狀態"));
			fbName			= parseStrVal(row, 			getColumnIdxFromTitle("FB名稱"));
			activity		= parseStrVal(row, 			getColumnIdxFromTitle("社團/fanpage"));
			modelId			= parseStrVal(row, 			getColumnIdxFromTitle("型號"));
			productName		= parseStrVal(row, 			getColumnIdxFromTitle("明細"));
			price			= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("含運金額"));
			memberPrice		= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("會員價(實收價格)"));
			priority		= parseNumericOrStr(row,	getColumnIdxFromTitle("順序"));
			orderDate		= parseSqlDateVal(row, 		getColumnIdxFromTitle("接單日"));
			otherNote		= parseStrVal(row, 			getColumnIdxFromTitle("其他備註"));
			checkBillStatus = parseStrVal(row, 			getColumnIdxFromTitle("對帳狀態"));
			mobile			= parseStrVal(row, 			getColumnIdxFromTitle("手機號"));
			idNo			= parseStrVal(row, 			getColumnIdxFromTitle("身份證字號"));
			discountType	= parseBooleanVal(row, 		getColumnIdxFromTitle("會員九折")) ? "會員九折" : parseStrVal(row, getColumnIdxFromTitle("會員九折"));
//			arrivalStatus 	= parseBooleanVal(row, 		getColumnIdxFromTitle("已到貨")) ? "v" : null;
			shippingDate  	= parseSqlDateVal(row, 		getColumnIdxFromTitle("出貨日"));
			sendMethod 		= parseStrOrDate(row, 		getColumnIdxFromTitle("郵寄方式"));
			note 			= parseStrVal(row, 			getColumnIdxFromTitle("備註"));
		}else{
			salePoint		= SalesDetail.SALE_POINT_ESLITE_DUNNAN;
			saleStatus		= parseStrVal(row, 			getColumnIdxFromTitle("狀態"));
			fbName			= parseStrVal(row, 			getColumnIdxFromTitle("FB名稱/客人姓名"));
			orderDate		= parseSqlDateVal(row, 		getColumnIdxFromTitle("銷售日期"));
			modelId			= parseStrVal(row, 			getColumnIdxFromTitle("型號"));
			productName		= parseStrVal(row, 			getColumnIdxFromTitle("明細"));
			price			= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("定價"));
			memberPrice		= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("會員價(實收價格)"));
			payDate			= parseSqlDateVal(row, 		getColumnIdxFromTitle("付款日期"));
			mobile			= parseCellNumericOrStr(row,getColumnIdxFromTitle("手機號"));
			idNo			= parseStrVal(row, 		getColumnIdxFromTitle("身份證字號"));
			discountType 	= parseStrVal(row, 		getColumnIdxFromTitle("折扣説明"));
			note 			= parseStrVal(row, 		getColumnIdxFromTitle("備註"));
			shippingDate  	= parseSqlDateVal(row, 	getColumnIdxFromTitle("出貨日"));
			//contactInfo 	= parseStrVal(row, 		getColumnIdxFromTitle("郵寄地址電話"));
			registrant		= parseStrVal(row, 		getColumnIdxFromTitle("登單者"));
		}
		
		if(!"99. 已出貨".equals(saleStatus)){// 代表該銷售明細可能跑單、退貨...總之就是交易失敗
			return false;
		}
		
		if(StringUtils.isNotBlank(idNo) && ("N/A".equals(idNo) || "-".equals(idNo))){
			idNo = null;
		}
		
		if(StringUtils.isNotBlank(idNo) && idNo.contains("@")){
			Long count = (Long)s.createQuery("SELECT COUNT(m.id) FROM " + Member.class.getName() + " m WHERE upper(m.email) = :idNo").setString("idNo", idNo.toUpperCase()).uniqueResult();
			System.out.println("idNo seems email: " + idNo + " count: " + count);
		}
		
		mobile = adjustMobile(mobile);
		idNo = adjustIdNo(idNo);
		
		SalesDetail salesDetail = new SalesDetail();
		salesDetail.setSalePoint(salePoint);
		salesDetail.setSaleStatus(saleStatus);
		salesDetail.setFbName(fbName);
		salesDetail.setActivity(activity);
		salesDetail.setModelId(modelId);
		salesDetail.setProductName(productName);
		salesDetail.setPrice(price);
		salesDetail.setMemberPrice(memberPrice);
		salesDetail.setPriority(priority);
		salesDetail.setOrderDate(orderDate);
		salesDetail.setOtherNote(otherNote);
		salesDetail.setCheckBillStatus(checkBillStatus);
		salesDetail.setMobile(mobile);
		salesDetail.setIdNo(idNo);
		salesDetail.setDiscountType(discountType);
		salesDetail.setArrivalStatus(arrivalStatus);
		salesDetail.setShippingDate(shippingDate);
		salesDetail.setSendMethod(sendMethod);
		salesDetail.setNote(note);
		salesDetail.setPayDate(payDate);
		salesDetail.setContactInfo(contactInfo);
		salesDetail.setRegistrant(registrant);
		
		if(StringUtils.isNotBlank(idNo)){
			String idNoToUpper = idNo.toUpperCase();
			Object obj = s.createQuery("SELECT m FROM " + Member.class.getName() + " m WHERE upper(m.idNo) = :idNo").setString("idNo", idNoToUpper).uniqueResult();
			if(obj != null){
				salesDetail.setMember((Member)obj);
			}
		}
		
		if(salesDetail.getMember() != null
		&& StringUtils.isBlank(salesDetail.getMember().getId())
		&& StringUtils.isNotBlank(mobile)){
			Object obj = s.createQuery("SELECT m FROM " + Member.class.getName() + " m WHERE m.mobile = :mobile").setString("mobile", mobile).uniqueResult();
			if(obj != null){
				salesDetail.setMember((Member)obj);
			}
		}
		
		String sheetName = getWorkbook().getSheetAt(sheetIdx).getSheetName();
		sheetName = sheetName.trim();
		
		String rowId = fileName + "|" + sheetName + "|" + readableRowNum;
		salesDetail.setRowId(rowId);
		
		s.save(salesDetail);
		return true;
	}
	
	@Override
	protected List<Integer> sheetRange(){
		return DEFAULT_SHEET_RANGE;
	}
	/**
	 * 因為Excel的手機號前面的0常常被自動省略，所以盡量幫他加回來
	 * @param mobile
	 * @return
	 */
	private static String adjustMobile(String mobile){
		if(StringUtils.isBlank(mobile)){
			return null;
		}
		if(NumberUtils.isNumber(mobile) && mobile.length() == 9 && mobile.indexOf("9") == 0){
			mobile = "0" + mobile;
		}
		return mobile;
	}
	
	private static String adjustIdNo(String idNo){
		if(StringUtils.isBlank(idNo)){
			return null;
		}
		Matcher m = IDNO.matcher(idNo);
		if(m.matches()){
			return idNo.toUpperCase();
		}
		return idNo;
	}
	
	private boolean parseBooleanVal(Row row, int colIdx){
		String val = parseStrVal(row, colIdx);
		if(StringUtils.isNotBlank(val) && "V".equals(val.trim().toUpperCase())){
			return true;
		}
		return false;
	}
	/**
	 * 在應當為數值格式的欄位，出現字串資料，所以要另外判斷過濾
	 * @param row
	 * @param colIdx
	 * @return
	 */
	private double parseNumericOrEmpty(Row row, int colIdx){
		String val = parseNumericOrStr(row, colIdx);
		double d = 0;
		if(StringUtils.isNumeric(val)){
			d = Double.valueOf(val);
		}
		return d;
	}
	/**
	 * 字串和日期混雜，要另行處理
	 * @param row
	 * @param colIdx
	 * @return
	 */
	private String parseStrOrDate(Row row, int colIdx){
		String val = parseNumericOrStr(row, colIdx);
		String result = null;
		if(StringUtils.isNotBlank(val)){
			if(StringUtils.isNumeric(val)){
				Cell cell = row.getCell(colIdx);
				java.util.Date date = cell.getDateCellValue();
				try{
					DateFormat df = DatetimeUtil.DF_yyyyMMdd_DASHED;
					result = df.format(date);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}else{
				result = val;
			}
		}
		return result;
	}
	private static void testRead(){
		int[] sheets = new int[]{0};
		int[] cols = new int[]{Fb.郵寄方式};
		read("E:\\angrycat_workitem\\銷售明細\\2016_01_22_from_ifly\\OHM 201601銷售明細_test.xlsx", sheets, cols);
		
	}
	
	private static void testReadAndPersist(){
		String fileName = "201603_OHM銷售明細-";
		String src = "E:\\angrycat_workitem\\銷售明細\\2016_04_08_from_miko\\"+fileName+".xlsx";
		Map<String, String> msg = null;
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			SalesDetailExcelImporter i = acac.getBean(SalesDetailExcelImporter.class);
			i.setFileName(fileName); // for rowId use
			msg = i.readAndPersist(src);
		}finally{
			if(msg != null && !msg.isEmpty()){
				msg.forEach((k,v)->{
					System.out.println("k: " + k + ", v: " + v);
				});
			}
		}		
	}
	
	private static void testAdjustMobile(){
		String t = "981189186";
		String m = adjustMobile(t);
		System.out.println(m);
	}
	
	private static void testAdjustIdNo(){
		String i1 = "p122879009";
		String i2 = "f009334123";
		String i3 = "  ";
		System.out.println(adjustIdNo(i1) + "|" + adjustIdNo(i2) + "|" + adjustIdNo(i3));
	}
	
	public static void main(String[]args){
		testReadAndPersist();
//		testAdjustMobile();
//		testAdjustIdNo();
	}
}
