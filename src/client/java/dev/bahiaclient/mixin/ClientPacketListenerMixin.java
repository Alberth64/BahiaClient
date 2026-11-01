package dev.bahiaclient.mixin;

import dev.bahiaclient.macros.MoonBlazeDaggerMacro;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
   @Inject(method = "setSubtitleText", at = @At("HEAD"))
   private void bahiaclient$trackDaggerShield(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
      MoonBlazeDaggerMacro.onSubtitle(packet.text().getString());
   }
}
