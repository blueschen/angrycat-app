package com.angrycat.erp.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.Session;

import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.query.QueryScrollable;

public abstract class ExcelExporter<T> {
	
	public abstract List<ObjectFormat> getFormats();
	/**
	 * 匯出一般格式的Excel，盡量與匯入格式一致
	 * @param queryQueryService
	 * @return
	 */
	public File normal(QueryScrollable queryQueryService){
		File tempFile = queryQueryService.executeScrollableQuery((rs, sfw)->{
			List<ObjectFormat> formats = getFormats();
			String tempPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".xlsx";
			
			File file = new File(tempPath);
			try(FileOutputStream fos = new FileOutputStream(file);
				SXSSFWorkbook wb = new SXSSFWorkbook(100);){				
				wb.setCompressTempFiles(true);
				
				DataFormat df = wb.createDataFormat();
				CellStyle cs = wb.createCellStyle();
				cs.setDataFormat(df.getFormat("@")); // 文字格式
				
				Sheet sheet = wb.createSheet();
				int rowCount = 0;
				Row firstRow = sheet.createRow(rowCount++);
				for(int i = 0; i < formats.size(); i++){
					ObjectFormat f = formats.get(i);
					Cell cell = firstRow.createCell(i);
					cell.setCellValue(f.getName());
				}
								
				int batchSize = sfw.getBatchSize();
				Session s = sfw.currentSession();
				int currentCount = 0;
				while(rs.next()){
					Object obj = rs.get(0);
					Row row = sheet.createRow(rowCount++);
					for(int i = 0; i < formats.size(); i++){
						ObjectFormat f = formats.get(i);
						String val = f.getValue(obj);
						if(StringUtils.isNotBlank(val)){
							Cell cell = row.createCell(i);
							cell.setCellValue(val);
							cell.setCellStyle(cs);
						}
					}
					if(++currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
				}
				wb.write(fos);
				boolean tempDeleted = wb.dispose(); // SXSSF需要手動清理暫存檔
				System.out.println("SXSSFWorkbook temp " + (tempDeleted ? "" : "NOT") + " deleted");
			}catch(Throwable t){
				throw new RuntimeException(t);
			}			
			return file;
		});
		return tempFile;
	}
}
