package de.waterdu.atlantis.mod.pixelmon.battle;

import com.mojang.datafixers.util.Either;
import com.pixelmonmod.pixelmon.api.battles.BattleEndCause;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import de.waterdu.atlantis.util.java.collection.Pair;
import de.waterdu.atlantis.util.level.ConfigurableLevel;
import de.waterdu.atlantis.util.level.LocalPosition;
import de.waterdu.atlantis.util.server.ServerUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Battle<A extends BattleParticipant, B extends BattleParticipant> {

    private final Class<A> aType;
    private final Class<B> bType;
    private final Predicate<A> aTest;
    private final Predicate<B> bTest;
    private final List<BattleListener<A, B>> listeners = new ArrayList<>();

    private Either<Entity, PartyStorage> a = null;
    private Either<Entity, PartyStorage> b = null;
    private LocalPosition aPosition = null;
    private LocalPosition bPosition = null;
    private ConfigurableLevel level = null;

    private boolean battleBuilderCreated = false;

    protected Battle(Class<A> aType, Class<B> bType, Predicate<A> aTest, Predicate<B> bTest) {
        this.aType = aType;
        this.bType = bType;
        this.aTest = aTest;
        this.bTest = bTest;
    }

    public Battle<A, B> listener(BattleListener<A, B> listener) {
        this.listeners.add(listener);
        return this;
    }

    public Battle<A, B> level(ConfigurableLevel level) {
        this.level = level;
        return this;
    }

    public Battle<A, B> level(String key) {
        return this.level(ConfigurableLevel.of(key));
    }

    public Battle<A, B> level(String mod, String key) {
        return this.level(ConfigurableLevel.of(mod, key));
    }

    public Battle<A, B> level(ResourceLocation location) {
        return this.level(ConfigurableLevel.of(location));
    }

    public Battle<A, B> level(ResourceKey<Level> level) {
        return this.level(ConfigurableLevel.of(level));
    }

    public Battle<A, B> level(Level level) {
        return this.level(ConfigurableLevel.of(level));
    }

    public Battle<A, B> participants(Entity a, Entity b) {
        return this.a(a).b(b);
    }

    public Battle<A, B> participants(PartyStorage a, Entity b) {
        return this.a(a).b(b);
    }

    public Battle<A, B> participants(Entity a, PartyStorage b) {
        return this.a(a).b(b);
    }

    public Battle<A, B> participants(PartyStorage a, PartyStorage b) {
        return this.a(a).b(b);
    }
    
    protected Battle<A, B> a(Entity a) {
        this.a = Either.left(a);
        return this;
    }

    protected Battle<A, B> b(Entity b) {
        this.b = Either.left(b);
        return this;
    }

    protected Battle<A, B> a(PartyStorage a) {
        this.a = Either.right(a);
        return this;
    }

    protected Battle<A, B> b(PartyStorage b) {
        this.b = Either.right(b);
        return this;
    }

    public Battle<A, B> positions(LocalPosition a, LocalPosition b) {
        this.aPosition = a;
        this.bPosition = b;
        return this;
    }

    public Battle<A, B> teleport() {
        if (this.aPosition != null && this.bPosition != null) {
            Entity a = null;
            Entity b = null;
            if (this.a.left().isPresent()) {
                a = this.a.left().get();
            } else if (this.a.right().isPresent()) {
                a = ServerUtils.player(this.a.right().get().getUniqueId()).orElse(null);
            }
            if (this.b.left().isPresent()) {
                b = this.b.left().get();
            } else if (this.b.right().isPresent()) {
                b = ServerUtils.player(this.b.right().get().getUniqueId()).orElse(null);
            }
            if (a != null && b != null) {
                if (this.level != null) {
                    this.aPosition.teleport(a, this.level.get());
                    this.bPosition.teleport(b, this.level.get());
                } else {
                    this.aPosition.teleport(a);
                    this.bPosition.teleport(b);
                }
            }
        }
        return this;
    }
    
    public TeamSelectionRegistry.Builder battle() {
        if (this.a == null || this.b == null) {
            throw new IllegalStateException("Null participants for Atlantis battle");
        }
        this.battleBuilderCreated = true;
        var builder = TeamSelectionRegistry.builder();
        this.a.left().ifPresent(builder::members);
        this.a.right().ifPresent(builder::members);
        this.b.left().ifPresent(builder::members);
        this.b.right().ifPresent(builder::members);
        return builder.battleStartConsumer(this::onBattleStart);
    }

    protected void forwardBattleStartEvent(BattleStartedEvent.Post event) {
        if (!this.battleBuilderCreated) {
            this.onBattleStart(event.getBattleController());
        }
    }

    @SuppressWarnings("unchecked")
    protected Optional<Pair<A, B>> checkBattle(BattleController battle) {
        if (battle.participants().size() != 2) {
            return Optional.empty();
        }
        A a = null;
        B b = null;
        for (var bp : battle.participants()) {
            if (a == null && this.aType.isInstance(bp) && this.aTest.test((A) bp)) {
                a = (A) bp;
            } else if (b == null && this.bType.isInstance(bp) && this.bTest.test((B) bp)) {
                b = (B) bp;
            }
        }
        if (a != null && b != null && !a.getAllies().contains(b)) {
            return Optional.of(Pair.of(a, b));
        }
        return Optional.empty();
    }

    protected void onBattleStart(BattleController battle) {
        this.checkBattle(battle).ifPresent(participants -> {
            this.teleport();
            var a = participants.getA();
            var b = participants.getB();
            var ticket = new Battle.Ticket.Start<>(a, b, battle.teams().stream().filter(team -> team.contains(a)).findFirst().orElseThrow(), battle.teams().stream().filter(team -> team.contains(b)).findFirst().orElseThrow(), battle);
            for (var listener : this.listeners) {
                listener.onBattleStart(ticket);
            }
        });
    }

    protected void onBattleEnd(BattleEndEvent event, UUID uuid, List<UUID> toDeregister) {
        var battle = event.getBattleController();
        this.checkBattle(battle).ifPresent(participants -> {
            var a = participants.getA();
            var b = participants.getB();
            var ticket = new Battle.Ticket.End<>(a, b, battle.teams().stream().filter(team -> team.contains(a)).findFirst().orElseThrow(), battle.teams().stream().filter(team -> team.contains(b)).findFirst().orElseThrow(), battle, event);
            for (var listener : this.listeners) {
                listener.onBattleEnd(ticket);
            }
            toDeregister.add(uuid);
        });
    }

    public static class Ticket<A extends BattleParticipant, B extends BattleParticipant> {

        private final A a;
        private final B b;
        private final List<BattleParticipant> aTeam;
        private final List<BattleParticipant> bTeam;
        private final BattleController battle;

        protected Ticket(A a, B b, List<BattleParticipant> aTeam, List<BattleParticipant> bTeam, BattleController battle) {
            this.a = a;
            this.b = b;
            this.aTeam = aTeam;
            this.bTeam = bTeam;
            this.battle = battle;
        }

        public A a() {
            return this.a;
        }

        public B b() {
            return this.b;
        }

        public List<BattleParticipant> teamA() {
            return this.aTeam;
        }

        public List<BattleParticipant> teamB() {
            return this.bTeam;
        }

        public BattleController battle() {
            return this.battle;
        }

        public static class Start<A extends BattleParticipant, B extends BattleParticipant> extends Ticket<A, B> {

            protected Start(A a, B b, List<BattleParticipant> aTeam, List<BattleParticipant> bTeam, BattleController battle) {
                super(a, b, aTeam, bTeam, battle);
            }
        }

        public static class End<A extends BattleParticipant, B extends BattleParticipant> extends Ticket<A, B> {

            private final Map<BattleParticipant, BattleResults> results;
            private final boolean abnormal;
            private final BattleEndCause cause;

            protected End(A a, B b, List<BattleParticipant> aTeam, List<BattleParticipant> bTeam, BattleController battle, BattleEndEvent event) {
                super(a, b, aTeam, bTeam, battle);
                this.abnormal = event.isAbnormal();
                this.cause = event.getCause();
                this.results = event.getResults();
            }

            public void execute(Consumer<A> aWins, Consumer<B> bWins, Consumer<A> aLoses, Consumer<B> bLoses, Consumer<A> aDraws, Consumer<B> bDraws) {
                var a = this.a();
                var b = this.b();

                switch (this.resultsA()) {
                    case VICTORY:
                        aWins.accept(a);
                        break;
                    case DRAW:
                        aDraws.accept(a);
                        break;
                    default:
                        aLoses.accept(a);
                        break;
                }

                switch (this.resultsB()) {
                    case VICTORY:
                        bWins.accept(b);
                        break;
                    case DRAW:
                        bDraws.accept(b);
                        break;
                    default:
                        bLoses.accept(b);
                        break;
                }
            }

            public BattleResults resultsA() {
                return this.results.get(this.a());
            }

            public BattleResults resultsB() {
                return this.results.get(this.b());
            }

            public Map<BattleParticipant, BattleResults> results() {
                return this.results;
            }

            public boolean abnormal() {
                return this.abnormal;
            }

            public BattleEndCause cause() {
                return this.cause;
            }
        }
    }
}
