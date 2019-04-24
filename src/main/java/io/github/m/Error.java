package io.github.m;

/**
 * M wrapper class for errors.
 */
public class Error extends Throwable implements Value {
    public Error(String message) {
        super(message);
    }

    public Error(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Value invoke(Value arg) {
        return this;
    }

    public static Error wrap(Throwable t) {
        return new Error(t.getMessage(), t);
    }

    @MField(name = "error")
    public static final Value error = new Value.Impl1(message -> new Error(Symbol.toString(List.from(message))));
}
