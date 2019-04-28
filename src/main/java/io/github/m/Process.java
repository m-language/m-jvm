package io.github.m;

/**
 * M implementation of IO processes.
 */
@FunctionalInterface
public interface Process extends Value {
    Value run();

    @Override
    default Value invoke(Value arg) {
        return new ThenRunWith(this, arg);
    }

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
    Value impure = new Value.Impl1(Impure::new);

    @MField(name = "then-run-with")
    Value thenRunWith = new Value.Impl2((proc, fn) -> new ThenRunWith((Process) proc, fn));

    @MField(name = "then-run")
    Value thenRun = new Value.Impl2((first, second) -> new ThenRun((Process) first, (Process) second));

    @MField(name = "run-with")
    Value runWith = new Value.Impl2((proc, fn) -> new RunWith((Process) proc, fn));
}
