package io.github.m;

/**
 * M wrapper class for errors.
 */
public class Error extends Throwable implements Value {
    private static final long serialVersionUID = 5916392373513454218L;

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

    @Override
    public String toString() {
        return "Error(" + getMessage() + ")";
    }

    public static Error wrap(Throwable t) {
        return new Error(t.getMessage(), t);
    }

    @MField(name = "error")
    public static final Value error = new Value.Impl1("error", message -> new Error(Symbol.toString(List.from(message))));
}
