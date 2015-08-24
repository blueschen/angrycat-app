package com.angrycat.erp.excel;

import static com.angrycat.erp.excel.ExcelColumn.Member.Facebook_姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.Ohmliy_VIP;
import static com.angrycat.erp.excel.ExcelColumn.Member.VIP延續;
import static com.angrycat.erp.excel.ExcelColumn.Member.備註;
import static com.angrycat.erp.excel.ExcelColumn.Member.出生年月日;
import static com.angrycat.erp.excel.ExcelColumn.Member.地址;
import static com.angrycat.erp.excel.ExcelColumn.Member.性別;
import static com.angrycat.erp.excel.ExcelColumn.Member.真實姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.聯絡電話;
import static com.angrycat.erp.excel.ExcelColumn.Member.身份證字號;
import static com.angrycat.erp.excel.ExcelColumn.Member.轉VIP日期;
import static com.angrycat.erp.excel.ExcelColumn.Member.郵遞區號;
import static com.angrycat.erp.excel.ExcelColumn.Member.電子信箱;

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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Member;

@Component
@Scope("prototype")
public class ExcelImporter {
	@Autowired
	@Qualifier("sessionFactoryWrapper")
	private SessionFactoryWrapper sfw;
	
	@Autowired
	private MemberVipDiscount discount;
		
	public static void main(String[]args){
		readAndWrite("C:\\angrycat_workitem\\OHM Beads TW (AngryCat) 一般會員資料.xlsx", "C:\\angrycat_workitem\\test.xlsx");
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
	
	public Map<String, String> persist(byte[] data){
		
		int totalCount = 0;
		
		String IDNO_NOT_EXISTED = "idNoNotExisted";
		String IDNO_DUPLICATE = "idNoDuplicate";
		int VIP_MAX_YEAR = 2;
		
		Map<String, Integer> msg = new LinkedHashMap<>();
		Map<String, String> logWarn = new HashMap<>();
		Session s = null;
		Transaction tx = null;
		discount.setToVipDateReset(false);
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook wb = WorkbookFactory.create(bais);
			Sheet sheet = wb.getSheetAt(0);
			totalCount = sheet.getLastRowNum();
			
			Iterator<Row> itr = sheet.iterator();
			
			s = sfw.openSession();
			tx = s.beginTransaction();
			
			int insertCount = 0;
			int batchSize = sfw.getBatchSize();
			while(itr.hasNext()){
				Row row = itr.next();
				int rowNum = row.getRowNum();
				if(rowNum == 0){
					continue;
				}
								
				String VIP			= parseStrVal(row, Ohmliy_VIP);
				String fbNickname	= parseStrVal(row, Facebook_姓名);
				String name			= parseStrVal(row, 真實姓名);
				String gender		= parseStrVal(row, 性別);
				String idNo			= parseStrVal(row, 身份證字號);
				Date birthday		= parseSqlDateVal(row, 出生年月日);
				String email		= parseStrVal(row, 電子信箱);
				String mobile		= parseNumericOrStr(row, 聯絡電話);
				String postalCode	= parseNumericOrStr(row, 郵遞區號);
				String address		= parseStrVal(row, 地址);
				Date toVipDate		= parseSqlDateVal(row, 轉VIP日期);
				String note			= parseStrVal(row, 備註);
				String vipYear		= parseNumericOrStr(row, VIP延續);
				
				if(StringUtils.isBlank(idNo)){
					msg.put(IDNO_NOT_EXISTED+rowNum, rowNum);
					continue;
				}
				
				idNo = idNo.toUpperCase();
				Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.idNo = :idNo").setString("idNo", idNo).uniqueResult();
				int count = num.intValue();
				if(count > 0){
					msg.put(IDNO_DUPLICATE+rowNum, rowNum);
					continue;
				}
				Member m = new Member();
				m.setImportant("VIP".equals(VIP) || "R-VIP".equals(VIP));
				m.setFbNickname(fbNickname);
				m.setName(name);
				m.setGender("男".equals(gender) ? Member.GENDER_MALE : Member.GENDER_FEMALE);
				m.setIdNo(idNo);
				m.setBirthday(birthday);
				m.setEmail(email);
				m.setMobile(mobile);
				m.setPostalCode(postalCode);
				m.setAddress(address);
				m.setToVipDate(toVipDate);
				m.setNote(note);
								
				s.save(m);
				
				if(m.getBirthday()!=null && m.getToVipDate()!=null){
					int vipEffectiveYearCount = 0;
					if(StringUtils.isBlank(vipYear)){
						vipEffectiveYearCount = 1;
					}else{
						vipEffectiveYearCount = Integer.parseInt(vipYear);
						if(vipEffectiveYearCount > VIP_MAX_YEAR){
							vipEffectiveYearCount = VIP_MAX_YEAR;
						}
					}
					discount.setAddCount(vipEffectiveYearCount);
					discount.applyRule(m);
					m.setImportant(true);
				}
				
				s.save(m);
				
				if(++insertCount % batchSize == 0){
					s.flush();
					s.clear();
				}
			}
			
			tx.commit();
			
		}catch(Throwable e){
			tx.rollback();
			String stackTrace = ExceptionUtils.getStackTrace(e);
			logWarn.put("errorMsg", stackTrace);
			System.out.println(stackTrace);
		}finally{
			s.close();
		}
		if(!logWarn.isEmpty()){
			return logWarn;
		}
		
		String infoTotalCount = "總筆數: " + totalCount;
		String infoImportCount = "實際匯入筆數: " + (totalCount - msg.size());
		
		System.out.println(infoTotalCount);
		System.out.println(infoImportCount);
		
		String warning = "";
		String warnAboutIdNoNotExisted = "";
		String warnAboutIdNoDuplicate = "";
		
		List<Integer> idNoNotExisted = findMsgRowNums(msg, IDNO_NOT_EXISTED);
		if(!idNoNotExisted.isEmpty()){
			warnAboutIdNoNotExisted = "身分證字號不存在共"+idNoNotExisted.size()+"筆\n行數:" + StringUtils.join(idNoNotExisted, "、");
			System.out.println(warnAboutIdNoNotExisted);
			warning += (warnAboutIdNoNotExisted + "\n");
		}
//		List<Integer> idDuplicate = findMsgRowNums(msg, IDNO_DUPLICATE);
//		if(!idDuplicate.isEmpty()){
//			warnAboutIdNoDuplicate = "身分證字號重複共"+idDuplicate.size()+"筆\n行數:" + StringUtils.join(idDuplicate, "、");
//			System.out.println(warnAboutIdNoDuplicate);
//			warning += (warnAboutIdNoDuplicate + "\n");
//		}
		
		String infoMsg = infoTotalCount + "\n" + infoImportCount;
		if(StringUtils.isNotBlank(warning)){
			infoMsg += ("\n" + warning);
			logWarn.put("warnMsg", infoMsg);
		}else{
			logWarn.put("infoMsg", infoMsg);
		}
		return logWarn;
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
		String result = null;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		String v = cell.getStringCellValue();
		if(StringUtils.isNotBlank(v)){
			result = StringUtils.trim(v);
		}
		return result;
	}
	
	java.util.Date parseDateVal(Row row, int columnIndex){
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
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				date = df.parse(str);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}		
		return date;
	}
	
	java.sql.Date parseSqlDateVal(Row row, int columnIndex){
		java.util.Date d = parseDateVal(row, columnIndex);
		return d != null ? new java.sql.Date(d.getTime()) : null; 
	}
	
	Double parseNumericVal(Row row, int columnIndex){
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		double d = cell.getNumericCellValue();
		return d;
	}
	
	String parseNumericOrStr(Row row, int columnIndex){
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
