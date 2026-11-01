package dev.bahiaclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.bahiaclient.MoonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
   @org.spongepowered.asm.mixin.Unique
   private AvatarRenderState bahiaclient$hiddenHeadState;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack bahiaclient$originalHeadEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack bahiaclient$originalChestEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack bahiaclient$originalLegsEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack bahiaclient$originalFeetEquipment;

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void bahiaclient$hideCustomHeadArmor(
      PoseStack poseStack, SubmitNodeCollector collector, int light, HumanoidRenderState state, float yRot, float xRot, CallbackInfo callback
   ) {
      Minecraft client = Minecraft.getInstance();
      if (MoonConfig.featuresEnabled && MoonConfig.hideArmor && state instanceof AvatarRenderState avatar
         && client.player != null && avatar.id == client.player.getId()) {
         this.bahiaclient$hiddenHeadState = avatar;
         if (MoonConfig.hideHelmet) {
            this.bahiaclient$originalHeadEquipment = avatar.headEquipment;
            avatar.headEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideChestplate) {
            this.bahiaclient$originalChestEquipment = avatar.chestEquipment;
            avatar.chestEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideLeggings) {
            this.bahiaclient$originalLegsEquipment = avatar.legsEquipment;
            avatar.legsEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideBoots) {
            this.bahiaclient$originalFeetEquipment = avatar.feetEquipment;
            avatar.feetEquipment = ItemStack.EMPTY;
         }
      }
   }

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
      at = @At("RETURN")
   )
   private void bahiaclient$restoreHeadEquipment(
      PoseStack poseStack, SubmitNodeCollector collector, int light, HumanoidRenderState state, float yRot, float xRot, CallbackInfo callback
   ) {
      if (this.bahiaclient$hiddenHeadState != null) {
         this.bahiaclient$restoreHeadEquipment();
      }
   }

   @org.spongepowered.asm.mixin.Unique
   private void bahiaclient$restoreHeadEquipment() {
      if (this.bahiaclient$originalHeadEquipment != null) {
         this.bahiaclient$hiddenHeadState.headEquipment = this.bahiaclient$originalHeadEquipment;
      }
      if (this.bahiaclient$originalChestEquipment != null) {
         this.bahiaclient$hiddenHeadState.chestEquipment = this.bahiaclient$originalChestEquipment;
      }
      if (this.bahiaclient$originalLegsEquipment != null) {
         this.bahiaclient$hiddenHeadState.legsEquipment = this.bahiaclient$originalLegsEquipment;
      }
      if (this.bahiaclient$originalFeetEquipment != null) {
         this.bahiaclient$hiddenHeadState.feetEquipment = this.bahiaclient$originalFeetEquipment;
      }
      this.bahiaclient$hiddenHeadState = null;
      this.bahiaclient$originalHeadEquipment = null;
      this.bahiaclient$originalChestEquipment = null;
      this.bahiaclient$originalLegsEquipment = null;
      this.bahiaclient$originalFeetEquipment = null;
   }
}
