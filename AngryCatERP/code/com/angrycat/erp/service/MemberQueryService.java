package com.angrycat.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.Member;

@Service
@Scope("prototype")
public class MemberQueryService extends KendoUiService<Member, Member> {
	private static final long serialVersionUID = -647818435220341328L;
}
