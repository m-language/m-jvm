package io.github.m;

/**
 * M wrapper class for chars.
 */
public final class Char implements Value.Delegate {
    public final char value;

    public Char(char value) {
        this.value = value;
    }

    @Override
    public Value value() {
        return new Nat((int) value);
    }

    public static Char from(Value value) {
        if (value instanceof Char) {
            return (Char) value;
        } else {
            return new Char((char) Nat.from(value).value);
        }
    }

    @MField(name = "space")
    public static final Value space = new Char(' ');

    @MField(name = "tab")
    public static final Value tab = new Char('\t');

    @MField(name = "linefeed")
    public static final Value linefeed = new Char('\n');

    @MField(name = "carriage-return")
    public static final Value carriageReturn = new Char('\r');

    @MField(name = "char.=")
    public static final Value eq = new Value.Impl2((x, y) -> new Bool(from(x).value == from(y).value));

    @MField(name = "char->nat")
    public static final Value toNat = new Value.Impl1(value -> new Nat((int) from(value).value));
}
