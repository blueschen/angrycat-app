package com.angrycat.erp.member.vip;

import static com.angrycat.erp.common.XSSFUtil.getCellColumnIdxFromTitle;
import static com.angrycat.erp.common.XSSFUtil.parseCellStrVal;
import static com.angrycat.erp.common.XSSFUtil.readXSSF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.shortnews.MitakeSMSHttpPost;
import com.angrycat.erp.test.BaseTest;


@Component
@Scope("prototype")
public class RenewVip {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private MemberVipDiscount discount;
	@Autowired
	private MitakeSMSHttpPost mitakeSMSHttpPost;
	
	public void process(String src){
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

			String queryHql = "SELECT m FROM " + Member.class.getName() + " m WHERE m.idNo IN (:idNo)";
			Map<String, Object> params = new HashMap<>();
			params.put("idNo", idNos);
			
			// 續會一年
			sfw.executeSession(s->{
				List<Member> members = s.createQuery(queryHql).setProperties(params).list();
				System.out.println("共" +members.size()+ "筆");
				members.forEach(m->{
					discount.applyRule(m);
					
					System.out.println(m.getName() + "|" + m.getIdNo());
					m.getVipDiscountDetails().forEach(v->{
						System.out.println(ReflectionToStringBuilder.toString(v, ToStringStyle.MULTI_LINE_STYLE));
					});
//					System.out.println(ReflectionToStringBuilder.toString(m, ToStringStyle.MULTI_LINE_STYLE));
//					s.update(m);
//					s.flush();
//					s.clear();
				});
			});
			
			// 發簡訊
			String template = "您的VIP已續會至{toVipEndDate}";
//			StringBuffer sb = mitakeSMSHttpPost.sendShortMsgToMembers(queryHql, params, m->{
//				String content = template.replace("{toVipEndDate}", DF_yyyyMMdd_DASHED.format(m.getToVipEndDate()));
//				return content;
//			});
		});
	}
	
	private static void testProcess(){
		BaseTest.executeApplicationContext(acac->{
			RenewVip vip = acac.getBean(RenewVip.class);
			vip.process("E:\\angrycat_workitem\\member\\2016_03_18_vip_renew_list_from_miko\\VIP_adjusted.xlsx");
		});
	}
	public static void main(String[]args){
		testProcess();
	}
}
