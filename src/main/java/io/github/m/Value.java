package io.github.m;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * M implementation of functions.
 */
@FunctionalInterface
public interface Value {
    default Value invoke() {
        return invoke(null);
    }

    Value invoke(Value arg);

    default Value invoke(Value arg1, Value arg2) {
        return invoke(arg1).invoke(arg2);
    }

    default Value invoke(Value arg1, Value arg2, Value arg3) {
        return invoke(arg1, arg2).invoke(arg3);
    }

    default Value invoke(Value arg1, Value arg2, Value arg3, Value arg4) {
        return invoke(arg1, arg2, arg3).invoke(arg4);
    }

    default Value invoke(Value arg1, Value arg2, Value arg3, Value arg4, Value arg5) {
        return invoke(arg1, arg2, arg3, arg4).invoke(arg5);
    }

    interface Delegate extends Value {
        Value value();

        @Override
        default Value invoke(Value arg) {
            return value().invoke(arg);
        }
    }

    final class Impl1 implements Value {
        private final Function<Value, Value> impl;

        public Impl1(Function<Value, Value> impl) {
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return impl.apply(arg);
        }
    }

    final class Impl2 implements Value {
        private final BiFunction<Value, Value, Value> impl;

        public Impl2(BiFunction<Value, Value, Value> impl) {
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Impl1(arg2 -> invoke(arg, arg2));
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return impl.apply(arg1, arg2);
        }
    }

    final class Impl3 implements Value {
        private final TriFunction<Value, Value, Value, Value> impl;

        public Impl3(TriFunction<Value, Value, Value, Value> impl) {
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Impl2((arg2, arg3) -> invoke(arg, arg2, arg3));
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3) {
            return impl.apply(arg1, arg2, arg3);
        }
    }

    final class Impl4 implements Value {
        private final QuadFunction<Value, Value, Value, Value, Value> impl;

        public Impl4(QuadFunction<Value, Value, Value, Value, Value> impl) {
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Impl3((arg2, arg3, arg4) -> invoke(arg, arg2, arg3, arg4));
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4) {
            return impl.apply(arg1, arg2, arg3, arg4);
        }
    }

    final class Impl5 implements Value {
        private final PentaFunction<Value, Value, Value, Value, Value, Value> impl;

        public Impl5(PentaFunction<Value, Value, Value, Value, Value, Value> impl) {
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Impl4((arg2, arg3, arg4, arg5) -> invoke(arg, arg2, arg3, arg4, arg5));
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4, Value arg5) {
            return impl.apply(arg1, arg2, arg3, arg4, arg5);
        }
    }
}
