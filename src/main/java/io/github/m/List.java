package io.github.m;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * M implementation of lists.
 */
public interface List extends Iterable<Value>, Value {
    @Override
    default Iterator<Value> iterator() {
        return new Iterator<Value>() {
            List list = List.this;

            @Override
            public boolean hasNext() {
                return list instanceof Cons;
            }

            @Override
            public Value next() {
                if (list instanceof Cons) {
                    Value car = ((Cons) list).car;
                    list = ((Cons) list).cdr;
                    return car;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * The singleton empty list.
     */
    List NIL = new List() {
        @Override
        public Value invoke(Value arg) {
            return new Partial(this, arg);
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return arg2;
        }

        @Override
        public String toString() {
            return "()";
        }
    };

    /**
     * The cons of a value to a list.
     */
    final class Cons implements List, Value.Delegate {
        /**
         * The head of the list.
         */
        public final Value car;

        /**
         * The tail of the list.
         */
        public final List cdr;

        public Cons(Value car, List cdr) {
            this.car = car;
            this.cdr = cdr;
        }

        @Override
        public String toString() {
            Stream.Builder<Value> builder = Stream.builder();
            iterator().forEachRemaining(builder);
            return builder.build()
                    .map(Value::toString)
                    .collect(Collectors.joining(" ", "(", ")"));
        }

        /**
         * Delegates this list to a pair of its car and cdr.
         */
        @Override
        public Value value() {
            return new Pair(car, cdr);
        }
    }

    /**
     * Converts a value to a list.
     */
    static List from(Value value) {
        if (value instanceof List) {
            return (List) value;
        } else {
            return (List) value.invoke(
                    new Value.Impl3("(fn a b c (cons a b))", (a, b, c) -> new Cons(a, from(b))),
                    NIL
            );
        }
    }

    @MField(name = "nil")
    Value nil = NIL;

    @MField(name = "cons")
    Value cons = new Value.Impl2("cons", (car, cdr) -> new Cons(car, from(cdr)));
}
