package de.waterdu.atlantis.mod.pixelmon.battle;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import de.waterdu.atlantis.util.java.ExceptionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class AtlantisBattles {

    private static final Map<UUID, Battle<?, ?>> BATTLES = new ConcurrentHashMap<>();

    private AtlantisBattles() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static <A extends BattleParticipant, B extends BattleParticipant> Battle<A, B> register(Class<A> aType, Class<B> bType, Predicate<A> aTest, Predicate<B> bTest) {
        return AtlantisBattles.register(aType, bType, aTest, bTest, null);
    }

    public static <A extends BattleParticipant, B extends BattleParticipant> Battle<A, B> register(Class<A> aType, Class<B> bType, Predicate<A> aTest, Predicate<B> bTest, AtomicReference<UUID> keyReference) {
        var key = UUID.randomUUID();
        var battle = new Battle<>(aType, bType, aTest, bTest);
        AtlantisBattles.BATTLES.put(key, battle);
        if (keyReference != null) {
            keyReference.set(key);
        }
        return battle;
    }

    public static boolean deregister(UUID uuid) {
        return AtlantisBattles.BATTLES.remove(uuid) != null;
    }

    @SuppressWarnings("unchecked")
    public static <A extends BattleParticipant, B extends BattleParticipant> Optional<Battle<A, B>> get(UUID uuid) {
        return Optional.ofNullable((Battle<A, B>) AtlantisBattles.BATTLES.getOrDefault(uuid, null));
    }

    public static void onBattleStart(BattleStartedEvent.Post event) {
        for (var battle : AtlantisBattles.BATTLES.values()) {
            battle.forwardBattleStartEvent(event);
        }
    }

    public static void onBattleEnd(BattleEndEvent event) {
        var toDeregister = new ArrayList<UUID>();
        for (var battle : AtlantisBattles.BATTLES.entrySet()) {
            battle.getValue().onBattleEnd(event, battle.getKey(), toDeregister);
        }
        for (var uuid : toDeregister) {
            AtlantisBattles.deregister(uuid);
        }
    }
}
