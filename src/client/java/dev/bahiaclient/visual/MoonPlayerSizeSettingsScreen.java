package dev.bahiaclient.visual;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.ui.MoonScreenStyle;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoonPlayerSizeSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 340;
   private static final int PANEL_HEIGHT = 230;

   public MoonPlayerSizeSettingsScreen() {
      super(Component.literal("Player Size"));
   }

   protected void init() {
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      int buttonWidth = 280;
      int buttonX = centerX - buttonWidth / 2;
      this.addRenderableWidget(Button.builder(
         Component.literal("Player Size").withStyle(style -> style.withColor(MoonConfig.playerSizeEnabled ? ChatFormatting.AQUA : ChatFormatting.GRAY)),
         button -> this.togglePlayerSize()
      ).bounds(buttonX, top + 64, buttonWidth, 22).build());
      this.addRenderableWidget(Button.builder(Component.literal("- 0.1"), button -> this.changeSize(-0.1F)).bounds(centerX - 115, top + 104, 70, 22).build());
      this.addRenderableWidget(Button.builder(Component.literal("1.0x"), button -> this.setSize(1.0F)).bounds(centerX - 35, top + 104, 70, 22).build());
      this.addRenderableWidget(Button.builder(Component.literal("+ 0.1"), button -> this.changeSize(0.1F)).bounds(centerX + 45, top + 104, 70, 22).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.gui.setScreen(new MoonVisualSettingsScreen()))
            .bounds(buttonX, top + 146, buttonWidth, 22)
            .build()
      );
   }

   private void togglePlayerSize() {
      MoonConfig.playerSizeEnabled = !MoonConfig.playerSizeEnabled;
      MoonConfig.save();
      this.clearWidgets();
      this.init();
   }

   private void changeSize(float amount) {
      this.setSize(MoonConfig.playerSize + amount);
   }

   private void setSize(float size) {
      MoonConfig.playerSize = MoonConfig.clampSize(size);
      MoonConfig.save();
   }

   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      MoonScreenStyle.drawPanel(graphics, this.width / 2, top, 340, PANEL_HEIGHT);
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      graphics.centeredText(this.font, this.title, centerX, top + 14, -1);
      graphics.centeredText(this.font, "Current size: " + String.format(Locale.ROOT, "%.1fx", MoonConfig.playerSize), centerX, top + 45, -4799016);
      graphics.centeredText(this.font, "Changes the rendered size of your player model.", centerX, top + 224, -7891030);
   }
}
