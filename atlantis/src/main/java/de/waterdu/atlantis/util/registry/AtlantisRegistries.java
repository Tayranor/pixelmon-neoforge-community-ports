package de.waterdu.atlantis.util.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class AtlantisRegistries {
    private AtlantisRegistries() {}

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> get(ResourceKey<? extends Registry<T>> key) {
        if (key.equals(Registries.ITEM)) return (Registry<T>) BuiltInRegistries.ITEM;
        if (key.equals(Registries.BLOCK)) return (Registry<T>) BuiltInRegistries.BLOCK;
        if (key.equals(Registries.BLOCK_ENTITY_TYPE)) return (Registry<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        if (key.equals(Registries.SOUND_EVENT)) return (Registry<T>) BuiltInRegistries.SOUND_EVENT;
        if (key.equals(Registries.MOB_EFFECT)) return (Registry<T>) BuiltInRegistries.MOB_EFFECT;
        if (key.equals(Registries.POTION)) return (Registry<T>) BuiltInRegistries.POTION;
        if (key.equals(Registries.PARTICLE_TYPE)) return (Registry<T>) BuiltInRegistries.PARTICLE_TYPE;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) throw new IllegalStateException("Registry unavailable before server starts: " + key.location());
        return server.registryAccess().registryOrThrow(key);
    }
}
