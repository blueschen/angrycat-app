package com.angrycat.erp.shortnews;

import static com.angrycat.erp.condition.ConditionFactory.propertyDesc;
import static com.angrycat.erp.condition.ConditionFactory.putInt;
import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.function.ConsumerThrowable;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.service.TimeService;

import static com.angrycat.erp.common.EmailContact.*;
/**
 * 三竹簡訊服務，主要搭配會員查詢功能
 * @author JerryLin
 *
 */
@Service
@Scope("prototype")
public class MitakeSMSHttpPost {
	private static final String sGeneralURL = "http://smexpress.mitake.com.tw/SmSendPost.asp"; // 發送多筆一般簡訊 URL
	private static final String sLongURL = "http://smexpress.mitake.com.tw:7003/SpLmPost"; // 發送多筆長簡訊 URL
	private static final String sUserName = "0975009776"; // 使用者帳號
	private static final String sPassword = "27761505"; // 使用者密碼
	private static final long lTimeout = 30000; // 逾時時間(單位:毫秒)
	private static String sEncoding = "Big5";
	public static final String GENERAL_CONNECT_URL = sGeneralURL + "?username=" + sUserName + "&password=" + sPassword + "&encoding=" + sEncoding;
	public static final String LONG_CONNECT_URL = sLongURL + "?username=" + sUserName + "&password=" + sPassword + "&encoding=" + sEncoding;
	
	private static final String SERIAL_NO = "serialNo"; // 流水號
	private static final String DEST_NAME = "DestName"; // 收訊人名稱
	private static final String DST_ADDR = "dstaddr"; // 受訊方手機號碼
	private static final String SM_BODY = "smbody"; // 簡訊內容
	private static final String DLV_TIME = "dlvtime"; // 簡訊預約時間
	private static final String VLD_TIME = "vldtime"; // 簡訊有效期限
	private static final String RESPONSE = "response"; // 狀態回報網址
	private static final String CLIENT_ID = "ClientID"; // 客戶簡訊ID
	
	private static final String T_SERIAL_NO = betweenBraces(SERIAL_NO); // 流水號
	private static final String T_DEST_NAME = betweenBraces(DEST_NAME); // 收訊人名稱
	private static final String T_DST_ADDR = betweenBraces(DST_ADDR); // 受訊方手機號碼
	private static final String T_SM_BODY = betweenBraces(SM_BODY); // 簡訊內容
	private static final String T_DLV_TIME = betweenBraces(DLV_TIME); // 簡訊預約時間
	
	private static final String POST_MSG_TEMPLATE = "["+T_SERIAL_NO+"]\r\n"
													+ "dstaddr="+T_DST_ADDR+"\r\n"
													+ "smbody="+T_SM_BODY+"\r\n";
	
	private static final String TIMEOUT_READ_PROP_NAME = "sun.net.client.defaultReadTimeout";
	private static final String TIMEOUT_CONNECT_PROP_NAME = "sun.net.client.defaultConnectTimeout";
	
	public static final String NO_DATA_FOUND_STOP_SEND_SHORT_MSG = "沒有符合條件的資料，終止發送簡訊過程";
	
	@Autowired
	@Qualifier("queryBaseService")
	private QueryBaseService<Member, Member> memberQueryService;
	@Autowired
	private TimeService timeService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	private String url = GENERAL_CONNECT_URL;
	private boolean testMode;
	private int memberCount;

	public void setUrl(String url) {
		this.url = url;
	}
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	@PostConstruct
	public void initQuery(){
		memberQueryService.setRootAndInitDefault(Member.class);
		// 條件1:生日月份
		// 條件3:同一筆VIP折扣紀錄到期日等於或大/晚於系統執行時間
		// 條件4:同一筆VIP折扣紀錄沒有使用過
		memberQueryService.createAssociationAlias("join p.vipDiscountDetails", "detail", null)
			.addWhere(putInt("month(p.birthday) = :pBirthday"))
//			.addWhere(putSqlDate("detail.effectiveStart <= :pEffectiveStart", timeService.atStartOfToday())) // 因為設定每個月1日發送簡訊，所以不用考慮有效起始日
			.addWhere(putSqlDate("detail.effectiveEnd >= :pEffectiveEnd")) // 資料庫VIP結束日存的是當天的起點(00:00:00)，所以不能拿某天系統的當下做為比較基準，這樣同一天的資料會被過濾掉
			.addWhere(propertyDesc("detail.discountUseDate IS NULL"));
	}	
	private void initShortMsgConfig(){
		if(StringUtils.isBlank(System.getProperty(TIMEOUT_READ_PROP_NAME))){
			System.setProperty(TIMEOUT_READ_PROP_NAME, ""+lTimeout);
		}
		if(StringUtils.isBlank(System.getProperty(TIMEOUT_CONNECT_PROP_NAME))){
			System.setProperty(TIMEOUT_CONNECT_PROP_NAME, ""+lTimeout);
		}
	}
	/**
	 * 只寄給自己看看簡訊對不對
	 */
	private static void testSendSelf(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		MitakeSMSHttpPost bean = acac.getBean(MitakeSMSHttpPost.class);
		bean.sendPostShortMsg("11/5-11/11 OHM誠品敦南專櫃滿5000送500，VIP再享9折優惠，詳情請洽02-27716304");
		acac.close();
	}
	public static void main(String[]args){
//		MitakeSMSHttpPost SMS = new MitakeSMSHttpPost();
//		SMS.testSendPostShortMsg();
//		testSendBirthMonthMsg();
//		testGetLocalDateTime();
//		testQueryMembers();
		testSendShortMsgToBirthMonth();
//		testSendSelf();
//		testSendShortMsg();
	}
	private static String betweenBraces(String name){
		return "{" + name + "}";
	}
	/**
	 * 設定主動回報網址
	 */
	private static byte[] getActiveResponseConfig()throws Throwable{
		return "response=http://192.168.1.200/smreply.asp\r\n".getBytes(sEncoding);
	}
	/**
	 * 設定傳送訊息必填欄位
	 */
	private static byte[] getRequiredConfig(int idx, String mobile, String content)throws Throwable{
		String serialNo = StringUtils.leftPad(""+idx, 3, "0");
		String sendConfig = POST_MSG_TEMPLATE.replace(T_SERIAL_NO, serialNo)
											.replace(T_DST_ADDR, mobile)
											.replace(T_SM_BODY, content);
		System.out.println(sendConfig);
		return sendConfig.getBytes(sEncoding);
	}
	private static List<Member> getTestMembers(){
		List<Member> members = new ArrayList<>();
		Member m1 = new Member();
		m1.setMobile("0972981126");
		members.add(m1);
		return members;
	}
	/**
	 * 一般簡訊，70個中文字含標點符號為一則扣一點 
	 * 長簡訊，67個中文字含標點符號為一則扣一點
	 * 
	 */
	public void sendPostShortMsg(ConsumerThrowable<DataOutputStream> configureSendData, ConsumerThrowable<BufferedReader> returnMsg){
		initShortMsgConfig();
		HttpURLConnection huc = null;
		DataOutputStream out = null;
		BufferedReader buReader = null;
		String errTrace = null;
		try{
			huc = (HttpURLConnection)new URL(url).openConnection();
			huc.setInstanceFollowRedirects(true);
			huc.setDoInput(true);
			huc.setDoOutput(true);
			huc.setUseCaches(false);
			huc.setRequestMethod("POST");
			huc.setRequestProperty("Accept", "*/*");
			huc.setRequestProperty("Accept-Language", "zh-tw");
			huc.setRequestProperty("Content-Type", "text/html");
			huc.setRequestProperty("Accept-Encoding", "gzip, deflate");
			huc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
			
			out = new DataOutputStream(huc.getOutputStream());
			if(configureSendData != null){
				configureSendData.accept(out);
			}
			
			out.flush();
			out.close();

			buReader = new BufferedReader(new InputStreamReader(huc.getInputStream(), sEncoding));
			if(returnMsg != null){
				returnMsg.accept(buReader);
			}
		}catch(Throwable e){
			errTrace = ExceptionUtils.getStackTrace(e);			
			e.printStackTrace();
		}finally{
			try{
				if(buReader != null){
					buReader.close();
				}
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				buReader = null;
			}
			try{
				if(out != null){
					out.close();
				}
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				out = null;
			}
			huc = null;
			if(StringUtils.isNotBlank(errTrace)){
				String sendMsg = errTrace;
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
				simpleMailMessage.setTo(JERRY);
				simpleMailMessage.setText(sendMsg);
				simpleMailMessage.setSubject("MitakeSMSHttpPost.sendPostShortMsg執行發生錯誤");
				String[] cc = new String[]{BLUES};
				simpleMailMessage.setCc(cc);
				mailSender.send(simpleMailMessage);
			}
		}
	}
	/**
	 * 如果每一個發送對象的簡訊都是一樣的，就呼叫這個API
	 * @param queryHql
	 * @param params
	 * @param content
	 * @return
	 */
	public StringBuffer sendShortMsgToMembers(String queryHql, Map<String, Object> params, String content){
		return sendShortMsgToMembers(queryHql, params, (m->{return content;}));
	}
	/**
	 * 如果需要隨不同發送對象，簡訊也不一樣，就使用這個API
	 * @param queryHql
	 * @param params
	 * @param genContent
	 * @return
	 */
	public StringBuffer sendShortMsgToMembers(String queryHql, Map<String, Object> params, Function<Member, String> genContent){
		List<Member> members = sfw.executeSession(s->{
			List<Member> results = s.createQuery(queryHql).setProperties(params).list();
			return results;
		});
		
		final StringBuffer sb = new StringBuffer();
		if(testMode){
			String testMsg = "這是測試模式，只會顯示查到的資料，不會真的去發簡訊...";
			sb.append(testMsg);
			System.out.println(testMsg);
			System.out.println("共查到" + members.size() + "筆");
			members.forEach(m->{
				System.out.println(m.getName() + "|" + m.getMobile() + m.getToVipDate() + "|" + m.getToVipEndDate());
//				System.out.println(ReflectionToStringBuilder.toString(m, ToStringStyle.MULTI_LINE_STYLE)); // enable this statement, must take consideration in lazy loading issue
				System.out.println(genContent.apply(m));
			});
			return sb;
		}
		if(members == null || members.isEmpty()){
			sb.append(NO_DATA_FOUND_STOP_SEND_SHORT_MSG);
			return sb;
		}
		sendPostShortMsg(out->{
			int currentCount = 0;
			int effectiveCount = 0;
			int runtimeErrCount = 0;
			
			sb.append("發送清單:\n");
			boolean runtimeError = false;
			for(int i = 0; i < members.size(); i++){
				Member member = members.get(i);
				String mobile = member.getMobile();
				String name = member.getName();
				currentCount++;
				if(isMobile(mobile)){
					++effectiveCount;
					try{
						byte[] config = getRequiredConfig(effectiveCount, mobile, genContent.apply(member));
						out.write(config);
//						out.write("".getBytes(sEncoding));
						sb.append(name + "|" + mobile + "\n");
					}catch(Throwable e){
						// TODO
						String trace = ExceptionUtils.getStackTrace(e);
						sb.append(mobile + "設定必填欄位錯誤:\n" + trace);
						runtimeError = true;
						++runtimeErrCount;
					}
				}
				
			}
			if(runtimeError){
				String sendMsg = sb.toString();
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
				simpleMailMessage.setTo(JERRY);
				simpleMailMessage.setText(sendMsg);
				simpleMailMessage.setSubject("MitakeSMSHttpPost.sendShortMsgToMembers設定簡訊執行錯誤");
				String[] cc = new String[]{BLUES};
				simpleMailMessage.setCc(cc);
				mailSender.send(simpleMailMessage);
			}
			memberCount = currentCount;
			sb.append("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆\n");
			System.out.println("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆");
		},buReader->{
			String msg = null;
			sb.append("發送簡訊後回傳訊息:\n");
			while((msg = buReader.readLine()) != null){
				System.out.println(msg);
				sb.append(msg + "\n");
				if(msg.contains("AccountPoint")){
					String[] s = msg.split("=");
					String remainingPoints = s[1];
					int remaining = Integer.parseInt(remainingPoints);
					if(remaining <= 500){						
						String sendMsg = "簡訊點數即將用完，請盡快儲值，剩餘點數: " + remainingPoints;
						SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
						simpleMailMessage.setTo(JOYCE);
						simpleMailMessage.setText(sendMsg);
						simpleMailMessage.setSubject("簡訊點數即將用完請協助儲值");
						String[] cc = new String[]{IFLY,
													BLUES,
													JERRY};
						simpleMailMessage.setCc(cc);
						mailSender.send(simpleMailMessage);
					}
				}
			}
		});
		
		return sb;
	}
	
	/**
	 * @deprecated
	 * 發送簡訊給會員，如果有特殊查詢條件，應在呼叫此method之前就設定完畢，沒有指定就是查出資料庫所有會員資料。
	 * 如果中途有例外發生，他會繼續下去，把錯誤資料秀在主控台
	 * @param content
	 */
	public StringBuffer sendShortMsgToMembers(final String content){
		final StringBuffer sb = new StringBuffer();
		if(StringUtils.isBlank(content)){
			sb.append("沒有提供簡訊內容");
			return sb;
		}else{
			sb.append("簡訊內容: " + content + "\n");
			sb.append("發送訊息字數: " + content.length() + "\n");
		}
		sendPostShortMsg(out->{
			if(testMode){
				sb.append("執行測試模式...僅列出符合資格之會員...不會寄出簡訊");
				List<Member> members = getTestMembers();
				for(int i = 0; i < members.size(); i++){
					Member m = members.get(i);
					int serial = i+1;
					String mobile = m.getMobile();
					if(isMobile(mobile)){
						byte[] config = getRequiredConfig(serial, mobile, content);
						out.write(config);
//						out.write("".getBytes(sEncoding));
					}
				}
			}else{
				sb.append("開始設定設定簡訊\n");
				memberQueryService.executeScrollableQuery((rs, sfw)->{
					int batchSize = sfw.getBatchSize();
					Session s = sfw.currentSession();
					int currentCount = 0;
					int effectiveCount = 0;
					int runtimeErrCount = 0;
					while(rs.next()){
						++currentCount;
						Member member = (Member)rs.get(0);
						String mobile = member.getMobile();
						if(isMobile(mobile)){
							++effectiveCount;
							try{
								byte[] config = getRequiredConfig(effectiveCount, mobile, content);
								out.write(config);
//								out.write("".getBytes(sEncoding));
							}catch(Throwable e){
								// TODO
								String trace = ExceptionUtils.getStackTrace(e);
								sb.append(mobile + ":設定簡訊必填欄位過程發生錯誤\n" + trace);
								++runtimeErrCount;
							}
						}
						if(currentCount % batchSize == 0){
							s.flush();
							s.clear();
						}
					}
					memberCount = currentCount;
					sb.append("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆\n");
					System.out.println("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆");
					return null;
				});
			}

		},buReader->{
			String msg = null;
			sb.append("發送簡訊後回傳訊息:\n");
			while((msg = buReader.readLine()) != null){
				System.out.println(msg);
				sb.append(msg + "\n");
				if(msg.contains("AccountPoint")){
					String[] s = msg.split("=");
					String remainingPoints = s[1];
					int MINIMUM = Integer.parseInt(remainingPoints) + 100;
					if(MINIMUM <= memberCount){
						sb.append("簡訊點數即將用完，請盡快儲值。會員人數: " + memberCount + ", 剩餘點數: " + remainingPoints + "\n");
					}
				}
			}
		});
		System.out.println("sb result: " + sb.toString());
		return sb;
	}
	/**
	 * 查詢會員，顯示資料，主要是用來測試查詢條件與結果是否相符
	 */
	public StringBuffer queryMembers(){
		StringBuffer sb = new StringBuffer();
		memberQueryService.executeScrollableQuery((rs, sfw)->{
			int batchSize = sfw.getBatchSize();
			Session s = sfw.currentSession();
			int currentCount = 0;
			sb.append("執行測試模式...\n");
			while(rs.next()){
				++currentCount;
				Member member = (Member)rs.get(0);
				String mobile = member.getMobile();
				if(isMobile(mobile)){
					System.out.println("name: " + member.getName()+ "mobile: " + mobile + ", birth: " + member.getBirthday());
					sb.append("name: " + member.getName()+ "mobile: " + mobile + ", birth: " + member.getBirthday() + "\n");
				}else{
					System.out.println("手機有誤 name: " + member.getName()+ "mobile: " + mobile + ", birth: " + member.getBirthday());
					sb.append("手機有誤 name: " + member.getName()+ "mobile: " + mobile + ", birth: " + member.getBirthday());
				}
				if(currentCount % batchSize == 0){
					s.flush();
					s.clear();
				}
			}
			System.out.println("total count: " + currentCount);
			sb.append("total count: " + currentCount);
			return null;
		});
		return sb;
	}
	/**
	 * @deprecated
	 * 發送訊息給指定生日月份的會員
	 * @param birthMonth
	 * @param content
	 */
	public StringBuffer sendShortMsgToBirthMonth(int birthMonth, String content){		
//		System.out.println("msg: " + content);
//		memberQueryService.getSimpleExpressions().get("pBirthday").setValue(birthMonth);
//		memberQueryService.getSimpleExpressions().get("pEffectiveEnd").setValue(timeService.atStartOfToday());
//		QueryGenerator qg = memberQueryService.toQueryGenerator();
//		String query = qg.toCompleteStr();
//		System.out.println("執行hql:\n" + query);
//		System.out.println("查詢條件");
//		Map<String, Object> params = qg.getParams();
//		params.forEach((k,v)->{
//			System.out.println(k + ": " + v);
//		});
				
		String hql = "SELECT p "
				+ "FROM com.angrycat.erp.model.Member p "
				+ "join p.vipDiscountDetails detail "
				+ "WHERE month(p.birthday) = (:pBirthday) "
				+ "AND detail.effectiveEnd >= (:pEffectiveEnd) "
				+ "AND detail.discountUseDate IS NULL";
		
		Map<String, Object> params = new HashMap<>();
		params.put("pBirthday", birthMonth);
		params.put("pEffectiveEnd", timeService.atStartOfToday());
		
		StringBuffer sb = null;
		sb = sendShortMsgToMembers(hql, params, content);
		return sb;
	}
	/**
	 * 設定發送生日活動簡訊，如要使用，資料庫要切換到NAS主機，而且要關閉程式的測試模式
	 */
	private static void testSendShortMsgToBirthMonth(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		MitakeSMSHttpPost bean = acac.getBean(MitakeSMSHttpPost.class);
		bean.setTestMode(true);
		int month = 1;
		String msg = "OHM Beads祝您生日快樂，"+month+"月壽星可享單筆訂單8折優惠，誠品敦南專櫃與網路通路皆可使用，詳情請洽02-27716304";
		bean.sendShortMsgToBirthMonth(month, msg);
		acac.close();
	}
	private static void testQueryMembers(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		MitakeSMSHttpPost bean = acac.getBean(MitakeSMSHttpPost.class);
		bean.queryMembers();
		acac.close();
	}
	/**
	 * 是否為有效手機號碼
	 * @param mobile
	 * @return
	 */
	private boolean isMobile(String mobile){
		if(StringUtils.isBlank(mobile) || !mobile.trim().startsWith("09") || mobile.trim().length() != 10){
//			System.out.println("不是手機號碼: " + mobile);
			return false;
		}
		return true;
	}
	/**
	 * 寄送給所有會員簡訊
	 * 正式發送前，要先轉換資料庫連線到NAS正式機上
	 */
	private static void testSendShortMsg(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		MitakeSMSHttpPost bean = acac.getBean(MitakeSMSHttpPost.class);
		bean.sendShortMsgToMembers("11/5-11/11 OHM誠品敦南專櫃滿5000送500，VIP再享9折優惠，詳情請洽02-27716304");
		
		acac.close();
	}
	/**
	 * 主要的測試程式
	 */
	public void sendPostShortMsg(String content){
		sendPostShortMsg(out->{
			System.out.println("發送訊息字數: " + content.length());
			out.write("[101]\r\n".getBytes(sEncoding));
			out.write("DestName=JerryLin\r\n".getBytes(sEncoding));
			out.write("dstaddr=0972981126\r\n".getBytes(sEncoding));
			out.write(("smbody="+content+"\r\n").getBytes(sEncoding));

			out.write(getActiveResponseConfig());
//			out.write("dlvtime=20151028095800\r\n".getBytes(sEncoding));

//			out.write("[102]\r\n".getBytes(sEncoding));
//			out.write("DestName=給Joyce\r\n".getBytes(sEncoding));
//			out.write("dstaddr=0931387210\r\n".getBytes(sEncoding));
//			out.write("smbody=對面阿桌發測試簡訊\r\n".getBytes(sEncoding));
//			out.write("dlvtime=20151028095800\r\n".getBytes(sEncoding));
//
//			out.write("[103]\r\n".getBytes(sEncoding));
//			out.write("DestName=小明\r\n".getBytes(sEncoding));
//			out.write("dstaddr=0999000000\r\n".getBytes(sEncoding));
//			out.write("smbody=我是測試3\r\n".getBytes(sEncoding));
//			out.write("dlvtime=20100720120000\r\n".getBytes(sEncoding));
		
		}, buReader->{
			String sLine = "";
			while((sLine = buReader.readLine()) != null){
				System.out.println(sLine);
			}
		
		});
	}
	
	private static String getLocalDateTime(){
		LocalDateTime t = LocalDateTime.now();
		return t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
	}
	
	private static void testGetLocalDateTime(){
		System.out.println(getLocalDateTime());
	}
}
