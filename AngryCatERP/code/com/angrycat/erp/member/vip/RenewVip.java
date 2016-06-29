package com.angrycat.erp.member.vip;

import static com.angrycat.erp.common.DatetimeUtil.DF_yyyyMMdd_DASHED;
import static com.angrycat.erp.common.EmailContact.BLUES;
import static com.angrycat.erp.common.EmailContact.IFLY;
import static com.angrycat.erp.common.EmailContact.JERRY;
import static com.angrycat.erp.common.EmailContact.MIKO;
import static com.angrycat.erp.common.XSSFUtil.getCellColumnIdxFromTitle;
import static com.angrycat.erp.common.XSSFUtil.parseCellStrVal;
import static com.angrycat.erp.common.XSSFUtil.readXSSF;
import static com.angrycat.erp.shortnews.MitakeSMSHttpPost.NO_DATA_FOUND_STOP_SEND_SHORT_MSG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.shortnews.MitakeSMSHttpPost;
import com.angrycat.erp.test.BaseTest;


@Component
@Scope("prototype")
/**
 * 更新VIP，通常為延長一年
 * @author JerryLin
 *
 */
public class RenewVip {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private MemberVipDiscount discount;
	@Autowired
	private MitakeSMSHttpPost mitakeSMSHttpPost;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	/**
	 * 從制式Excel取得會員資料，並更新一年VIP
	 * @param src
	 */
	public void renewVipFromXlsx(String src){
		readXSSF(src, wb->{
			Sheet sheet = wb.getSheetAt(0);
			Iterator<Row> rowItr = sheet.iterator();
			Row title = null;
			List<String> idNos = new ArrayList<>();
			while(rowItr.hasNext()){
				Row row = rowItr.next();
				int rowNum = row.getRowNum();
				if(rowNum == 0){
					title = row;
					continue;
				}
				int nameIdx = getCellColumnIdxFromTitle(title, "name");
				int idNoIdx = getCellColumnIdxFromTitle(title, "idNo");
				int mobileIdx = getCellColumnIdxFromTitle(title, "mobile");
				
				String name = parseCellStrVal(row, nameIdx);
				String idNo = parseCellStrVal(row, idNoIdx);
				String mobile = parseCellStrVal(row, mobileIdx);
				
				idNos.add(idNo);
			}
			
			System.out.println(StringUtils.join(idNos, "','"));
			
			String queryHql = "SELECT DISTINCT(m) FROM " + Member.class.getName() + " m left join fetch m.vipDiscountDetails v WHERE m.idNo IN (:idNo)";
			Map<String, Object> params = new HashMap<>();
			params.put("idNo", idNos);
			
			renewVip(queryHql, params);
		});
	}
	
	/**
	 * 透過會員ID找到符合資格者，並更新一年VIP
	 * @param ids
	 */
	public void renewVipByMemberIds(List<String> ids){
		String queryHql = "SELECT DISTINCT(m) FROM " + Member.class.getName() + " m left join fetch m.vipDiscountDetails v WHERE m.id IN (:ids)";
		Map<String, Object> params = new HashMap<>();
		params.put("ids", ids);
		
		renewVip(queryHql, params);
	}
	
	private void renewVip(String queryHql, Map<String, Object> params){
		// 續會一年
		sfw.executeSession(s->{
			List<Member> members = s.createQuery(queryHql).setProperties(params).list();
			System.out.println("共" +members.size()+ "筆");
			members.forEach(m->{
				s.evict(m); // 因為續會的時候，會替換掉原來的collection，造成錯誤，所以先evict掉物件跟session的關係
				discount.applyRule(m); // 續會logic
				
				System.out.println(m.getName() + "|" + m.getIdNo());
//				m.getVipDiscountDetails().forEach(v->{
//					System.out.println(ReflectionToStringBuilder.toString(v, ToStringStyle.MULTI_LINE_STYLE));
//				});
//				System.out.println(ReflectionToStringBuilder.toString(m, ToStringStyle.MULTI_LINE_STYLE));
				s.update(m);
				s.flush();
				s.clear();
			});
		});
		
		// 發簡訊
		String template = "親愛的OHM會員您好，感謝您的支持，您的VIP資格已自動延展一年，到期日為{toVipEndDate}，如有任何問題請洽專櫃02-27716304";
		StringBuffer sb = mitakeSMSHttpPost.sendShortMsgToMembers(queryHql, params, m->{
			String content = template.replace("{toVipEndDate}", DF_yyyyMMdd_DASHED.format(m.getToVipEndDate()));
			return content;
		});
		
		String sendMsg = sb.toString();
		String subject = "VIP續會簡訊發送後訊息";
		if(sendMsg.contains(NO_DATA_FOUND_STOP_SEND_SHORT_MSG)){
			subject = "VIP續會沒有找到符合資格的會員";
		}
		
		// 發email通知相關人員
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
		simpleMailMessage.setTo(MIKO);
		simpleMailMessage.setText(sendMsg);
		simpleMailMessage.setSubject(subject);
		String[] cc = new String[]{IFLY,BLUES,JERRY};
		simpleMailMessage.setCc(cc);
		mailSender.send(simpleMailMessage);
	}
	
	private static void testRenewVipFromXlsx(){
		BaseTest.executeApplicationContext(acac->{
			RenewVip vip = acac.getBean(RenewVip.class);
			vip.mitakeSMSHttpPost.setTestMode(true);
			vip.renewVipFromXlsx("E:\\angrycat_workitem\\member\\2016_03_18_vip_renew_list_from_miko\\VIP_adjusted.xlsx");
		});
	}
	
	private static void startupRenewVipFromXlsx(){
		BaseTest.executeApplicationContext(acac->{
			RenewVip vip = acac.getBean(RenewVip.class);
			vip.renewVipFromXlsx("E:\\angrycat_workitem\\member\\2016_03_18_vip_renew_list_from_miko\\VIP_adjusted.xlsx");
		});
	}
	
	private static void testRenewVipByMemberIds(){
		BaseTest.executeApplicationContext(acac->{
			RenewVip vip = acac.getBean(RenewVip.class);
			vip.renewVipByMemberIds(Arrays.asList("20151026-125509411-GWBKu"
					,"20151026-125508985-lUWdK"
					,"20151026-125511786-olUwa"
					,"20151026-125509537-Fnayi"
					,"20151026-125513290-EFkWq"
					,"20151026-125511651-bEYGp"
					,"20151026-125510478-ynJsi"
					,"20151026-125510886-mkLNU"));
		});
	}
	
	public static void main(String[]args){
		testRenewVipByMemberIds();
	}
}
