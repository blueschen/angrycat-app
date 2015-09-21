package com.angrycat.erp.test;

import java.io.File;
import java.io.IOException;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

public class MdbTest extends BaseTest {
	
	
	public static void main(String[]args){
		testOpenLockedMdb();
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
}
