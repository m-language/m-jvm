package io.github.m;

/**
 * M wrapper class for chars.
 */
public class Char implements Value.Delegate {
    public final char value;

    private Char(char value) {
        this.value = value;
    }

    @Override
    public Value value() {
        return Nat.valueOf((int) value);
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }

    public static Char valueOf(char value) {
        return new Char(value);
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
    public static final Value eq = new Value.Impl2("char.=", (x, y) -> Bool.valueOf(from(x).value == from(y).value));

    @MField(name = "char->nat")
    public static final Value toNat = new Value.Impl1("char->nat", value -> Nat.valueOf((int) from(value).value));
}
