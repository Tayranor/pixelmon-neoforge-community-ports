package de.waterdu.atlantis.util.java.math;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.waterdu.atlantis.util.java.JsonUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Formula {

    private final Map<String, Variable> variables = new LinkedHashMap<>();
    private final Collection<String> lines;

    public Formula(Collection<String> lines) {
        this.lines = lines;
        for (var segment : lines) {
            segment = segment.replace(" ", "");
            if (segment.contains("=")) {
                var equalsSplit = new String[]{segment.substring(0, segment.indexOf("=")), segment.substring(segment.indexOf("=") + 1)};
                var variable = equalsSplit[0];
                var formula = equalsSplit[1];
                this.variables.put(variable, new Variable(formula));
            }
        }
    }

    public Map<String, Double> process(Map<String, Double> defaultVariables) {
        var variables = new LinkedHashMap<>(defaultVariables);
        for (var constant : Constant.values()) {
            for (var representation : constant.representations()) {
                variables.put(representation, constant.value());
            }
        }
        for (var entry : this.variables.entrySet()) {
            var name = String.valueOf(entry.getKey());
            var value = entry.getValue().calculate(variables);
            variables.put(name, value);
        }
        return variables;
    }

    public Collection<String> lines() {
        return this.lines;
    }

    public static class TypeAdapter implements JsonSerializer<Formula>, JsonDeserializer<Formula> {

        @Override
        public Formula deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Formula(JsonUtils.deserializeList(json.getAsJsonArray(), JsonElement::getAsString));
        }

        @Override
        public JsonElement serialize(Formula src, Type typeOfSrc, JsonSerializationContext context) {
            return JsonUtils.serializeList(src.lines(), JsonPrimitive::new);
        }
    }
}
