package de.waterdu.atlantis.event.item;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class LootTableEvent extends Event implements ICancellableEvent {

    private final LootContext lootContext;

    protected LootTableEvent(LootContext lootContext) {
        this.lootContext = lootContext;
    }

    public LootContext getLootContext() {
        return this.lootContext;
    }

    public Optional<ServerPlayer> parameterAsPlayer(LootContextParam<? extends Entity> parameter) {
        var e = this.lootContext.getParamOrNull(parameter);
        if (e instanceof ServerPlayer player) {
            return Optional.of(player);
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> parameter(LootContextParam<T> parameter) {
        return Optional.ofNullable(this.lootContext.getParamOrNull(parameter));
    }

    public static class Pre extends LootTableEvent implements ICancellableEvent {

        private final Consumer<ItemStack> itemConsumer;

        public Pre(LootContext lootContext, Consumer<ItemStack> itemConsumer) {
            super(lootContext);
            this.itemConsumer = itemConsumer;
        }

        public void addDrops(ItemStack... drops) {
            for (var drop : drops) {
                this.itemConsumer.accept(drop);
            }
        }

        public void overrideDrops(ItemStack... drops) {
            this.setCanceled(true);
            this.addDrops(drops);
        }
    }

    public static class Post extends LootTableEvent implements ICancellableEvent {

        private final List<ItemStack> drops;

        public Post(LootContext lootContext, List<ItemStack> drops) {
            super(lootContext);
            this.drops = drops;
        }

        public void clearDrops() {
            this.drops.clear();
        }

        public List<ItemStack> drops() {
            return this.drops;
        }

        public void addDrops(ItemStack... drops) {
            this.drops.addAll(Lists.newArrayList(drops));
        }

        public void overrideDrops(ItemStack... drops) {
            this.clearDrops();
            this.addDrops(drops);
        }
    }
}
