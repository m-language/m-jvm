package io.github.m;

/**
 * M wrapper class for booleans.
 */
public class Bool implements Value {
    public final boolean value;

    public Bool(boolean value) {
        this.value = value;
    }

    @Override
    public Value invoke(Value arg) {
        return new Value.Impl1(arg2 -> invoke(arg, arg2));
    }

    @Override
    public Value invoke(Value arg1, Value arg2) {
        if (value) {
            return arg1;
        } else {
            return arg2;
        }
    }

    public static final Bool TRUE = new Bool(true) {
        @Override
        public Value invoke(Value arg1, Value arg2) {
            return arg1;
        }
    };

    public static final Bool FALSE = new Bool(false) {
        @Override
        public Value invoke(Value arg1, Value arg2) {
            return arg2;
        }
    };

    public static Bool from(Value value) {
        if (value instanceof Bool) {
            return (Bool) value;
        } else {
            return (Bool) value.invoke(TRUE, FALSE);
        }
    }

    @MField(name = "true")
    public static final Value $true = TRUE;

    @MField(name = "false")
    public static final Value $false = FALSE;
}