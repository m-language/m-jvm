package io.github.m;

import java.util.HashMap;
import java.util.Map;

/**
 * M wrapper class for data.
 */
public interface Data extends Value.Delegate {
    /**
     * The type of the data.
     */
    Symbol type();

    /**
     * The value of a field given a name.
     */
    Value get(Symbol name);

    /**
     * Delegates this data to a pair of its type and a function from a symbol to
     * the value of the field with that symbol's name.
     */
    @Override
    default Value value() {
        Value function = new Impl1(type().value, name -> get(Symbol.from(name)));
        return new Pair(type(), function);
    }

    /**
     * An abstract implementation of data.
     */
    class Abstract implements Data {
        /**
         * The type of the data.
         */
        private final Symbol type;

        /**
         * A map of fields of the data.
         */
        private final Map<String, Value> fields;

        public Abstract(Symbol type, Map<String, Value> fields) {
            this.type = type;
            this.fields = fields;
        }

        @SafeVarargs
        public Abstract(String type, kotlin.Pair<String, Value>... fields) {
            this(Symbol.valueOf(type), toMap(fields));
        }

        @Override
        public Symbol type() {
            return type;
        }

        @Override
        public Value get(Symbol name) {
            Value value = fields.get(name.value);
            if (value == null)
                throw new Error("No field " + name + " for data " + type().value);
            else
                return value;
        }

        @Override
        public String toString() {
            return type.toString() + fields.toString();
        }

        private static Map<String, Value> toMap(kotlin.Pair<String, Value>[] fields) {
            Map<String, Value> map = new HashMap<>(fields.length);
            for (kotlin.Pair<String, Value> field : fields) {
                map.put(field.getFirst(), field.getSecond());
            }
            return map;
        }
    }

    @MField(name = "data")
    Value data = new Value.Impl2("data", (type, fields) -> {
        Map<String, Value> map = new HashMap<>();
        List.from(fields).forEach(value -> {
            Pair pair = Pair.from(value);
            map.put(Symbol.from(pair.first).value, pair.second);
        });
        return new Abstract((Symbol) type, map);
    });
}
