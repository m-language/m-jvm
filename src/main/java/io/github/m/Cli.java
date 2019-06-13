package io.github.m;

/**
 * Bindings for M command line interfaces.
 */
public final class Cli {
    /**
     * The implementation of the main function for an M program.
     *
     * @param args  The array of arguments passed to the program.
     * @param clazz The class to run.
     */
    public static void run(String[] args, Class<?> clazz) throws Throwable {
        try {
            Value main = (Value) clazz.getField(Symbol.normalize("")).get(null);
            Process process = (Process) main.invoke(toList(args));
            Value result = process.run();

            if (result instanceof Error)
                throw ((Error) result);
        } catch (Throwable e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                stackTrace[i] = new StackTraceElement(
                        stackTrace[i].getClassName(),
                        clean(stackTrace[i].getMethodName()),
                        stackTrace[i].getFileName(),
                        stackTrace[i].getLineNumber()
                );
            }
            e.setStackTrace(stackTrace);
            throw e;
        }
        System.out.flush();
    }

    private static List toList(String[] args) {
        List list = List.NIL;
        for (int i = args.length - 1; i >= 0; i--) {
            list = new List.Cons(Symbol.valueOf(args[i]), list);
        }
        return list;
    }

    private static String clean(String string) {
        int uPosition = string.indexOf('_');
        if (uPosition == -1) {
            return Symbol.denormalize(string);
        } else {
            return clean(string.substring(0, uPosition));
        }
    }
}
