package com.angrycat.erp.function;
/**
 * Java lambda can't allow throw Throwable, but this new interface can.
 * @author JerryLin
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface FunctionThrowable<T, R> {
	public R apply(T t)throws Throwable;
}
