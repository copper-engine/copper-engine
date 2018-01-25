package org.copperengine.core.util;

@FunctionalInterface
public interface FunctionWithException<T, R> {
    R apply(T t) throws Exception;
}