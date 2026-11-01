package dev.zeya.fabricmoon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.zeya.fabricmoon.MoonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void zeyafabricmoon$hideLocalCustomHead(
      PoseStack poseStack, SubmitNodeCollector collector, int light, LivingEntityRenderState state, float yRot, float xRot, CallbackInfo callback
   ) {
      Minecraft client = Minecraft.getInstance();
      if (MoonConfig.featuresEnabled && MoonConfig.hideArmor && MoonConfig.hideHelmet && state instanceof AvatarRenderState avatar
         && client.player != null && avatar.id == client.player.getId()) {
         callback.cancel();
      }
   }
}
