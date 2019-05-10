package io.github.m;

/**
 * M wrapper class for booleans.
 */
public final class Bool implements Value {
    /**
     * The primitive value of this boolean.
     */
    public final boolean value;

    private Bool(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    /**
     * Partially applies this value to an argument.
     */
    @Override
    public Value invoke(Value arg) {
        return new Partial(this, arg);
    }

    /**
     * Returns the first argument if this is true, or the second if this is false.
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
     * Converts a value to a boolean.
     */
    public static Bool from(Value value) {
        if (value instanceof Bool) {
            return (Bool) value;
        } else {
            return (Bool) value.invoke(TRUE, FALSE);
        }
    }

    /**
     * Converts a value to a primitive boolean.
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
