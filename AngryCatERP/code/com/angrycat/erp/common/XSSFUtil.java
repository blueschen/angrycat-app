package com.angrycat.erp.common;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
/**
 * XSSF是Apache POI專案特地針對Microsoft .xlsx的Excel檔釋出的處理工具<br>
 * XSSFUtil封裝相關的函式
 * @author JerryLin
 *
 */
public class XSSFUtil {
	
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
	public static Object getXSSFValueByCellType(Cell cell){
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
	 * 轉出字串，若遇到日期，則以yyyy-MM-dd格式再轉成字串
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static String parseCellStrVal(Row row, int columnIndex){
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
	
	/**
	 * 轉換數值為倍精度浮點數
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static Double parseCellNumericVal(Row row, int columnIndex){
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
	
	/**
	 * 轉換java.util.Date值
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static java.util.Date parseCellDateVal(Row row, int columnIndex){
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

	/**
	 * 根據XSSFCell type判斷值為字串或數值<br>
	 * 若為字串直接回傳，若為數值則轉為(可讀)字串後回傳<br>
	 * 這個方法是為了最大合理容錯而定義
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static String parseCellNumericOrStr(Row row, int columnIndex){
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
	/**
	 * Excel回傳數值可能代表數字，也可能代表時間<br>
	 * 這個方法將數值轉成Date
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static Date parseCellNumericToSqlDate(Row row, int columnIndex){
		Double d = parseCellNumericVal(row, columnIndex);
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
	
	/**
	 * 以第一個row的標題名稱去找column index
	 * @param firstRow
	 * @param title
	 * @return
	 */
	public static int getCellColumnIdxFromTitle(Row firstRow, String title){
		Iterator<Cell> cellIterator = firstRow.iterator();
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
	
	/**
	 * 檢查列的每個欄位，如果裡面的值都是空的，代表這一列沒有資料
	 * @param row
	 * @param colCount
	 * @return
	 */
	public static boolean isRowEmpty(Row row, int colCount){
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
	
	
}
