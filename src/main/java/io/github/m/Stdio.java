package io.github.m;

import java.io.IOException;

public final class Stdio {
    @MField(name = "stdout")
    public static final Value stdout = new Value.Impl1("stdout", arg ->
            new Process.Impl("(stdout char)", () -> {
                System.out.write(Char.from(arg).value);
                return List.NIL;
            }));

    @MField(name = "stderr")
    public static final Value stderr = new Value.Impl1("stderr", arg ->
            new Process.Impl("(stderr char)", () -> {
                System.err.write(Char.from(arg).value);
                return List.NIL;
            }));

    @MField(name = "stdin")
    public static final Value stdin = new Process.Impl("stdin", () -> {
        System.out.flush();
        System.err.flush();
        try {
            return Char.valueOf((char) System.in.read());
        } catch (IOException e) {
            return Error.wrap(e);
        }
    });
}
