package io.github.m;

import java.util.function.Supplier;

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
public interface Process extends Value {
    /**
     * Unsafely runs a process.
     */
    Value run();

    /**
     * Flat maps this process with a value.
     */
    @Override
    default Value invoke(Value arg) {
        return new ThenRunWith(this, arg);
    }

    /**
     * The default implementation of a process.
     */
    final class Impl implements Process {
        public final String name;
        public final Supplier<Value> impl;

        public Impl(String name, Supplier<Value> impl) {
            this.name = name;
            this.impl = impl;
        }

        @Override
        public Value run() {
            return impl.get();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Creates a process from a value.
     */
    final class Impure implements Process {
        public final Value value;

        public Impure(Value value) {
            this.value = value;
        }

        @Override
        public Value run() {
            return value;
        }
    }

    /**
     * Flat maps a process with a value.
     */
    final class ThenRunWith implements Process {
        public final Process process;
        public final Value value;

        public ThenRunWith(Process process, Value value) {
            this.process = process;
            this.value = value;
        }

        @Override
        public Value run() {
            return ((Process) value.invoke(process.run())).run();
        }
    }

    /**
     * Runs two processes sequentially.
     */
    final class ThenRun implements Process {
        public final Process first;
        public final Process second;

        public ThenRun(Process first, Process second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Value run() {
            first.run();
            return second.run();
        }
    }

    /**
     * Maps a process with a value..
     */
    final class RunWith implements Process {
        public final Process process;
        public final Value value;

        public RunWith(Process process, Value value) {
            this.process = process;
            this.value = value;
        }

        @Override
        public Value run() {
            return value.invoke(process.run());
        }
    }

    @MField(name = "impure")
    Value impure = new Value.Impl1("impure", Impure::new);

    @MField(name = "then-run-with")
    Value thenRunWith = new Value.Impl2("then-run-with", (proc, fn) -> new ThenRunWith((Process) proc, fn));

    @MField(name = "then-run")
    Value thenRun = new Value.Impl2("then-run", (first, second) -> new ThenRun((Process) first, (Process) second));

    @MField(name = "run-with")
    Value runWith = new Value.Impl2("run-with", (proc, fn) -> new RunWith((Process) proc, fn));
}
