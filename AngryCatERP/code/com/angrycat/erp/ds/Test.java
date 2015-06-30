package com.angrycat.erp.ds;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;

public interface Test<T> {
	public void executeTXSession(Consumer<Session> consumer);
	public List<T> executeQuerySession(Function<Session, List<T>> func);
	public void executeTransaction(Consumer<Session>consumer);
}
