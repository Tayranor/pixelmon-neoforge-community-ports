package de.waterdu.atlantis.mixin.entity;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.event.entity.ActuallyHurtEvent;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CombatTracker.class)
public abstract class MixinCombatTracker {

    @Shadow
    @Final
    private LivingEntity mob;

    @Inject(method = "recordDamage", at = @At(value = "TAIL"))
    public void onRecordDamage(DamageSource source, float damage, CallbackInfo ci) {
        Atlantis.EVENT_BUS.post(new ActuallyHurtEvent(this.mob, source, damage));
    }
}