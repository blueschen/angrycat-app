package com.angrycat.erp.test;

import java.io.File;
import java.io.IOException;

import org.apache.poi.util.TempFile;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.angrycat.erp.excel.MemberExcelExporter;
import com.angrycat.erp.initialize.config.RootConfig;

public class MemberExcelExporterTest {
	public static void main(String[]args){
		try {
			File f = TempFile.createTempFile("aaa", "bbb");
			System.out.println("temp dir: " + f.getAbsolutePath());
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testOnePos();
	}
	public static void testNormal(){
		AnnotationConfigApplicationContext aa = new AnnotationConfigApplicationContext(RootConfig.class);
		MemberExcelExporter exporter = aa.getBean(MemberExcelExporter.class);
		exporter.normal();
		aa.close();
	}
	public static void testOnePos(){
		AnnotationConfigApplicationContext aa = new AnnotationConfigApplicationContext(RootConfig.class);
		MemberExcelExporter exporter = aa.getBean(MemberExcelExporter.class);
		exporter.setOnePosTemplatePath("E:\\angrycat_workitem\\member\\v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls");
		exporter.onePos();
		aa.close();
	}
}
