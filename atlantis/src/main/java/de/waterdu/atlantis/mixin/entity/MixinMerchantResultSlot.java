package de.waterdu.atlantis.mixin.entity;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.event.entity.VillagerTradeEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MerchantResultSlot.class)
public abstract class MixinMerchantResultSlot {

    @Shadow
    protected abstract void checkTakeAchievements(ItemStack p_75208_1_);

    @Shadow
    @Final
    private MerchantContainer slots;

    @Shadow
    @Final
    private Merchant merchant;

    /**
     * @author Water
     * @reason Atlantis event injection
     */
    @Overwrite
    public void onTake(Player player, ItemStack stack) {
        this.checkTakeAchievements(stack);
        MerchantOffer merchantoffer = this.slots.getActiveOffer();
        if (merchantoffer != null) {
            ItemStack itemstack = this.slots.getItem(0);
            ItemStack itemstack1 = this.slots.getItem(1);
            ItemStack itemstackCopy = itemstack.copy();
            ItemStack itemstack1Copy = itemstack1.copy();
            Atlantis.EVENT_BUS.post(new VillagerTradeEvent.Pre(itemstack, itemstack1, stack, this.merchant, merchantoffer, player));
            if (merchantoffer.take(itemstack, itemstack1) || merchantoffer.take(itemstack1, itemstack)) {
                this.merchant.notifyTrade(merchantoffer);
                player.awardStat(Stats.TRADED_WITH_VILLAGER);
                this.slots.setItem(0, itemstack);
                this.slots.setItem(1, itemstack1);
                Atlantis.EVENT_BUS.post(new VillagerTradeEvent.Post(itemstackCopy, itemstack1Copy, stack, this.merchant, merchantoffer, player));
            }
            VillagerTradeEvent.XP event = Atlantis.EVENT_BUS.post(new VillagerTradeEvent.XP(itemstack, itemstack1, stack, this.merchant, merchantoffer, player, merchantoffer.getXp()));
            this.merchant.overrideXp(this.merchant.getVillagerXp() + event.xp());
        }
    }
}