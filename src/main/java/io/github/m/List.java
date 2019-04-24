package io.github.m;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

    List NIL = new List() {
        @Override
        public Value invoke(Value arg) {
            return new Value.Impl1(arg2 -> invoke(arg, arg2));
        }

        @Override
        public Value invoke(Value arg1, Value arg2) {
            return arg2;
        }
    };

    final class Cons implements List, Value.Delegate {
        public final Value car;
        public final List cdr;

        public Cons(Value car, List cdr) {
            this.car = car;
            this.cdr = cdr;
        }

        @Override
        public Value value() {
            return new Pair(car, cdr);
        }
    }

    static List from(Value value) {
        if (value instanceof List) {
            return (List) value;
        } else {
            return (List) value.invoke(
                    new Value.Impl3((a, b, c) -> new Cons(a, from(b))),
                    NIL
            );
        }
    }

    @MField(name = "nil")
    Value nil = NIL;

    @MField(name = "cons")
    Value cons = new Value.Impl2((car, cdr) -> new Cons(car, from(cdr)));
}
