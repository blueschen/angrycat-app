package com.angrycat.erp.test;

import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;

public class ExcelImporterTest extends BaseTest{

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
		String memberSource = "E:\\angrycat_workitem\\member\\OHMLIY List_20150720.xlsx";
		String memberSample = "C:\\Users\\JerryLin\\Desktop\\member_sample.xlsx";
		
		try(FileInputStream fis = new FileInputStream(memberSource);){
		byte[] data = IOUtils.toByteArray(fis);

		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		ExcelImporter ei = acac.getBean(ExcelImporter.class);
		SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
		
		sfw.executeTransaction(s->{
			int deleteCount = s.createQuery("DELETE FROM " + Member.class.getName()).executeUpdate();
			System.out.println("success delete " + deleteCount);
		});
			
		ei.persist(data);
		
		sfw.executeSession(s->{
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
