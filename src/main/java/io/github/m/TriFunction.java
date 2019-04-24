package io.github.m;

@FunctionalInterface
public interface TriFunction<T, U, X, R> {
    R apply(T t, U u, X x);
}
