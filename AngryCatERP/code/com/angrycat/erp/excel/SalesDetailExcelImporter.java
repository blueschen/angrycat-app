package com.angrycat.erp.excel;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.excel.ExcelColumn.SalesDetail.EsliteDunnan;
import com.angrycat.erp.excel.ExcelColumn.SalesDetail.Fb;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.SalesDetail;

@Component
@Scope("prototype")
public class SalesDetailExcelImporter extends ExcelImporter {
	
	private static final List<Integer> DEFAULT_SHEET_RANGE = Arrays.asList(0, 1);
	
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
		String idNo				= null;
		String discountType 	= null;
		String arrivalStatus	= null;
		Date shippingDate		= null;
		String sendMethod		= null;
		String note				= null;
		
		Date payDate			= null;
		String contactInfo		= null;
		String registrant		= null;
		
		if(sheetIdx == 0){
			salePoint		= SalesDetail.SALE_POINT_FB;
			saleStatus		= parseStrVal(row, 			Fb.狀態);
			fbName			= parseStrVal(row, 			Fb.FB名稱);
			activity		= parseStrVal(row, 			Fb.活動);
			modelId			= parseStrVal(row, 			Fb.型號);
			productName		= parseStrVal(row, 			Fb.產品名稱);
			price			= parseNumericOrEmpty(row, 	Fb.含運價格);
			memberPrice		= parseNumericOrEmpty(row, 	Fb.會員價);
			priority		= parseNumericOrStr(row,	Fb.順序);
			orderDate		= parseSqlDateVal(row, 		Fb.接單日);
			otherNote		= parseStrVal(row, 			Fb.其他備註);
			checkBillStatus = parseStrVal(row, 			Fb.對帳狀態);
			idNo			= parseStrVal(row, 			Fb.身分證字號);
			discountType	= parseBooleanVal(row, 		Fb.折扣類型) ? "會員九折" : null;
			arrivalStatus 	= parseBooleanVal(row, 		Fb.是否已到貨) ? "已到貨" : null;
			shippingDate  	= parseSqlDateVal(row, 		Fb.出貨日);
			sendMethod 		= parseStrOrDate(row, 		Fb.郵寄方式);
			note 			= parseStrVal(row, 			Fb.備註);
		}else{
			salePoint		= SalesDetail.SALE_POINT_ESLITE_DUNNAN;
			saleStatus		= parseStrVal(row, 		EsliteDunnan.狀態);
			fbName			= parseStrVal(row, 		EsliteDunnan.FB名稱);
			orderDate		= parseSqlDateVal(row, 	EsliteDunnan.銷售日期);
			modelId			= parseStrVal(row, 		EsliteDunnan.型號);
			productName		= parseStrVal(row, 		EsliteDunnan.產品名稱);
			price			= parseNumericOrEmpty(row, 	EsliteDunnan.定價);
			memberPrice		= parseNumericOrEmpty(row, 	EsliteDunnan.會員價);
			payDate			= parseSqlDateVal(row, 	EsliteDunnan.付款日期);
			idNo			= parseStrVal(row, 		EsliteDunnan.身分證字號);
			discountType 	= parseStrVal(row, 		EsliteDunnan.折扣類型);
			note 			= parseStrVal(row, 		EsliteDunnan.備註);
			shippingDate  	= parseSqlDateVal(row, 	EsliteDunnan.出貨日);
			contactInfo 	= parseStrVal(row, 		EsliteDunnan.聯絡方式);
			registrant		= parseStrVal(row, 		EsliteDunnan.登單者);
		}
		
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
		salesDetail.setIdNo(idNo);
		salesDetail.setDiscountType(discountType);
		salesDetail.setArrivalStatus(arrivalStatus);
		salesDetail.setShippingDate(shippingDate);
		salesDetail.setSendMethod(sendMethod);
		salesDetail.setNote(note);
		salesDetail.setPayDate(payDate);
		salesDetail.setContactInfo(contactInfo);
		salesDetail.setRegistrant(registrant);

		s.save(salesDetail);
		return true;
	}
	
	@Override
	protected List<Integer> sheetRange(){
		return DEFAULT_SHEET_RANGE;
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
		Map<String, String> msg = null;
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			SalesDetailExcelImporter i = acac.getBean(SalesDetailExcelImporter.class);
			msg = i.readAndPersist("E:\\angrycat_workitem\\銷售明細\\2016_01_22_from_ifly\\OHM 201601銷售明細.xlsx");
		}finally{
			if(msg != null && !msg.isEmpty()){
				msg.forEach((k,v)->{
					System.out.println("k: " + k + ", v: " + v);
				});
			}
		}		
	}
	
	public static void main(String[]args){
		testReadAndPersist();
	}
}
