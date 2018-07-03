package com.angrycat.erp.component;

import static com.angrycat.erp.common.XSSFUtil.parseCellNumericVal;
import static com.angrycat.erp.common.XSSFUtil.parseCellStrVal;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope("prototype")
@Lazy(value = true)
public class XSSFProcessor {
	private Workbook wb;
	private int headerRowIdx;
	private Sheet sheet;
	private Map<String, Integer> header = new LinkedHashMap<>();
	
	private Iterator<Row> iterator;
	private Row currentRow;
	private int currentRowIdx;
	private int columnEndIdx = -1;
	
	public XSSFProcessor(Workbook wb, String sheetName, String columnEndSymbol){
		this(wb, sheetName, 0, columnEndSymbol);
	}
	public XSSFProcessor(Workbook wb, String sheetName, int headerRowIdx, String columnEndSymbol){
		this.wb = wb;
		this.sheet = wb.getSheet(sheetName);
		this.headerRowIdx = headerRowIdx;
		Row headerRow = sheet.getRow(headerRowIdx);
		Iterator<Cell> headerIterator = headerRow.iterator();
		
		if(columnEndSymbol != null){
			columnEndIdx = convertColStringToIndex(columnEndSymbol);
		}
		int columnIdx = -1;
		while(headerIterator.hasNext()){
			++columnIdx;
			String columnVal = parseCellStrVal(headerRow, columnIdx);
			header.put(columnVal, columnIdx);
			if(isLast(columnIdx)){
				break;
			}
		}
//		System.out.println("header column count:" + (columnIdx+1));
	}
	private boolean isLast(int columnIdx){
		return columnIdx >= columnEndIdx;
	}
	public int getHeaderRowIdx() {
		return headerRowIdx;
	}
	public void setCurrentRow(Row currentRow){
		this.currentRow = currentRow;
	}
	public int getCurrentRowIdx(){
		return this.currentRowIdx;
	}
	public Map<String, Integer> getHeader(){
		return this.header;
	}
	public boolean isHeaderContains(String columnName){
		return header.containsKey(columnName);
	}
	public int getHeaderColumnIdx(String columnName){
		int columnIdx = header.get(columnName);
		return columnIdx;
	}
	public String getStrVal(Row row, String columnName){
		if(!isHeaderContains(columnName)){
			return null;
		}
		int columnIdx = getHeaderColumnIdx(columnName);
		String strVal = parseCellStrVal(row, columnIdx);
		return strVal;
	}
	public String getStrVal(String columnName){
		return getStrVal(currentRow, columnName);
	}
	public Double getDouble(Row row, String columnName){
		if(!isHeaderContains(columnName)){
			return null;
		}
		int columnIdx = getHeaderColumnIdx(columnName);
		Double doubleVal = parseCellNumericVal(row, columnIdx);
		return doubleVal;
	}
	public Double getDouble(String columnName){
		return getDouble(currentRow, columnName);
	}
	public double getDoubleVal(Row row, String columnName){
		Double doubleVal = getDouble(row, columnName);
		return doubleVal != null ? doubleVal : 0;
	}
	public double getDoubleVal(String columnName){
		return getDoubleVal(currentRow, columnName);
	}
	public int getIntValu(Row row, String columnName){
		Double doubleVal = getDouble(row, columnName);
		return doubleVal != null ? doubleVal.intValue() : 0;
	}
	public int getIntValu(String columnName){
		return getIntValu(currentRow, columnName);
	}
	/**
	 * 回傳header所在row以後的Iterator
	 * @return
	 */
	public Iterator<Row> bypassHeader(){
		Iterator<Row> itr = sheet.iterator();
		int rowIdx = -1;
		while(itr.hasNext()){
			++rowIdx;
			itr.next();
			if(rowIdx == headerRowIdx){
				break;
			}
		}
		return itr;
	}
	public void iteratorBypassHeader(){
		this.iterator = bypassHeader();
		this.currentRowIdx = headerRowIdx;
	}
	public void iterator(){
		this.iterator = sheet.iterator();
		this.currentRowIdx = -1;
	}
	public boolean hasNext(){
		return iterator.hasNext();
	}
	public XSSFProcessor next(){
		if(hasNext()){
			currentRow = iterator.next();
			this.currentRowIdx++;
		}else{
			currentRow = null;
		}
		return this;
	}
}
