package de.waterdu.atlantis.mixin.item;

import de.waterdu.atlantis.ext.item.ItemExt;
import de.waterdu.atlantis.ext.item.ItemStackExt;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.item.ItemUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Mixin(value = ItemStack.class, remap = false)
public abstract class MixinItemStack implements ItemExt, ItemStackExt {

    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Override
    public ResourceKey<Item> key() {
        return this.self().getItem().builtInRegistryHolder().key();
    }

    @Override
    public ResourceLocation id() {
        return this.key().location();
    }

    @Override
    public ConfigurableItemStack configurable() {
        return ConfigurableItemStack.of(this.self());
    }

    @Override
    public <T> void setDisplayName(T name) {
        ItemUtils.setDisplayName(this.self(), name);
    }

    @Override
    public <T> void setDisplayNameWithPlaceholders(T name, Object... contexts) {
        ItemUtils.setDisplayNameWithPlaceholders(this.self(), name, contexts);
    }

    @Override
    public <T> void setDisplayNameWithPlaceholders(T name, Collection<Object> contexts) {
        ItemUtils.setDisplayNameWithPlaceholders(this.self(), name, contexts);
    }

    @Override
    public <T> T computeComponentIfAbsent(DataComponentType<T> component, Supplier<T> ifAbsent) {
        var self = this.self();
        var data = self.get(component);
        if (data == null) {
            data = ifAbsent.get();
            self.set(component, data);
        }
        return data;
    }

    @Override
    public <T> void componentScope(DataComponentType<T> component, UnaryOperator<T> scope) {
        this.componentScope(component, () -> null, scope);
    }

    @Override
    public <T> void componentScope(DataComponentType<T> component, Supplier<T> ifAbsent, UnaryOperator<T> scope) {
        var self = this.self();
        var data = self.get(component);
        if (data == null) {
            data = ifAbsent.get();
        }
        self.set(component, scope.apply(data));
    }

    @Override
    public void sanitizeComponents() {
        this.components.remove(null);
    }

    @Unique
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }
}
