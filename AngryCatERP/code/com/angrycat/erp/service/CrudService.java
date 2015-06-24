package com.angrycat.erp.service;

import java.util.List;

import com.angrycat.erp.query.QueryExecutable;
import com.angrycat.erp.web.component.ConditionConfig;

public interface CrudService<T> extends QueryExecutable<T> {
	public ConditionConfig<T> executeQueryPageableAfterDelete(List<String> ids);
	public ConditionConfig<T> getConditionConfig();
	public void copyConditionConfig(ConditionConfig<T> conditionConfig);
	public ConditionConfig<T> executeQueryPageable(ConditionConfig<T> conditionConfig);
	public T saveOrMerge(Object...obj);
	public T findById(String id);
}
