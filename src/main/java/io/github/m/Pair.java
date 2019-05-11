package io.github.m;

/**
 * M wrapper class for pairs.
 */
public final class Pair implements Value {
    /**
     * The first value of the pair.
     */
    public final Value first;

    /**
     * The second value of the pair.
     */
    public final Value second;

    public Pair(Value first, Value second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    /**
     * Applies the argument to the first and second elements of this pair.
     */
    @Override
    public Value invoke(Value arg) {
        return arg.invoke(first, second);
    }

    /**
     * Converts a value to a pair.
     */
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
