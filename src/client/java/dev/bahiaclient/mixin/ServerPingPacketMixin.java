package dev.bahiaclient.mixin;

import dev.bahiaclient.hud.MoonPingTracker;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ServerPingPacketMixin {
   @Inject(method = "handleKeepAlive", at = @At("HEAD"))
   private void bahiaclient$onKeepAliveReceived(ClientboundKeepAlivePacket packet, CallbackInfo callback) {
      MoonPingTracker.onKeepAliveReceived(packet.getId());
   }

   @Inject(method = "handleKeepAlive", at = @At("TAIL"))
   private void bahiaclient$onKeepAliveSent(ClientboundKeepAlivePacket packet, CallbackInfo callback) {
      MoonPingTracker.onKeepAliveSent(packet.getId());
   }
}
