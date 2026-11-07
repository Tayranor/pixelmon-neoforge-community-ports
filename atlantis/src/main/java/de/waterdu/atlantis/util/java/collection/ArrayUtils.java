package de.waterdu.atlantis.util.java.collection;

import de.waterdu.atlantis.util.java.ExceptionUtils;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ArrayUtils {

    private ArrayUtils() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static <T> T[] coerceTail(T[] src, T[] dest) {
        System.arraycopy(src, Math.max(0, src.length - dest.length), dest, 0, dest.length);
        return dest;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T, V> V[] map(Class<V> type, Function<T, V> mapper, T... src) {
        var newArray = (V[]) Array.newInstance(type, src.length);
        for (var i = 0; i < src.length; i++) {
            newArray[i] = mapper.apply(src[i]);
        }
        return newArray;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(Class<T> type, int size, IntFunction<T> mapper) {
        var array = (T[]) Array.newInstance(type, size);
        for (var i = 0; i < size; i++) {
            array[i] = mapper.apply(i);
        }
        return array;
    }
}
