package de.waterdu.atlantis.mixin.item;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.event.item.LootTableEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = LootTable.class)
public abstract class MixinLootTable {

    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onGetRandomItemsRaw(LootContext context, Consumer<ItemStack> consumer, CallbackInfo ci) {
        if(Atlantis.EVENT_BUS.post(new LootTableEvent.Pre(context, consumer)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void onGetRandomItems(LootContext context, CallbackInfoReturnable<List<ItemStack>> cir) {
        LootTableEvent.Post event = Atlantis.EVENT_BUS.post(new LootTableEvent.Post(context, cir.getReturnValue()));
        if(event.isCanceled()) {
            cir.setReturnValue(new ArrayList<>());
        } else {
            cir.setReturnValue(event.drops());
        }
    }
}