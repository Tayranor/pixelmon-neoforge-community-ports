package de.waterdu.atlantis.ui.internal;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.self.event.PageEvent;
import de.waterdu.atlantis.ui.api.AtlantisUI;
import de.waterdu.atlantis.ui.api.ClickData;
import de.waterdu.atlantis.ui.api.ClickState;
import de.waterdu.atlantis.util.concurrency.Concurrency;
import de.waterdu.atlantis.util.entity.PlayerReference;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtlantisContainerMenuInternal extends AbstractContainerMenu implements AtlantisContainerMenu {

    protected List<ItemStack> inventoryItemStacks = NonNullList.create();
    protected List<Slot> inventorySlots = new ArrayList<>();

    protected final AtlantisInventoryInternal inventory;
    protected ClickState lastClick = null;
    protected boolean enqueueUpdate = false;
    protected final AtomicInteger updates = new AtomicInteger(0);
    protected boolean changingPage = false;
    protected boolean forceAll = false;

    public AtlantisContainerMenuInternal(AtlantisInventoryInternal inventory, Player player) {
        super(inventory.getOptions().getFormFactor().getType(inventory.getOptions()), 1);

        var playerInventory = player.getInventory();
        
        this.inventory = inventory;
        inventory.setContainer(this);
        inventory.startOpen(player);
        var draggable = inventory.isDraggable();

        var slot = 0;
        if (inventory.getOptions().getFormFactor().hasRows()) {
            var numRows = inventory.getContainerSize() / 9;
            var dy = (numRows - 4) * 18;

            for (var y = 0; y < numRows; ++y) {
                for (var x = 0; x < 9; ++x) {
                    this.addSlot(new AtlantisSlotInternal(inventory, draggable ? x + y * 9 : slot++, 8 + x * 18, 18 + y * 18));
                }
            }

            for (var y = 0; y < 3; ++y) {
                for (var x = 0; x < 9; ++x) {
                    if (draggable) {
                        this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 103 + y * 18 + dy));
                    } else {
                        this.addSlot(new AtlantisSlotInternal(inventory, slot++, 8 + x * 18, 103 + y * 18 + dy));
                    }
                }
            }

            for (var x = 0; x < 9; ++x) {
                if (draggable) {
                    this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 161 + dy));
                } else {
                    this.addSlot(new AtlantisSlotInternal(inventory, slot++, 8 + x * 18, 161 + dy));
                }
            }
        } else {
            for (var j = 0; j < this.inventory.getContainerSize(); ++j) {
                this.addSlot(new AtlantisSlotInternal(this.inventory, draggable ? j : slot++, 0, 0));
            }

            for (var y = 0; y < 3; ++y) {
                for (var x = 0; x < 9; ++x) {
                    if (draggable) {
                        this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, y * 18 + 51));
                    } else {
                        this.addSlot(new AtlantisSlotInternal(inventory, slot++, 8 + x * 18, y * 18 + 51));
                    }
                }
            }

            for (var x = 0; x < 9; ++x) {
                if (draggable) {
                    this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 109));
                } else {
                    this.addSlot(new AtlantisSlotInternal(inventory, slot++, 8 + x * 18, 109));
                }
            }
        }
    }

    @Override
    protected @NonNull Slot addSlot(@NonNull Slot slotIn) {
        if (this.inventory.isDraggable()) {
            var slot = super.addSlot(slotIn);
            this.inventorySlots.add(slot);
            this.inventoryItemStacks.add(ItemStack.EMPTY);
            return slot;
        }
        slotIn.index = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
        super.addSlot(slotIn);
        return slotIn;
    }

    public AtlantisInventoryInternal getAquaUIInventory() {
        return this.inventory;
    }

    @Override
    public boolean stillValid(@NonNull Player playerIn) {
        return true;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        if (this.inventory.isDraggable()) {
            var stackCopy = ItemStack.EMPTY;
            var slot = this.slots.get(index);
            if (slot.hasItem()) {
                var stack = slot.getItem();
                stackCopy = stack.copy();
                if (index < this.inventory.getContainerSize()) {
                    if (!this.moveItemStackTo(stack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 0, this.inventory.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }
            return stackCopy;
        }
        this.resetDisplay();
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int dragType, @NonNull ClickType clickTypeIn, @NonNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var state = new ClickState(ClickState.MouseButton.get(dragType), ClickState.ClickStyle.get(clickTypeIn));
            state.tryDoubleClick(this.lastClick);
            this.lastClick = state;

            if (this.inventory.isDraggable()) {
                this.doButtonClick(slot, state);
                super.clicked(slot, dragType, clickTypeIn, player);
                return;
            }

            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(-1, 1, 0, ItemStack.EMPTY));
            this.resetDisplay();
            this.doButtonClick(slot, state);
        }
    }

    private void doButtonClick(final int slot, final ClickState state) {
        Atlantis.THREAD_POOL.submit(() -> {
            var button = this.inventory.getButtons().button(slot);
            if (button.presses() == Integer.MIN_VALUE || button.presses() > 0) {
                if (button.presses() > 0) {
                    button.changePresses(-1);
                }
                if (!AtlantisUI.LOCKED.contains(this.inventory.getPlayer().uuid())) {
                    var data = new ClickData(this.inventory.getPlayer(), this.inventory.getButtons(), slot, button.group(), state);
                    state.withData(data);
                    if (!Atlantis.EVENT_BUS.post(new PageEvent.Press(this.inventory.getPlayer(), this.inventory.getPage(), button, data)).isCanceled()) {
                        Concurrency.sync(() -> this.inventory.getPage().onButtonClick(this.inventory.getPlayer(), slot, state));
                        if (button.getClickAction() != null) {
                            if (button.isAsync()) {
                                Concurrency.async(() -> button.getClickAction().ifLeft(action -> action.accept(data)).ifRight(action -> action.accept(this.inventory.page, data)));
                            } else {
                                button.getClickAction().ifLeft(action -> action.accept(data)).ifRight(action -> action.accept(this.inventory.page, data));
                            }
                        }
                    }
                    AtlantisUI.LOCKED.add(this.inventory.getPlayer().uuid());
                }
            }
        });
    }

    @Override
    protected boolean moveItemStackTo(@NonNull ItemStack stack, int start, int end, boolean backwards) {
        if (this.inventory.isDraggable()) {
            return super.moveItemStackTo(stack, start, end, backwards);
        }
        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(@NonNull ItemStack stack, @NonNull Slot slotIn) {
        if (this.inventory.isDraggable()) {
            return super.canTakeItemForPickAll(stack, slotIn);
        }
        return false;
    }

    @Override
    public void removed(@NonNull Player playerIn) {
        if (playerIn instanceof ServerPlayer player) {
            var reference = PlayerReference.get(player);
            super.removed(playerIn);
            this.inventory.stopOpen(playerIn);
            player.connection.send(new ClientboundContainerClosePacket(player.containerMenu.containerId));
            player.containerCounter = 0;
            player.containerMenu = player.inventoryMenu;
            player.containerMenu.broadcastChanges();
            player.initMenu(player.containerMenu);

            if (this.inventory.isDraggable()) {
                if (!this.inventory.isClosed()) {
                    this.inventory.getPage().afterClose(reference);
                    this.inventory.setClosed(true);
                }
                return;
            }

            if (this.changingPage) {
                this.changingPage = false;
            } else {
                if (!this.inventory.getPage().onClose(reference) || Atlantis.EVENT_BUS.post(new PageEvent.Destruct(reference, this.inventory.getPage())).isCanceled()) {
                    AtlantisUI.open(player, this.inventory.getPage(), this.inventory.getButtons(), true);
                } else {
                    if (!this.inventory.isClosed()) {
                        this.inventory.getPage().afterClose(reference);
                        this.inventory.setClosed(true);
                    }
                }
            }
        }
    }

    @Override
    public void setItem(int slotId, int stateId, @NonNull ItemStack stack) {
        if (this.inventory.isDraggable()) {
            super.setItem(slotId, stateId, stack);
        }
    }

    @Override
    public boolean canDragTo(@NonNull Slot slot) {
        if (this.inventory.isDraggable()) {
            return super.canDragTo(slot);
        }
        return false;
    }

    public void resetDisplay() {
        this.updateDisplay(true);
    }

    public void updateDisplay(boolean forceAll) {
        this.updateDisplay(this.inventory.getPlayer(), forceAll);
    }

    public void updateDisplay(PlayerReference player, boolean forceAll) {
        this.enqueueUpdate = true;
        this.forceAll = this.forceAll || forceAll;
    }

    public void updateDisplayEnqueued(PlayerReference player) {
        var entity = player.entityDirect();
        if (entity == null) {
            return;
        }

        if (this.inventory.isDraggable()) {
            if (this.updates.getAndIncrement() == 0) {
                for (var i = 0; i < this.inventory.getButtons().size(true); i++) {
                    var button = this.inventory.getButtons().button(i);
                    var display = button.isEmpty() ? ItemStack.EMPTY : button.display(player);
                    this.inventory.getButtons().setButton(i, button.group(), button.page(), button);
                    this.setItem(i, this.getStateId(), display);
                }
                this.forceAll = false;
            }
            this.enqueueUpdate = false;
            AtlantisUI.LOCKED.remove(player.uuid());
        } else {
            Atlantis.THREAD_POOL.submit(() -> {
                if (this.enqueueUpdate) {
                    for (var i = 0; i < this.inventory.getButtons().size(true); i++) {
                        var button = this.inventory.getButtons().button(i);
                        if (this.forceAll || (button != null && button.isChanged())) {
                            button.markClean();
                            if (!this.forceAll) {
                                entity.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.getStateId(), i, button.isEmpty() ? ItemStack.EMPTY : button.display(player)));
                            }
                        }
                    }
                    if (this.forceAll) {
                        entity.initMenu(this);
                        this.forceAll = false;
                    }
                    this.enqueueUpdate = false;
                    AtlantisUI.LOCKED.remove(player.uuid());
                }
            });
        }
    }

    @Override
    public @NonNull Slot getSlot(int slotId) {
        return this.inventorySlots.get(Math.min(this.inventorySlots.size() - 1, slotId));
    }

    @Override
    public void broadcastChanges() {
        if (this.inventory.isDraggable()) {
            super.broadcastChanges();
        }
    }

    @Override
    public @NonNull ItemStack getCarried() {
        if (this.inventory.isDraggable()) {
            return super.getCarried();
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getInventoryItemStacks() {
        return this.inventoryItemStacks;
    }

    public void setInventoryItemStacks(List<ItemStack> inventoryItemStacks) {
        this.inventoryItemStacks = inventoryItemStacks;
    }

    public List<Slot> getInventorySlots() {
        return this.inventorySlots;
    }

    public void setInventorySlots(List<Slot> inventorySlots) {
        this.inventorySlots = inventorySlots;
    }
}