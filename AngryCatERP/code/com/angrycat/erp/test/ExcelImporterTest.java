package com.angrycat.erp.test;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.angrycat.erp.ds.SessionExecutable;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;

public class ExcelImporterTest {

	public static void main(String[] args) {
		testImportMember();
//		Calendar b = Calendar.getInstance();
//		b.set(Calendar.YEAR, 1900);
//		b.set(Calendar.MONTH, 0);
//		b.set(Calendar.DATE, 1);
//		b.set(Calendar.HOUR_OF_DAY, 0);
//		b.set(Calendar.MINUTE, 0);
//		b.set(Calendar.SECOND, 0);
//		b.set(Calendar.MILLISECOND, 0);
//		
//		Calendar c = Calendar.getInstance();
//		System.out.println(c.getTime());
	}

	public static void testImportMember(){
		
		try(FileInputStream fis = new FileInputStream("E:\\angrycat_workitem\\member\\OHM Beads TW (AngryCat) 一般會員資料.xlsx");){
		byte[] data = IOUtils.toByteArray(fis);

		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		ExcelImporter ei = acac.getBean(ExcelImporter.class);
		SessionExecutable<Member> se = acac.getBean(SessionExecutable.class);
			
		ei.persist(data);
		
		se.executeQuerySession(s->{
			List<Member> results = s.createQuery("FROM " + Member.class.getName() + " m").list();
			System.out.println("results size: " + results.size());
			return results;
		});
		acac.close();
		}catch(Throwable e){
			e.printStackTrace();
		}
		
	}
}
