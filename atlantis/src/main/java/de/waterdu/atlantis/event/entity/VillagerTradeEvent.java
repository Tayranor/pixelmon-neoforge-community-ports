package de.waterdu.atlantis.event.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class VillagerTradeEvent extends Event {

    private final ItemStack offerItemA;
    private final ItemStack offerItemB;
    private final ItemStack resultItem;
    private final Merchant merchant;
    private final MerchantOffer offer;
    private final Player player;

    protected VillagerTradeEvent(ItemStack offerItemA, ItemStack offerItemB, ItemStack resultItem, Merchant merchant,
                                 MerchantOffer offer, Player player) {
        this.offerItemA = offerItemA;
        this.offerItemB = offerItemB;
        this.resultItem = resultItem;
        this.merchant = merchant;
        this.offer = offer;
        this.player = player;
    }

    public ItemStack offerItemA() {
        return this.offerItemA;
    }

    public ItemStack offerItemB() {
        return this.offerItemB;
    }

    public ItemStack resultItem() {
        return this.resultItem;
    }

    public Merchant merchant() {
        return this.merchant;
    }

    public MerchantOffer offer() {
        return this.offer;
    }

    public Player player() {
        return this.player;
    }

    public static class Pre extends VillagerTradeEvent {

        public Pre(ItemStack offerItemA, ItemStack offerItemB, ItemStack resultItem, Merchant merchant, MerchantOffer offer, Player player) {
            super(offerItemA, offerItemB, resultItem, merchant, offer, player);
        }
    }

    public static class Post extends VillagerTradeEvent {

        public Post(ItemStack offerItemA, ItemStack offerItemB, ItemStack resultItem, Merchant merchant, MerchantOffer offer, Player player) {
            super(offerItemA, offerItemB, resultItem, merchant, offer, player);
        }
    }

    public static class XP extends VillagerTradeEvent implements ICancellableEvent {

        private int xp;

        public XP(ItemStack offerItemA, ItemStack offerItemB, ItemStack resultItem, Merchant merchant, MerchantOffer offer, Player player, int xp) {
            super(offerItemA, offerItemB, resultItem, merchant, offer, player);
            this.xp = xp;
        }

        public int xp() {
            return this.isCanceled() ? 0 : this.xp;
        }

        public void setXP(int xp) {
            this.xp = xp;
        }
    }
}
