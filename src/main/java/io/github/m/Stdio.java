package io.github.m;

import java.io.IOException;

public final class Stdio {
    @MField(name = "stdout")
    public static final Value stdout = arg -> (Process) () -> {
        System.out.write(Char.from(arg).value);
        return List.NIL;
    };

    @MField(name = "stderr")
    public static final Value stderr = arg -> (Process) () -> {
        System.err.write(Char.from(arg).value);
        return List.NIL;
    };

    @MField(name = "stdin")
    public static final Value stdin = (Process) () -> {
        System.out.flush();
        System.err.flush();
        try {
            return Char.valueOf((char) System.in.read());
        } catch (IOException e) {
            return Error.wrap(e);
        }
    };
}
