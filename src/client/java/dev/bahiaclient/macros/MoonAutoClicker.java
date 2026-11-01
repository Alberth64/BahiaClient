package dev.bahiaclient.macros;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.MoonNetworkPolicy;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MoonAutoClicker {
   private static long nextLeftClickNanos;

   private MoonAutoClicker() {
   }

   public static void tick(Minecraft client) {
      if (MoonNetworkPolicy.allowsModServerActions()
         && MoonConfig.featuresEnabled
         && client.player != null
         && client.level != null
         && client.gui.screen() == null
         && client.isWindowActive()) {
         long now = System.nanoTime();
         nextLeftClickNanos = clickWhileHeld(client.mouseHandler.isLeftPressed(), MoonConfig.leftAutoClick, MoonConfig.leftCps, 0, now, nextLeftClickNanos);
      } else {
         nextLeftClickNanos = 0L;
      }
   }

   private static long clickWhileHeld(boolean held, boolean enabled, int cps, int button, long now, long nextClick) {
      if (held && enabled) {
         long interval = 1000000000L / MoonConfig.clampCps(cps);
         if (nextClick == 0L) {
            nextClick = now;
         }

         if (now >= nextClick) {
            KeyMapping.click(Type.MOUSE.getOrCreate(button));
            nextClick = now + interval;
         }

         return nextClick;
      } else {
         return 0L;
      }
   }
}
