package io.github.m.function;

@FunctionalInterface
public interface TriFunction<T, U, X, R> {
    R apply(T t, U u, X x);
}
