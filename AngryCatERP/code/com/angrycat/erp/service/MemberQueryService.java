package com.angrycat.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.model.Member;

@Service
@Scope("prototype")
public class MemberQueryService extends KendoUiService<Member, Member> {
	private static final long serialVersionUID = -647818435220341328L;
	
	@Override
	public String conditionConfigToJsonStr(Object cc){
		String json = CommonUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}	

}
