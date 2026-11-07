package de.waterdu.atlantis.ui.internal;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.Settings;
import de.waterdu.atlantis.file.AtlantisData;
import de.waterdu.atlantis.self.event.PageEvent;
import de.waterdu.atlantis.ui.api.AtlantisUI;
import de.waterdu.atlantis.ui.api.Button;
import de.waterdu.atlantis.ui.api.ButtonCollector;
import de.waterdu.atlantis.ui.api.Page;
import de.waterdu.atlantis.ui.api.PageOptions;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.placeholder.AtlantisPlaceholderProxy;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

public class AtlantisInventoryInternal extends SimpleContainer {

    protected Page page;
    protected Buttons buttons;

    protected final ButtonCollector collector = ButtonCollector.of(button ->
            this.buttons.setButton(button.index(), button.group(), button.page(), button)
    );

    protected PageOptions options;
    protected boolean draggable;
    protected boolean closed = false;

    protected PlayerReference reference;
    protected AtlantisContainerMenuInternal container;

    public AtlantisInventoryInternal(Page page, ServerPlayer player, int slots) {
        this(page, player, slots, null);
    }

    public AtlantisInventoryInternal(Page page, ServerPlayer player, int slots, Set<Button> buttons) {
        super(slots);
        this.page = page;
        this.reference = PlayerReference.get(player);
        this.options = page.getPageOptions(this.reference);
        this.buttons = new Buttons(this.options, slots, 1, player);
        if (buttons != null) {
            this.collector.collect(buttons);
        } else {
            page.addButtons(this.reference, this.collector);
            var buttonSet = new HashSet<Button>();
            page.addButtons(this.reference, buttonSet);
            if (!buttonSet.isEmpty()) {
                this.collector.collect(buttonSet);
            }
        }
        this.draggable = this.getOptions().isDraggable();
        var def = AtlantisData.getDecorations(page);
        if (def != null) {
            var event = new PageEvent.Decorate(this.reference, page, def, def.getButtons());
            if (!Atlantis.EVENT_BUS.post(event).isCanceled()) {
                this.collector.collect(event.decorations());
            }
        }
        this.collector.unlock();
    }

    public AtlantisInventoryInternal(Page page, ServerPlayer player, Buttons buttons, int slots) {
        super(slots);
        this.reference = PlayerReference.get(player);
        this.options = page.getPageOptions(this.reference);
        this.draggable = this.getOptions().isDraggable();
        this.buttons = buttons;
        this.page = page;
        this.collector.unlock();
    }

    public void setContainer(AtlantisContainerMenuInternal container) {
        this.container = container;
    }

    public PageOptions getOptions() {
        return this.options;
    }

    public int getRows() {
        return this.getOptions().getRows();
    }

    public void onUpdate(ServerPlayer player, AtlantisContainerMenuInternal container) {
        Atlantis.THREAD_POOL.submit(() -> {
            if (this.collector.isUnlocked()) {
                long tick = player.level().getGameTime();
                this.buttons = this.page.onUpdate(tick, this.reference, this.buttons);
                this.buttons.invokeUpdateTasks(tick);
            }
            container.updateDisplay(this.reference, false);
            container.updateDisplayEnqueued(this.reference);
        });
    }

    @Override
    public int getContainerSize() {
        PageOptions.FormFactor ff = this.getOptions().getFormFactor();
        return ff.hasRows() ? this.getOptions().getRows() * 9 : ff.getSlots();
    }

    @Override
    public boolean isEmpty() {
        if (this.draggable) {
            return super.isEmpty();
        }
        return false;
    }

    @Override
    public @NonNull ItemStack getItem(int index) {
        if (this.draggable) {
            return super.getItem(index);
        }
        var button = this.buttons.button(index);
        return button == null ? ItemStack.EMPTY : button.display(this.reference);
    }

    @Override
    public @NonNull ItemStack removeItem(int index, int count) {
        if (this.draggable) {
            return super.removeItem(index, count);
        }
        AtlantisUI.open(this.reference, this.page, this.buttons);
        return ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int index) {
        if (this.draggable) {
            return super.removeItemNoUpdate(index);
        }
        AtlantisUI.open(this.reference, this.page, this.buttons);
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, @NonNull ItemStack stack) {
        if (this.draggable) {
            super.setItem(index, stack);
            return;
        }
        AtlantisUI.open(this.reference, this.page, this.buttons);
    }

    @Override
    public int getMaxStackSize() {
        if (this.draggable) {
            return super.getMaxStackSize();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void setChanged() {
        if (this.draggable) {
            super.setChanged();
            if (!this.container.enqueueUpdate) {
                this.page.onChanged(this.reference, this, false);
            }
        }
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (this.draggable) {
            return super.stillValid(player);
        }
        return false;
    }

    @Override
    public void startOpen(@NonNull Player player) {
        if (this.draggable) {
            super.startOpen(player);
        }
    }

    @Override
    public void stopOpen(@NonNull Player player) {
        if (this.draggable) {
            this.page.onChanged(this.reference, this, true);
            super.stopOpen(player);
        }
    }

    @Override
    public boolean canPlaceItem(int index, @NonNull ItemStack stack) {
        return this.draggable;
    }

    @Override
    public void clearContent() {
        if (this.draggable) {
            super.clearContent();
        }
    }

    private String getBuffer(String text) {
        int width = FontUtils.getStringWidth(text);
        switch (this.getOptions().getTitleAlignment()) {
            case LEFT:
                return "";
            case CENTER:
                return StringUtils.repeat(" ", (int) Math.round((double) (Math.max(0, (FontUtils.SPACES_WIDTH / 2) - (width / 2))) / (double) FontUtils.SPACE_WIDTH));
            case RIGHT:
                return StringUtils.repeat(" ", (int) Math.round((double) (Math.max(0, FontUtils.SPACES_WIDTH - width)) / (double) FontUtils.SPACE_WIDTH));
        }
        return text;
    }

    public int resolveGroup(int groupID) {
        return this.buttons.resolveGroup(groupID);
    }

    public Buttons.ButtonPageGroup group(int groupID) {
        return this.buttons.group(groupID);
    }

    public Component getName() {
        return this.getOptions().getTitle().compile();
    }

    public MutableComponent getDisplayName() {
        var title = this.getOptions().getTitle();
        if (Settings.get().isUsePlaceholdersInPages()) {
            title = AtlantisPlaceholderProxy.replace(this.reference.entityDirect(), title);
        }
        var component = title.compile();
        return Component.literal(this.getBuffer(component.getString())).append(component);
    }

    public Page getPage() {
        return this.page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public PlayerReference getPlayer() {
        return this.reference;
    }

    public void setPlayer(PlayerReference reference) {
        this.reference = reference;
    }

    public Buttons getButtons() {
        return this.buttons;
    }

    public void setButtons(Buttons buttons) {
        this.buttons = buttons;
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}