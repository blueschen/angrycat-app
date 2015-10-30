package com.angrycat.erp.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

public class MdbTest extends BaseTest {
	
	
	public static void main(String[]args){
//		testOpenLockedMdb();
		int t = -999999999;
		System.out.println(t);
	}
	
	private static void testOpenLockedMdb(){
		try {
			Database db = new DatabaseBuilder(new File("E:\\angrycat_workitem\\pos\\onepos\\onepos.dat"))
				.setCodecProvider(new CryptCodecProvider("31072100"))
				.open();
			
			Table table = db.getTable("CAFE_Config");
			table.forEach(row->{
				System.out.println("Column CopyBar val: " + row.get("CopyBar"));
				System.out.println("Column CopyKitchen val: " + row.get("CopyKitchen"));
				System.out.println("Column SurCharge val: " + row.get("SurCharge"));
				
			});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void openOnePos(String path){
		if(StringUtils.isBlank(path)){
			path = "C:\\ONE-POS DB\\onepos.dat";
		}
		try(Database db = new DatabaseBuilder(new File("E:\\angrycat_workitem\\pos\\onepos\\onepos.dat"))
		.setCodecProvider(new CryptCodecProvider("31072100"))
		.open();){
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		
	}
}
