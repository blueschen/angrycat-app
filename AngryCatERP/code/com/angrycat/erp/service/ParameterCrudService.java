package com.angrycat.erp.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.query.HibernateQueryExecutable;

@Service
public class ParameterCrudService extends CrudBaseService<Parameter> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5896650225588143252L;

	@Autowired
	public ParameterCrudService(LocalSessionFactoryBean lsfc,
			HibernateQueryExecutable<Parameter> hqe) {
		super(lsfc, hqe, Parameter.class);
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
