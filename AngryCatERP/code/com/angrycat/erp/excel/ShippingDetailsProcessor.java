package com.angrycat.erp.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ShippingDetailsProcessor {

	private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256; 
//	private static final short EXCEL_ROW_HEIGHT_FACTOR = 20; 
	private static final int UNIT_OFFSET_LENGTH = 7; 
	private static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109, 146, 182, 219 };
	
	// ref. https://stackoverflow.com/questions/11573993/setting-column-width-in-apache-poi
	private static short pixel2WidthUnits(int pxs) {
	    short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH)); 
	    widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];  
	    return widthUnits; 
	} 
	
	public static byte[] renderXlsx(byte[] data, Map<String, Object> options) throws Exception{
		
		Map<String, List<String>> shippingDetails = new LinkedHashMap<>();
		byte[] outputData = null;
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook workbook = WorkbookFactory.create(bais);){
			Sheet sheet = workbook.getSheet("內容");
			
			if(sheet == null){
				return outputData;
			}
			
			Iterator<Row> rows = sheet.rowIterator();
			int count = 0;
			while(rows.hasNext()){
				count++;
				if(count == 1){// 從第二行開始
					continue;
				}
				Row row = rows.next();
				Cell firstCell = row.getCell(0);
				int cellType = firstCell.getCellType();
				if(cellType != Cell.CELL_TYPE_STRING){
					continue;
				}
				String customerName = firstCell.getStringCellValue();
				List<String> details = shippingDetails.get(customerName);
				if(details == null){
					details = new ArrayList<>();
					shippingDetails.put(customerName, details);
				}
				Cell secondCell = row.getCell(1);
				if(secondCell.getCellType() != Cell.CELL_TYPE_STRING){
					continue;
				}
				String productName = secondCell.getStringCellValue();
				details.add(productName);
			}
		}
		LocalDateTime time = LocalDateTime.now();
		int year = time.getYear();
		int month = time.getMonth().getValue();
		int day = time.getDayOfMonth();
		
		String shippingDate = (String)options.get("shippingDate");
		String dateF = shippingDate == null ? year + "-" + month + "-" + day : shippingDate;
		
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Workbook workbook = new XSSFWorkbook();){
			
			CellStyle borderStyle = workbook.createCellStyle();
			borderStyle.setBorderBottom(CellStyle.BORDER_HAIR);
			borderStyle.setBorderLeft(CellStyle.BORDER_HAIR);
			borderStyle.setBorderRight(CellStyle.BORDER_HAIR);
			borderStyle.setBorderTop(CellStyle.BORDER_HAIR);
			
			CellStyle alignCenterStyle = workbook.createCellStyle();
			alignCenterStyle.cloneStyleFrom(borderStyle);
			alignCenterStyle.setAlignment(CellStyle.ALIGN_CENTER);
			
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontName("Microsoft JhengHei");
			
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
			titleStyle.setFont(font);
			
			CellStyle subTitleStyle = workbook.createCellStyle();
			subTitleStyle.cloneStyleFrom(borderStyle);
			subTitleStyle.setAlignment(CellStyle.ALIGN_CENTER);
			subTitleStyle.setFont(font);
			
			int sheetIdx = -1;
			int lastCol = 1;
			for(Map.Entry<String, List<String>> details : shippingDetails.entrySet()){
				String customerName = details.getKey();				
				Sheet sheet = workbook.createSheet(customerName);
				++sheetIdx;
				
				Row titleRow = sheet.createRow(0);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellStyle(titleStyle);
				titleCell.setCellValue("AngryCat　出　貨　明　細");
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));
				
				sheet.setColumnWidth(0, pixel2WidthUnits(547));
				sheet.setColumnWidth(1, pixel2WidthUnits(81));
				
				Row shippingDateRow = sheet.createRow(1);
				Cell shippingDateCell = shippingDateRow.createCell(0);
				shippingDateCell.setCellValue("出貨日期 Date：" + dateF); // TODO which one
				
				Row customerNameRow = sheet.createRow(2);
				Cell customerNameCell = customerNameRow.createCell(0);
				customerNameCell.setCellValue("客戶 Name：" + customerName);
				
				sheet.createRow(3);

				Row detailHeader = sheet.createRow(4);
				Cell item = detailHeader.createCell(0);
				item.setCellValue("Item 品項");
				item.setCellStyle(subTitleStyle);
				Cell quantity = detailHeader.createCell(1);
				quantity.setCellValue("Qty 數量");
				quantity.setCellStyle(subTitleStyle);
				
				int currentRowIdx = 4;
				List<String> products = details.getValue();
				for(String product : products){
					++currentRowIdx;
					Row detail = sheet.createRow(currentRowIdx);
					Cell productCell = detail.createCell(0);
					productCell.setCellValue(product);
					productCell.setCellStyle(borderStyle);
					Cell quantityCell = detail.createCell(1);
					quantityCell.setCellValue(1);
					quantityCell.setCellStyle(alignCenterStyle);
				}
				Row total = sheet.createRow(++currentRowIdx);
				total.createCell(1).setCellValue("Total：" + products.size());
				
//				sheet.setSelected(true); // when selected, which enables Desktop.print multiple sheets
				workbook.setPrintArea(sheetIdx, 0, lastCol, 0, currentRowIdx);
			}
//			for(int i = 0; i <= sheetIdx; i++){
//				String printArea = workbook.getPrintArea(i);
//				System.out.println(printArea);
//			}
			workbook.write(baos);
			outputData = baos.toByteArray();
		}
		return outputData;
	}

}
