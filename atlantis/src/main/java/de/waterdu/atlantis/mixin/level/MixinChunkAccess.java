package de.waterdu.atlantis.mixin.level;

import de.waterdu.atlantis.Settings;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess {

    @Redirect(method = "getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;)V"))
    private void redirectUnprimedHeightmapError(Logger logger, String msg) {
        if (!Settings.get().isSuppressUnprimedHeightmapErrors()) {
            logger.error(msg);
        }
    }
}
