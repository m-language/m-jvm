package io.github.m.function;

@FunctionalInterface
public interface PentaFunction<T, U, X, N, S, R> {
    R apply(T t, U u, X x, N n, S s);
}
