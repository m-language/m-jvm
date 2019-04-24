package io.github.m;

/**
 * M wrapper class for either.
 */
public interface Either extends Value {
    Value fold(Value left, Value right);

    @Override
    default Value invoke(Value arg) {
        return new Value.Impl1(arg2 -> invoke(arg, arg2));
    }

    @Override
    default Value invoke(Value arg1, Value arg2) {
        return fold(arg1, arg2);
    }

    final class Left implements Either {
        public final Value value;

        public Left(Value value) {
            this.value = value;
        }

        @Override
        public Value fold(Value left, Value right) {
            return left.invoke(value);
        }
    }

    final class Right implements Either {
        public final Value value;

        public Right(Value value) {
            this.value = value;
        }

        @Override
        public Value fold(Value left, Value right) {
            return right.invoke(value);
        }
    }

    static Either from(Value value) {
        if (value instanceof Either) {
            return (Either) value;
        } else {
            return (Either) value.invoke(new Value.Impl1(Left::new), new Value.Impl1(Right::new));
        }
    }

    @MField(name = "left")
    Value left = new Value.Impl1(Left::new);

    @MField(name = "right")
    Value right = new Value.Impl1(Right::new);
}
