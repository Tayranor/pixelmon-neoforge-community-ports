package de.waterdu.atlantis.ext.item;

import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import net.minecraft.core.component.DataComponentType;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface ItemStackExt {

    default <T> void setDisplayName(T name) {}

    default <T> void setDisplayNameWithPlaceholders(T name, Object... contexts) {}

    default <T> void setDisplayNameWithPlaceholders(T name, Collection<Object> contexts) {}

    default <T> T computeComponentIfAbsent(DataComponentType<T> component, Supplier<T> ifAbsent) { return null; }

    default <T> void componentScope(DataComponentType<T> component, UnaryOperator<T> scope) {}

    default <T> void componentScope(DataComponentType<T> component, Supplier<T> ifAbsent, UnaryOperator<T> scope) {}

    default void sanitizeComponents() {}

    default ConfigurableItemStack configurable() {
        return null;
    }
}
