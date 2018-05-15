package com.angrycat.erp.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
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
	
	private static Map<String, String> bannedSymbols = new HashMap<>();
	static{
		bannedSymbols.put(":", "冒號");
		bannedSymbols.put("*", "星號");
		bannedSymbols.put("/", "斜線");
		bannedSymbols.put("\\", "反斜線");
		bannedSymbols.put("[", "起始中括號");
		bannedSymbols.put("]", "結束中括號");
	}
	private static final Pattern bannedFormat1 = Pattern.compile("^[a-zA-Z]+([0-9]+)$");
	
	private static final ZoneId zoneId = ZoneId.systemDefault();
	
	static String validateBannedFormat1(String input){
		// 根據org.apache.poi.ss.formula.SheetNameFormatter#nameLooksLikePlainCellReference的檢核規則先行判斷
		// 避免執行到後面在Integer.parseInt的時候因超出範圍丟出例外
		Matcher m = bannedFormat1.matcher(input);
		String n = null;
		while(m.find()){
			n = m.group(1);
		}
		String warning = null;
		if(n != null && n.length() > 10){
			warning = input + "字母後方數字超過十位數";
		}
		return warning;
	}
		
	static boolean validateCustomer(String customerName, int count, List<String> warnings){
		Set<String> validates = new LinkedHashSet<>();
		for(Map.Entry<String, String> banned : bannedSymbols.entrySet()){
			String symbol = banned.getKey();
			if(customerName.contains(symbol)){
				validates.add(banned.getValue() + "("+ symbol +")");
			}
		}
		
		String prefix = "第" + count + "列客戶";
		if(!validates.isEmpty()){
			String msg = prefix +"出現禁用符號：" + validates.stream().collect(Collectors.joining("、"));
			warnings.add(msg);
			return false;
		}
		String bannedFormat1Msg = validateBannedFormat1(customerName);
		if(bannedFormat1Msg != null){
			String msg = prefix + bannedFormat1Msg;
			warnings.add(msg);
			return false;
		}
		return true;
	}
	
	static boolean validateCustomerCell(Cell firstCell, int count, List<String> warnings){
		if(firstCell == null){
			//warnings.add(prefix + "不存在");
			return false;
		}
		int cellType = firstCell.getCellType();
		if(cellType != Cell.CELL_TYPE_STRING){
			//warnings.add(prefix + "不是文字");
			return false;
		}
		String customerName = firstCell.getStringCellValue();
		if(customerName == null || "".equals(customerName.trim())){
			//warnings.add(prefix + "沒有資料");
			return false;
		}
		return true;
	}
	
	public static byte[] renderXlsx(byte[] data, Map<String, Object> options, List<String> warnings) throws Exception{
		
		Map<String, List<String>> shippingDetails = new LinkedHashMap<>();
		byte[] outputData = null;
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook workbook = WorkbookFactory.create(bais);){
			Sheet sheet = workbook.getSheet("內容");
			
			if(sheet == null){
				warnings.add("名稱為「內容」的sheet沒找到");
				return outputData;
			}
			
			Iterator<Row> rows = sheet.rowIterator();
			int count = 0;
			while(rows.hasNext()){
				Row row = rows.next();
				count++;
				if(count == 1){// 從第二列開始
					continue;
				}
				Cell firstCell = row.getCell(0);

				if(!validateCustomerCell(firstCell, count, warnings)){
					continue;
				}
				
				String customerName = firstCell.getStringCellValue().trim();
				
				if(!validateCustomer(customerName, count, warnings)){
					continue;
				}
				
				List<String> details = shippingDetails.get(customerName);
				if(details == null){
					details = new ArrayList<>();
					shippingDetails.put(customerName, details);
				}
				Cell secondCell = row.getCell(1);
				if(secondCell.getCellType() != Cell.CELL_TYPE_STRING){
					warnings.add("第" + count + "列產品/型號不是文字");
					continue;
				}
				String productName = secondCell.getStringCellValue();
				details.add(productName);
			}
		}
				
		if(!warnings.isEmpty()){
			return outputData;
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
			borderStyle.setWrapText(true);
			
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
			
			CellStyle detailRowStyle = workbook.createCellStyle();
			detailRowStyle.setWrapText(true);
			
			int sheetIdx = -1;
			int lastCol = 1;
			for(Map.Entry<String, List<String>> details : shippingDetails.entrySet()){
				String customerName = details.getKey();				
				Sheet sheet = workbook.createSheet(customerName);
				++sheetIdx;
				
				Row titleRow = sheet.createRow(0);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellStyle(titleStyle);
				titleCell.setCellValue("出　貨　明　細");
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
					
					detail.setRowStyle(detailRowStyle);
				}
				Row total = sheet.createRow(++currentRowIdx);
				total.createCell(1).setCellValue("Total：" + products.size());
				
				sheet.setSelected(true); // when selected, which enables Desktop.print multiple sheets
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
	/**
	 * 依據第一行的標題列決定欄位數
	 * @param data
	 * @param options
	 * @return
	 * @throws Exception
	 */
	public static byte[] renderAgeteXlsx(byte[] data, Map<String, Object> options, List<String> warnings) throws Exception{
		
		Map<String, List<List<String>>> shippingDetails = new LinkedHashMap<>();
		byte[] outputData = null;
		List<String> titles = new ArrayList<>();
		LinkedList<Integer> columnWidths = new LinkedList<>();
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook workbook = WorkbookFactory.create(bais);){
			Sheet sheet = workbook.getSheet("內容");
			
			if(sheet == null){
				warnings.add("名稱為「內容」的sheet沒找到");
				return outputData;
			}
			
			Iterator<Row> rows = sheet.rowIterator();
			int count = 0;
			int leastColumnCount = 2; // 依照常識判斷，至少兩行要有資料；其他沒有資料的欄位，只能當做是最後一行略過
			while(rows.hasNext()){
				count++;
				if(count == 1){// 取得各欄位title
					Row firstRow = rows.next();
					Iterator<Cell> firstRowCells = firstRow.cellIterator();
					int columnCount = 0;
					while(firstRowCells.hasNext()){
						Cell cell = firstRowCells.next();
						++columnCount;
						
						if(cell.getCellType() != Cell.CELL_TYPE_STRING){// 除了第一行之外其他都要檢核是否為文字，因為標題列的資料需要在其他地方使用
							if(columnCount == 1){
								// 略過不檢查
							}else if(columnCount <= leastColumnCount){
								warnings.add("標題列第"+columnCount+"行不是文字");
								continue;
							}else{
								break;
							}
						}
						
						if(columnCount != 1 && cell.getCellType() == Cell.CELL_TYPE_STRING){
							titles.add(cell.getStringCellValue());
						}
						int columnWidth = sheet.getColumnWidth(columnCount-1);
						columnWidths.add(columnWidth);
					}
					continue;
				}
								
				Row row = rows.next();
				Cell firstCell = row.getCell(0);
				
				if(!validateCustomerCell(firstCell, count, warnings)){
					continue;
				}
				
				String customerName = firstCell.getStringCellValue().trim();
				
				if(!validateCustomer(customerName, count, warnings)){
					continue;
				}
				
				List<List<String>> details = shippingDetails.get(customerName);
				if(details == null){
					details = new ArrayList<>();
					shippingDetails.put(customerName, details);
				}
				List<String> detail = new ArrayList<>();
				int totalColumnCount = titles.size() + 1;
				details.add(detail);
				for(int i = 1; i < totalColumnCount; i++){
					Cell cell = row.getCell(i);
					String val = null;
					if(cell.getCellType() == Cell.CELL_TYPE_STRING){
						val = cell.getStringCellValue();
					}else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
						if(!HSSFDateUtil.isCellDateFormatted(cell)){
							double num = cell.getNumericCellValue();
							val = new BigDecimal(num).toPlainString();
						}else{
							val = cell.getDateCellValue().toInstant().atZone(zoneId).toLocalDate().toString();
							//val = LocalDate.from(cell.getDateCellValue().toInstant()).toString(); // 這段會出現錯誤，可能是沒有提供zoneId??
						}
					}
					detail.add(val);
				}
			}
		}
		
		if(!warnings.isEmpty()){
			return outputData;
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
			borderStyle.setWrapText(true);
			
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
			
			CellStyle detailCellStyle = workbook.createCellStyle();
			detailCellStyle.cloneStyleFrom(borderStyle);
			detailCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			
			CellStyle detailRowStyle = workbook.createCellStyle();
			detailRowStyle.setWrapText(true);
			
			int sheetIdx = -1;
			int lastCol = titles.size();
			
			int first = columnWidths.removeFirst();
			columnWidths.addLast(first);
			
			for(Map.Entry<String, List<List<String>>> details : shippingDetails.entrySet()){
				String customerName = details.getKey();				
				Sheet sheet = workbook.createSheet(customerName);
				++sheetIdx;
				
				Row titleRow = sheet.createRow(0);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellStyle(titleStyle);
				titleCell.setCellValue("出　貨　明　細");
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));
				
				Row shippingDateRow = sheet.createRow(1);
				Cell shippingDateCell = shippingDateRow.createCell(0);
				shippingDateCell.setCellValue("出貨日期 Date：" + dateF); // TODO which one
				
				Row customerNameRow = sheet.createRow(2);
				Cell customerNameCell = customerNameRow.createCell(0);
				customerNameCell.setCellValue("客戶 Name：" + customerName);
				
				sheet.createRow(3);

				Row detailHeader = sheet.createRow(4);
				
				for(int i = 0; i < titles.size(); i++){
					String title = titles.get(i);
					Cell item = detailHeader.createCell(i);
					item.setCellValue(title);
					item.setCellStyle(subTitleStyle);
				}
				Cell qtyItem = detailHeader.createCell(titles.size());
				qtyItem.setCellValue("Qty 數量");
				qtyItem.setCellStyle(subTitleStyle);
				
				int currentRowIdx = 4;
				
				List<List<String>> items = details.getValue();				
				for(List<String> item : items){
					++currentRowIdx;
					Row detail = sheet.createRow(currentRowIdx);
					int size = item.size();
					for(int i = 0; i < size; i++){
						Cell cell = detail.createCell(i);
						cell.setCellValue(item.get(i));
						cell.setCellStyle(detailCellStyle);
					}
					Cell quantityCell = detail.createCell(size);
					quantityCell.setCellValue(1);
					quantityCell.setCellStyle(alignCenterStyle);
					detail.setRowStyle(detailRowStyle);
				}
				Row total = sheet.createRow(++currentRowIdx);
				total.createCell(lastCol).setCellValue("Total：" + items.size());
				
				for(int i = 0; i < columnWidths.size(); i++){
					int columnWidth = columnWidths.get(i);
					sheet.setColumnWidth(i, columnWidth);
				}
				
				sheet.setSelected(true); // when selected, which enables Desktop.print multiple sheets
				workbook.setPrintArea(sheetIdx, 0, lastCol, 0, currentRowIdx);
			}
			
			workbook.write(baos);
			outputData = baos.toByteArray();
		}
		return outputData;
	}
}
