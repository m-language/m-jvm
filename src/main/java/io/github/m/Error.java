package io.github.m;

/**
 * M wrapper class for errors.
 */
public class Error extends Throwable implements Value {
    public Error(String message) {
        super(message);
    }

    @Override
    public Value invoke(Value arg) {
        return this;
    }

    @MField(name = "error")
    public static final Value error = new Value.Impl1(message -> new Error(Symbol.toString(List.from(message))));
}
