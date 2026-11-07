package de.waterdu.atlantis.mod.pixelmon;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import de.waterdu.atlantis.mod.pixelmon.battle.AtlantisBattles;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class PixelmonListener {

    public PixelmonListener() {
        Pixelmon.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBattleStart(BattleStartedEvent.Post event) {
        AtlantisBattles.onBattleStart(event);
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        AtlantisBattles.onBattleEnd(event);
    }
}
