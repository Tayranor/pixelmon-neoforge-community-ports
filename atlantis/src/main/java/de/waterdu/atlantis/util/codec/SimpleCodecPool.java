package de.waterdu.atlantis.util.codec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SimpleCodecPool<T> {

    private final Map<String, SimpleCodec<T>> codecs = new HashMap<>();

    public SimpleCodec<T> add(SimpleCodec<T> codec) {
        this.codecs.put(codec.getID(), codec);
        return codec;
    }

    public Optional<SimpleCodec<T>> get(String id) {
        return Optional.ofNullable(this.codecs.get(id));
    }
}
