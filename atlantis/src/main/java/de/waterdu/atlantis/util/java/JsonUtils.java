package de.waterdu.atlantis.util.java;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class JsonUtils {

    private JsonUtils() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static <T> T getOrDefault(JsonObject object, String key, Class<T> type, T def, JsonDeserializationContext context) {
        if (object.has(key) && !object.get(key).isJsonNull()) {
            return context.deserialize(object.get(key), type);
        } else {
            return def;
        }
    }

    public static <T> T getOrDefault(JsonObject object, String key, TypeToken<T> type, T def, JsonDeserializationContext context) {
        if (object.has(key) && !object.get(key).isJsonNull()) {
            return context.deserialize(object.get(key), type.getType());
        } else {
            return def;
        }
    }

    public static <T> JsonArray serializeList(Collection<T> collection, Function<T, JsonElement> serializer) {
        var array = new JsonArray();
        for (var element : collection) {
            array.add(serializer.apply(element));
        }
        return array;
    }

    public static <T> ImmutableList<T> deserializeListImmutable(JsonArray array, Function<JsonElement, T> deserializer) {
        var list = JsonUtils.deserializeList(array, deserializer);
        list.remove(null);
        return ImmutableList.copyOf(list);
    }

    public static <T> List<T> deserializeList(JsonArray array, Function<JsonElement, T> deserializer) {
        var list = new ArrayList<T>();
        for (var property : array) {
            var apply = deserializer.apply(property);
            if (apply != null) {
                list.add(apply);
            }
        }
        return list;
    }
}
