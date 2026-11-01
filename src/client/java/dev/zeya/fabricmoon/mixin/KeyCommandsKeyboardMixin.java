package dev.zeya.fabricmoon.mixin;

import dev.zeya.fabricmoon.inventory.MoonWardrobeQuickSwap;
import dev.zeya.fabricmoon.ui.MoonSettingsScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyCommandsKeyboardMixin {
   @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
   private void zeyafabricmoon$handleMenuAndLoadoutKey(long window, int action, KeyEvent event, CallbackInfo ci) {
      if (action == 1 && event.key() == 75) {
         Minecraft client = Minecraft.getInstance();
         if (client.gui.screen() == null) {
            client.gui.setScreen(new MoonSettingsScreen());
            ci.cancel();
         }
      }

      if (action == 1 && MoonWardrobeQuickSwap.onKeyPressed(Minecraft.getInstance(), event.key())) {
         ci.cancel();
      }
   }

}
