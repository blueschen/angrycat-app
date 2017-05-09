package com.angrycat.erp.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.component.CBCTBankTransferCSVProcessor.CBCTBankTransfer;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.TransferReply;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class CBCTBankTransferCSVProcessorTests {
	@Autowired
	private CBCTBankTransferCSVProcessor processor;
	@Autowired
	private SessionFactoryWrapper sfw;

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
	public void findOnlyAccount(){
		String t1 = "0000143000004839";
		Matcher m1 = CBCTBankTransferCSVProcessor.ONLY_ACCOUNT.matcher(t1);
		assertTrue(m1.matches());
		
		String t2 = "812台新銀行";
		Matcher m2 = CBCTBankTransferCSVProcessor.ONLY_ACCOUNT.matcher(t2);
		assertFalse(m2.matches());
		
		String t3 = "0011*****52968";
		Matcher m3 = CBCTBankTransferCSVProcessor.ONLY_ACCOUNT.matcher(t3);
		assertTrue(m3.matches());
		
		String t4 = "0";
		Matcher m4 = CBCTBankTransferCSVProcessor.ONLY_ACCOUNT.matcher(t4);
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
	public void splitContinueComma(){
		String t1 = ",,";
		System.out.println(t1.split(",", -1).length);
	}
	@Test
	public void importBytes() throws IOException {
		String path = "C:\\Users\\JerryLin\\Desktop\\交易明細查詢.csv";
		processor.importBytes(Files.readAllBytes(Paths.get(path)));
	}
	@Test
	public void restoreData(){
		sfw.executeTransaction(s->{
			List<TransferReply> founds = s.createQuery("SELECT t FROM " + TransferReply.class.getName() + " t WHERE t.billChecked = :billChecked OR t.computerBillCheckNote IS NOT NULL")
				.setBoolean("billChecked", true)	
				.list();
			for(TransferReply f : founds){
				f.setBillChecked(false);
				f.setComputerBillCheckNote(null);
				s.update(f);
			}
			s.flush();
			s.clear();
		});		
	}
	@Test
	public void updateExactFound(){
		restoreData();
		
		List<CBCTBankTransfer> csvData = new ArrayList<>();
		
		CBCTBankTransfer csv1 = new CBCTBankTransfer();
		csv1.transferAmount = 2500;
		csv1.transferAccountCheck = "34112";
		csv1.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv1);
		
		CBCTBankTransfer csv2 = new CBCTBankTransfer();
		csv2.transferAmount = 2400;
		csv2.transferAccountCheck = "32121";
		csv2.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv2);
		
		CBCTBankTransfer csv3 = new CBCTBankTransfer();
		csv3.transferAmount = 2411;
		csv3.transferAccountCheck = "56112";
		csv3.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv3);
		
		processor.setCsvData(csvData);
		Map<String, String> msg = processor.updateTranferReplies();
		System.out.println(msg);
		
		String today = LocalDate.now().toString();
		
		sfw.executeSession(s->{		
			String exactQuery = "SELECT t "
					+ "FROM " + TransferReply.class.getName() + " t "
					+ "WHERE t.transferAccountCheck = :transferAccountCheck"
					+ " AND t.transferAmount = :transferAmount"
					+ " AND t.transferDate = :transferDate";
			
			csvData.stream().forEach(csv->{
				List<TransferReply> found = s.createQuery(exactQuery)
					.setString("transferAccountCheck", csv.transferAccountCheck)
					.setInteger("transferAmount", csv.transferAmount)
					.setDate("transferDate", csv.transferDate)
					.list();
					
				found.forEach(tr->{
//					System.out.println(ReflectionToStringBuilder.toString(tr, ToStringStyle.MULTI_LINE_STYLE));
					assertEquals(null, today, tr.getComputerBillCheckNote());
					assertTrue(tr.isBillChecked());
				});
			});
		});
	}
	@Test
	public void updateDateOnlyNotMatchFound(){
		restoreData();
		
		List<CBCTBankTransfer> csvData = new ArrayList<>();
		
		CBCTBankTransfer csv1 = new CBCTBankTransfer();
		csv1.transferAmount = 2500;
		csv1.transferAccountCheck = "34112";
		csv1.transferDate = Date.valueOf(LocalDate.of(2017, 4, 3));
		csvData.add(csv1);
		
		CBCTBankTransfer csv2 = new CBCTBankTransfer();
		csv2.transferAmount = 2400;
		csv2.transferAccountCheck = "32121";
		csv2.transferDate = Date.valueOf(LocalDate.of(2017, 4, 3));
		csvData.add(csv2);
		
		CBCTBankTransfer csv3 = new CBCTBankTransfer();
		csv3.transferAmount = 2411;
		csv3.transferAccountCheck = "56112";
		csv3.transferDate = Date.valueOf(LocalDate.of(2017, 4, 3));
		csvData.add(csv3);
		
		processor.setCsvData(csvData);
		Map<String, String> msg = processor.updateTranferReplies();
		System.out.println(msg);
		
		String computerNote = "僅轉帳日期不符";
		
		sfw.executeSession(s->{		
			String exactQuery = "SELECT t "
					+ "FROM " + TransferReply.class.getName() + " t "
					+ "WHERE t.transferAccountCheck = :transferAccountCheck"
					+ " AND t.transferAmount = :transferAmount";
			
			csvData.stream().forEach(csv->{
				List<TransferReply> found = s.createQuery(exactQuery)
					.setString("transferAccountCheck", csv.transferAccountCheck)
					.setInteger("transferAmount", csv.transferAmount)
					.list();
					
				found.forEach(tr->{
//					System.out.println(ReflectionToStringBuilder.toString(tr, ToStringStyle.MULTI_LINE_STYLE));
					assertEquals(null, computerNote, tr.getComputerBillCheckNote());
					assertFalse(tr.isBillChecked());
				});
			});
		});
	}
	@Test
	public void updateAmountOnlyNotMatchFound(){
		restoreData();
		
		List<CBCTBankTransfer> csvData = new ArrayList<>();
		
		CBCTBankTransfer csv1 = new CBCTBankTransfer();
		csv1.transferAmount = 2501;
		csv1.transferAccountCheck = "34112";
		csv1.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv1);
		
		CBCTBankTransfer csv2 = new CBCTBankTransfer();
		csv2.transferAmount = 2401;
		csv2.transferAccountCheck = "32121";
		csv2.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv2);
		
		CBCTBankTransfer csv3 = new CBCTBankTransfer();
		csv3.transferAmount = 2412;
		csv3.transferAccountCheck = "56112";
		csv3.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv3);
		
		processor.setCsvData(csvData);
		Map<String, String> msg = processor.updateTranferReplies();
		System.out.println(msg);
		
		String computerNote = "僅匯款金額不符";
		
		sfw.executeSession(s->{		
			String exactQuery = "SELECT t "
					+ "FROM " + TransferReply.class.getName() + " t "
					+ "WHERE t.transferAccountCheck = :transferAccountCheck"
					+ " AND t.transferDate = :transferDate";
			
			csvData.stream().forEach(csv->{
				List<TransferReply> found = s.createQuery(exactQuery)
					.setString("transferAccountCheck", csv.transferAccountCheck)
					.setDate("transferDate", csv.transferDate)
					.list();
					
				found.forEach(tr->{
//					System.out.println(ReflectionToStringBuilder.toString(tr, ToStringStyle.MULTI_LINE_STYLE));
					assertEquals(null, computerNote, tr.getComputerBillCheckNote());
					assertFalse(tr.isBillChecked());
				});
			});
		});
	}
	@Test
	public void updateCheckOnlyNotMatchFound(){
		restoreData();
		
		List<CBCTBankTransfer> csvData = new ArrayList<>();
		
		CBCTBankTransfer csv1 = new CBCTBankTransfer();
		csv1.transferAmount = 2500;
		csv1.transferAccountCheck = "34**2";
		csv1.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv1);
		
		CBCTBankTransfer csv2 = new CBCTBankTransfer();
		csv2.transferAmount = 2400;
		csv2.transferAccountCheck = "32**1";
		csv2.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv2);
		
		CBCTBankTransfer csv3 = new CBCTBankTransfer();
		csv3.transferAmount = 2411;
		csv3.transferAccountCheck = "56**2";
		csv3.transferDate = Date.valueOf(LocalDate.of(2017, 4, 5));
		csvData.add(csv3);
		
		processor.setCsvData(csvData);
		Map<String, String> msg = processor.updateTranferReplies();
		System.out.println(msg);
		
		String computerNote = "僅帳號後五碼不符";
		
		sfw.executeSession(s->{		
			String exactQuery = "SELECT t "
					+ "FROM " + TransferReply.class.getName() + " t "
					+ "WHERE t.transferAmount = :transferAmount"
					+ " AND t.transferDate = :transferDate";
			
			csvData.stream().forEach(csv->{
				List<TransferReply> found = s.createQuery(exactQuery)
					.setInteger("transferAmount", csv.transferAmount)
					.setDate("transferDate", csv.transferDate)
					.list();
					
				found.forEach(tr->{
//					System.out.println(ReflectionToStringBuilder.toString(tr, ToStringStyle.MULTI_LINE_STYLE));
					assertEquals(null, computerNote, tr.getComputerBillCheckNote());
					assertFalse(tr.isBillChecked());
				});
			});
		});
	}
}
