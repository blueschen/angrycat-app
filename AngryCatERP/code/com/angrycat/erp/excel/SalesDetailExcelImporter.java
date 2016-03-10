package com.angrycat.erp.excel;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.SalesDetail;

@Component
@Scope("prototype")
public class SalesDetailExcelImporter extends ExcelImporter {
	
	private static final List<Integer> DEFAULT_SHEET_RANGE = Arrays.asList(0);
	
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
		if(sheetIdx == 0){
			salePoint		= SalesDetail.SALE_POINT_FB;
			saleStatus		= parseStrVal(row, 			getColumnIdxFromTitle("狀態"));
			fbName			= parseStrVal(row, 			getColumnIdxFromTitle("FB名稱"));
			activity		= parseStrVal(row, 			getColumnIdxFromTitle("代購/團購"));
			productName		= parseStrVal(row, 			getColumnIdxFromTitle("明細"));
			modelId			= parseStrVal(row, 			getColumnIdxFromTitle("型號"));
			price			= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("含運金額"));
			memberPrice		= parseNumericOrEmpty(row, 	getColumnIdxFromTitle("會員價格"));
			priority		= parseNumericOrStr(row,	getColumnIdxFromTitle("順序"));
			orderDate		= parseSqlDateVal(row, 		getColumnIdxFromTitle("接單日"));
			//otherNote		= parseStrVal(row, 			Fb.其他備註);
			checkBillStatus = parseStrVal(row, 			getColumnIdxFromTitle("對帳狀態"));
			idNo			= parseStrVal(row, 			getColumnIdxFromTitle("身份證字號"));
			discountType	= parseBooleanVal(row, 		getColumnIdxFromTitle("會員九折")) ? "會員九折" : null;
			arrivalStatus 	= parseBooleanVal(row, 		getColumnIdxFromTitle("已到貨")) ? "v" : null;
			shippingDate  	= parseSqlDateVal(row, 		getColumnIdxFromTitle("出貨日"));
			sendMethod 		= parseStrOrDate(row, 		getColumnIdxFromTitle("郵寄方式"));
			note 			= parseStrVal(row, 			getColumnIdxFromTitle("備註"));
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
		
		if(!"99. 已出貨".equals(saleStatus)){// 代表該銷售明細可能跑單、退貨...總之就是交易失敗
			return false;
		}
		
		if(StringUtils.isNotBlank(idNo) && "N/A".equals(idNo)){
			idNo = null;
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
		
		if(StringUtils.isNotBlank(idNo)){
			Object obj = s.createQuery("SELECT m.id FROM " + Member.class.getName() + " m WHERE m.idNo = :idNo").setString("idNo", idNo).uniqueResult();
			if(obj != null){
				salesDetail.setMemberId((String)obj);
			}
		}
		
		String sheetName = getWorkbook().getSheetAt(sheetIdx).getSheetName();
		sheetName = sheetName.trim();
		
		String rowId = fileName + "_" + sheetName + "_" + readableRowNum;
		salesDetail.setRowId(rowId);
		
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
		//String src1 = "E:\\angrycat_workitem\\銷售明細\\2016_01_22_from_ifly\\OHM 201601銷售明細.xlsx";
		String src2 = "E:\\angrycat_workitem\\銷售明細\\2016_03_08_from_miko\\201504_OHM銷售明細-.xlsx";
		Map<String, String> msg = null;
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			SalesDetailExcelImporter i = acac.getBean(SalesDetailExcelImporter.class);
			i.setFileName("201504_OHM銷售明細-"); // for rowId use
			msg = i.readAndPersist(src2);
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
