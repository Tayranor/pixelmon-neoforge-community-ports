package de.waterdu.atlantis.occasion;

import com.google.common.collect.Lists;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.java.EventHandler;
import de.waterdu.atlantis.util.java.collection.Pair;
import de.waterdu.atlantis.util.java.interfaces.EventConsumer;
import de.waterdu.atlantis.util.level.ConfigurableLevel;
import de.waterdu.atlantis.util.level.LevelPosition;
import de.waterdu.atlantis.util.level.LocalPosition;
import de.waterdu.atlantis.util.server.ServerUtils;
import de.waterdu.atlantis.util.text.MessageUtils;
import net.minecraft.Util;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Occasion extends EventHandler<Occasion> {

    public static final Occasion VOID = new Occasion(Util.NIL_UUID);

    protected final UUID uuid;
    protected final AtomicBoolean repeats;
    protected final AtomicLong tick;
    protected final List<PlayerReference> members;
    protected final Set<PlayerReference> memberSet;
    protected final List<Optional<Moment>> moments;
    protected final Map<UUID, Pair<Integer, Optional<Moment>>> momentMap;
    protected final AtomicInteger currentMoment;

    protected final Map<PlayerReference, Optional<LevelPosition>> specificRecoveryPositions;
    protected final AtomicReference<LevelPosition> recoveryPosition;

    protected final AtomicReference<Consumer<Occasion>> onComplete;
    protected final AtomicReference<BiConsumer<Long, Occasion>> onTick;
    protected final AtomicReference<BiConsumer<PlayerReference, Occasion>> onPlayerAdded;
    protected final AtomicReference<BiConsumer<PlayerReference, Occasion>> onPlayerRemoved;
    protected final AtomicReference<BiConsumer<PlayerReference, Occasion>> onPlayerChanged;

    private Occasion() {
        this(UUID.randomUUID());
    }

    private Occasion(UUID uuid) {
        super();
        this.uuid = uuid;
        this.repeats = new AtomicBoolean(false);
        this.tick = new AtomicLong(0);
        this.members = Lists.newCopyOnWriteArrayList();
        this.memberSet = new HashSet<>();
        this.moments = new ArrayList<>();
        this.momentMap = new HashMap<>();
        this.currentMoment = new AtomicInteger(0);
        this.specificRecoveryPositions = new HashMap<>();
        this.recoveryPosition = new AtomicReference<>(ServerUtils.spawnPoint());
        this.onComplete = new AtomicReference<>(occasion -> {});
        this.onTick = new AtomicReference<>((tick, occasion) -> {});
        this.onPlayerAdded = new AtomicReference<>((player, occasion) -> {});
        this.onPlayerRemoved = new AtomicReference<>((player, occasion) -> {});
        this.onPlayerChanged = new AtomicReference<>((player, occasion) -> {});
    }

    private Occasion init() {
        this.currentMoment().ifPresent(moment -> moment.onStart.get().accept(moment));
        return this;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public boolean repeats() {
        return this.repeats.get();
    }

    public void reset() {
        this.removeAllMembers();
        if (this.repeats()) {
            this.resetTick();
            this.currentMoment.set(0);
            this.specificRecoveryPositions.clear();
            for (var moment : this.moments) {
                moment.ifPresent(Moment::reset);
            }
        }
    }

    public boolean active() {
        return !this.completed();
    }

    public boolean completed() {
        return this.currentMoment.get() >= this.moments.size();
    }

    public int addMembers(Object... members) {
        var added = new AtomicInteger(0);
        for (var member : members) {
            PlayerReference.getSafely(member).ifPresent(reference -> {
                if (!this.memberSet.contains(reference)) {
                    this.members.add(reference);
                    this.memberSet.add(reference);
                    this.onPlayerAdded.get().accept(reference, this);
                    this.onPlayerChanged.get().accept(reference, this);
                    this.currentMoment().ifPresent(moment -> {
                        moment.onPlayerAdded.get().accept(reference, moment);
                        moment.onPlayerChanged.get().accept(reference, moment);
                    });
                    added.getAndIncrement();
                }
            });
        }
        return added.get();
    }

    public boolean isMember(Object reference) {
        return PlayerReference.getSafely(reference).map(this.memberSet::contains).orElse(false);
    }

    public Moment addMoment(int index, Moment moment) {
        var wrappedMoment = Optional.of(moment);
        this.moments.add(index, wrappedMoment);
        this.momentMap.put(moment.uuid(), Pair.of(index, wrappedMoment));
        return moment;
    }

    protected List<PlayerReference> membersDirect() {
        return this.members;
    }

    public void forEachMember(Consumer<PlayerReference> forEach) {
        for (PlayerReference reference : this.members) {
            forEach.accept(reference);
        }
    }

    public void forEachMember(Consumer<ServerPlayer> forEach, Consumer<PlayerReference> orElse) {
        for (var reference : this.members) {
            var player = reference.entity();
            if (player.isPresent()) {
                forEach.accept(player.get());
            } else if (orElse != null) {
                orElse.accept(reference);
            }
        }
    }

    public <T> void send(T message) {
        MessageUtils.send(message, this.members);
    }

    public Optional<Moment> currentMoment() {
        return this.completed() ? Optional.empty() : this.moments.get(this.currentMomentOrdinal());
    }

    public int currentMomentOrdinal() {
        return this.currentMoment.get();
    }

    public Optional<Moment> moment(UUID uuid) {
        if (!this.momentMap.containsKey(uuid)) {
            return Optional.empty();
        }
        return this.momentMap.get(uuid).getB();
    }

    public Optional<Moment> moment(int ordinal) {
        if (ordinal < 0 || ordinal >= this.moments.size()) {
            return Optional.empty();
        }
        return this.moments.get(ordinal);
    }

    public int momentOrdinal(Moment moment) {
        if (!this.momentMap.containsKey(moment.uuid())) {
            return -1;
        }
        return this.momentMap.get(moment.uuid()).getA();
    }

    public boolean incrementMoment() {
        this.currentMoment().ifPresent(moment -> {
            moment.onEnd.get().accept(moment);
            moment.updateBossBar(ServerBossEvent::removeAllPlayers);
            this.forEachMember(member -> moment.updateIndividualBossBar(member, ServerBossEvent::removeAllPlayers));
        });
        this.currentMoment.getAndIncrement();
        this.currentMoment().ifPresent(moment -> moment.onStart.get().accept(moment));
        var complete = this.completed();
        if (complete) {
            this.onComplete.get().accept(this);
            this.reset();
        }
        return complete;
    }

    public boolean gotoMoment(int index) {
        if (index < 0 || index >= this.momentCount()) {
            return false;
        }
        this.currentMoment().ifPresent(moment -> {
            moment.onEnd.get().accept(moment);
            moment.updateBossBar(ServerBossEvent::removeAllPlayers);
            this.forEachMember(member -> moment.updateIndividualBossBar(member, ServerBossEvent::removeAllPlayers));
        });
        this.currentMoment.set(index);
        this.currentMoment().ifPresent(moment -> moment.onStart.get().accept(moment));
        return true;
    }

    public void complete() {
        this.currentMoment().ifPresent(moment -> {
            moment.onEnd.get().accept(moment);
            moment.updateBossBar(ServerBossEvent::removeAllPlayers);
            this.forEachMember(member -> moment.updateIndividualBossBar(member, ServerBossEvent::removeAllPlayers));
        });
        this.currentMoment.set(this.momentCount());
        this.onComplete.get().accept(this);
        this.reset();
    }

    public boolean removeMember(Object reference) {
        return PlayerReference.getSafely(reference).map(player -> {
            var changed = this.members.remove(player);
            if (changed) {
                this.memberSet.remove(player);
                OccasionManager.removePlayer(player);
                var recovery = this.recoveryPosition(player);
                OccasionRecovery.add(player, -1, recovery);
                recovery.teleport(player);
                this.onPlayerRemoved.get().accept(player, this);
                this.onPlayerChanged.get().accept(player, this);
                this.currentMoment().ifPresent(moment -> {
                    moment.onPlayerRemoved.get().accept(player, moment);
                    moment.onPlayerChanged.get().accept(player, moment);
                    player.entity().ifPresent(entity -> {
                        moment.updateBossBar(bar -> bar.removePlayer(entity));
                        moment.updateIndividualBossBar(player, bar -> bar.removePlayer(entity));
                        moment.removeIndividualBossBar(player);
                    });
                });
            }
            return changed;
        }).orElse(false);
    }

    public void removeAllMembers() {
        for (var reference : this.members) {
            this.memberSet.remove(reference);
            OccasionManager.removePlayer(reference);
            var recovery = this.recoveryPosition(reference);
            OccasionRecovery.add(reference, -1, recovery);
            recovery.teleport(reference);
            this.onPlayerRemoved.get().accept(reference, this);
            this.onPlayerChanged.get().accept(reference, this);
            this.currentMoment().ifPresent(moment -> {
                moment.onPlayerRemoved.get().accept(reference, moment);
                moment.onPlayerChanged.get().accept(reference, moment);
                reference.entity().ifPresent(entity -> {
                    moment.updateBossBar(bar -> bar.removePlayer(entity));
                    moment.updateIndividualBossBar(reference, bar -> bar.removePlayer(entity));
                    moment.removeIndividualBossBar(reference);
                });
            });
            this.members.remove(reference);
        }
    }

    public int memberCount() {
        return this.memberSet.size();
    }

    public int momentCount() {
        return this.moments.size();
    }

    public void teleportToStart(Object reference) {
        this.currentMoment().map(moment -> moment.startPosition.get()).ifPresent(position -> position.teleport(PlayerReference.get(reference)));
    }

    public void teleportAllToStart() {
        this.currentMoment().map(moment -> moment.startPosition.get()).ifPresent(position -> {
            for (var reference : this.membersDirect()) {
                position.teleport(reference);
            }
        });
    }

    public LevelPosition recoveryPosition(Object reference) {
        return this.specificRecoveryPositions.getOrDefault(PlayerReference.get(reference), Optional.empty()).orElse(this.recoveryPosition.get());
    }

    public void setSpecificRecoveryPosition(Object reference, LevelPosition position) {
        this.specificRecoveryPositions.put(PlayerReference.get(reference), Optional.of(position));
    }

    public void teleportToRecovery(Object reference) {
        this.recoveryPosition(reference).teleport(PlayerReference.get(reference));
    }

    @Override
    public <E extends Event> void handleEvent(E event) {
        super.handleEvent(event);
        this.currentMoment().ifPresent(moment -> moment.handleEvent(event));
    }

    public void resetTick() {
        this.tick.set(0);
    }

    public long currentTick() {
        return this.tick.get();
    }

    protected boolean tick() {
        if (!this.completed()) {
            this.tick.incrementAndGet();
            this.currentMoment().ifPresent(Moment::tick);
            this.onTick.get().accept(this.currentTick(), this);
            return false;
        }
        if (this.repeats()) {
            this.reset();
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Occasion other)) {
            return false;
        }
        return this.uuid.equals(other.uuid);
    }

    @Override
    public String toString() {
        return "Occasion(" + this.uuid + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Occasion occasion;

        private Builder() {
            this.occasion = new Occasion();
            this.defaultLoginLogoutHandlers();
        }

        public Moment.Builder newMoment() {
            return Moment.builder(this);
        }

        public Builder repeats() {
            this.occasion.repeats.set(true);
            return this;
        }

        public Builder single() {
            this.occasion.repeats.set(false);
            return this;
        }

        public Builder recoveryPosition(ConfigurableLevel level, LocalPosition position) {
            return this.recoveryPosition(LevelPosition.of(level, position));
        }

        public Builder recoveryPosition(LevelPosition position) {
            this.occasion.recoveryPosition.set(position);
            return this;
        }

        public <E extends Event> Builder eventHandler(Class<E> event, EventConsumer<E, Occasion> handler) {
            this.occasion.addEventHandler(event, handler);
            OccasionManager.registerEventHandlers(event);
            return this;
        }

        public <E extends Event> Builder clearEventHandlers(Class<E> event) {
            this.occasion.clearEventHandlers(event);
            return this;
        }

        public Builder defaultLoginLogoutHandlers() {
            return this
                    .eventHandler(PlayerEvent.PlayerLoggedInEvent.class,
                            (event, handler) -> handler.currentMoment().ifPresent(
                                    moment -> moment.logouts.put(PlayerReference.get(event.getEntity()), -1L)
                            )
                    )
                    .eventHandler(PlayerEvent.PlayerLoggedOutEvent.class,
                            (event, handler) -> handler.currentMoment().ifPresent(
                                    moment -> moment.logouts.put(PlayerReference.get(event.getEntity()), System.currentTimeMillis())
                            )
                    );
        }

        public Builder noLoginLogoutHandlers() {
            return this.clearEventHandlers(PlayerEvent.PlayerLoggedInEvent.class).clearEventHandlers(PlayerEvent.PlayerLoggedOutEvent.class);
        }

        public Builder onComplete(Consumer<Occasion> onComplete) {
            this.occasion.onComplete.set(onComplete);
            return this;
        }

        public Builder onTick(BiConsumer<Long, Occasion> onTick) {
            this.occasion.onTick.set(onTick);
            return this;
        }

        public Builder onPlayerAdded(BiConsumer<PlayerReference, Occasion> onPlayerAdded) {
            this.occasion.onPlayerAdded.set(onPlayerAdded);
            return this;
        }

        public Builder onPlayerRemoved(BiConsumer<PlayerReference, Occasion> onPlayerRemoved) {
            this.occasion.onPlayerRemoved.set(onPlayerRemoved);
            return this;
        }

        public Builder onPlayerChanged(BiConsumer<PlayerReference, Occasion> onPlayerChanged) {
            this.occasion.onPlayerChanged.set(onPlayerChanged);
            return this;
        }

        public Occasion build() {
            return OccasionManager.add(this.occasion).init();
        }
    }
}
