package local.gameshark;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadarPermission(boolean allowed) implements CustomPacketPayload {
    private static volatile boolean clientAllowed;
    public static final Type<RadarPermission> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("sharkscope", "radar_permission"));
    public static final StreamCodec<ByteBuf, RadarPermission> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, RadarPermission::allowed, RadarPermission::new);

    public static boolean clientAllowed() { return clientAllowed; }
    public static void setClientAllowed(boolean allowed) { clientAllowed = allowed; }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
