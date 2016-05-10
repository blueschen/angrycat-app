package com.angrycat.erp.query;

import java.util.function.BiFunction;

import org.hibernate.ScrollableResults;

import com.angrycat.erp.component.SessionFactoryWrapper;

public interface QueryScrollable {
	public <F>F executeScrollableQuery(BiFunction<ScrollableResults, SessionFactoryWrapper, F> executeLogic);
}
