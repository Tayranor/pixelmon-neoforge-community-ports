package de.waterdu.atlantis.ui.api;

import de.waterdu.atlantis.util.text.Text;
import de.waterdu.atlantis.util.text.TextUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Function;

public class PageOptions {

    private static final PageOptions DEFAULTS = new PageOptions();

    private static final ResourceLocation COOKING_POT_ID = ResourceLocation.parse("pixelmon:cooking_pot");
    private static final ResourceLocation INFUSER_ID = ResourceLocation.parse("pixelmon:infuser");
    private static final ResourceLocation WASHING_MACHINE_ID = ResourceLocation.parse("pixelmon:washing_machine");
    private static final ResourceLocation POKE_BAG_ID = ResourceLocation.parse("pixelmon:poke_bag");
    private static final ResourceLocation DAY_CARE_ID = ResourceLocation.parse("pixelmon:day_care");

    private FormFactor formFactor = FormFactor.CHEST;
    private Text title = Text.empty();
    private int rows = 3;
    private boolean draggable = false;
    private TextAlignment titleAlignment = TextAlignment.LEFT;
    private long refreshRate = 1;
    private boolean inventoryHidden = false;

    private transient int hashCode = -1;

    public FormFactor getFormFactor() {
        return this.formFactor;
    }

    public int getRows() {
        return this.rows;
    }

    public Text getTitle() {
        return this.title;
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public TextAlignment getTitleAlignment() {
        return this.titleAlignment;
    }

    public long getRefreshRate() {
        return this.refreshRate;
    }

    public boolean isInventoryHidden() {
        return this.inventoryHidden && !this.draggable;
    }

    public static PageOptions defaults() {
        return PageOptions.DEFAULTS;
    }

    @Override
    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = Long.hashCode(Long.parseLong(
                    this.rows + "" +
                            this.title.get().length() +
                            this.formFactor.slots +
                            (this.draggable ? "1" : "0") +
                            this.titleAlignment.ordinal() +
                            this.refreshRate +
                            (this.inventoryHidden ? "1" : "0")
            ) + this.title.hashCode());
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PageOptions)) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

    public static PageOptions.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final PageOptions options;

        public Builder() {
            this.options = new PageOptions();
        }

        @Deprecated
        public PageOptions.Builder setFormFactor(FormFactor formFactor) {
            return this.formFactor(formFactor);
        }

        public PageOptions.Builder formFactor(FormFactor formFactor) {
            this.options.formFactor = formFactor;
            return this;
        }

        @Deprecated
        public PageOptions.Builder setRows(int rows) {
            return this.rows(rows);
        }

        public PageOptions.Builder rows(int rows) {
            this.options.rows = rows;
            return this;
        }

        @Deprecated
        public <T> PageOptions.Builder setTitle(T title) {
            return this.title(title);
        }

        public <T> PageOptions.Builder title(T title) {
            this.options.title = TextUtils.asText(title);
            return this;
        }

        @Deprecated
        public PageOptions.Builder setDraggable(boolean draggable) {
            return this.draggable(draggable);
        }

        public PageOptions.Builder draggable(boolean draggable) {
            this.options.draggable = draggable;
            return this;
        }

        @Deprecated
        public PageOptions.Builder setTitleAlignment(TextAlignment titleAlignment) {
            return this.titleAlignment(titleAlignment);
        }

        public PageOptions.Builder titleAlignment(TextAlignment alignment) {
            this.options.titleAlignment = alignment;
            return this;
        }

        @Deprecated
        public PageOptions.Builder setRefreshRate(long refreshRate) {
            return this.refreshRate(refreshRate);
        }

        public PageOptions.Builder refreshRate(long rate) {
            this.options.refreshRate = rate;
            return this;
        }

        @Deprecated
        public PageOptions.Builder setInventoryHidden(boolean inventoryHidden) {
            return this.inventoryHidden(inventoryHidden);
        }

        public PageOptions.Builder inventoryHidden(boolean hidden) {
            this.options.inventoryHidden = hidden;
            return this;
        }

        public PageOptions.Builder hideInventory() {
            this.options.inventoryHidden = true;
            return this;
        }

        public PageOptions.Builder showInventory() {
            this.options.inventoryHidden = false;
            return this;
        }

        public PageOptions build() {
            return this.options;
        }
    }

    public static class FormFactor {

        public static FormFactor CHEST;
        public static FormFactor FURNACE;
        public static FormFactor ENCHANTING_TABLE;
        public static FormFactor VILLAGER_TRADE;
        public static FormFactor ANVIL;
        public static FormFactor CRAFTING_TABLE;
        public static FormFactor DISPENSER;
        public static FormFactor BREWING_STAND;
        public static FormFactor HOPPER;
        public static FormFactor BEACON;
        public static FormFactor BLAST_FURNACE;
        public static FormFactor GRINDSTONE;
        public static FormFactor LECTERN;
        public static FormFactor LOOM;
        public static FormFactor SHULKER_BOX;
        public static FormFactor SMITHING_TABLE;
        public static FormFactor SMOKER;
        public static FormFactor CARTOGRAPHY_TABLE;
        public static FormFactor STONECUTTER;

        public static FormFactor COOKING_POT;
        public static FormFactor INFUSER;
        public static FormFactor WASHING_MACHINE;
        public static FormFactor POKE_BAG;
        public static FormFactor DAY_CARE;

        private final int slots;
        private final boolean hasRows;
        private final Function<PageOptions, MenuType<? extends AbstractContainerMenu>> menuTypeFunction;

        FormFactor(int slots, boolean hasRows, Function<PageOptions, MenuType<? extends AbstractContainerMenu>> menuTypeFunction) {
            this.slots = slots;
            this.hasRows = hasRows;
            this.menuTypeFunction = menuTypeFunction;
        }

        public static FormFactor create(int slots, boolean hasRows, Function<PageOptions, MenuType<? extends AbstractContainerMenu>> menuTypeFunction) {
            return new FormFactor(slots, hasRows, menuTypeFunction);
        }

        public static void createDefaults(HolderLookup.Provider registryAccess) {
            FormFactor.CHEST = FormFactor.create(54, true, options -> switch (options.rows) {
                case 1 -> MenuType.GENERIC_9x1;
                case 2 -> MenuType.GENERIC_9x2;
                case 4 -> MenuType.GENERIC_9x4;
                case 5 -> MenuType.GENERIC_9x5;
                case 6 -> MenuType.GENERIC_9x6;
                default -> MenuType.GENERIC_9x3;
            });
            FormFactor.FURNACE = FormFactor.create(3, false, options -> MenuType.FURNACE);
            FormFactor.ENCHANTING_TABLE = FormFactor.create(2, false, options -> MenuType.ENCHANTMENT);
            FormFactor.VILLAGER_TRADE = FormFactor.create(3, false, options -> MenuType.MERCHANT);
            FormFactor.ANVIL = FormFactor.create(3, false, options -> MenuType.ANVIL);
            FormFactor.CRAFTING_TABLE = FormFactor.create(10, false, options -> MenuType.CRAFTING);
            FormFactor.DISPENSER = FormFactor.create(9, false, options -> MenuType.GENERIC_3x3);
            FormFactor.BREWING_STAND = FormFactor.create(5, false, options -> MenuType.BREWING_STAND);
            FormFactor.HOPPER = FormFactor.create(5, false, options -> MenuType.HOPPER);
            FormFactor.BEACON = FormFactor.create(1, false, options -> MenuType.BEACON);
            FormFactor.BLAST_FURNACE = FormFactor.create(3, false, options -> MenuType.BLAST_FURNACE);
            FormFactor.GRINDSTONE = FormFactor.create(3, false, options -> MenuType.GRINDSTONE);
            FormFactor.LECTERN = FormFactor.create(0, false, options -> MenuType.LECTERN);
            FormFactor.LOOM = FormFactor.create(4, false, options -> MenuType.LOOM);
            FormFactor.SHULKER_BOX = FormFactor.create(27, false, options -> MenuType.SHULKER_BOX);
            FormFactor.SMITHING_TABLE = FormFactor.create(3, false, options -> MenuType.SMITHING);
            FormFactor.SMOKER = FormFactor.create(3, false, options -> MenuType.SMOKER);
            FormFactor.CARTOGRAPHY_TABLE = FormFactor.create(3, false, options -> MenuType.CARTOGRAPHY_TABLE);
            FormFactor.STONECUTTER = FormFactor.create(2, false, options -> MenuType.STONECUTTER);
            FormFactor.COOKING_POT = FormFactor.create(11, false, options -> net.minecraft.core.registries.BuiltInRegistries.MENU.get(COOKING_POT_ID));
            FormFactor.INFUSER = FormFactor.create(4, false, options -> net.minecraft.core.registries.BuiltInRegistries.MENU.get(INFUSER_ID));
            FormFactor.WASHING_MACHINE = FormFactor.create(2, false, options -> net.minecraft.core.registries.BuiltInRegistries.MENU.get(WASHING_MACHINE_ID));
            FormFactor.POKE_BAG = FormFactor.create(27, false, options -> net.minecraft.core.registries.BuiltInRegistries.MENU.get(POKE_BAG_ID));
            FormFactor.DAY_CARE = FormFactor.create(0, false, options -> net.minecraft.core.registries.BuiltInRegistries.MENU.get(DAY_CARE_ID));
        }

        public MenuType<? extends AbstractContainerMenu> getType(PageOptions options) {
            return this.menuTypeFunction.apply(options);
        }

        public boolean hasRows() {
            return this.hasRows;
        }

        public int getSlots() {
            return this.slots;
        }
    }

    public enum TextAlignment {

        LEFT,
        CENTER,
        RIGHT
    }
}
