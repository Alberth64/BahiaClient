package dev.bahiaclient.mixin;

import dev.bahiaclient.BahiaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
   @Shadow
   protected EditBox input;

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void bahiaclient$openSettingsCommands(KeyEvent event, CallbackInfoReturnable<Boolean> callback) {
      if (!event.isConfirmation()) {
         return;
      }

      String command = this.input.getValue().trim();
      if (command.startsWith("/")) {
         command = command.substring(1).trim();
      }

      String[] arguments = command.split("\\s+");
      if (arguments.length == 0) {
         return;
      }

      String root = arguments[0];
      if (!root.equalsIgnoreCase("bahia") && !root.equalsIgnoreCase("b")) {
         return;
      }

      Minecraft client = Minecraft.getInstance();
      if (arguments.length == 1) {
         BahiaClient.openSettings(client);
      } else if (arguments.length == 2 && arguments[1].equalsIgnoreCase("hud")) {
         BahiaClient.openHudEditor(client);
      } else {
         return;
      }

      callback.setReturnValue(true);
   }
}
