package com.angrycat.erp.shortnews;

import static com.angrycat.erp.common.EmailContact.BLUES;
import static com.angrycat.erp.common.EmailContact.IFLY;
import static com.angrycat.erp.common.EmailContact.JERRY;
import static com.angrycat.erp.common.EmailContact.JOYCE;
import static com.angrycat.erp.common.EmailContact.MIKO;
import static com.angrycat.erp.condition.ConditionFactory.propertyDesc;
import static com.angrycat.erp.condition.ConditionFactory.putInt;
import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.angrycat.erp.test.BaseTest;
/**
 * 三竹簡訊服務，主要搭配會員查詢功能
 * @author JerryLin
 *
 */
@Service
@Scope("prototype")
public class MitakeSMSHttpPost {
	private static final String multiShortURL = "https://smexpress.mitake.com.tw:8800/SmSendPost"; // 發送多筆短簡訊 URL // TODO 這個網址有誤，先不要用這個
	private static final String multiLongURL = "https://smexpress.mitake.com.tw:7103/SpLmPost"; // 發送多筆長簡訊 URL
	private static final String sUserName = ""; // 使用者帳號
	private static final String sPassword = ""; // 使用者密碼
	private static final long lTimeout = 30000; // 逾時時間(單位:毫秒)
	private static String sEncoding = "Big5";
	public static final String SHORT_CONNECT_URL = multiShortURL + "?username=" + sUserName + "&password=" + sPassword + "&encoding=" + sEncoding;
	public static final String LONG_CONNECT_URL = multiLongURL + "?username=" + sUserName + "&password=" + sPassword + "&encoding=" + sEncoding;
	
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
	private static final String T_VLD_TIME = betweenBraces(VLD_TIME); // 簡訊有效期限
	
	private static final String SHORT_POST_MSG_TEMPLATE = "["+T_SERIAL_NO+"]\r\n"
													+ "dstaddr="+T_DST_ADDR+"\r\n"
													+ "smbody="+T_SM_BODY+"\r\n";
	private static final String LONG_POST_MSG_TEMPLATE = T_SERIAL_NO
													+ "$$"+T_DST_ADDR
													+ "$$" // 簡訊預約時間
													+ "$$" // 簡訊有效時間
													+ "$$" // 收訊人名稱
													+ "$$" // 狀態回報網址
													+ "$$"+T_SM_BODY // 簡訊內容
													+ "\r\n" // 斷行分隔不同簡訊--這行一定要，否則所有簡訊會視為同一則而發給最後一個人
													;

	
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
	
	private String url = LONG_CONNECT_URL;
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
//		testSendShortMsgToBirthMonth();
//		testSendSelf();
//		testSendShortMsg();
//		shortMsgNotify20160401Activity();
		shortMsgNotifyForTesting();
//		strLen();
//		shortMsgNotify20160429Activity(); // 4/29活動簡訊
//		shortMsgNotify20160530Activity(); // 5/30活動簡訊
//		shortMsgNotify20160623Activity(); // 6/23活動簡訊
//		shortMsgNotify20160714Activity();
//		testUrlEncodeToBig5();
//		uuidLen();
	}
	
	private static void uuidLen(){
		System.out.println(UUID.randomUUID().toString().length());
	}
	
	private static void testUrlEncodeToBig5(){
		String content1 = "7/15-7/19 敦南誠品滿千送百，OHM加碼滿8000再折600，最高單筆可折1100，社團同步優惠中，詳情請洽FB粉絲團或02-27761505";
		String content2 = "7/15-7/19 敦南誠品滿千送百，OHM加碼滿8000再折600，最高單筆可折1100，社團同步優惠中FB粉絲團或洽02-27761505";
		String encode = urlEncodeToBig5(content2);
		System.out.println(encode);
	}
	
	private static String urlEncodeToBig5(String content){
		String encode = "";
		try {
			encode = URLEncoder.encode(content, "Big5").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return encode;
	}
	
	private static void strLen(){
		String content = "7/15-7/19 敦南誠品滿千送百，OHM加碼滿8000再折600，最高單筆可折1100，社團同步優惠中，詳情請洽FB粉絲團或02-277";
		System.out.println(content.length());
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
	private byte[] getRequiredConfig(int idx, String mobile, String content)throws Throwable{
		String template = (url == LONG_CONNECT_URL ? LONG_POST_MSG_TEMPLATE : SHORT_POST_MSG_TEMPLATE);
//		String serialNo = StringUtils.leftPad(""+idx, 3, "0");
		String serialNo = UUID.randomUUID().toString();
		String sendConfig = template.replace(T_SERIAL_NO, serialNo)
									.replace(T_DST_ADDR, mobile)
									.replace(T_SM_BODY, content);
		System.out.println("getRequiredConfig\n" + sendConfig);
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
	 * 短簡訊，70個中文字含標點符號為一則扣一點 ，超過70字不會自動扣點，只會截斷超過字數的訊息寄出
	 * 長簡訊，70個中文字含標點符號為一則扣一點，超過70字會自動扣點--只有智慧手機能正確收到長簡訊，舊型手機不支援長簡訊，超過70字會變成亂碼；
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
				System.out.println(m.getName() + "|" + m.getMobile() + "|" + m.getToVipDate() + "|" + m.getToVipEndDate());
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
						throw new RuntimeException(e);
					}
				}
				
			}
			memberCount = currentCount;
			sb.append("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆\n");
			System.out.println("查詢結果: " + currentCount + " 筆，有效資料: " + effectiveCount + "筆，執行錯誤: " + runtimeErrCount + "筆");
		},buReader->{
			String msg = null;
			sb.append("發送簡訊後回傳訊息:\n");
			String STATUS_INSTANT_SUCCESS = "1"; // 即時簡訊發送成功
			String STATUS_RESERV_SUCCESS = "0"; // 預約簡訊發送成功
			String statuscode = "";
			String duplicate = "";
			while((msg = buReader.readLine()) != null){
				System.out.println(msg);
				sb.append(msg + "\n");
				if(msg.contains("statuscode")){
					String[] s = msg.split("=");
					statuscode = s[1];
				}
				if(msg.contains("Duplicate")){
					String[] s = msg.split("=");
					duplicate = s[1];
				}
				if(msg.contains("AccountPoint")
				&& !duplicate.equals("Y") // 代表不是重複發送
				&& (statuscode.equals(STATUS_INSTANT_SUCCESS)
					|| statuscode.equals(STATUS_RESERV_SUCCESS))){
					String[] s = msg.split("=");
					String remainingPoints = s[1];
					int remaining = Integer.parseInt(remainingPoints);
					System.out.println("remainingPoints: " + remainingPoints + ", remaining: " + remaining);
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
//						mailSender.send(simpleMailMessage);
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
	/**
	 * 發送簡訊給所有會員，並且將發送後資訊寄送到相關業務人員
	 * @param subject
	 * @param content
	 */
	private void shortMsgNotifyAllMembers(String subject, String content){
		String queryHql = "SELECT DISTINCT(p) FROM " + Member.class.getName() + " p WHERE p.mobile IS NOT NULL";
		StringBuffer sb = sendShortMsgToMembers(queryHql, Collections.emptyMap(), content);
		
		String sendMsg = sb.toString();
		if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
			subject += "沒有找到符合資格的會員";
		}else{
			subject += "簡訊發送後訊息";
		}
		if(!testMode){
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(IFLY);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(subject);
			String[] cc = new String[]{MIKO,BLUES,JERRY};
			simpleMailMessage.setCc(cc);
			mailSender.send(simpleMailMessage);
		}
	}
	/**
	 * 發送簡訊給非VIP會員
	 * @param subject
	 * @param content
	 */
	private void shortMsgNotifyNotVIPMembers(String subject, String content){
		String queryHql = "SELECT DISTINCT(p) FROM " + Member.class.getName() + " p WHERE p.mobile IS NOT NULL AND p.important = :important";
		Map<String, Object> params = new HashMap<>();
		params.put("important", false);
		StringBuffer sb = sendShortMsgToMembers(queryHql, params, content);
		
		String sendMsg = sb.toString();
		if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
			subject += "沒有找到符合資格的會員";
		}else{
			subject += "簡訊發送後訊息";
		}
		if(!testMode){
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(IFLY);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(subject);
			String[] cc = new String[]{MIKO,BLUES,JERRY};
			simpleMailMessage.setCc(cc);
			mailSender.send(simpleMailMessage);
		}
	}
	private static void shortMsgNotify20160714Activity(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
			service.setTestMode(true);
			
			service.shortMsgNotifyAllMembers("敦南誠品滿千送百", "7/15-7/19 敦南誠品滿千送百，OHM加碼滿8000再折600，最高單筆可折1100，社團同步優惠中，詳情請洽FB粉絲團或02-27716304");
		});
	}
	private static void shortMsgNotify20160623Activity(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
			service.setTestMode(true);
			
			service.shortMsgNotifyNotVIPMembers("OHM比漾促銷活動", "6/9-6/26 OHM在永和比漾廣場，單筆滿萬當筆9折再升VIP，還可參加滿5000送500年中慶活動，詳情洽02-27761505");
		});
	}
	
	private static void shortMsgNotify20160530Activity(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
			service.setTestMode(true);
			
			service.shortMsgNotifyAllMembers("OHM絕版品特價出清", "OHM絕版品特價出清7折起，敦南誠品專櫃及FB網路社團同步優惠，詳情請洽02-27716304");
		});
	}
	
	private static void shortMsgNotify20160401Activity(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
			service.setTestMode(true);
			
			String queryHql = "SELECT DISTINCT(p) FROM " + Member.class.getName() + " p WHERE p.mobile IS NOT NULL";
			String content = "4/2-4/5 OHM敦南誠品，單筆滿5000送500，可現抵可累贈，詳情請洽02-27716304";
			StringBuffer sb = service.sendShortMsgToMembers(queryHql, Collections.emptyMap(), content);
			
			String sendMsg = sb.toString();
			String subject = "4/2-4/5 OHM敦南誠品活動簡訊發送後訊息";
			if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
				subject = "4/2-4/5 OHM敦南誠品活動沒有找到符合資格的會員";
			}
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(service.templateMessage);
			simpleMailMessage.setTo(IFLY);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(subject);
			String[] cc = new String[]{MIKO,BLUES,JERRY};
			simpleMailMessage.setCc(cc);
			service.mailSender.send(simpleMailMessage);
		});
	}
	
	private static void shortMsgNotify20160429Activity(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
			service.setTestMode(true);
			
			String queryHql = "SELECT DISTINCT(p) FROM " + Member.class.getName() + " p WHERE p.mobile IS NOT NULL";
			String content = "4/29-5/9 OHM母親節特惠活動，單筆滿6600即贈純銀手鏈/手環一只，網路社團與誠品敦南專櫃同步詳情請洽02-27716304";
			StringBuffer sb = service.sendShortMsgToMembers(queryHql, Collections.emptyMap(), content);
			
			String sendMsg = sb.toString();
			String subject = "4/29-5/9 OHM活動簡訊發送後訊息";
			if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
				subject = "4/29-5/9 OHM活動沒有找到符合資格的會員";
			}
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(service.templateMessage);
			simpleMailMessage.setTo(IFLY);
			simpleMailMessage.setText(sendMsg);
			simpleMailMessage.setSubject(subject);
			String[] cc = new String[]{MIKO,BLUES,JERRY};
			simpleMailMessage.setCc(cc);
			service.mailSender.send(simpleMailMessage);
		});
	}
	/**
	 * 主要的測試程式
	 */
	private static void shortMsgNotifyForTesting(){
		BaseTest.executeApplicationContext(acac->{
			MitakeSMSHttpPost service = acac.getBean(MitakeSMSHttpPost.class);
//			service.setTestMode(true);
			
			String greaterThan70Chars = "多筆長簡訊測試請忽略2";
			
			String queryHql = "SELECT DISTINCT(p) FROM " + Member.class.getName() + " p WHERE p.name IN (:pName)";
			String content = greaterThan70Chars;
			Map<String, Object> params = new HashMap<>();
			params.put("pName", Arrays.asList("t1", "張雅筠"));
			StringBuffer sb = service.sendShortMsgToMembers(queryHql, params, content);
			
			String sendMsg = sb.toString();
			String subject = "發給自家人的測試簡訊發送後訊息";
			if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
				subject = "發給自家人的測試簡訊沒有找到符合資格的會員";
			}
			System.out.println("sendMsg: " + sendMsg);
//			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(service.templateMessage);
//			simpleMailMessage.setTo(IFLY);
//			simpleMailMessage.setText(sendMsg);
//			simpleMailMessage.setSubject(subject);
//			String[] cc = new String[]{MIKO,BLUES,JERRY};
//			simpleMailMessage.setCc(cc);
//			service.mailSender.send(simpleMailMessage);
		});
	}	
	
}
