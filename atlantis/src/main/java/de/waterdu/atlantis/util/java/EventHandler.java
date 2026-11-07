package de.waterdu.atlantis.util.java;

import de.waterdu.atlantis.util.java.interfaces.EventConsumer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class EventHandler<H extends EventHandler<H>> {

    private final Map<Class<? extends Event>, Optional<List<EventConsumer<? extends Event, H>>>> listeners;
    private final Map<Class<? extends Event>, Optional<Method>> playerGetters;

    public EventHandler() {
        this.listeners = new HashMap<>();
        this.playerGetters = new HashMap<>();
    }

    public <E extends Event> void addEventHandler(final Class<E> event, final EventConsumer<E, H> listener) {
        this.listeners.computeIfAbsent(event, key -> Optional.of(new ArrayList<>())).get().add(listener);
        try {
            this.playerGetters.put(event, Optional.of(event.getMethod("getPlayer")));
        } catch (NoSuchMethodException ignored) {
            this.playerGetters.put(event, Optional.empty());
        }
    }

    public <E extends Event> void clearEventHandlers(final Class<E> event) {
        this.listeners.computeIfAbsent(event, key -> Optional.of(new ArrayList<>())).get().clear();
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void handleEvent(final E event) {
        var type = event.getClass();
        if (this.playerGetters.getOrDefault(type, Optional.empty()).map(method -> this.playerValid(ReflectionUtils.invokeSafely(method, event))).orElse(true)) {
            this.listeners.getOrDefault(type, Optional.empty()).ifPresent(list -> {
                for (var consumer : list) {
                    ((EventConsumer<E, H>) consumer).consume(event, (H) this);
                }
            });
        }
    }

    public boolean playerValid(Player player) {
        return true;
    }
}
