package io.github.m;

/**
 * M wrapper class for strings.
 */
public class Symbol implements Value.Delegate {
    public final String value;

    private Symbol(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Value value() {
        if (value.isEmpty()) {
            return List.NIL;
        } else {
            return new Pair(Char.valueOf(value.charAt(0)), new Symbol(value.substring(1)));
        }
    }

    public static Symbol valueOf(String string) {
        return new Symbol(string);
    }

    public static Symbol from(Value value) {
        if (value instanceof Symbol) {
            return (Symbol) value;
        } else {
            return new Symbol(toString(value));
        }
    }

    public static String toString(Value value) {
        StringBuilder builder = new StringBuilder();
        List.from(value).forEach(x -> builder.append(Char.from(x).value));
        return builder.toString();
    }

    static List toList(String string) {
        char[] chars = string.toCharArray();
        List list = List.NIL;
        for (int i = chars.length - 1; i >= 0; i--) {
            list = new List.Cons(Char.valueOf(chars[i]), list);
        }
        return list;
    }

    public static String normalize(String string) {
        char[] chars = string.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            if ((c >= 'a' && c <= 'z') || c == '_') {
                builder.append(c);
            } else {
                builder.append('$');
                builder.append(Integer.valueOf((int) c).toString());
            }
        }
        String value = builder.toString();
        if (value.isEmpty()) {
            return "$$";
        } else {
            return value;
        }
    }

    public static String denormalize(String string) {
        char[] chars = string.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '$') {
                StringBuilder number = new StringBuilder();
                for (i++; i < chars.length && chars[i] >= '0' && chars[i] <= '9'; i++) {
                    number.append(chars[i]);
                }
                String s = number.toString();
                if (!s.isEmpty()) {
                    builder.append((char) Integer.parseInt(s));
                }
                i--;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    @MField(name = "symbol.=")
    public static final Value eq = new Value.Impl2("symbol.=", (x, y) -> Bool.valueOf(from(x).value.equals(from(y).value)));

    @MField(name = "symbol.+")
    public static final Value add = new Value.Impl2("symbol.+", (x, y) -> new Symbol(from(x).value + from(y).value));

    @MField(name = "symbol->list")
    public static final Value toList = new Value.Impl1("symbol->list", value -> toList(from(value).value));

    @MField(name = "normalize")
    public static final Value normalize = new Value.Impl1("normalize", value -> new Symbol(normalize(from(value).value)));

    @MField(name = "denormalize")
    public static final Value denormalize = new Value.Impl1("denormalize", value -> new Symbol(denormalize(from(value).value)));
}
