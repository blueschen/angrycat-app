package com.angrycat.erp.excel;

import static com.angrycat.erp.excel.ExcelColumn.Member.Facebook_姓名;
import static com.angrycat.erp.excel.ExcelColumn.Member.Ohmliy_VIP;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.ds.SessionExecutable;
import com.angrycat.erp.model.Member;

@Component
@Scope("prototype")
public class ExcelImporter {
	@Autowired
	private SessionExecutable<Member> se;
	
	
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
	
	public void persist(byte[] data){
		
		int totalCount = 0;
		
		String IDNO_NOT_EXISTED = "idNoNotExisted";
		String IDNO_DUPLICATE = "idNoDuplicate";
		
		Map<String, Integer> msg = new LinkedHashMap<>();
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook wb = WorkbookFactory.create(bais);
			Sheet sheet = wb.getSheetAt(0);
			totalCount = sheet.getLastRowNum();
			
			se.executeTXSession(s->{
				Iterator<Row> itr = sheet.iterator();
				
				while(itr.hasNext()){
					Row row = itr.next();
					int rowNum = row.getRowNum();
					if(rowNum == 0){
						continue;
					}
					
					System.out.println("rowNum: " + rowNum);
					
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
					
					if(StringUtils.isBlank(idNo)){
						msg.put(IDNO_NOT_EXISTED+rowNum, rowNum);
						System.out.println("沒有提供身分證字號, current row count: " + rowNum);
						continue;
					}
					
					idNo = idNo.toUpperCase();
					Number num = (Number)s.createQuery("SELECT COUNT(m) FROM " + Member.class.getName() + " m WHERE m.idNo = ?").setString(0, idNo).uniqueResult();
					int count = num.intValue();
					if(count > 0){
						msg.put(IDNO_DUPLICATE+rowNum, rowNum);
						System.out.println("身分證字號重複:" + idNo);
						continue;
//						throw new RuntimeException("身分證字號重複");
					}
					Member m = new Member();
					m.setImportant("VIP".equals(VIP));
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
				}
				s.flush();
				s.clear();

				
			});

			
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("總筆數: " + totalCount);
			List<Integer> idNoNotExisted = findMsgRowNums(msg, IDNO_NOT_EXISTED);
			System.out.println("身分證字號不存在共"+idNoNotExisted.size()+"筆\n行數:" + StringUtils.join(idNoNotExisted, "、"));
			List<Integer> idDuplicate = findMsgRowNums(msg, IDNO_DUPLICATE);
			System.out.println("身分證字號重複共"+idDuplicate.size()+"筆\n行數:" + StringUtils.join(idDuplicate, "、"));
			System.out.println("實際匯入筆數: " + (totalCount - msg.size()));
			
		}
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
		String v = row.getCell(columnIndex).getStringCellValue();
		if(StringUtils.isNotBlank(v)){
			result = StringUtils.trim(v);
			System.out.println("str val: " + v);
		}
		return result;
	}
	
	java.util.Date parseDateVal(Row row, int columnIndex){
		java.util.Date date = null;
		Cell cell = row.getCell(columnIndex);
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

		System.out.println("Date val: " + date);
		
		return date;
	}
	
	java.sql.Date parseSqlDateVal(Row row, int columnIndex){
		java.util.Date d = parseDateVal(row, columnIndex);
		return d != null ? new java.sql.Date(d.getTime()) : null; 
	}
	
	double parseNumericVal(Row row, int columnIndex){
		double d = row.getCell(columnIndex).getNumericCellValue();
		return d;
	}
	
	String parseNumericOrStr(Row row, int columnIndex){
		Cell cell = row.getCell(columnIndex);
		int type = cell.getCellType();
		String val = null;
		if(type == Cell.CELL_TYPE_STRING){
			val = cell.getStringCellValue();
		}else if(type == Cell.CELL_TYPE_NUMERIC){
			double d = cell.getNumericCellValue();
			val = new BigDecimal(d).toString();
		}
		System.out.println("parseNumericOrStr val: " + val);
		return val;
	}
	
	Date parseNumericToSqlDate(Row row ,int columnIndex){
		double d = parseNumericVal(row, columnIndex);
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1900);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.MILLISECOND, (int)d);
		
		Date date = new Date(c.getTimeInMillis());
		System.out.println("date val: " + date);
		
		return date;
	}	
}
