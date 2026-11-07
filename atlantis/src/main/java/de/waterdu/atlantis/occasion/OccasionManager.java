package de.waterdu.atlantis.occasion;

import com.google.common.collect.Sets;
import de.waterdu.atlantis.Settings;
import de.waterdu.atlantis.mod.pixelmon.AtlantisPixelmon;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.java.ExceptionUtils;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class OccasionManager {

    private static final AtomicReference<OccasionListener> LISTENER = new AtomicReference<>();
    private static final Map<EventPriority, Set<Class<? extends Event>>> REGISTERED_HANDLERS = new HashMap<>();
    private static final Set<IEventBus> EVENT_BUSES = new HashSet<>();
    private static final Set<Occasion> OCCASIONS = Sets.newConcurrentHashSet();
    private static final Map<UUID, Optional<Occasion>> UUID_MAP = new ConcurrentHashMap<>();
    private static final Map<PlayerReference, Optional<Occasion>> PLAYER_MAP = new ConcurrentHashMap<>();

    private OccasionManager() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static void register() {
        LISTENER.set(new OccasionListener());
        OccasionManager.registerEventBus(NeoForge.EVENT_BUS);
        AtlantisPixelmon.registerEventBus();
        OccasionManager.registerEventHandlers(EventPriority.LOWEST,
                PlayerEvent.PlayerLoggedInEvent.class,
                PlayerEvent.PlayerLoggedOutEvent.class
        );
    }

    public static void registerEventBus(IEventBus bus) {
        EVENT_BUSES.add(bus);
    }

    @SafeVarargs
    public static void registerEventHandlers(Class<? extends Event>... events) {
        OccasionManager.registerEventHandlers(Settings.get().getOccasionsDefaultEventHandlerPriority(), events);
    }

    @SafeVarargs
    public static void registerEventHandlers(EventPriority priority, Class<? extends Event>... events) {
        for (var event : events) {
            if (REGISTERED_HANDLERS.computeIfAbsent(priority, key -> new HashSet<>()).add(event)) {
                for (var bus : EVENT_BUSES) {
                    bus.addListener(priority, false, event, OccasionManager::handleEvent);
                }
            }
        }
    }

    public static void handleEvent(Event event) {
        for (var occasion : OCCASIONS) {
            occasion.handleEvent(event);
        }
    }

    protected static Occasion add(Occasion occasion) {
        OCCASIONS.add(occasion);
        UUID_MAP.put(occasion.uuid(), Optional.of(occasion));
        return occasion;
    }

    protected static void remove(Occasion occasion) {
        OCCASIONS.remove(occasion);
        UUID_MAP.remove(occasion.uuid());
        for (var reference : occasion.membersDirect()) {
            OccasionManager.removePlayer(reference);
        }
    }

    public static Optional<Occasion> getFor(UUID uuid) {
        return UUID_MAP.getOrDefault(uuid, Optional.empty());
    }

    public static Optional<Occasion> getFor(PlayerReference reference) {
        return PLAYER_MAP.getOrDefault(reference, Optional.empty());
    }

    public static boolean isIn(PlayerReference reference) {
        return OccasionManager.getFor(reference).map(Occasion::active).orElse(false);
    }

    protected static void removePlayer(PlayerReference reference) {
        PLAYER_MAP.remove(reference);
    }

    protected static void tick() {
        OCCASIONS.removeIf(occasion -> {
            if (occasion.tick()) {
                UUID_MAP.remove(occasion.uuid());
                for (var reference : occasion.membersDirect()) {
                    OccasionManager.removePlayer(reference);
                }
                return true;
            }
            return false;
        });
    }
}
