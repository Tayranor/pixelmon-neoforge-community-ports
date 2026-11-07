package de.waterdu.atlantis.event.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class ActuallyHurtEvent extends Event {

    private final LivingEntity entity;
    private final DamageSource source;
    private final float amount;

    public ActuallyHurtEvent(LivingEntity entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }

    public LivingEntity entity() {
        return this.entity;
    }

    public DamageSource source() {
        return this.source;
    }

    public float amount() {
        return this.amount;
    }
}
