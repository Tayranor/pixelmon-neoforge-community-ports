package de.waterdu.atlantis.util.java.interfaces;

import net.neoforged.bus.api.Event;

@FunctionalInterface
public interface EventConsumer<E extends Event, H> {

    void consume(E event, H handler);
}
