package de.waterdu.atlantis.mixin.network;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.network.PacketInterceptEvent;
import de.waterdu.atlantis.network.PacketInterceptWrapper;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class)
public abstract class MixinConnection {

    @Shadow private PacketListener packetListener;

    @Shadow protected abstract void doSendPacket(Packet<?> p_packet, @Nullable PacketSendListener sendListener, boolean flush);

    @Shadow
    protected abstract void channelRead0(ChannelHandlerContext context, Packet<?> packet);

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "HEAD"), cancellable = true)
    public void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof PacketInterceptWrapper) && this.packetListener instanceof ServerPlayerConnection spc) {
            var event = Atlantis.EVENT_BUS.post(new PacketInterceptEvent((Connection) (Object) this, spc.getPlayer(), packet));
            if (event.isCanceled()) {
                ci.cancel();
            } else if (event.isPacketChanged()) {
                ci.cancel();
                this.channelRead0(context, event.packet());
            }
        }
    }

    @Inject(method = "doSendPacket", at = @At(value = "HEAD"), cancellable = true)
    public void onDoSendPacket(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        if (!(packet instanceof PacketInterceptWrapper) && this.packetListener instanceof ServerPlayerConnection spc) {
            var event = Atlantis.EVENT_BUS.post(new PacketInterceptEvent((Connection) (Object) this, spc.getPlayer(), packet));
            if (event.isCanceled()) {
                ci.cancel();
            } else if (event.isPacketChanged()) {
                ci.cancel();
                this.doSendPacket(event.packet(), listener, flush);
            }
        }
    }
}
