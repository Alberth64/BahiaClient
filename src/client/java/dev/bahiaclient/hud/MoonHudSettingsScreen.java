package dev.bahiaclient.hud;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.BahiaClient;
import dev.bahiaclient.ui.MoonColorPickerScreen;
import dev.bahiaclient.ui.MoonScreenStyle;
import dev.bahiaclient.ui.MoonSettingsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoonHudSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 340;
   private static final int PANEL_HEIGHT = 330;

   public MoonHudSettingsScreen() {
      super(Component.literal("HUD"));
   }

   @Override
   protected void init() {
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      this.addToggle("FPS / Ping / TPS", MoonConfig.showHud, () -> MoonConfig.showHud = !MoonConfig.showHud, centerX, top + 54);
      this.addToggle("Enemy Info", MoonConfig.combatHudShowTargetHealth, () -> MoonConfig.combatHudShowTargetHealth = !MoonConfig.combatHudShowTargetHealth, centerX, top + 82);
      this.addRenderableWidget(
         Button.builder(Component.literal("Move HUD"), button -> BahiaClient.openHudEditor(this.minecraft))
            .bounds(centerX - 135, top + 112, 270, 22)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Text Color"), button -> this.minecraft.gui.setScreen(
            new MoonColorPickerScreen("HUD Text Color", MoonConfig.hudTextColor, this, color -> {
               MoonConfig.hudTextColor = 0xFF000000 | color;
               MoonConfig.save();
            })
         )).bounds(centerX - 135, top + 150, 270, 22).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Background Color"), button -> this.minecraft.gui.setScreen(
            new MoonColorPickerScreen("HUD Background Color", MoonConfig.hudBackgroundColor, this, color -> {
               MoonConfig.hudBackgroundColor = (MoonConfig.hudBackgroundColor & 0xFF000000) | color;
               MoonConfig.save();
            })
         )).bounds(centerX - 135, top + 178, 270, 22).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Accent Color"), button -> this.minecraft.gui.setScreen(
            new MoonColorPickerScreen("HUD Accent Color", MoonConfig.hudAccentColor, this, color -> {
               MoonConfig.hudAccentColor = 0xFF000000 | color;
               MoonConfig.save();
            })
         )).bounds(centerX - 135, top + 206, 270, 22).build()
      );
      this.addToggle("Background", MoonConfig.hudBackgroundEnabled, () -> MoonConfig.hudBackgroundEnabled = !MoonConfig.hudBackgroundEnabled, centerX, top + 234);
      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.gui.setScreen(new MoonSettingsScreen()))
            .bounds(centerX - 70, top + PANEL_HEIGHT - 34, 140, 22)
            .build()
      );
   }

   private void addToggle(String label, boolean enabled, Runnable toggle, int centerX, int y) {
      this.addRenderableWidget(Button.builder(Component.literal(label).withStyle(style -> style.withColor(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY)), button -> {
         toggle.run();
         MoonConfig.save();
         this.clearWidgets();
         this.init();
      }).bounds(centerX - 135, y, 270, 22).build());
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      MoonScreenStyle.drawPanel(graphics, this.width / 2, MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT), PANEL_WIDTH, PANEL_HEIGHT);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      graphics.centeredText(this.font, this.title, this.width / 2, MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT) + 14, -1);
   }
}
