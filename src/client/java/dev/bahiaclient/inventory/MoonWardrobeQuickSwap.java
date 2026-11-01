package dev.bahiaclient.inventory;

import dev.bahiaclient.MoonConfig;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public final class MoonWardrobeQuickSwap {
   private static final int[] LOADOUT_MENU_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};
   private static final int MENU_TIMEOUT_TICKS = 100;
   private static int pendingLoadoutSlot = -1;
   private static int menuTimeoutTicks;

   private MoonWardrobeQuickSwap() {
   }

   public static boolean onKeyPressed(Minecraft client, int keyCode) {
      if (!MoonConfig.featuresEnabled || !MoonConfig.wardrobeQuickSwapEnabled || client.player == null || client.gameMode == null || keyCode < 0) {
         return false;
      }

      int slot = findLoadoutForKey(keyCode);
      if (slot < 0) {
         return false;
      }

      if (client.gui.screen() instanceof AbstractContainerScreen<?> screen) {
         if (isLoadoutMenu(screen)) {
            return executeSwap(client, screen, slot);
         }
         return false;
      }

      if (client.gui.screen() != null || pendingLoadoutSlot >= 0) {
         return false;
      }

      pendingLoadoutSlot = slot;
      menuTimeoutTicks = MENU_TIMEOUT_TICKS;
      client.player.connection.sendCommand("loadout");
      return true;
   }

   public static void tick(Minecraft client) {
      if (pendingLoadoutSlot < 0) {
         return;
      }

      if (!MoonConfig.wardrobeQuickSwapEnabled) {
         clearPending();
         return;
      }

      if (client.player == null || client.gameMode == null) {
         clearPending();
         return;
      }

      if (client.gui.screen() instanceof AbstractContainerScreen<?> screen && isLoadoutMenu(screen)) {
         executeSwap(client, screen, pendingLoadoutSlot);
         clearPending();
      } else if (--menuTimeoutTicks <= 0) {
         clearPending();
         sendFeedback(client, "The /loadout menu did not open; no loadout was equipped.");
      }
   }

   private static int findLoadoutForKey(int keyCode) {
      for (int i = 0; i < MoonConfig.wardrobeQuickSwapKeysLoadout.length; i++) {
         if (MoonConfig.wardrobeQuickSwapKeysLoadout[i] == keyCode) {
            return i;
         }
      }
      return -1;
   }

   private static boolean executeSwap(Minecraft client, AbstractContainerScreen<?> screen, int slotIndex) {
      AbstractContainerMenu menu = screen.getMenu();
      int menuSlot = LOADOUT_MENU_SLOTS[slotIndex];
      if (menuSlot >= menu.slots.size()) {
         sendFeedback(client, "The /loadout menu does not contain the expected slot.");
         return false;
      }

      client.gameMode.handleContainerInput(menu.containerId, menuSlot, 0, ContainerInput.PICKUP, client.player);
      client.player.closeContainer();
      sendFeedback(client, "Loadout " + (slotIndex + 1) + " selected.");
      return true;
   }

   private static boolean isLoadoutMenu(AbstractContainerScreen<?> screen) {
      Component title = screen.getTitle();
      if (title == null) {
         return false;
      }

      String titleText = title.getString().replaceAll("§.", "").trim().toLowerCase(Locale.ROOT);
      return titleText.contains("loadout") || titleText.contains("load-out");
   }

   private static void clearPending() {
      pendingLoadoutSlot = -1;
      menuTimeoutTicks = 0;
   }

   private static void sendFeedback(Minecraft client, String message) {
      if (client.gui.chatListener() != null) {
         client.gui.chatListener().handleSystemMessage(Component.literal("[BahiaClient] [Loadout] " + message), false);
      }
   }
}
