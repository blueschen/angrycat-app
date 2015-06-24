package com.angrycat.erp.excel;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelImporter {
	public static void main(String[]args){
		readAndWrite("C:\\angrycat_workitem\\OHM Beads TW (AngryCat) 一般會員資料.xlsx", "C:\\angrycat_workitem\\test.xlsx");
	}
	
	public static void readAndWrite(String src, String dest){
		
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
	
	public static void readByte(byte[] data){
		
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Workbook wb = WorkbookFactory.create(bais);
			Sheet sheet = wb.getSheetAt(0);
			sheet.forEach(row->{
				
				
				
				
			});
			
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			System.out.println("executing finally...");
		}
	}
}
