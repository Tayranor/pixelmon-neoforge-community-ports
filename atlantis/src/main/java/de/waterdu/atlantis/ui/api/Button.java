package de.waterdu.atlantis.ui.api;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import de.waterdu.atlantis.Settings;
import de.waterdu.atlantis.ui.internal.Buttons;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.item.ItemUtils;
import de.waterdu.atlantis.util.java.interfaces.TriConsumer;
import de.waterdu.atlantis.util.text.Text;
import de.waterdu.atlantis.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Class which make up what is shown on a {@link Page}.
 * These can be created on the fly when a page is shown to a player, or can be created and held in advance.
 *
 * @author      Sam Plummer sam@waterdu.de
 * @version     1.0
 * @since       02/03/2019
 */
public class Button {

    /**
     * UUID to identify this button with.
     */
    private final UUID uuid = UUID.randomUUID();

    /**
     * Name of this button. Shown as the first line of the button.
     */
    private Text name;

    /**
     * Name of this button, as an {@link Component} directly, to allow for use of translations, etc.
     */
    private Component directName = null;

    /**
     * Lore of this button. Shown as subsequent lines of the button.
     */
    private List<Text> lore;

    /**
     * Lore of this button, as {@link Component}s directly, to allow for use of translations, etc.
     */
    private List<Component> directLore = null;

    /**
     * Tooltip of this button.
     */
    private String tooltip = "";

    /**
     * Item which this button is displayed as.
     */
    private ItemStack item;

    /**
     * Whether to hide tooltips on the button or not.
     */
    private boolean hideTooltips;

    /**
     * Whether to display this button as the stack provided fully, ignoring name and lore.
     */
    private boolean stackOverride;

    /**
     * Whether to apply HasEffect to the button.
     */
    private boolean hasEffect;

    /**
     * Index at which this button will be shown on the {@link Page}.
     */
    private int index;

    /**
     * Group this button is for.
     */
    private int group = 0;

    /**
     * Page this button is for.
     * -1 equals all pages.
     */
    private int page = -1;

    /**
     * Number of repeat presses allowed in this current page session.
     * Integer.MIN_VALUE equals unlimited.
     */
    private int presses = Integer.MIN_VALUE;

    /**
     * Task to run when this button is clicked.
     */
    private Either<Consumer<ClickData>, BiConsumer<Page, ClickData>> clickAction = null;

    /**
     * Whether the task is run async or sync
     */
    private boolean async = true;

    /**
     * Whether to update this on the next tick.
     */
    private boolean changed = true;

    /**
     * Whether this represents nothing or not.
     */
    private boolean empty = false;

    /**
     * Whether this button has a low priority.
     */
    private boolean lowPriority = false;

    /**
     * Button update callbacks.
     */
    private final List<ButtonUpdateTask> updateTasks = Lists.newCopyOnWriteArrayList();

    /**
     * Contexts to pass to placeholders.
     */
    private final List<Object> placeholderContexts = Lists.newCopyOnWriteArrayList();

    /**
     * Constructs a new button.
     *
     * @param   name    {@link Button#name}
     * @param   lore    {@link Button#lore}
     * @param   item    {@link Button#item}
     * @param   index   {@link Button#index}
     */
    protected Button(Text name, List<Text> lore, ItemStack item, int index) {
        this(name, lore, item, index, true, false);
    }

    /**
     * Constructs a new button with legacy options.
     *
     * @param   name    {@link Button#name}
     * @param   lore    {@link Button#lore}
     * @param   item    {@link Button#item}
     * @param   index   {@link Button#index}
     */
    protected Button(String name, String lore, ItemStack item, int index) {
        this.name = Text.of(name);
        this.lore = new ArrayList<>();
        if (lore != null) {
            for (String line : lore.split("\\n")) {
                this.lore.add(Text.of(line));
            }
        }
        this.item = item;
        this.index = index;
        this.stackOverride = false;
        this.hideTooltips = true;
        this.hasEffect = false;
    }

    /**
     * Constructs a new button.
     *
     * @param   name        {@link Button#name}
     * @param   lore        {@link Button#lore}
     * @param   item        {@link Button#item}
     * @param   index       {@link Button#index}
     * @param   hideTooltips       {@link Button#hideTooltips}
     * @param   hasEffect   {@link Button#hasEffect}
     */
    protected Button(Text name, List<Text> lore, ItemStack item, int index, boolean hideTooltips, boolean hasEffect) {
        this.name = name;
        this.lore = lore;
        this.item = item;
        this.index = index;
        this.stackOverride = false;
        this.hideTooltips = hideTooltips;
        this.hasEffect = hasEffect;
    }

    /**
     * Constructs a button with stack override.
     *
     * @param   item    {@link Button#item}
     * @param   index   {@link Button#index}
     */
    protected Button(ItemStack item, int index) {
        this.name = Text.empty();
        this.lore = new ArrayList<>();
        this.item = item;
        this.index = index;
        this.stackOverride = true;
        this.hideTooltips = false;
        this.hasEffect = false;
    }

    protected Button() {
        this.name = Text.empty();
        this.lore = new ArrayList<>();
        this.index = 0;
        this.stackOverride = false;
        this.hideTooltips = false;
        this.hasEffect = false;
        this.empty = true;
    }

    /**
     * Creates the ItemStack which is then what is actually displayed in the UI as this button.
     *
     * @return Display of this button.
     * @see ItemStack
     */
    public ItemStack display(PlayerReference player) {
        if (this.item == null) {
            return ItemStack.EMPTY;
        }

        if (this.stackOverride) {
            return this.item;
        }

        var placeholders = Settings.get().isUsePlaceholdersInPages();

        if (this.directName != null) {
            ItemUtils.setDisplayName(this.item, this.directName);
        } else {
            if (placeholders) {
                if (!this.placeholderContexts.contains(player)) {
                    this.placeholderContexts.add(player);
                }
                ItemUtils.setDisplayNameWithPlaceholders(this.item, this.name, this.placeholderContexts);
            } else {
                ItemUtils.setDisplayName(this.item, this.name);
            }
        }

        if (this.directLore != null) {
            if (!this.directLore.isEmpty()) {
                ItemUtils.setDisplayLore(this.item, this.directLore);
            }
        } else {
            if (!this.lore.isEmpty()) {
                if (placeholders) {
                    ItemUtils.setDisplayLoreWithPlaceholders(this.item, this.placeholderContexts, this.lore);
                } else {
                    ItemUtils.setDisplayLore(this.item, this.lore);
                }
            }
        }

        ItemUtils.setPixelmonTooltip(this.item, this.tooltip);

        if (this.hasEffect) {
            ItemUtils.applyGlow(this.item);
        }

        if (this.hideTooltips) {
            ItemUtils.hideTooltips(this.item);
        }


        return this.item;
    }

    public int index() {
        return this.index;
    }

    public Button markDirty() {
        this.changed = true;
        return this;
    }

    public Button setIndex(int index) {
        this.index = index;
        return this.markDirty();
    }

    public Button setGroup(int group) {
        this.group = Math.max(0, group);
        return this;
    }

    public Button setPage(int page) {
        this.page = page;
        return this.markDirty();
    }

    public boolean isVisible(Buttons buttons) {
        return this.isVisible(buttons.group(this.group));
    }

    public boolean isVisible(Buttons.ButtonPageGroup group) {
        return this.page == -1 || (group != null && this.page == group.currentPageID());
    }

    public Button setPresses(int presses) {
        this.presses = presses;
        return this.markDirty();
    }

    public <T> Button setName(T name) {
        this.name = TextUtils.asText(name);
        return this.markDirty();
    }

    public Button setDirectName(Component name) {
        this.directName = name;
        return this.markDirty();
    }

    public Button removeDirectName() {
        this.directName = null;
        return this.markDirty();
    }

    public Button setHideTooltips(boolean hideTooltips) {
        this.hideTooltips = hideTooltips;
        return this.markDirty();
    }

    public Button clearLore() {
        this.lore.clear();
        return this.markDirty();
    }

    public <T> Button setLore(T lore) {
        this.clearLore();
        if (lore != null) {
            for (String line : TextUtils.asString(lore).split("\\n")) {
                this.lore.add(Text.of(line));
            }
        }
        return this.markDirty();
    }

    public <T> Button setLore(List<T> lore) {
        this.clearLore();
        if (lore != null) {
            for (T line : lore) {
                this.lore.add(TextUtils.asText(line));
            }
        }
        return this.markDirty();
    }

    @SafeVarargs
    public final <T> Button addLore(T... lore) {
        if (lore != null) {
            for (T line : lore) {
                this.lore.add(TextUtils.asText(line));
            }
        }
        return this.markDirty();
    }

    public <T> Button addLoreLine(T line) {
        this.lore.add(TextUtils.asText(line));
        return this.markDirty();
    }

    public <T> Button addLoreLineRaw(T line) {
        this.lore.add(TextUtils.asText(line).raw());
        return this.markDirty();
    }

    private void setupDirectLore() {
        if (this.directLore == null) {
            this.directLore = new ArrayList<>();
        }
    }

    public Button removeDirectLore() {
        this.directLore = null;
        return this.markDirty();
    }

    public Button clearDirectLore() {
        this.setupDirectLore();
        this.directLore.clear();
        return this.markDirty();
    }

    public Button setDirectLore(Component lore) {
        this.clearDirectLore();
        this.directLore.add(lore);
        return this.markDirty();
    }

    public Button setDirectLore(List<Component> lore) {
        this.clearDirectLore();
        if (lore != null) {
            this.directLore.addAll(lore);
        }
        return this.markDirty();
    }

    public Button addDirectLore(Component... lore) {
        this.setupDirectLore();
        if (lore != null) {
            this.directLore.addAll(Arrays.asList(lore));
        }
        return this.markDirty();
    }

    public Button addDirectLoreLine(Component line) {
        this.setupDirectLore();
        this.directLore.add(line);
        return this.markDirty();
    }

    public Button setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this.markDirty();
    }

    public Button setHasEffect(boolean hasEffect) {
        this.hasEffect = hasEffect;
        return this.markDirty();
    }

    public Button setItem(ItemStack item) {
        this.item = item;
        return this.markDirty();
    }

    public Button setItem(Item item) {
        return this.setItem(item, 1);
    }

    public Button setItem(Item item, int count) {
        return this.setItem(new ItemStack(item, count));
    }

    public Button setItem(Block block) {
        return this.setItem(Item.BY_BLOCK.get(block));
    }

    public Button setItem(Block block, int count) {
        return this.setItem(Item.BY_BLOCK.get(block), count);
    }

    public Button setItem(String item) {
        return this.setItem(ConfigurableItemStack.of(item));
    }

    public Button setItem(String item, int count) {
        return this.setItem(ConfigurableItemStack.of(item), count);
    }

    public Button setItem(ConfigurableItemStack item) {
        return this.setItem(item.newStack());
    }

    public Button setItem(ConfigurableItemStack item, int count) {
        return this.setItem(item.newStack(count));
    }

    public Button setStackOverride(boolean stackOverride) {
        this.stackOverride = stackOverride;
        return this.markDirty();
    }

    public Button setClickAction(Consumer<ClickData> clickAction) {
        this.clickAction = Either.left(clickAction);
        return this.markDirty();
    }

    public Button setClickAction(BiConsumer<Page, ClickData> clickAction) {
        this.clickAction = Either.right(clickAction);
        return this.markDirty();
    }

    public Button sync() {
        this.async = false;
        return this;
    }

    public Button async() {
        this.async = true;
        return this;
    }

    public Button clearClickAction() {
        this.clickAction = null;
        return this.markDirty();
    }

    public Button markClean() {
        this.changed = false;
        return this;
    }

    public Button setLowPriority() {
        this.lowPriority = true;
        return this;
    }

    public Button setStandardPriority() {
        this.lowPriority = false;
        return this;
    }

    public Text name() {
        return this.name;
    }

    public Component directName() {
        return this.directName;
    }

    public boolean hasDirectName() {
        return this.directName != null;
    }

    public List<Text> lore() {
        return this.lore;
    }

    public List<Component> directLore() {
        return this.directLore;
    }

    public boolean hasDirectLore() {
        return this.directLore != null;
    }

    public String tooltip() {
        return this.tooltip;
    }

    public ItemStack item() {
        return this.item;
    }

    public boolean isStackOverride() {
        return this.stackOverride;
    }

    public boolean hasEffect() {
        return this.hasEffect;
    }

    public int group() {
        return this.group;
    }

    public int page() {
        return this.page;
    }

    public int presses() {
        return this.presses;
    }

    public boolean hideTooltips() {
        return this.hideTooltips;
    }

    public Button changePresses(int presses) {
        this.presses += presses;
        return this;
    }

    public Either<Consumer<ClickData>, BiConsumer<Page, ClickData>> getClickAction() {
        return this.clickAction;
    }

    public boolean isAsync() {
        return this.async;
    }

    public Button setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public boolean isChanged() {
        return this.changed;
    }

    public Button setChanged(boolean changed) {
        this.changed = changed;
        return this;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean isVisible() {
        return !this.isEmpty();
    }

    public Button setEmpty(boolean empty) {
        this.empty = empty;
        return this;
    }

    public boolean isLowPriority() {
        return this.lowPriority;
    }

    public Button setLowPriority(boolean lowPriority) {
        this.lowPriority = lowPriority;
        return this;
    }

    public List<ButtonUpdateTask> getUpdateTasks() {
        return this.updateTasks;
    }

    public Button addUpdateTask(ButtonUpdateTask updateTask) {
        this.updateTasks.add(updateTask);
        return this.markDirty();
    }

    public Button removeUpdateTask(ButtonUpdateTask updateTask) {
        this.updateTasks.remove(updateTask);
        return this.markDirty();
    }

    public Button clearUpdateTasks() {
        this.updateTasks.clear();
        return this.markDirty();
    }

    public Button copyUpdateTasks(Button button) {
        this.updateTasks.clear();
        this.updateTasks.addAll(button.updateTasks);
        return this.markDirty();
    }

    public Button addPlaceholderContext(Object context) {
        this.placeholderContexts.add(context);
        return this.markDirty();
    }

    public Button clearPlaceholderContexts() {
        this.placeholderContexts.clear();
        return this.markDirty();
    }

    public boolean isSamePlace(Button button) {
        return button != null && this.index() == button.index() && this.page() == button.page() && this.group() == button.group();
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Button)) {
            return false;
        }
        return this.uuid.equals(((Button) obj).uuid);
    }

    @Override
    public String toString() {
        return "Button(" + this.uuid + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder of(Object name, Object item, int index) {
        Button.Builder builder = Button.builder().name(name).index(index);
        if (item instanceof Item) {
            builder.item((Item) item);
        } else if (item instanceof ItemStack) {
            builder.item((ItemStack) item);
        } else if (item instanceof Block) {
            builder.item((Block) item);
        } else if (item instanceof ConfigurableItemStack) {
            builder.item((ConfigurableItemStack) item);
        } else {
            builder.item(String.valueOf(item));
        }
        return builder;
    }

    public static Builder of(Object name, Object lore, Object item, int index) {
        return Button.of(name, item, index).lore(lore);
    }

    public static class Builder {

        private final Button button;

        public Builder() {
            this.button = new Button();
            this.button.empty = false;
            this.button.hideTooltips = true;
        }

        public <T> Builder name(T name) {
            this.button.name = TextUtils.asText(name);
            return this;
        }

        public <T> Builder rawName(T name) {
            this.button.name = TextUtils.asText(name).raw();
            return this;
        }

        public Builder directName(Component name) {
            this.button.directName = name;
            return this;
        }
        
        public Builder removeDirectName() {
            this.button.directName = null;
            return this;
        }

        public Builder clearLore() {
            this.button.lore.clear();
            return this;
        }

        public <T> Builder lore(T lore) {
            this.clearLore();
            if (lore != null) {
                for (String line : TextUtils.asString(lore).split("\\n")) {
                    this.button.lore.add(Text.of(line));
                }
            }
            return this;
        }

        public <T> Builder lore(List<T> lore) {
            this.clearLore();
            if (lore != null) {
                for (T line : lore) {
                    this.button.lore.add(TextUtils.asText(line));
                }
            }
            return this;
        }

        @SafeVarargs
        public final <T> Builder lore(T... lore) {
            if (lore != null) {
                for (T line : lore) {
                    this.button.lore.add(TextUtils.asText(line));
                }
            }
            return this;
        }

        public <T> Builder loreLine(T line) {
            this.button.lore.add(TextUtils.asText(line));
            return this;
        }

        public Builder blankLoreLine() {
            return this.loreLine("");
        }

        public <T> Builder rawLoreLine(T line) {
            this.button.lore.add(TextUtils.asText(line).raw());
            return this;
        }

        public Builder clearDirectLore() {
            this.button.setupDirectLore();
            this.button.directLore.clear();
            return this;
        }

        public Builder directLore(Component lore) {
            this.clearDirectLore();
            if (lore != null) {
                this.button.directLore.add(lore);
            }
            return this;
        }

        public Builder directLore(List<Component> lore) {
            this.clearDirectLore();
            if (lore != null) {
                this.button.directLore.addAll(lore);
            }
            return this;
        }
        
        public Builder directLore(Component... lore) {
            return this.directLore(Arrays.asList(lore));
        }

        public Builder directLoreLine(Component line) {
            this.button.setupDirectLore();
            this.button.directLore.add(line);
            return this;
        }

        public Builder blankDirectLoreLine() {
            return this.directLoreLine(Component.literal(""));
        }
        
        public Builder removeDirectLore() {
            this.button.directLore = null;
            return this;
        }

        public Builder tooltip(String tooltip) {
            this.button.tooltip = tooltip;
            return this;
        }

        public Builder item(Item item) {
            return this.item(item, 1);
        }

        public Builder item(Item item, int count) {
            return this.item(new ItemStack(item, count));
        }

        public Builder item(Block block) {
            return this.item(Item.BY_BLOCK.get(block));
        }

        public Builder item(Block block, int count) {
            return this.item(Item.BY_BLOCK.get(block), count);
        }

        public Builder item(String item) {
            return this.item(ConfigurableItemStack.of(item));
        }

        public Builder item(String item, int count) {
            return this.item(ConfigurableItemStack.of(item), count);
        }

        public Builder item(ConfigurableItemStack item) {
            return this.item(item.newStack());
        }

        public Builder item(ConfigurableItemStack item, int count) {
            return this.item(item.newStack(count));
        }

        public Builder item(ItemStack stack) {
            this.button.item = stack;
            return this;
        }

        public Builder stackOverride() {
            return this.stackOverride(true);
        }

        public Builder stackOverride(boolean stackOverride) {
            this.button.stackOverride = stackOverride;
            return this;
        }

        public Builder effect() {
            return this.effect(true);
        }

        public Builder effect(boolean hasEffect) {
            this.button.hasEffect = hasEffect;
            return this;
        }

        public Builder index(int index) {
            this.button.index = index;
            return this;
        }

        public Builder group(int group) {
            this.button.group = group;
            return this;
        }

        public Builder page(int page) {
            this.button.page = page;
            return this;
        }

        public Builder presses(int presses) {
            this.button.presses = presses;
            return this;
        }

        public Builder singlePress() {
            return this.presses(1);
        }

        public Builder hideTooltips(boolean hideTooltips) {
            this.button.hideTooltips = hideTooltips;
            return this;
        }

        public Builder clickAction(Consumer<ClickData> clickAction) {
            this.button.clickAction = Either.left(clickAction);
            return this;
        }

        public Builder nextPage() {
            return this.clickAction(ClickData.NEXT_PAGE);
        }

        public Builder prevPage() {
            return this.clickAction(ClickData.PREV_PAGE);
        }

        public Builder closePage() {
            return this.clickAction(ClickData.CLOSE_PAGE);
        }

        public Builder clickAction(BiConsumer<Page, ClickData> clickAction) {
            this.button.clickAction = Either.right(clickAction);
            return this;
        }

        public Builder sync() {
            this.button.sync();
            return this;
        }

        public Builder async() {
            this.button.async();
            return this;
        }

        public Builder lowPriority() {
            this.button.setLowPriority();
            return this;
        }

        public Builder standardPriority() {
            this.button.setStandardPriority();
            return this;
        }

        public Builder updateTask(TriConsumer<Button, Long, ButtonUpdateTask> task) {
            return this.updateTask(1, task);
        }

        public Builder updateTask(int tickrate, TriConsumer<Button, Long, ButtonUpdateTask> task) {
            return this.updateTask(tickrate, 0, task);
        }

        public Builder updateTask(int tickrate, int delay, TriConsumer<Button, Long, ButtonUpdateTask> task) {
            this.button.addUpdateTask(new ButtonUpdateTask(tickrate, delay, task));
            return this;
        }

        public Builder placeholderContexts(Object... contexts) {
            this.button.placeholderContexts.addAll(Lists.newArrayList(contexts));
            return this;
        }

        public Builder doIf(boolean doIf, Consumer<Builder> task) {
            if (doIf) {
                task.accept(this);
            }
            return this;
        }

        public Builder doIf(BooleanSupplier doIf, Consumer<Builder> task) {
            return this.doIf(doIf.getAsBoolean(), task);
        }

        public Button build() {
            return this.button;
        }
    }

    public static Button emptyButton() {
        return new Button();
    }

    public static Button emptyButton(int index) {
        Button button = new Button();
        button.setIndex(index);
        return button;
    }

    public static Button emptyButton(int index, int page) {
        Button button = Button.emptyButton(index);
        button.setPage(page);
        return button;
    }

    public static Button emptyButton(int index, int page, int group) {
        Button button = Button.emptyButton(index);
        button.setPage(page);
        button.setGroup(group);
        return button;
    }
}