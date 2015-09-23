package com.angrycat.erp.excel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.excel.ExcelColumn.Product.OnePos;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet1;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.service.http.HttpService;

@Service
@Scope("prototype")
public class OnePosProductExcelAccessor {
	private static final String JPG_POSTFIX = "-o-a.jpg";
	private static final String URL_TEMPLATE = "http://ohmbeads.com/ohm2/media/import/{no}" + JPG_POSTFIX;
	private static final String DEFAULT_IMG_DIR = "C:\\ONE-POS DB\\Project\\";
	@Autowired
	private HttpService httpService;

	private void storeImage(String src, String jpgDestDir, int sheetIdex, int cellIdx){
		try(InputStream is = new FileInputStream(src);
		XSSFWorkbook wb = new XSSFWorkbook(is);){
			Sheet sheet = wb.getSheetAt(sheetIdex);
			Iterator<Row> rows = sheet.iterator();
			
			while(rows.hasNext()){
				Row row = rows.next();
				int rowNum = row.getRowNum();
				if(rowNum == 0){
					continue;
				}
				Cell cell = row.getCell(cellIdx);
				String no = cell.getStringCellValue();
				storeImage(jpgDestDir, no);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private void storeImage(String jpgDestDir, String no){
		if(StringUtils.isBlank(jpgDestDir)){
			jpgDestDir = DEFAULT_IMG_DIR;
		}
		String url = URL_TEMPLATE.replace("{no}", no);
		String dest = jpgDestDir + no + JPG_POSTFIX;
		try(FileOutputStream fos = new FileOutputStream(dest)){
			httpService.sendPost(url, bis->{
				try{
					IOUtils.copy(bis, fos);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			});
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	public void process(byte[] data){
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
			XSSFWorkbook wb = new XSSFWorkbook(bais);){
			
			Sheet sheet1 = wb.getSheetAt(0);
			String sheet1Name = sheet1.getSheetName();
			Iterator<Row> sheet1Rows = sheet1.iterator();
			DataFormatter df = new DataFormatter();
			
			String tempPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".xls";
			File file = new File(tempPath);
			try(FileOutputStream fos = new FileOutputStream(file);
				SXSSFWorkbook outWb = new SXSSFWorkbook(100);){
				outWb.setCompressTempFiles(true);
				
				Sheet catSheet = outWb.createSheet("categories"); // 第一個Sheet是產品類別
				Sheet brandSheet = outWb.createSheet("brand"); // 第二個Sheet是產品品牌
				Sheet productSheet = outWb.createSheet("products"); // 第三個Sheet是產品(品項)
				Sheet clientSheet = outWb.createSheet("clients"); // 第四個Sheet是客戶
				Sheet vendorSheet = outWb.createSheet("vendors"); // 第五個Sheet是供應商
				
				int rownum = 0;
				while(sheet1Rows.hasNext()){
					Row row = sheet1Rows.next();
					rownum = row.getRowNum();
					if(rownum == 0){// 略過標題列
						continue;
					}
					
					Cell no = row.getCell(Sheet1.型號);
					Cell nameEng = row.getCell(Sheet1.英文名字);
					Cell price = row.getCell(Sheet1.定價);
					
					String noVal = df.formatCellValue(no);
					String nameEngVal = df.formatCellValue(nameEng);
					String priceVal = df.formatCellValue(price);
					
					Row pRow = productSheet.createRow(rownum);
					Cell productIdCell = pRow.createCell(OnePos.產品編號);
					Cell productNameCell = pRow.createCell(OnePos.產品名稱);
					Cell modelCell = pRow.createCell(OnePos.型號);
					Cell priceCell = pRow.createCell(OnePos.售價);
				}
			}
			

			
			
			
			
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		
	}
	
	public static void main(String[]args){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		
		OnePosProductExcelAccessor importer = acac.getBean(OnePosProductExcelAccessor.class);
		importer.storeImage("E:\\angrycat_workitem\\member\\臺灣OHM商品總庫存清單_T20150923.xlsx", "E:\\angrycat_workitem\\member\\image\\", 2, 0);
		
		acac.close();
	}
}
