package de.waterdu.atlantis.mod.pixelmon.battle;

import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;

public interface BattleListener<A extends BattleParticipant, B extends BattleParticipant> {

    void onBattleStart(Battle.Ticket.Start<A, B> ticket);

    void onBattleEnd(Battle.Ticket.End<A, B> ticket);
}
