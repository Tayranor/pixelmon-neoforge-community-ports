package local.gameshark;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.minecraft.server.level.ServerPlayer;

@Mod("sharkscope")
public final class SharkScope {
    public SharkScope() {
        NeoForge.EVENT_BUS.addListener(this::playerJoined);
    }

    private void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && NetworkRegistry.hasChannel(player.connection, RadarPermission.TYPE.id())) {
            PacketDistributor.sendToPlayer(player, new RadarPermission(ServerSettings.enabled()));
        }
    }

    @EventBusSubscriber(modid = "sharkscope", bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1").optional();
            registrar.playToClient(RadarPermission.TYPE, RadarPermission.STREAM_CODEC,
                    (payload, context) -> RadarPermission.setClientAllowed(payload.allowed()));
        }
    }
}
