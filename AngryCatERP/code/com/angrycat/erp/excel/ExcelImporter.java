package com.angrycat.erp.excel;

import static com.angrycat.erp.excel.ExcelColumn.Member.Facebook_姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.Ohmliy_VIP;
import static com.angrycat.erp.excel.ExcelColumn.Member.VIP延續;
import static com.angrycat.erp.excel.ExcelColumn.Member.備註;
import static com.angrycat.erp.excel.ExcelColumn.Member.出生年月日;
import static com.angrycat.erp.excel.ExcelColumn.Member.地址;
import static com.angrycat.erp.excel.ExcelColumn.Member.性別;
import static com.angrycat.erp.excel.ExcelColumn.Member.真實姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.室內電話;
import static com.angrycat.erp.excel.ExcelColumn.Member.手機電話;
import static com.angrycat.erp.excel.ExcelColumn.Member.身份證字號;
import static com.angrycat.erp.excel.ExcelColumn.Member.轉VIP日期;
import static com.angrycat.erp.excel.ExcelColumn.Member.郵遞區號;
import static com.angrycat.erp.excel.ExcelColumn.Member.電子信箱;
import static com.angrycat.erp.excel.ExcelColumn.Member.生日使用8折優惠;
import static com.angrycat.erp.excel.ExcelColumn.Member.國家代碼;
import static com.angrycat.erp.excel.ExcelColumn.Member.COLUMN_COUNT;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.log.DataChangeLogger;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.web.controller.MemberController;


@Component
@Scope("prototype")
public class ExcelImporter {
	@Autowired
	@Qualifier("sessionFactoryWrapper")
	private SessionFactoryWrapper sfw;
	@Autowired
	private MemberVipDiscount discount;
	private int colNum = 0;
	
		
	public static void main(String[]args){
//		readAndWrite("C:\\angrycat_workitem\\OHM Beads TW (AngryCat) 一般會員資料.xlsx", "C:\\angrycat_workitem\\test.xlsx");
//		read("E:\\angrycat_workitem\\member\\2015_10_05\\OHM Beads TW (AngryCat) 一般會員資料_update.xlsx", 0, 0);
		testPattern();
	}

	private static void testPattern(){
		String pattern = "[A-Z]{2}";

		String t1 = "TW";
		String t2 = "TW ";
		String t3 = "";
		String t4 = "123";
		String t5 = "1TW";
		
		System.out.println(Pattern.matches(pattern, t1));
		System.out.println(Pattern.matches(pattern, t2));
		System.out.println(Pattern.matches(pattern, t3));
		System.out.println(Pattern.matches(pattern, t4));
		System.out.println(Pattern.matches(pattern, t5));
	}
	
	private static void isFound(String pattern, String input){
		Pattern pat = Pattern.compile(pattern);
		Matcher m = pat.matcher(input);
		System.out.println(m.find());
	}
	
	private static void readAndWrite(String src, String dest){
		
		try(FileOutputStream fos = new FileOutputStream(dest);){
			
			InputStream is = new FileInputStream(src);			
			Workbook wb = WorkbookFactory.create(is);
			wb.write(fos);
			
			System.out.println("successfully read from: " + src);
			System.out.println("successfully write to: " + dest);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("executing finally...");
		}
	}
	
	/**
	 * 顯示資料源的欄位值，必須指定檔案位置、第幾頁、第幾欄
	 * @param src
	 * @param sheetIdx
	 * @param colIdx
	 */
	private static void read(String src, int sheetIdx, int colIdx){
		try(InputStream is = new FileInputStream(src);
			XSSFWorkbook wb = new XSSFWorkbook(is);){
			
			Sheet sheet = wb.getSheetAt(sheetIdx);
			final DataFormatter df = new DataFormatter();
			sheet.forEach(row->{
				Cell cell = row.getCell(colIdx);
				System.out.println("cell: " + cell);
				System.out.println("cell type: " + cell.getCellType());
				System.out.println("cell value: " + df.formatCellValue(cell));
			});
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("executing finally...");
		}
		
		
		
	}
	
	public Map<String, String> persist(byte[] data){
		return persist(data, null);
	}
	
	public Map<String, String> persist(byte[] data, DataChangeLogger dataChangeLogger){
		
		int totalCount = 0;
		
		String NAME_NOT_EXISTED = "nameNoNotExisted";
		String MOBILE_OR_TEL_REQUIRED = "mobileOrTelRequired";
		String MOBILE_DUPLICATE = "mobileDuplicate";
		String TEL_DUPLICATE = "telDuplicate";
		String CLIENT_ID_DUPLICATE = "clientIdDuplicate";
		String COUNTRY_CODE_FORMAT_NOT_CORRECT = "countryCodeFormatNotCorrect";
		int VIP_MAX_YEAR = 2;
		
		Map<String, Integer> msg = new LinkedHashMap<>();
		Map<String, String> logWarn = new HashMap<>();
		Session s = null;
		Transaction tx = null;
		int rowNum = 0;
		int readableRowNum = 0;
		int insertCount = 0;
		final String DEFAULT_COUNTRY_CODE = "TW";
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);){
			
			Workbook wb = WorkbookFactory.create(bais);
			Sheet sheet = wb.getSheetAt(0);
			totalCount = sheet.getLastRowNum();			
			Iterator<Row> itr = sheet.iterator();
			
			s = sfw.openSession();
			tx = s.beginTransaction();
			
			int batchSize = sfw.getBatchSize();
			String DEFAULT_TEL = "00000";
			
			while(itr.hasNext()){
				Row row = itr.next();
				rowNum = row.getRowNum();
				readableRowNum = rowNum+1;
				if(rowNum == 0 || isRowEmpty(row, COLUMN_COUNT)){
					continue;
				}
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
					msg.put(COUNTRY_CODE_FORMAT_NOT_CORRECT+readableRowNum, readableRowNum);
					continue;
				}else{
					if(StringUtils.isBlank(countryCode)){
						countryCode = DEFAULT_COUNTRY_CODE;
					}
					clientId = MemberController.genNextClientId(s, countryCode);
				}
				if(StringUtils.isBlank(name)){
					msg.put(NAME_NOT_EXISTED+readableRowNum, readableRowNum);
					continue;
				}
				if(StringUtils.isBlank(mobile) && StringUtils.isBlank(tel)){
					tel = DEFAULT_TEL;
				}				
				
				if(StringUtils.isNotBlank(mobile)){
					Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.name = :name AND m.mobile = :mobile").setString("name", name).setString("mobile", mobile).uniqueResult();
					int count = num.intValue();
					if(count > 0){
						msg.put(MOBILE_DUPLICATE+readableRowNum, readableRowNum);
						continue;
					}
				}
				if(StringUtils.isNotBlank(tel)){
					Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.name = :name AND m.tel = :tel").setString("name", name).setString("tel", tel).uniqueResult();
					int count = num.intValue();
					if(count > 0){
						msg.put(TEL_DUPLICATE+readableRowNum, readableRowNum);
						continue;
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
//				if(dataChangeLogger != null){
//					dataChangeLogger.logAdd(m, s);
//				}
				
				if(++insertCount % batchSize == 0){
					s.flush();
					s.clear();
				}
			}
		}catch(Throwable e){
			String stackTrace = ExceptionUtils.getStackTrace(e);
			logWarn.put("errorMsg", "程式執行到第"+readableRowNum+"行第"+(colNum+1)+"列發生錯誤\n"+stackTrace);
			System.out.println(stackTrace);
		}finally{
			if(!logWarn.isEmpty() || !msg.isEmpty()){
				tx.rollback();
			}else{
				tx.commit();
			}
			s.close();
		}
		if(!logWarn.isEmpty()){
			return logWarn;
		}
		
		String infoTotalCount = "總筆數: " + totalCount;
		String infoImportCount = "實際匯入筆數: " + insertCount;
		
		System.out.println(infoTotalCount);
		System.out.println(infoImportCount);
		
		StringBuffer warning = new StringBuffer();
		warning = genWarnMsg(msg, warning, NAME_NOT_EXISTED, "姓名不存在");
		warning = genWarnMsg(msg, warning, MOBILE_OR_TEL_REQUIRED, "手機和室內電話至少要提供一項");
		warning = genWarnMsg(msg, warning, MOBILE_DUPLICATE, "姓名和行動電話已重複");
		warning = genWarnMsg(msg, warning, TEL_DUPLICATE, "姓名和室內電話已重複");
		warning = genWarnMsg(msg, warning, CLIENT_ID_DUPLICATE, "客戶編號已重複");
		warning = genWarnMsg(msg, warning, COUNTRY_CODE_FORMAT_NOT_CORRECT, "國碼應為兩碼大寫英文字母");
		
		String infoMsg = infoTotalCount + "\n" + infoImportCount;
		if(StringUtils.isNotBlank(warning.toString())){
			logWarn.put("warnMsg", warning.toString());
		}else{
			logWarn.put("infoMsg", infoMsg);
		}
		return logWarn;
	}
	
	private static boolean isRowEmpty(Row row, int colCount){
		final DataFormatter df = new DataFormatter();
		boolean empty = true;
		for(int i = 0; i < colCount; i++){
			Cell cell = row.getCell(i);
			if(cell != null){
				String val = df.formatCellValue(cell);
				if(StringUtils.isNotBlank(val)){
					empty = false;
					break;
				}	
			}
		}
		return empty;
	}
	
	private StringBuffer genWarnMsg(Map<String, Integer> msg, StringBuffer warnMsg, String msgKey, String msgTitle){
		List<Integer> warnNums = findMsgRowNums(msg, msgKey);
		String warn = "";
		if(!warnNums.isEmpty()){
			warn = msgTitle+"共"+warnNums.size()+"筆\n行數:" + StringUtils.join(warnNums, "、");
			System.out.println(warn);
			warnMsg.append(warn + "\n");
		}
		return warnMsg;
	}
	
	String getUniqueKeyField(){
		return "idNo";
	}
	
	private List<Integer> findMsgRowNums(Map<String, Integer> msg, String hint){
		List<Integer> results = 
		msg
			.keySet()
			.stream()
			.filter(s->s.contains(hint))
			.collect(Collectors.toList())
				.stream()
				.map(s->{  
					String r = s.replace(hint, "");
					return Integer.parseInt(r);
				})
				.collect(Collectors.toList());
		return results;
	}
	
	String parseStrVal(Row row, int columnIndex){
		colNum = columnIndex;
		String result = null;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		String v = cell.getStringCellValue();
		if(StringUtils.isNotBlank(v)){
			result = StringUtils.trim(v);
			result = result.replace("\n", "");
		}
		return result;
	}
	
	java.util.Date parseDateVal(Row row, int columnIndex){
		colNum = columnIndex;
		java.util.Date date = null;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		int type = cell.getCellType();
		try{
			if(type == Cell.CELL_TYPE_NUMERIC){
				date = cell.getDateCellValue();
			}else if(type == Cell.CELL_TYPE_STRING){
				String str = cell.getStringCellValue();
				if(StringUtils.isNotBlank(str)){
					str = str.trim();
					String pattern = DatetimeUtil.getDatePattern(str);
					if(pattern == null){
						throw new RuntimeException("不正確的日期格式:" + str);
					}
					DateFormat df = new SimpleDateFormat(pattern);
					date = df.parse(str);
				}
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}		
		return date;
	}
	
	java.sql.Date parseSqlDateVal(Row row, int columnIndex){
		colNum = columnIndex;
		java.util.Date d = parseDateVal(row, columnIndex);
		return d != null ? new java.sql.Date(d.getTime()) : null; 
	}
	
	Double parseNumericVal(Row row, int columnIndex){
		colNum = columnIndex;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		double d = cell.getNumericCellValue();
		return d;
	}
	
	String parseNumericOrStr(Row row, int columnIndex){
		colNum = columnIndex;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		int type = cell.getCellType();
		String val = null;
		if(type == Cell.CELL_TYPE_STRING){
			val = cell.getStringCellValue();
		}else if(type == Cell.CELL_TYPE_NUMERIC){
			double d = cell.getNumericCellValue();
			val = new BigDecimal(d).toString();
		}
		return val;
	}
	
	Date parseNumericToSqlDate(Row row ,int columnIndex){
		colNum = columnIndex;
		Double d = parseNumericVal(row, columnIndex);
		if(d == null){
			return null;
		}
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1900);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.MILLISECOND, d.intValue());
		
		Date date = new Date(c.getTimeInMillis());
		
		return date;
	}	
}
