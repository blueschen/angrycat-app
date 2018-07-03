package com.angrycat.erp.excel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ProductStockExcelImporterTests {
	@Autowired
	private ProductStockExcelImporter importer;
	@Test
	public void resolveToDB(){
		importer.setSrc("C:\\Users\\JerryLin\\Desktop\\OHM庫存表.xlsx");
		importer.setMergeDisabled(false);
		importer.resolveOHMToDB();
		
//		importer.setSrc("C:\\Users\\JerryLin\\Desktop\\阿伯保養品.xlsx");
//		importer.setMergeDisabled(false);
//		importer.resolveTowntalkToDB();
	}
}
