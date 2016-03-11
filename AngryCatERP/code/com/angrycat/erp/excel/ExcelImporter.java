package com.angrycat.erp.excel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.log.DataChangeLogger;


@Component
@Scope("prototype")
public abstract class ExcelImporter {
	private static final List<Integer> DEFAULT_SHEET_RANGE = Arrays.asList(0);
	
	@Autowired
	@Qualifier("sessionFactoryWrapper")
	private SessionFactoryWrapper sfw;
	private int colNum = 0;
	private Workbook wb;
	private Row titleRow;
		
	public static void main(String[]args){
//		readAndWrite("C:\\angrycat_workitem\\OHM Beads TW (AngryCat) 一般會員資料.xlsx", "C:\\angrycat_workitem\\test.xlsx");
//		read("E:\\angrycat_workitem\\member\\2015_10_05\\OHM Beads TW (AngryCat) 一般會員資料_update.xlsx", 0, new int[]{0});
//		testPattern();
		testDouble();
	}

	private static void testDouble(){
		double d = 10;
		Object obj = d;
		if(obj.getClass() == Double.class){
			System.out.println("primary double is Double.class");
		}
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
	
	static void readAndWrite(String src, String dest){
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
	 * 顯示資料源的欄位值及類型，必須指定檔案位置、第幾頁、第幾欄。
	 * 可以直接測試位在本機的xlsx檔案，在測試上的好處就是不需啟動Web環境和使用UI
	 * @param src
	 * @param sheetIdxs
	 * @param colIdxs
	 */
	static void read(String src, int[] sheetIdxs, int[] colIdxs){
		try(InputStream is = new FileInputStream(src);
			XSSFWorkbook wb = new XSSFWorkbook(is);){
			
			for(int sheetIdx : sheetIdxs){
				Sheet sheet = wb.getSheetAt(sheetIdx);
				
				DataFormat df = wb.createDataFormat();
				CellStyle cs = wb.createCellStyle();
				cs.setDataFormat(df.getFormat("@")); // 文字格式
				
				sheet.forEach(row->{
					for(int colIdx : colIdxs){
						Cell cell = row.getCell(colIdx);
//						cell.setCellStyle(cs);
						DataFormatter formatter = new DataFormatter();
						System.out.println("第" + row.getRowNum() + "列");
						System.out.println("Cell Val: " + cell.getRichStringCellValue());
						System.out.println("===================");
					}
				});
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("executing finally...");
		}
	}
	
	/**
	 * 根據Cell Type，取得XSSF Cell的值。
	 * 目前僅處理字串、數值、和布林值。
	 * 字串會額外判斷是否為空或null之類的值，若是這種情況，回傳null。
	 * 字串若有資料，則會trim掉空白，也會取代\n、\r、\t才用。
	 * 其中，字串和數值都有可能是日期，遇到這種狀況，會把他轉成java.util.Date傳出。
	 * 字串為日期的情況比較複雜，這裡涵蓋yyyy-MM-dd、MM-dd-yyyy、yyyy/MM/dd、MM/dd/yyyy這四種格式，都會轉成Java日期。
	 * @param cell
	 * @return
	 */
	static Object getXSSFValueByCellType(Cell cell){
		if(cell == null){
			return null;
		}
		int type = cell.getCellType();
		Object val = null;
		switch(type){
			case XSSFCell.CELL_TYPE_STRING:
				String strVal = cell.getStringCellValue();
				if(StringUtils.isBlank(strVal)){
					break;
				}
				strVal = strVal.trim();
				strVal = strVal.replace("\n", " ");
				strVal = strVal.replace("\t", " ");
				strVal = strVal.replace("\r", " ");
				String pattern = DatetimeUtil.getDatePatternOrEmptyStr(strVal);
				if(StringUtils.isBlank(pattern)){
					val = strVal;
				}else{
					SimpleDateFormat df = new SimpleDateFormat(pattern);
					try{
						val = df.parse(strVal);
					}catch(Throwable e){
						throw new RuntimeException(e);
					}
				}
				break;
				// 如果遇到類型為自訂的日期，isCellDateFormatted無法判斷他是日期，會把他當作一般數值；
				// isValidExcelDate可以判斷某個double值是否可以轉為日期，但也會混淆一般數值
				// 比較正規的作法，是使用isCellDateFormatted做初步判斷
			case XSSFCell.CELL_TYPE_NUMERIC:
				double num = cell.getNumericCellValue();
				if(DateUtil.isCellDateFormatted(cell)){ 
					val = cell.getDateCellValue();
				}else{
					val = num;
				}
				break;
			case XSSFCell.CELL_TYPE_BOOLEAN:
				val = cell.getBooleanCellValue();
				break;
			case XSSFCell.CELL_TYPE_FORMULA: // ref. http://stackoverflow.com/questions/7608511/java-poi-how-to-read-excel-cell-value-and-not-the-formula-computing-it
				switch(cell.getCachedFormulaResultType()){
					case XSSFCell.CELL_TYPE_STRING:
						val = cell.getStringCellValue();
						break;
					case XSSFCell.CELL_TYPE_NUMERIC:
						val = cell.getNumericCellValue();
						break;
				}
				break;
			case XSSFCell.CELL_TYPE_BLANK:
				break;
			case XSSFCell.CELL_TYPE_ERROR:				
				break;
		}
		return val;
	}
	
	/**
	 * 讀取指定位置Excel，並嘗試轉成資料庫資料
	 * @param src
	 */
	Map<String, String> readAndPersist(String src){
		Map<String, String> msg= null;
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src))){
			byte[] data = IOUtils.toByteArray(bis);
			msg = persist(data);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("executinhg readAndPersist finally");
		}
		return msg;
	}
	
	/**
	 * 將Excel位元組資料存到資料庫，並傳出處理訊息。
	 * @param data
	 * @return
	 */
	public Map<String, String> persist(byte[] data){
		return persist(data, null);
	}
	/**
	 * 將Excel位元組資料存到資料庫，並傳出處理訊息
	 * TODO 資料異動的部分待確認事項: 由Excel匯入資料是否要存入異動記錄。這裡的模糊空間是，通常匯入的動作是用在上線前初始化資料庫，實際上後來的匯入資料都是手動鍵入。
	 * @param data
	 * @param dataChangeLogger
	 * @return
	 */
	public Map<String, String> persist(byte[] data, DataChangeLogger dataChangeLogger){		
		int totalCount = 0;
		
		Map<String, Integer> msg = new LinkedHashMap<>();
		Map<String, String> logWarn = new HashMap<>();
		Session s = null;
		Transaction tx = null;
		int rowNum = 0;
		int readableRowNum = 0;
		int insertCount = 0;
		
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);){
			s = sfw.openSession();
			tx = s.beginTransaction();
			int batchSize = sfw.getBatchSize();
			
			wb = WorkbookFactory.create(bais);
			beforeProcessRow();
			List<Integer> sheetRange = sheetRange();
			for(int sheetIdx : sheetRange){
				Sheet sheet = wb.getSheetAt(sheetIdx);
				totalCount += sheet.getLastRowNum();			
				Iterator<Row> itr = sheet.iterator();
				
				rowNum = 0;
				readableRowNum = 0;
				
				while(itr.hasNext()){
					Row row = itr.next();
					rowNum = row.getRowNum();
					readableRowNum = rowNum+1;
					if(rowNum == 0){
						this.titleRow = row;
						continue;
					}
					if(isRowEmpty(row, row.getLastCellNum())){
						continue;
					}
					boolean saveSuccess = processRow(row, s, sheetIdx, readableRowNum, msg);
					if(!saveSuccess){
						continue;
					}
//					if(dataChangeLogger != null){
//						dataChangeLogger.logAdd(m, s);
//					}
					
					if(++insertCount % batchSize == 0){
						s.flush();
						s.clear();
					}
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
		for(String k : msgKeys()){
			warning = genWarnMsg(msg, warning, k);
		}
		
		String infoMsg = infoTotalCount + "\n" + infoImportCount;
		if(StringUtils.isNotBlank(warning.toString())){
			logWarn.put("warnMsg", warning.toString());
		}else{
			logWarn.put("infoMsg", infoMsg);
		}
		return logWarn;
	}
	
	protected void beforeProcessRow(){}
	
	protected Workbook getWorkbook(){
		return wb;
	}
	/**
	 * 以第一個row的標題名稱去找column index
	 * @param title
	 * @return
	 */
	protected int getColumnIdxFromTitle(String title){
		Iterator<Cell> cellIterator = titleRow.iterator();
		while(cellIterator.hasNext()){
			Cell cell = cellIterator.next();
			Object val = getXSSFValueByCellType(cell);
			if(val instanceof String){
				String str = (String)val;
				if(StringUtils.isNotBlank(str) && str.equals(title)){
					int colIdx = cell.getColumnIndex();
					return colIdx;
				}
			}
		}
		return -1;
	}
	
	protected List<Integer> sheetRange(){
		return DEFAULT_SHEET_RANGE;
	}
	
	abstract boolean processRow(Row row, Session s, int sheetIdx, int readableRowNum, Map<String, Integer> msg);
	
	protected List<String> msgKeys(){
		return Collections.emptyList();
	} 
	
	/**
	 * 檢查列的每個欄位，如果裡面的值都是空的，代表這一列沒有資料
	 * @param row
	 * @param colCount
	 * @return
	 */
	private static boolean isRowEmpty(Row row, int colCount){
		boolean empty = true;
		for(int i = 0; i < colCount; i++){
			Cell cell = row.getCell(i);
			if(cell != null){
				CellReference ref = new CellReference(row.getRowNum(), i);
				String val = ref.formatAsString();
				if(StringUtils.isNotBlank(val)){
					empty = false;
					break;
				}	
			}
		}
		return empty;
	}
	
	private StringBuffer genWarnMsg(Map<String, Integer> msg, StringBuffer warnMsg, String msgKey){
		List<Integer> warnNums = findMsgRowNums(msg, msgKey);
		String warn = "";
		if(!warnNums.isEmpty()){
			warn = msgKey+"共"+warnNums.size()+"筆\n行數:" + StringUtils.join(warnNums, "、");
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
	/**
	 * 轉出字串，若遇到日期，則以yyyy-MM-dd格式再轉成字串
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	String parseStrVal(Row row, int columnIndex){
		colNum = columnIndex;
		String result = null;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		Object obj = getXSSFValueByCellType(cell);
		if(obj != null){
			if(obj.getClass() == String.class){
				result = (String)obj;
			}else if(obj.getClass() == java.util.Date.class){
				DateFormat df = DatetimeUtil.DF_yyyyMMdd_DASHED;
				try{
					result = df.format((java.util.Date)obj);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
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
		Object val = getXSSFValueByCellType(cell);
		if(val instanceof java.util.Date){
			date = (java.util.Date)val;
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
		Object obj = getXSSFValueByCellType(cell);
		if(obj!=null){
			if(obj.getClass() == String.class){
				return Double.valueOf((String)obj);
			}else if(obj.getClass() == Double.class){
				return (Double)obj;
			}
		}
		return null;
	}
	
	String parseNumericOrStr(Row row, int columnIndex){
		colNum = columnIndex;
		Cell cell = row.getCell(columnIndex);
		if(cell == null){
			return null;
		}
		String val = null;
		Object obj = getXSSFValueByCellType(cell);
		if(obj!=null){
			if(obj.getClass() == String.class){
				val = (String)obj;
			}else if(obj.getClass() == Double.class){
				val = new BigDecimal((Double)obj).toString();
			}
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
