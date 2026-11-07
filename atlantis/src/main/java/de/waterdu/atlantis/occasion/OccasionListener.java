package de.waterdu.atlantis.occasion;

import de.waterdu.atlantis.util.entity.PlayerReference;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class OccasionListener {

    public OccasionListener() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        var reference = PlayerReference.get(event.getEntity());
        var recovery = OccasionRecovery.pop(reference);
        var occasion = OccasionManager.getFor(reference);
        var recover = recovery != null;
        if (recover && occasion.isPresent() && occasion.get().currentMoment.get() == recovery.getA()) {
            recover = false;
            occasion.get().teleportToStart(reference);
        }
        if (recover) {
            recovery.getB().teleport(reference);
        } else {
            OccasionManager.handleEvent(event);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        OccasionManager.handleEvent(event);
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post event) {
        OccasionManager.tick();
    }
}
