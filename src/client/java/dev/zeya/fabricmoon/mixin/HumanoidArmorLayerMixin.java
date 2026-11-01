package dev.zeya.fabricmoon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.zeya.fabricmoon.MoonConfig;
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
   private AvatarRenderState zeyafabricmoon$hiddenHeadState;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack zeyafabricmoon$originalHeadEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack zeyafabricmoon$originalChestEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack zeyafabricmoon$originalLegsEquipment;
   @org.spongepowered.asm.mixin.Unique
   private ItemStack zeyafabricmoon$originalFeetEquipment;

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void zeyafabricmoon$hideCustomHeadArmor(
      PoseStack poseStack, SubmitNodeCollector collector, int light, HumanoidRenderState state, float yRot, float xRot, CallbackInfo callback
   ) {
      Minecraft client = Minecraft.getInstance();
      if (MoonConfig.featuresEnabled && MoonConfig.hideArmor && state instanceof AvatarRenderState avatar
         && client.player != null && avatar.id == client.player.getId()) {
         this.zeyafabricmoon$hiddenHeadState = avatar;
         if (MoonConfig.hideHelmet) {
            this.zeyafabricmoon$originalHeadEquipment = avatar.headEquipment;
            avatar.headEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideChestplate) {
            this.zeyafabricmoon$originalChestEquipment = avatar.chestEquipment;
            avatar.chestEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideLeggings) {
            this.zeyafabricmoon$originalLegsEquipment = avatar.legsEquipment;
            avatar.legsEquipment = ItemStack.EMPTY;
         }
         if (MoonConfig.hideBoots) {
            this.zeyafabricmoon$originalFeetEquipment = avatar.feetEquipment;
            avatar.feetEquipment = ItemStack.EMPTY;
         }
      }
   }

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
      at = @At("RETURN")
   )
   private void zeyafabricmoon$restoreHeadEquipment(
      PoseStack poseStack, SubmitNodeCollector collector, int light, HumanoidRenderState state, float yRot, float xRot, CallbackInfo callback
   ) {
      if (this.zeyafabricmoon$hiddenHeadState != null) {
         this.zeyafabricmoon$restoreHeadEquipment();
      }
   }

   @org.spongepowered.asm.mixin.Unique
   private void zeyafabricmoon$restoreHeadEquipment() {
      if (this.zeyafabricmoon$originalHeadEquipment != null) {
         this.zeyafabricmoon$hiddenHeadState.headEquipment = this.zeyafabricmoon$originalHeadEquipment;
      }
      if (this.zeyafabricmoon$originalChestEquipment != null) {
         this.zeyafabricmoon$hiddenHeadState.chestEquipment = this.zeyafabricmoon$originalChestEquipment;
      }
      if (this.zeyafabricmoon$originalLegsEquipment != null) {
         this.zeyafabricmoon$hiddenHeadState.legsEquipment = this.zeyafabricmoon$originalLegsEquipment;
      }
      if (this.zeyafabricmoon$originalFeetEquipment != null) {
         this.zeyafabricmoon$hiddenHeadState.feetEquipment = this.zeyafabricmoon$originalFeetEquipment;
      }
      this.zeyafabricmoon$hiddenHeadState = null;
      this.zeyafabricmoon$originalHeadEquipment = null;
      this.zeyafabricmoon$originalChestEquipment = null;
      this.zeyafabricmoon$originalLegsEquipment = null;
      this.zeyafabricmoon$originalFeetEquipment = null;
   }
}
