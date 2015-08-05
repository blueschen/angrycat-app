package com.angrycat.erp.test;

import java.io.File;
import java.io.IOException;

import org.apache.poi.util.TempFile;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.initialize.config.RootConfig;

public class ExcelExporterTest {
	public static void main(String[]args){
		try {
			File f = TempFile.createTempFile("aaa", "bbb");
			System.out.println("temp dir: " + f.getAbsolutePath());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testBasic();
	}
	public static void testBasic(){
		AnnotationConfigApplicationContext aa = new AnnotationConfigApplicationContext(RootConfig.class);
		ExcelExporter exporter = aa.getBean(ExcelExporter.class);
		exporter.execute();
		aa.close();
	}
}
