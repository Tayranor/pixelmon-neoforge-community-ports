package de.waterdu.atlantis.self.event;

import de.waterdu.atlantis.file.datatypes.Data;
import de.waterdu.atlantis.util.config.Leaderboard;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.UUID;

public abstract class LeaderboardEvent<T extends Data, V extends Comparable<V>> extends Event {

    private final Leaderboard<T, V> leaderboard;

    private LeaderboardEvent(Leaderboard<T, V> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Leaderboard<T, V> leaderboard() {
        return this.leaderboard;
    }

    public static class Update<T extends Data, V extends Comparable<V>> extends LeaderboardEvent<T, V> implements ICancellableEvent {

        private final T data;
        private final UUID key;
        private final Comparable<V> value;

        public Update(Leaderboard<T, V> leaderboard, T data, UUID key, Comparable<V> value) {
            super(leaderboard);
            this.data = data;
            this.key = key;
            this.value = value;
        }

        public T data() {
            return this.data;
        }

        public UUID key() {
            return this.key;
        }

        public Comparable<V> value() {
            return this.value;
        }
    }

    public abstract static class Recalculate<T extends Data, V extends Comparable<V>> extends LeaderboardEvent<T, V> {

        private Recalculate(Leaderboard<T, V> leaderboard) {
            super(leaderboard);
        }

        public static class PreAll<T extends Data, V extends Comparable<V>> extends Recalculate<T, V> implements ICancellableEvent {

            public PreAll(Leaderboard<T, V> leaderboard) {
                super(leaderboard);
            }
        }

        public static class PostAll<T extends Data, V extends Comparable<V>> extends Recalculate<T, V> {

            public PostAll(Leaderboard<T, V> leaderboard) {
                super(leaderboard);
            }
        }

        public static class Pre<T extends Data, V extends Comparable<V>> extends Recalculate<T, V> implements ICancellableEvent {

            public Pre(Leaderboard<T, V> leaderboard) {
                super(leaderboard);
            }
        }

        public static class Post<T extends Data, V extends Comparable<V>> extends Recalculate<T, V> {

            public Post(Leaderboard<T, V> leaderboard) {
                super(leaderboard);
            }
        }
    }
}
