package io.github.m;

import io.github.m.function.PentaFunction;
import io.github.m.function.QuadFunction;
import io.github.m.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * M implementation of functions.
 */
@FunctionalInterface
public interface Value {
    default Value invoke() {
        return invoke(List.NIL);
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

    final class Partial implements Value {
        private final Value value;
        private final Value arg;

        public Partial(Value value, Value arg) {
            this.value = value;
            this.arg = arg;
        }

        @Override
        public Value invoke(Value arg) {
            return value.invoke(this.arg, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return value.invoke(this.arg, arg1, arg2);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3) {
            return value.invoke(this.arg, arg1, arg2, arg3);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4) {
            return value.invoke(this.arg, arg1, arg2, arg3, arg4);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            Value value = this;
            while (value instanceof Partial) {
                Partial partial = (Partial) value;
                builder.insert(0, " " + partial.arg);
                value = partial.value;
            }
            return "λ(" + value + builder.toString() + ")";
        }
    }

    final class Impl1 implements Value {
        private final String name;
        private final Function<Value, Value> impl;

        public Impl1(String name, Function<Value, Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return impl.apply(arg);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    final class Impl2 implements Value {
        private final String name;
        private final BiFunction<Value, Value, Value> impl;

        public Impl2(String name, BiFunction<Value, Value, Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Partial(this, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return impl.apply(arg1, arg2);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    final class Impl3 implements Value {
        private final String name;
        private final TriFunction<Value, Value, Value, Value> impl;

        public Impl3(String name, TriFunction<Value, Value, Value, Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Partial(this, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return new Partial(new Partial(this, arg1), arg2);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3) {
            return impl.apply(arg1, arg2, arg3);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    final class Impl4 implements Value {
        private final String name;
        private final QuadFunction<Value, Value, Value, Value, Value> impl;

        public Impl4(String name, QuadFunction<Value, Value, Value, Value, Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Partial(this, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return new Partial(new Partial(this, arg1), arg2);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3) {
            return new Partial(new Partial(new Partial(this, arg1), arg2), arg3);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4) {
            return impl.apply(arg1, arg2, arg3, arg4);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    final class Impl5 implements Value {
        private final String name;
        private final PentaFunction<Value, Value, Value, Value, Value, Value> impl;

        public Impl5(String name, PentaFunction<Value, Value, Value, Value, Value, Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value invoke(Value arg) {
            return new Partial(this, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return new Partial(new Partial(this, arg1), arg2);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3) {
            return new Partial(new Partial(new Partial(this, arg1), arg2), arg3);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4) {
            return new Partial(new Partial(new Partial(new Partial(this, arg1), arg2), arg3), arg4);
        }

        @Override
        public Value invoke(Value arg1, Value arg2, Value arg3, Value arg4, Value arg5) {
            return impl.apply(arg1, arg2, arg3, arg4, arg5);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
