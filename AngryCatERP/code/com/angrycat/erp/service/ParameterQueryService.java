package com.angrycat.erp.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.Parameter;

@Service
@Scope(value="prototype")
public class ParameterQueryService extends QueryBaseService<Parameter, Parameter> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5896650225588143252L;

	@Autowired
	public ParameterQueryService(SessionFactoryWrapper sfw) {
		super(sfw, Parameter.class);
	}

	@PostConstruct
	@Override
	public void init() {
		super.init();
		String rootAliasWith = DEFAULT_ROOT_ALIAS + ".";
		addWhere(ConditionFactory.putStr(rootAliasWith + "code=:pCode"))
		.addWhere(ConditionFactory.putInt(rootAliasWith + "sequence=:pSeq"));
	}
}
