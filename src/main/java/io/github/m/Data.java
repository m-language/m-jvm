package io.github.m;

import java.util.Collections;
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

    @Override
    default Value value() {
        return new Pair(type(), new Value.Impl1(name -> get(Symbol.from(name))));
    }

    class Abstract implements Data {
        public final Symbol type;
        public final Map<String, Value> fields;

        public Abstract(Symbol type, Map<String, Value> fields) {
            this.type = type;
            this.fields = Collections.unmodifiableMap(fields);
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
                return new Error("No field " + name + " for data " + type().value);
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
    Value data = new Value.Impl2((type, fields) -> {
        Map<String, Value> map = new HashMap<>();
        List.from(fields).forEach(value -> {
            Pair pair = Pair.from(value);
            map.put(Symbol.from(pair.first).value, pair.second);
        });
        return new Abstract((Symbol) type, map);
    });
}
