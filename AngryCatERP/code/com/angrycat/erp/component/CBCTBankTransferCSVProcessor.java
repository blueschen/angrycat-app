package com.angrycat.erp.component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.MinguoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.model.TransferReply;

@Component
@Scope("prototype")
/**
 * 處理中國信託轉帳csv檔<br>
 * 將資料轉成POJO以利後續使用
 * @author JerryLin
 *
 */
public class CBCTBankTransferCSVProcessor {

	static int titleIdx = -1;
	static final int 交易日期	= ++titleIdx; // 匯款日期
	static final int 摘要	= ++titleIdx;
	static final int 沖銷記號	= ++titleIdx;
	static final int 提款金額	= ++titleIdx;
	static final int 存款金額	= ++titleIdx; // 匯款金額
	static final int 結存金額	= ++titleIdx;
	static final int 備註	= ++titleIdx; // 匯款帳號後五碼在備註欄

	static final String BYPASS = "續上一筆";
	
	static final Pattern ONLY_ACCOUNT = Pattern.compile(".+\\d{5}$");
	static final Pattern ONLY_NUM_OR_COMMA = Pattern.compile("\\d+,?\\d*");
	static final Pattern FIND_NUM_WITH_COMMA = Pattern.compile("(\"\\d+(,\\d+){1,}\")");
	static final Pattern FIND_DATE = Pattern.compile("\\d{3}/\\d{2}/\\d{2}");
	
	@Autowired
	private SessionFactoryWrapper sfw;
	private List<CBCTBankTransfer> csvData;
	
	public List<CBCTBankTransfer> getCsvData(){
		return csvData;
	}
	public void setCsvData(List<CBCTBankTransfer> csvData){
		this.csvData = csvData;
	}
	
	public CBCTBankTransferCSVProcessor importBytes(byte[] bytes) {
		csvData = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));){
			String line = null;
			int count = 0;
			while((line = br.readLine()) != null) {
				count++;
				if(StringUtils.isBlank(line)
				|| !line.startsWith("=\"")
				|| count == 1 // 第一行是條件，兩兩對稱，第一項是條件名稱，第二項是條件值，依此類推
				|| count == 2 // 第二行是欄位名稱
				){
					continue;
				}
				
				line = removeCommaWithinNumber(line);				
				List<String> inputs = Arrays.asList(line.split(",", -1));
				if(inputs.size() <= 備註){
					continue;
				}
				if(inputs.get(摘要).contains(BYPASS)) { // 含有續上一筆代表用第二行表示同一筆剩下資料
					continue;
				}
				List<String> data = inputs.stream()
					.map((input) -> removeRedundant(input))
					.collect(Collectors.toList());
				
				String memo = data.get(備註).trim();
				String depositAmount = data.get(存款金額);
				String txDate = data.get(交易日期);
				
				if(!FIND_DATE.matcher(txDate).matches() // 應該有民國年交易日期
				|| memo.length() < 6
				|| !ONLY_ACCOUNT.matcher(memo).matches() // 備註要符合存款帳號後五碼格式
				|| StringUtils.isBlank(depositAmount)
				|| "0".equals(depositAmount)
				|| !StringUtils.isNumeric(depositAmount)){ // 存款金額只能為數字，且要大於0 ==>代表轉入
					continue;
				}
				
				CBCTBankTransfer cbct = new CBCTBankTransfer();
				cbct.lineCount = count;
				cbct.transferDate = toSqlDateFromROC(txDate);
				cbct.rocDate = txDate;
				cbct.transferAmount = formatNumber(depositAmount).intValue();
				cbct.transferAccountCheck = retrieveTransferAccountCheck(memo);
				csvData.add(cbct);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
//		for debug
//		for(CBCTBankTransfer cbct : csvData){
//			System.out.println(ReflectionToStringBuilder.toString(cbct, ToStringStyle.MULTI_LINE_STYLE));
//		}
//		System.out.println("effective csv count: " + csvData.size());
		return this;
	}
	
	/**
	 * HQL對於NOT IN語法的支援有限，所以自行轉換陣列字串
	 * @param data
	 * @return
	 */
	static String convertParameterListToSql(List<String> data){
		if(data.isEmpty()){
			return "";
		}
		String output = data.stream().collect(Collectors.joining("','"));
		output = "'" + output + "'";
		return output;
	}
	/**
	 * 用csv資料對帳
	 * TODO 是否要綁定異動記錄
	 */
	public Map<String, String> updateTranferReplies(){
		List<CBCTBankTransfer> data = csvData;
		Map<String, String> msg = new LinkedHashMap<>();
		
		int oriTotal = data.size();
		String today = LocalDate.now().toString();
		sfw.executeTransaction(s->{
			List<CBCTBankTransfer> remainings = new ArrayList<>();
			List<String> updatedIds = new ArrayList<>();
			
			String exactQuery = "SELECT t "
				+ "FROM " + TransferReply.class.getName() + " t "
				+ "WHERE t.transferAccountCheck = :transferAccountCheck"
				+ " AND t.transferAmount = :transferAmount"
				+ " AND t.transferDate = :transferDate"
				+ " AND t.billChecked = :billChecked";
			int billCheckedCount = 0;		
			for(CBCTBankTransfer d : data){
				List<TransferReply> exactFound = s.createQuery(exactQuery)
					.setString("transferAccountCheck", d.transferAccountCheck)
					.setInteger("transferAmount", d.transferAmount)
					.setDate("transferDate", d.transferDate)
					.setBoolean("billChecked", false) // 尚未對帳成功
					.list();
				
				int exactCount = exactFound.size();
				if(exactCount > 0){
					for(TransferReply f : exactFound){
						f.setBillChecked(true);
						f.setComputerBillCheckNote(d.lineCountf()+today);
						s.update(f);
						updatedIds.add(f.getId());
						++billCheckedCount;
					}
				}else{
					remainings.add(d);
				}
			}
			
			String ids = convertParameterListToSql(updatedIds);
			String excludeIds = !ids.isEmpty() ? " AND t.id NOT IN ("+ ids +")" : ids;
			
			String dateOnlyNotMatch = "SELECT t "
				+ "FROM " + TransferReply.class.getName() + " t "
				+ "WHERE t.transferAccountCheck = :transferAccountCheck"
				+ " AND t.transferAmount = :transferAmount"
				+ " AND t.billChecked = :billChecked"
				+ excludeIds;
			
			String amountOnlyNotMatch = "SELECT t "
				+ "FROM " + TransferReply.class.getName() + " t "
				+ "WHERE t.transferAccountCheck = :transferAccountCheck"
				+ " AND t.transferDate = :transferDate"
				+ " AND t.billChecked = :billChecked"
				+ excludeIds;
			
			String checkOnlyNotMatch = "SELECT t "
				+ "FROM " + TransferReply.class.getName() + " t "
				+ "WHERE t.transferAmount = :transferAmount"
				+ " AND t.transferDate = :transferDate"
				+ " AND t.billChecked = :billChecked"
				+ excludeIds;
			
			int dateNotMatched = 0;
			int amountNotMatched = 0;
			int checkNotMatched = 0;
			
			for(CBCTBankTransfer d : remainings){
				List<TransferReply> dateOnlyNotMatchFound = s.createQuery(dateOnlyNotMatch)
					.setString("transferAccountCheck", d.transferAccountCheck)
					.setInteger("transferAmount", d.transferAmount)
					.setBoolean("billChecked", false)
					.list();
				
				if(dateOnlyNotMatchFound.size() > 0){
					for(TransferReply f : dateOnlyNotMatchFound){
						f.setComputerBillCheckNote("轉帳日期:" + d.lineCountf() + d.rocDate);
						s.update(f);
						++dateNotMatched;
					}
					continue;
				}
				List<TransferReply> amountOnlyNotMatchFound = s.createQuery(amountOnlyNotMatch)
					.setString("transferAccountCheck", d.transferAccountCheck)
					.setDate("transferDate", d.transferDate)
					.setBoolean("billChecked", false)
					.list();
				if(amountOnlyNotMatchFound.size() > 0){
					for(TransferReply f: amountOnlyNotMatchFound){
						f.setComputerBillCheckNote("匯款金額:" + d.lineCountf() + d.transferAmount);
						s.update(f);
						++amountNotMatched;
					}
					continue;
				}
				List<TransferReply> checkOnlyNotMatchFound = s.createQuery(checkOnlyNotMatch)
					.setInteger("transferAmount", d.transferAmount)
					.setDate("transferDate", d.transferDate)
					.setBoolean("billChecked", false)
					.list();
				if(checkOnlyNotMatchFound.size() > 0){
					for(TransferReply f: checkOnlyNotMatchFound){
						f.setComputerBillCheckNote("帳後五碼:" + d.lineCountf() + d.transferAccountCheck);
						s.update(f);
						++checkNotMatched;
					}
				}
			}
			msg.put("匯入筆數", String.valueOf(oriTotal));
			msg.put("對帳成功筆數", String.valueOf(billCheckedCount));
			msg.put("僅轉帳日期不符筆數", String.valueOf(dateNotMatched));
			msg.put("僅匯款金額不符筆數", String.valueOf(amountNotMatched));
			msg.put("僅帳號後五碼不符筆數", String.valueOf(checkNotMatched));
			
			s.flush();
			s.clear();
		});
		return msg;
	}
	
	/**
	 * 因為csv裡面的金額達千分位含逗點，跟分隔符號相混，會造成切割單位錯誤<br>
	 * 所以在此先把金額裡面的逗點移除掉，以利後續處理
	 * @param input
	 * @return
	 */
	static String removeCommaWithinNumber(String input) {
		Matcher m = CBCTBankTransferCSVProcessor.FIND_NUM_WITH_COMMA.matcher(input);
		String r = input;
		while(m.find()){
			String g1 = m.group(1);
			String replacement = g1.replaceAll(",", "");
			r = r.replace(g1, replacement);
		}
		return r;
	}
	/**
	 * 每一欄資料格式皆是"結尾<br>
	 * 但起始可能是="或"<br>
	 * 移除這些多餘的部分就可以得到真正資料主體
	 * @param input
	 * @return
	 */
	static String removeRedundant(String input){
		input = StringUtils.removeStart(input, "=\"");
		input = StringUtils.removeStart(input, "\"");
		input = StringUtils.removeEnd(input, "\"");
		return input;
	}
	/**
	 * 如果字串有千分位逗點，也可以順利轉成數值(實際型別為Long)
	 * @param input
	 * @return
	 */
	static Number formatNumber(String input) {
		NumberFormat format = NumberFormat.getInstance();
		Number number = null;
		try{
			number = format.parse(input);
		}catch(Throwable e) {
			throw new RuntimeException(e);
		}
		return number;
	}
	/**
	 * 將民國時間yyy/MM/dd轉為sql.Date<br>
	 * ref. http://152.92.236.11/tutorial_java/datetime/iso/nonIso.html
	 * @param text
	 * @return
	 */
	static Date toSqlDateFromROC(String text) {
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        Chronology chrono = MinguoChronology.INSTANCE;
        String pattern = "yyy/M/d";
        DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient()
                              .appendPattern(pattern)
                              .toFormatter()
                              .withChronology(chrono)
                              .withDecimalStyle(DecimalStyle.of(locale));
        TemporalAccessor temporal = df.parse(text);
        ChronoLocalDate cDate = chrono.date(temporal);
        LocalDate ld = LocalDate.from(cDate);
        return Date.valueOf(ld);
	}
	/**
	 * 取得帳號末五碼
	 * @param input
	 * @return
	 */
	static String retrieveTransferAccountCheck(String input){
		if(input.length() < 5){
			return "";
		}
		int lastIdx = input.length();
		int firstIdx = lastIdx - 5;
		return input.substring(firstIdx, lastIdx);
	}
	public static class CBCTBankTransfer {
		public int lineCount;
		public String transferAccountCheck;
		public Date transferDate;
		public String rocDate;
		public int transferAmount;
		public String lineCountf(){
			return "("+ lineCount +")";
		}
	}
}
