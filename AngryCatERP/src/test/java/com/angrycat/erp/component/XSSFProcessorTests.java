package com.angrycat.erp.component;

import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class XSSFProcessorTests {
	@Autowired
	private BeanFactory beanFactory;
	
	private String srcFile = "C:\\Users\\JerryLin\\Desktop\\臺灣OHM商品總庫存清單_2016_12_07.xlsx";
	private String sheetName = "一般商品";
	
	String columnName_英文名字 = "英文名字";
	String columnName_將到貨 = "將到貨";
	String columnName_總庫存 = "總庫存";
	String columnName_型號 = "型號";
	String columnName_未出貨 = "未出貨";
	String columnName_備註 = "備註";
	String columnName_定價 = "定價";
	
	private void execute(Consumer<Workbook> consumer){
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(srcFile)));
			Workbook wb = WorkbookFactory.create(bis);){
			consumer.accept(wb);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Test
	public void getHeader(){
		execute(wb->{
			String columnEndSymbol = "Z";
			XSSFProcessor processor = beanFactory.getBean(XSSFProcessor.class, wb, sheetName, columnEndSymbol);
			Map<String, Integer> header = processor.getHeader();
			int columnCount = header.size();
			System.out.println("header count: " + columnCount);
			
			Map<String, String> columns = new LinkedHashMap<>();
			columns.put(columnName_英文名字, "G");
			columns.put(columnName_總庫存, "L");
			columns.put(columnName_備註, "U");
			columns.put(columnName_型號, "F");
			columns.put(columnName_定價, "H");
			
			header.forEach((k, v)->{
				if(columns.containsKey(k)){
					String symbol = columns.get(k);
					Integer expectedIdx = convertColStringToIndex(symbol);
					assertEquals(expectedIdx, v);
				}
				System.out.println(k+":"+v);
			});
		});
	}
	@Test
	public void bypassHeader(){
		execute(wb->{
			XSSFProcessor processor = beanFactory.getBean(XSSFProcessor.class, wb, sheetName, "Z");
			Iterator<Row> iterator = processor.bypassHeader();
			int rowIdx = 0;
			while(iterator.hasNext()){
				++rowIdx;
				Row row = iterator.next();
				if(rowIdx == 1){
					processor.setCurrentRow(row);
					String val = processor.getStrVal(columnName_英文名字);
					String expected = "Whale's Tale";
					assertEquals(expected, val);
					break;
				}
			}
		});
	}
	@Test
	public void iteratorBypassHeader(){
		execute(wb->{
			XSSFProcessor processor = beanFactory.getBean(XSSFProcessor.class, wb, sheetName, "Z");
			processor.iteratorBypassHeader();
			
			Map<Integer, Map<String, String>> coordinates = new LinkedHashMap<>();
			Map<String, String> row1 = new LinkedHashMap<>();
			row1.put(columnName_英文名字, "Whale's Tale");
			row1.put(columnName_型號, "AAA001");
			coordinates.put(1, row1);
			Map<String, String> row2 = new LinkedHashMap<>();
			row2.put(columnName_英文名字, "Mighty Bear");
			row2.put(columnName_型號, "AAA002");
			coordinates.put(2, row2);
			Map<String, String> row10 = new LinkedHashMap<>();
			row10.put(columnName_英文名字, "Conch Seashell");
			row10.put(columnName_型號, "AAA011");
			coordinates.put(10, row10);
			
			while(processor.hasNext()){
				processor.next();
				int currentRowIdx = processor.getCurrentRowIdx();
				if(coordinates.containsKey(currentRowIdx)){
					Map<String, String> row = coordinates.get(currentRowIdx);
					row.forEach((columnName, cellVal)->{
						String strVal = processor.getStrVal(columnName);
						assertEquals(cellVal, strVal);
						System.out.println("strVal:" + strVal);
					});
				}
				if(currentRowIdx == 11){
					break;
				}
			}
		});
	}
	@Test
	public void iterator(){
		execute(wb->{
			XSSFProcessor processor = beanFactory.getBean(XSSFProcessor.class, wb, sheetName, "Z");
			processor.iterator();
			
			Map<Integer, Map<String, String>> coordinates = new LinkedHashMap<>();
			Map<String, String> row1 = new LinkedHashMap<>();
			row1.put(columnName_英文名字, "Whale's Tale");
			row1.put(columnName_型號, "AAA001");
			coordinates.put(1, row1);
			Map<String, String> row2 = new LinkedHashMap<>();
			row2.put(columnName_英文名字, "Mighty Bear");
			row2.put(columnName_型號, "AAA002");
			coordinates.put(2, row2);
			Map<String, String> row10 = new LinkedHashMap<>();
			row10.put(columnName_英文名字, "Conch Seashell");
			row10.put(columnName_型號, "AAA011");
			coordinates.put(10, row10);
			
			while(processor.hasNext()){
				processor.next();
				int currentRowIdx = processor.getCurrentRowIdx();
				if(coordinates.containsKey(currentRowIdx)){
					Map<String, String> row = coordinates.get(currentRowIdx);
					row.forEach((columnName, cellVal)->{
						String strVal = processor.getStrVal(columnName);
						assertEquals(cellVal, strVal);
						System.out.println("strVal:" + strVal);
					});
				}
				if(currentRowIdx == 11){
					break;
				}
			}
		});
	}
}
