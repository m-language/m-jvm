package io.github.m;

/**
 * M wrapper class for pairs.
 */
public class Pair implements Value {
    public final Value first;
    public final Value second;

    public Pair(Value first, Value second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Value invoke(Value arg) {
        if (arg == Bool.TRUE) {
            return first;
        } else if (arg == Bool.FALSE) {
            return second;
        } else {
            return arg.invoke(first, second);
        }
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public static Pair from(Value value) {
        if (value instanceof Pair) {
            return (Pair) value;
        } else {
            return new Pair(value.invoke(Bool.TRUE), value.invoke(Bool.FALSE));
        }
    }

    @MField(name = "pair")
    public static Value pair = new Value.Impl2("pair", Pair::new);

    @MField(name = "first")
    public static Value $first = new Value.Impl1("first", pair -> Pair.from(pair).first);

    @MField(name = "second")
    public static Value $second = new Value.Impl1("second", pair -> Pair.from(pair).second);
}
