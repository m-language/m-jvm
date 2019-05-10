package io.github.m;

/**
 * M wrapper class for either.
 */
public interface Either extends Value {
    /**
     * The value of this either.
     */
    Value value();

    /**
     * Returns left if this either is left, or right if this either is right.
     */
    Value fold(Value left, Value right);

    /**
     * Partially applies this value to an argument.
     */
    @Override
    default Value invoke(Value arg) {
        return new Partial(this, arg);
    }

    /**
     * Alias of fold.
     */
    @Override
    default Value invoke(Value arg1, Value arg2) {
        return fold(arg1, arg2).invoke(value());
    }

    /**
     * The left instance of either such that fold(x, y) == x.
     */
    final class Left implements Either {
        private final Value value;

        public Left(Value value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Left(" + value + ")";
        }

        @Override
        public Value value() {
            return value;
        }

        @Override
        public Value fold(Value left, Value right) {
            return left;
        }
    }

    /**
     * The right instance of either such that fold(x, y) == y.
     */
    final class Right implements Either {
        private final Value value;

        public Right(Value value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Right(" + value + ")";
        }

        @Override
        public Value value() {
            return value;
        }

        @Override
        public Value fold(Value left, Value right) {
            return right;
        }
    }

    /**
     * Converts a value to an either.
     */
    static Either from(Value value) {
        if (value instanceof Either) {
            return (Either) value;
        } else {
            return (Either) value.invoke(left, right);
        }
    }

    @MField(name = "left")
    Value left = new Value.Impl1("left", Left::new);

    @MField(name = "right")
    Value right = new Value.Impl1("right", Right::new);
}
