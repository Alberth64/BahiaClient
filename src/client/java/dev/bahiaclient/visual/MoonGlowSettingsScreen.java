package dev.bahiaclient.visual;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.BahiaClient;
import dev.bahiaclient.ui.MoonColorPickerScreen;
import dev.bahiaclient.ui.MoonScreenStyle;
import dev.bahiaclient.ui.MoonSettingsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoonGlowSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 320;
   private static final int PANEL_HEIGHT = 220;

   public MoonGlowSettingsScreen() {
      super(Component.literal("Player Glow"));
   }

   @Override
   protected void init() {
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      this.addRenderableWidget(Button.builder(
         BahiaClient.toggleLabel("Player Glow", MoonConfig.playerGlow),
         button -> {
            MoonConfig.playerGlow = !MoonConfig.playerGlow;
            MoonConfig.save();
            this.clearWidgets();
            this.init();
         }
      ).bounds(centerX - 135, top + 60, 270, 24).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Select Color"), button -> this.minecraft.gui.setScreen(
            new MoonColorPickerScreen("Player Glow Color", MoonConfig.playerGlowColor, this, color -> {
               MoonConfig.playerGlowColor = color;
               MoonConfig.save();
            })
         )).bounds(centerX - 135, top + 96, 270, 24).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.gui.setScreen(new MoonVisualSettingsScreen()))
            .bounds(centerX - 70, top + 176, 140, 22)
            .build()
      );
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      MoonScreenStyle.drawPanel(graphics, this.width / 2, top, PANEL_WIDTH, PANEL_HEIGHT);
      graphics.fill(this.width / 2 - 30, top + 138, this.width / 2 + 30, top + 158, 0xFF000000 | MoonConfig.playerGlowColor);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      graphics.centeredText(this.font, this.title, centerX, top + 14, -1);
      graphics.centeredText(this.font, String.format("#%06X", MoonConfig.playerGlowColor), centerX, top + 160, -1);
   }
}
