package de.waterdu.atlantis.util.java.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public sealed interface Lazy<T> extends Supplier<T> {
    
    static <T> Lazy<T> of(@Nonnull Supplier<T> supplier) {
        return new Lazy.Fast<>(supplier);
    }
    
    static <T> Lazy<T> concurrentOf(@Nonnull Supplier<T> supplier) {
        return new Lazy.Concurrent<>(supplier);
    }

    T get();

    final class Fast<T> implements Lazy<T> {

        private final Supplier<T> supplier;
        private T instance;

        private Fast(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Nullable
        @Override
        public T get() {
            if (this.instance == null) {
                this.instance = this.supplier.get();
            }
            return this.instance;
        }
    }

    final class Concurrent<T> implements Lazy<T> {

        private final Object lock = new Object();
        private final Supplier<T> supplier;
        private final AtomicReference<T> instance;

        private Concurrent(Supplier<T> supplier) {
            this.supplier = supplier;
            this.instance = new AtomicReference<>(null);
        }

        @Nullable
        @Override
        public T get() {
            if (this.instance.get() == null) {
                synchronized (this.lock) {
                    if (this.instance.get() == null) {
                        this.instance.set(this.supplier.get());
                    }
                }
            }
            return this.instance.get();
        }
    }
}
