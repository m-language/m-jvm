package io.github.m;

/**
 * M wrapper class for nats.
 */
public class Nat implements Value {
    public final int value;

    private Nat(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public Value invoke(Value arg) {
        return new Value.Impl1(arg2 -> invoke(arg, arg2));
    }

    @Override
    public Value invoke(Value arg1, Value arg2) {
        Value x = arg2;
        for (int i = 0; i < value; i++) {
            x = arg1.invoke(x);
        }
        return x;
    }

    public static Nat valueOf(int value) {
        return new Nat(value);
    }

    public static Nat from(Value value) {
        if (value instanceof Nat) {
            return (Nat) value;
        } else {
            return (Nat) value.invoke(inc, zero);
        }
    }

    @MField(name = "nat.0")
    public static final Value zero = new Nat(0);

    @MField(name = "nat.1")
    public static final Value one = new Nat(1);

    @MField(name = "nat.0?")
    public static final Value isZero = new Value.Impl1(nat -> Bool.valueOf(from(nat).value == 0));

    @MField(name = "nat.inc")
    public static final Value inc = new Value.Impl1(nat -> new Nat(from(nat).value + 1));

    @MField(name = "nat.dec")
    public static final Value dec = new Value.Impl1(nat -> new Nat(from(nat).value - 1));

    @MField(name = "nat.+")
    public static final Value add = new Value.Impl2((x, y) -> new Nat(from(x).value + from(y).value));

    @MField(name = "nat.-")
    public static final Value sub = new Value.Impl2((x, y) -> new Nat(from(x).value - from(y).value));

    @MField(name = "nat.*")
    public static final Value mul = new Value.Impl2((x, y) -> new Nat(from(x).value * from(y).value));

    @MField(name = "nat./")
    public static final Value div = new Value.Impl2((x, y) -> new Nat(from(x).value / from(y).value));

    @MField(name = "nat.%")
    public static final Value mod = new Value.Impl2((x, y) -> new Nat(from(x).value % from(y).value));

    @MField(name = "nat.<")
    public static final Value lt = new Value.Impl2((x, y) -> Bool.valueOf(from(x).value < from(y).value));

    @MField(name = "nat.>")
    public static final Value gt = new Value.Impl2((x, y) -> Bool.valueOf(from(x).value > from(y).value));

    @MField(name = "nat.=")
    public static final Value eq = new Value.Impl2((x, y) -> Bool.valueOf(from(x).value == from(y).value));

    @MField(name = "nat->char")
    public static final Value toChar = new Value.Impl1(x -> Char.valueOf((char) from(x).value));
}
