package dev.bahiaclient.inventory;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.ui.MoonScreenStyle;
import dev.bahiaclient.ui.MoonSettingsScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.BeforeKeyPress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public final class MoonWardrobeSettingsScreen extends Screen implements BeforeKeyPress {
   private static final int PANEL_WIDTH = 360;
   private static final int PANEL_HEIGHT = 340;
   private int waitingForSlot = -1;
   private boolean keyboardListenerRegistered;

   public MoonWardrobeSettingsScreen() {
      super(Component.literal("Loadout Keybinds"));
   }

   @Override
   protected void init() {
      if (!this.keyboardListenerRegistered) {
         ScreenKeyboardEvents.beforeKeyPress(this).register(this);
         this.keyboardListenerRegistered = true;
      }
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      int buttonWidth = 94;
      int buttonHeight = 24;
      int gap = 8;
      int startX = centerX - (buttonWidth * 3 + gap * 2) / 2;
      int startY = top + 64;

      for (int i = 0; i < MoonConfig.wardrobeQuickSwapKeysLoadout.length; i++) {
         int slotIndex = i;
         int column = i % 3;
         int row = i / 3;
         int keyCode = MoonConfig.wardrobeQuickSwapKeysLoadout[i];
         String keyName = slotIndex == this.waitingForSlot ? "Press a key..." : getKeyName(keyCode);
         this.addRenderableWidget(
            Button.builder(Component.literal("Slot " + (i + 1) + ": " + keyName), button -> {
               this.waitingForSlot = slotIndex;
               this.clearWidgets();
               this.init();
            }).bounds(startX + column * (buttonWidth + gap), startY + row * (buttonHeight + gap), buttonWidth, buttonHeight).build()
         );
      }

      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.gui.setScreen(new MoonSettingsScreen()))
            .bounds(centerX - 70, top + PANEL_HEIGHT - 34, 140, 22)
            .build()
      );
   }

   @Override
   public void beforeKeyPress(Screen screen, KeyEvent event) {
      if (this.waitingForSlot < 0) {
         return;
      }

      int slotIndex = this.waitingForSlot;
      MoonConfig.wardrobeQuickSwapKeysLoadout[slotIndex] = event.key() == 256 ? -1 : event.key();
      MoonConfig.save();
      this.waitingForSlot = -1;
      this.clearWidgets();
      this.init();
   }

   private static String getKeyName(int keyCode) {
      if (keyCode < 0) {
         return "--";
      }
      if (keyCode >= 65 && keyCode <= 90) {
         return "\"" + (char)keyCode + "\"";
      }
      if (keyCode >= 48 && keyCode <= 57) {
         return "\"" + (char)keyCode + "\"";
      }
      return "\"Key " + keyCode + "\"";
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      MoonScreenStyle.drawPanel(graphics, this.width / 2, MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT), PANEL_WIDTH, PANEL_HEIGHT);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      graphics.centeredText(this.font, this.title, centerX, top + 14, -1);
      graphics.centeredText(this.font, "Press a key to open /loadout and select a slot.", centerX, top + 40, MoonScreenStyle.mutedColor());
   }
}
