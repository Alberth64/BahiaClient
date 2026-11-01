package dev.bahiaclient.visual;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.ui.MoonScreenStyle;
import dev.bahiaclient.ui.MoonSettingsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoonVisualSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 340;
   private static final int PANEL_HEIGHT = 340;

   public MoonVisualSettingsScreen() {
      super(Component.literal("Visual"));
   }

   @Override
   protected void init() {
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      this.addToggle("Fullbright", MoonConfig.fullbright, () -> MoonConfig.fullbright = !MoonConfig.fullbright, centerX, top + 52);
      this.addToggle("Hide Armor", MoonConfig.hideArmor, () -> MoonConfig.hideArmor = !MoonConfig.hideArmor, centerX, top + 78);
      this.addToggle("Mob Hitboxes", MoonConfig.entityHitboxes, () -> MoonConfig.entityHitboxes = !MoonConfig.entityHitboxes, centerX, top + 104);
      this.addRenderableWidget(
         Button.builder(Component.literal("Player Size"), button -> this.minecraft.gui.setScreen(new MoonPlayerSizeSettingsScreen()))
            .bounds(centerX - 135, top + 140, 270, 22)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Player Glow"), button -> this.minecraft.gui.setScreen(new MoonGlowSettingsScreen()))
            .bounds(centerX - 135, top + 166, 270, 22)
            .build()
      );
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
