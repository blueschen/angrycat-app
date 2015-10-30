package com.angrycat.erp.function;
/**
 * Java lambda can't allow throw Throwable, but this new interface can.
 * @author JerryLin
 *
 * @param <T>
 */
@FunctionalInterface
public interface ConsumerThrowable<T> {
	public void accept(T t)throws Throwable;
}
