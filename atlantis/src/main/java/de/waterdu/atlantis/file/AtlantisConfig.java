package de.waterdu.atlantis.file;

import com.google.common.collect.Lists;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.AtlantisLogger;
import de.waterdu.atlantis.Settings;
import de.waterdu.atlantis.file.auto.LoadOrder;
import de.waterdu.atlantis.file.datatypes.Configuration;
import de.waterdu.atlantis.file.datatypes.Data;
import de.waterdu.atlantis.file.datatypes.NamedData;
import de.waterdu.atlantis.file.datatypes.PlayerData;
import de.waterdu.atlantis.file.storage.Storage;
import de.waterdu.atlantis.file.typeadapters.InterfaceTypeAdapter;
import de.waterdu.atlantis.file.typeadapters.NumberListTypeAdapter;
import de.waterdu.atlantis.file.typeadapters.ResourceLocationTypeAdapter;
import de.waterdu.atlantis.mod.pixelmon.AtlantisPixelmon;
import de.waterdu.atlantis.permission.AtlantisPermission;
import de.waterdu.atlantis.self.event.ConfigEvent;
import de.waterdu.atlantis.ui.api.UIDef;
import de.waterdu.atlantis.util.config.Configurable;
import de.waterdu.atlantis.util.config.DatabaseSettings;
import de.waterdu.atlantis.util.config.Leaderboard;
import de.waterdu.atlantis.util.config.RedisSettings;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.java.DateTimePattern;
import de.waterdu.atlantis.util.java.DecimalPattern;
import de.waterdu.atlantis.util.java.Timezone;
import de.waterdu.atlantis.util.java.math.Formula;
import de.waterdu.atlantis.util.level.ConfigurableLevel;
import de.waterdu.atlantis.util.level.LevelPosition;
import de.waterdu.atlantis.util.level.LocalPosition;
import de.waterdu.atlantis.util.server.Command;
import de.waterdu.atlantis.util.text.Text;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AtlantisConfig {

    private static final AtomicBoolean INIT = new AtomicBoolean(false);
    private static final Map<String, AtlantisConfig> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> CLASS_TO_MOD = new ConcurrentHashMap<>();
    private static final List<Config<? extends PlayerData>> PLAYER_CONFIGS = new ArrayList<>();

    private final Lock lock = new ReentrantLock(true);
    private final Lock readyLock = new ReentrantLock(true);

    private final List<Runnable> scheduledTasks = new ArrayList<>();

    private boolean gsonBuilt = false;
    private final GsonBuilder gsonBuilder = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setLenient()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Configuration.class, new InterfaceTypeAdapter<Configuration>())
            .registerTypeAdapter(Data.class, new InterfaceTypeAdapter<Data>())
            .registerTypeAdapter(NamedData.class, new InterfaceTypeAdapter<NamedData>())
            .registerTypeAdapter(PlayerData.class, new InterfaceTypeAdapter<PlayerData>())
            .registerTypeAdapter(UIDef.Data.class, new UIDef.Data.TypeAdapter())
            .registerTypeAdapter(UIDef.class, new UIDef.TypeAdapter())
            .registerTypeAdapter(Text.class, new Text.TypeAdapter())
            .registerTypeAdapter(Text.Immutable.class, new Text.Immutable.TypeAdapter())
            .registerTypeAdapter(ConfigurableItemStack.class, new ConfigurableItemStack.TypeAdapter())
            .registerTypeAdapter(ConfigurableLevel.class, new ConfigurableLevel.TypeAdapter())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
            .registerTypeAdapter(LocalPosition.class, new LocalPosition.TypeAdapter())
            .registerTypeAdapter(LevelPosition.class, new LevelPosition.TypeAdapter())
            .registerTypeAdapter(Leaderboard.class, new Leaderboard.LeaderboardTypeAdapter())
            .registerTypeAdapter(DatabaseSettings.class, new DatabaseSettings.TypeAdapter())
            .registerTypeAdapter(DatabaseSettings.URL.class, new DatabaseSettings.TypeAdapter())
            .registerTypeAdapter(DatabaseSettings.Explicit.class, new DatabaseSettings.TypeAdapter())
            .registerTypeAdapter(RedisSettings.class, new RedisSettings.TypeAdapter())
            .registerTypeAdapter(RedisSettings.URL.class, new RedisSettings.TypeAdapter())
            .registerTypeAdapter(RedisSettings.Explicit.class, new RedisSettings.TypeAdapter())
            .registerTypeAdapter(Formula.class, new Formula.TypeAdapter())
            .registerTypeAdapter(NumberListTypeAdapter.INTEGER.getType(), NumberListTypeAdapter.INTEGER)
            .registerTypeAdapter(NumberListTypeAdapter.LONG.getType(), NumberListTypeAdapter.LONG)
            .registerTypeAdapter(NumberListTypeAdapter.SHORT.getType(), NumberListTypeAdapter.SHORT)
            .registerTypeAdapter(NumberListTypeAdapter.BYTE.getType(), NumberListTypeAdapter.BYTE)
            .registerTypeAdapter(NumberListTypeAdapter.BIG_INTEGER.getType(), NumberListTypeAdapter.BIG_INTEGER)
            .registerTypeAdapter(NumberListTypeAdapter.ATOMIC_INTEGER.getType(), NumberListTypeAdapter.ATOMIC_INTEGER)
            .registerTypeAdapter(NumberListTypeAdapter.ATOMIC_LONG.getType(), NumberListTypeAdapter.ATOMIC_LONG)
            .registerTypeAdapter(PlayerReference.class, new PlayerReference.TypeAdapter())
            .registerTypeAdapter(Timezone.class, new Timezone.TypeAdapter())
            .registerTypeAdapter(DateTimePattern.class, new DateTimePattern.TypeAdapter())
            .registerTypeAdapter(DecimalPattern.class, new DecimalPattern.TypeAdapter())
            .registerTypeAdapter(Message.class, new Message.TypeAdapter())
            .registerTypeAdapter(Command.class, new Command.TypeAdapter())
            .registerTypeAdapter(AtlantisPermission.class, new AtlantisPermission.TypeAdapter())
            ;

    private Gson gson = null;

    AtomicBoolean ready = new AtomicBoolean(false);

    final Map<String, Config<? extends Configuration>> configs = new ConcurrentHashMap<>();
    private File dir;

    private AtlantisConfig() {
        AtlantisPixelmon.registerTypeAdapters(this.gsonBuilder);
        Configurable.registerDefaultContexts(this.gsonBuilder);
    }

    private Optional<GsonBuilder> gsonBuilder() {
        if (this.gsonBuilt) {
            return Optional.empty();
        } else {
            return Optional.of(this.gsonBuilder);
        }
    }

    public static void registerTypeAdapters(String mod, Consumer<GsonBuilder> consumer) {
        AtlantisConfig.of(mod).registerTypeAdapters(consumer);
    }

    public void registerTypeAdapters(Consumer<GsonBuilder> consumer) {
        this.gsonBuilder().ifPresent(consumer);
    }

    public void buildGSON() {
        if (!this.gsonBuilt) {
            this.gsonBuilt = true;
            this.gson = this.gsonBuilder.create();
        }
    }

    public Gson gson() {
        this.buildGSON();
        return this.gson;
    }

    @SuppressWarnings("all")
    @ApiStatus.Internal
    static void preRegistration(String mod) {
        INSTANCES.put(mod, preRegistration0(mod));
    }

    @SuppressWarnings("all")
    @ApiStatus.Internal
    static AtlantisConfig preRegistration0(String mod) {
        var root = new File(".");
        var all = new File(root, "config");
        if (all.mkdir()) {
            AtlantisLogger.info("Created config directory");
        }
        var config = new File(all, mod);
        var instance = new AtlantisConfig();
        instance.dir = config;
        instance.dir.mkdir();
        return instance;
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    static <T extends Configuration> T getDuringRegistration(String mod, Class<? extends T> clazz) {
        var config = AtlantisConfig.of(mod).configs.get(clazz.getCanonicalName());
        try {
            var result = config.read(true).get();
            if (!result) {
                config.write();
            }
            config.remapLang();
            return (T) config.get();
        } catch (ExecutionException | InterruptedException e) {
            AtlantisLogger.error("Failed immediate read of {}: {}", config.name(), e.getMessage());
            return null;
        }
    }

    @ApiStatus.Internal
    static void postRegistration(String mod) {
        var instance = AtlantisConfig.of(mod);
        List<Config<?>> configs = Lists.newArrayList(instance.configs.values());
        configs.sort(Comparator.comparingInt(config -> config.order().ordinal()));
        for (final var config : configs) {
            if (!config.isPlayerData() || config.shouldForceLoad()) {
                config.read(true).whenComplete((r, err) -> config.runTask(() -> config.write()));
            }
        }
        if (instance.readyLock.tryLock()) {
            instance.ready.set(true);
            if (Atlantis.ID.equalsIgnoreCase(mod)) {
                AtlantisConfig.INIT.set(true);
            }
            instance.readyLock.unlock();
        }
    }

    @ApiStatus.Internal
    public static void markUnready(String mod) {
        var instance = AtlantisConfig.of(mod);
        if (instance.readyLock.tryLock()) {
            instance.ready.set(false);
            if (Atlantis.ID.equalsIgnoreCase(mod)) {
                AtlantisConfig.INIT.set(false);
            }
            AtlantisConfigFinder.restoreFromBackup();
            instance.readyLock.unlock();
        }
    }

    public boolean ready() {
        return this.ready.get();
    }

    static AtlantisConfig of(String mod) {
        return AtlantisConfig.INSTANCES.computeIfAbsent(mod, key -> AtlantisConfig.preRegistration0(mod));
    }

    public static Collection<AtlantisConfig> allInstances() {
        return AtlantisConfig.INSTANCES.values();
    }

    public static File dirOf(String mod) {
        return AtlantisConfig.of(mod).dir;
    }
    
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    static <T extends Configuration> void register(String mod, String path, Class<? extends Configuration> clazz, Configuration instance,
                                                   Storage storage, boolean forceLoad, boolean lang, boolean aon, LoadOrder order) {
        try {
            var config = AtlantisConfig.of(mod);
            var specific = new Config<>(config, mod, clazz.getCanonicalName(), config.gson(), (T) instance, (Class<T>) clazz, storage, forceLoad, lang, aon, order);
            var event = new ConfigEvent.Register(specific, path);
            Atlantis.EVENT_BUS.post(event);
            specific.init(event.path());
            config.configs.put(clazz.getCanonicalName(), specific);
            AtlantisConfig.CLASS_TO_MOD.put(clazz, mod);
            if (specific.isPlayerData()) {
                AtlantisConfig.PLAYER_CONFIGS.add((Config<? extends PlayerData>) specific);
            }
        } catch (Exception e) {
            AtlantisLogger.error("Failed to instantiate new {}:{} instance: {}", mod, clazz.getSimpleName(), e.getMessage());
        }
    }

    @ApiStatus.Internal
    static <T extends Configuration> T get(String mod, Class<? extends T> clazz) {
        var instance = AtlantisConfig.of(mod);
        if (!instance.ready.get()) {
            return AtlantisConfig.getDuringRegistration(mod, clazz);
        }
        var config = instance.<T>config(clazz.getCanonicalName());
        return config.get();
    }

    @ApiStatus.Internal
    static <T extends Configuration> T getOrElse(String mod, Class<? extends T> clazz, T orElse) {
        var instance = AtlantisConfig.of(mod);
        if (instance == null || !instance.ready.get()) {
            return orElse;
        }
        var result = instance.<T>config(clazz.getCanonicalName()).get();
        return result == null ? orElse : result;
    }

    @ApiStatus.Internal
    static <T extends Configuration> Collection<T> getAll(String mod, Class<? extends T> clazz) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).getAll();
    }

    @ApiStatus.Internal
    static void enqueue(final String mod, final Runnable task) {
        AtlantisConfig.of(mod).enqueue(task);
    }

    @ApiStatus.Internal
    static <T extends Configuration> void enqueue(String mod, Class<? extends T> clazz, Consumer<Collection<T>> task) {
        AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).runTask(task);
    }

    @ApiStatus.Internal
    static <T extends Configuration> void put(String mod, Class<? extends T> clazz, T instance) {
        AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).put(instance);
    }

    @ApiStatus.Internal
    static <T extends Configuration> Text format(String mod, Class<T> clazz, String key, Object... args) {
        var config = AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName());
        if (config.isLang()) {
            return config.format(key, args);
        }
        return Text.empty();
    }

    @ApiStatus.Internal
    static <T extends Configuration> CompletableFuture<T> getFor(String mod, Class<? extends T> clazz, UUID uuid) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).get(AtlantisConfig.copyUUID(uuid));
    }

    @ApiStatus.Internal
    static <T extends Configuration> CompletableFuture<T> getForOrPut(String mod, Class<? extends T> clazz, UUID uuid, Supplier<T> orElse) {
        var config = AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName());
        return config.get(AtlantisConfig.copyUUID(uuid)).thenApply((instance) -> {
            if (instance == null) {
                instance = orElse.get();
                config.put(instance);
                instance.save();
            }
            return instance;
        });
    }

    @ApiStatus.Internal
    static <T extends Configuration> CompletableFuture<T> remove(String mod, Class<? extends T> clazz, UUID uuid) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).remove(AtlantisConfig.copyUUID(uuid));
    }

    @ApiStatus.Internal
    static <T extends Configuration> CompletableFuture<T> remove(String mod, Class<? extends T> clazz, String name) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).remove(name);
    }

    @ApiStatus.Internal
    static <T extends Configuration> T getFor(String mod, Class<? extends T> clazz, String name) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).get(name);
    }

    @ApiStatus.Internal
    static <T extends Configuration> T getFor(String mod, Class<? extends T> clazz, Player player) {
        return AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).get(player);
    }

    @ApiStatus.Internal
    static <T extends Configuration> void load(String mod, Class<? extends T> clazz) {
        AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).read();
    }

    @ApiStatus.Internal
    static <T extends Configuration> void save(final String mod, final Class<? extends T> clazz) {
        Atlantis.THREAD_POOL.submit(() -> AtlantisConfig.of(mod).<T>config(clazz.getCanonicalName()).write());
    }

    @ApiStatus.Internal
    static <T extends Configuration> void save(final String mod, final T data) {
        AtlantisConfig.save(mod, data, false);
    }

    @ApiStatus.Internal
    static <T extends Configuration> void save(final String mod, final T data, final boolean immediate) {
        Runnable writeTask = () -> AtlantisConfig.of(mod).<T>config(data.getClass().getName()).write(data, immediate);
        if (data instanceof PlayerData) {
            writeTask.run();
        } else {
            Atlantis.THREAD_POOL.submit(writeTask);
        }
    }

    @ApiStatus.Internal
    static void loadAll(String mod, DataType type) {
        for (var config : AtlantisConfig.of(mod).configs.values()) {
            if (type.isValid(config)) {
                config.read();
            }
        }
    }

    @ApiStatus.Internal
    static void saveAll(final String mod, final DataType type) {
        Atlantis.THREAD_POOL.submit(() -> {
            for (var config : AtlantisConfig.of(mod).configs.values()) {
                if (type.isValid(config)) {
                    config.write();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> Config<T> config(String key) {
        return Objects.requireNonNull((Config<T>) this.configs.get(key), "Invalid configuration type " + key);
    }

    public void destruct() {
        for (var config : this.configs.values()) {
            config.destruct();
        }
    }

    static UUID copyUUID(UUID uuid) {
        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static String findOwnerMod(Class<? extends Configuration> clazz) {
        return AtlantisConfig.CLASS_TO_MOD.get(clazz);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        for (var config : AtlantisConfig.PLAYER_CONFIGS) {
            config.storage().onPlayerJoin(config, player);
        }
    }

    public static void onPlayerLeave(ServerPlayer player) {
        for (var config : AtlantisConfig.PLAYER_CONFIGS) {
            config.storage().onPlayerLeave(config, player);
        }
    }

    static void asyncSave() {
        for (var instance : AtlantisConfig.INSTANCES.values()) {
            instance.asyncSaveInner();
        }
    }

    public void asyncSaveInner() {
        for (var config : this.configs.values()) {
            config.asyncSave();
        }
    }

    void enqueue(final Runnable task) {
        if (this.ready.get() && this.lock.tryLock()) {
            this.lock.unlock();
            task.run();
        } else {
            if (this.scheduledTasks.isEmpty()) {
                var delay = Settings.get().getScheduledTaskDelay();
                this.scheduledTasks.add(task);
                Atlantis.THREAD_POOL.repeatUntilComplete(() -> {
                    if (this.ready.get() && this.lock.tryLock(delay, TimeUnit.MILLISECONDS)) {
                        this.lock.unlock();
                        for (var scheduledTask : this.scheduledTasks) {
                            scheduledTask.run();
                        }
                        this.scheduledTasks.clear();
                        return true;
                    } else {
                        return false;
                    }
                }, delay);
            } else {
                this.scheduledTasks.add(task);
            }
        }
    }
}
