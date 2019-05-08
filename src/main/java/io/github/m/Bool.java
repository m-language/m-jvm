package io.github.m;

/**
 * M wrapper class for booleans.
 */
public final class Bool implements Value {
    /**
     * The primitive value of this boolean.
     */
    public final boolean value;

    public Bool(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    /**
     * Partial application of {@link Bool#invoke(Value, Value)}.
     */
    @Override
    public Value invoke(Value arg) {
        return new Partial(this, arg);
    }

    /**
     * Returns {@param arg1} if this is true, or {@param arg2} if this is false.
     */
    @Override
    public Value invoke(Value arg1, Value arg2) {
        return value ? arg1 : arg2;
    }

    /**
     * Wraps a primitive boolean in a boolean.
     */
    public static Bool valueOf(boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /**
     * Tests if a value is a boolean.
     */
    public static boolean is(Value value) {
        return value instanceof Bool || value.invoke(TRUE, FALSE) instanceof Bool;
    }

    /**
     * Converts a {@link Value} to a boolean.
     */
    public static Bool from(Value value) {
        if (value instanceof Bool) {
            return (Bool) value;
        } else {
            return (Bool) value.invoke(TRUE, FALSE);
        }
    }

    /**
     * Converts a {@link Value} to a primitive boolean.
     */
    public static boolean primitiveFrom(Value value) {
        return Bool.from(value).value;
    }

    /**
     * The singleton truthy value.
     */
    public static final Bool TRUE = new Bool(true);

    /**
     * The singleton falsy value.
     */
    public static final Bool FALSE = new Bool(false);

    @MField(name = "true")
    public static final Value $true = TRUE;

    @MField(name = "false")
    public static final Value $false = FALSE;
}
