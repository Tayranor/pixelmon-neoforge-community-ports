package de.waterdu.atlantis.mixin.registry;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.event.registry.RegistryInjectEvent;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(RegistryDataLoader.class)
public abstract class MixinRegistryDataLoader {

    @Unique
    private static final AtomicReference<RegistryOps.RegistryInfoLookup> atlantis$REGISTRY_INFO_LOOKUP = new AtomicReference<>();

    @ModifyVariable(
            method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
            at = @At(value = "STORE")
    )
    private static RegistryOps.RegistryInfoLookup retrieveRegistryInfoLookup(RegistryOps.RegistryInfoLookup value) {
        atlantis$REGISTRY_INFO_LOOKUP.set(value);
        return value;
    }

    @Inject(
            method = "lambda$load$6(Ljava/util/Map;Lnet/minecraft/resources/RegistryDataLoader$Loader;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;freeze()Lnet/minecraft/core/Registry;")
    )
    private static void beforeFreeze(Map<ResourceKey<?>, Exception> map, RegistryDataLoader.Loader<?> loader, CallbackInfo ci) {
        Atlantis.EVENT_BUS.post(new RegistryInjectEvent(loader.registry(), atlantis$REGISTRY_INFO_LOOKUP.get()));
    }
}
