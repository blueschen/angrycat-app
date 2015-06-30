package com.angrycat.erp.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.Member;

@Service
@Scope("prototype")
public class MemberCrudService extends CrudBaseService<Member, Member> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -647818435220341328L;

	@Autowired
	public MemberCrudService(LocalSessionFactoryBean lsfb){
		super(lsfb, Member.class);
	}
	
	@PostConstruct
	@Override
	public void init(){
		super.init();
		String rootAliasWith = DEFAULT_ROOT_ALIAS + ".";
		addWhere(ConditionFactory.putStr(rootAliasWith+"name=:pName"))
		.addWhere(ConditionFactory.putInt(rootAliasWith+"gender=:pGender"))
		.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday=:pBirthday"))
		;
		
	}
}
