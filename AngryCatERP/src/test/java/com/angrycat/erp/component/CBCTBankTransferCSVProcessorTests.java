package com.angrycat.erp.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

public class CBCTBankTransferCSVProcessorTests {
	private CBCTBankTransferCSVProcessor processor;
	@Before
	public void setup(){
		processor = new CBCTBankTransferCSVProcessor();
	}
	@Test
	public void removeRedundant(){
		String t1 = "=\"106/05/02\"";
		String o1 = CBCTBankTransferCSVProcessor.removeRedundant(t1);
		assertEquals(null, "106/05/02", o1);
		
		String t2 = "\"167,638\"";
		String o2 = CBCTBankTransferCSVProcessor.removeRedundant(t2);
		assertEquals(null, "167,638", o2);
		
		String t3 = "=\"0\"";
		String o3 = CBCTBankTransferCSVProcessor.removeRedundant(t3);
		assertEquals(null, "0", o3);
		
		String t4 = "=\"0291*****47174\"";
		String o4 = CBCTBankTransferCSVProcessor.removeRedundant(t4);
		assertEquals(null, "0291*****47174", o4);
	}
	@Test
	public void formatNumber() throws ParseException{
		String t1 = "192,672";
		NumberFormat format = NumberFormat.getInstance();
		Number number = format.parse(t1);
		assertEquals(null, 192672d, number.doubleValue(), 0.001);
		assertEquals(null, 192672f, number.floatValue(), 0.001);
		assertEquals(null, 192672, number.intValue());
	}
	@Test
	public void toDateFromROC(){
		String t1 = "106/10/29";
		Date o1 = CBCTBankTransferCSVProcessor.toSqlDateFromROC(t1);
		System.out.println(o1);
		String t2 = "105/2/29";
		Date o2 = CBCTBankTransferCSVProcessor.toSqlDateFromROC(t2);
		System.out.println(o2);
	}
	@Test
	public void retrieveTransferAccountCheck(){
		String t1 = "0052035180045400";
		String o1 = CBCTBankTransferCSVProcessor.retrieveTransferAccountCheck(t1);
		assertEquals(null, "45400", o1);
		
		String t2 = "0000078506014726";
		String o2 = CBCTBankTransferCSVProcessor.retrieveTransferAccountCheck(t2);
		assertEquals(null, "14726", o2);
		
		String t3 = "0000109500031885";
		String o3 = CBCTBankTransferCSVProcessor.retrieveTransferAccountCheck(t3);
		assertEquals(null, "31885", o3);
	}
	@Test
	public void findOnlyNumberOrComma(){
		String t1 = "214,432";
		Matcher m1 = CBCTBankTransferCSVProcessor.ONLY_NUM_OR_COMMA.matcher(t1);
		assertTrue(m1.matches());
		
		String t2 = "812台新銀行";
		Matcher m2 = CBCTBankTransferCSVProcessor.ONLY_NUM_OR_COMMA.matcher(t2);
		assertFalse(m2.matches());
		
		String t3 = "0011*****52968";
		Matcher m3 = CBCTBankTransferCSVProcessor.ONLY_NUM_OR_COMMA.matcher(t3);
		assertFalse(m3.matches());
		
		String t4 = "0";
		Matcher m4 = CBCTBankTransferCSVProcessor.ONLY_NUM_OR_COMMA.matcher(t4);
		assertTrue(m4.matches());
	}
	@Test
	public void findOnlyNumber(){
		String t1 = "0000143000004839";
		Matcher m1 = CBCTBankTransferCSVProcessor.ONLY_NUM.matcher(t1);
		assertTrue(m1.matches());
		
		String t2 = "812台新銀行";
		Matcher m2 = CBCTBankTransferCSVProcessor.ONLY_NUM.matcher(t2);
		assertFalse(m2.matches());
		
		String t3 = "0011*****52968";
		Matcher m3 = CBCTBankTransferCSVProcessor.ONLY_NUM.matcher(t3);
		assertFalse(m3.matches());
		
		String t4 = "0";
		Matcher m4 = CBCTBankTransferCSVProcessor.ONLY_NUM.matcher(t4);
		assertTrue(m4.matches());
	}
	@Test
	public void replaceNumberWithComma(){
		String t1 = "=\"106/05/02\",=\"跨行轉入\",,,\"1,650\",\"170,217\",=\"0000125200462676\"";
		String o1 = CBCTBankTransferCSVProcessor.replaceNumberWithComma(t1);
		String expected = "=\"106/05/02\",=\"跨行轉入\",,,\"1650\",\"170217\",=\"0000125200462676\"";
		assertEquals(null, expected, o1);
		
		String t2 = "=\"106/05/02\",=\"跨行轉入\",,,\"0\",\"22,170,217\",=\"0000125200462676\"";
		String o2 = CBCTBankTransferCSVProcessor.replaceNumberWithComma(t2);
		expected = "=\"106/05/02\",=\"跨行轉入\",,,\"0\",\"22170217\",=\"0000125200462676\"";
		assertEquals(null, expected, o2);
	} 
	@Test
	public void importFile() {
		String path = "C:\\Users\\JerryLin\\Desktop\\交易明細查詢.csv";
		File f = new File(path);
		processor.importFile(f);
	}
}
