package dev.bahiaclient.mixin;

import dev.bahiaclient.hud.MoonServerStats;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ServerTimePacketMixin {
   @Inject(method = "handleSetTime", at = @At("TAIL"))
   private void bahiaclient$recordServerTime(ClientboundSetTimePacket packet, CallbackInfo callback) {
      MoonServerStats.recordServerTime(packet.gameTime());
   }
}
