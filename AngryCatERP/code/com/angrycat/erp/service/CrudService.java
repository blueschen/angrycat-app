package com.angrycat.erp.service;

import java.util.List;
import java.util.function.BiFunction;

import org.hibernate.ScrollableResults;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.query.QueryExecutable;
import com.angrycat.erp.web.component.ConditionConfig;

public interface CrudService<T, R> extends QueryExecutable<T> {
	public ConditionConfig<T> executeQueryPageableAfterDelete(List<String> ids);
	public ConditionConfig<T> getConditionConfig();
	public void copyConditionConfig(ConditionConfig<T> conditionConfig);
	public ConditionConfig<T> executeQueryPageable(ConditionConfig<T> conditionConfig);
	public T findById(String id);
	public <F>F executeScrollableQuery(BiFunction<ScrollableResults, SessionFactoryWrapper, F> executeLogic);
}
