package de.waterdu.atlantis.util.java;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassNameCache {

    private static final Map<Class<?>, String> NAME_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> SIMPLE_NAME_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> CANONICAL_NAME_MAP = new ConcurrentHashMap<>();

    private ClassNameCache() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static String getName(Class<?> clazz) {
        return ClassNameCache.NAME_MAP.computeIfAbsent(clazz, ___ -> clazz.getName());
    }

    public static String getSimpleName(Class<?> clazz) {
        return ClassNameCache.SIMPLE_NAME_MAP.computeIfAbsent(clazz, ___ -> clazz.getSimpleName());
    }

    public static String getCanonicalName(Class<?> clazz) {
        return ClassNameCache.CANONICAL_NAME_MAP.computeIfAbsent(clazz, ___ -> clazz.getCanonicalName());
    }
}
