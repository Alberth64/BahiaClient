package dev.bahiaclient.macros;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.BahiaClient;
import dev.bahiaclient.ui.MoonScreenStyle;
import dev.bahiaclient.ui.MoonSettingsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoonMacrosSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 600;
   private static final int PANEL_HEIGHT = 340;

   public MoonMacrosSettingsScreen() {
      super(Component.literal("Macros"));
   }

   @Override
   protected void init() {
      int centerX = this.width / 2;
      int top = MoonScreenStyle.panelTop(this.height, PANEL_HEIGHT);
      int buttonWidth = 270;
      int leftX = centerX - 300 + 20;
      int rightX = centerX + 10;
      this.addToggle("AutoClick", MoonConfig.leftAutoClick, () -> MoonConfig.leftAutoClick = !MoonConfig.leftAutoClick, leftX, top + 48);
      this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeCps(-1)).bounds(leftX + 54, top + 78, 42, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeCps(1)).bounds(leftX + 174, top + 78, 42, 20).build());

      this.addToggle("Auto Fishing", MoonConfig.autoFishing, () -> MoonConfig.autoFishing = !MoonConfig.autoFishing, leftX, top + 124);
      this.addToggle("Oxygen Recovery", MoonConfig.oxygenRecovery, () -> MoonConfig.oxygenRecovery = !MoonConfig.oxygenRecovery, leftX, top + 154);
      this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeOxygenThreshold(-10)).bounds(leftX + 54, top + 184, 42, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeOxygenThreshold(10)).bounds(leftX + 174, top + 184, 42, 20).build());

      this.addToggle("Blaze Dagger: auto-switch", MoonConfig.blazeDaggerAutoSwitch, () -> MoonConfig.blazeDaggerAutoSwitch = !MoonConfig.blazeDaggerAutoSwitch, rightX, top + 124);
      this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeBlazeDaggerSlot(-1)).bounds(rightX + 54, top + 154, 42, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeBlazeDaggerSlot(1)).bounds(rightX + 174, top + 154, 42, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeBlazeDaggerDelay(-25)).bounds(rightX + 54, top + 184, 42, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeBlazeDaggerDelay(25)).bounds(rightX + 174, top + 184, 42, 20).build());

      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.gui.setScreen(new MoonSettingsScreen()))
            .bounds(centerX - 280, top + 302, 560, 22)
            .build()
      );
   }

   private void addToggle(String label, boolean enabled, Runnable toggle, int x, int y) {
      this.addRenderableWidget(Button.builder(BahiaClient.toggleLabel(label, enabled), button -> {
         toggle.run();
         this.saveAndRefresh();
      }).bounds(x, y, 270, 22).build());
   }

   private void changeCps(int amount) {
      MoonConfig.leftCps = MoonConfig.clampCps(MoonConfig.leftCps + amount);
      this.saveAndRefresh();
   }

   private void changeOxygenThreshold(int amount) {
      MoonConfig.oxygenThreshold = MoonConfig.clampOxygen(MoonConfig.oxygenThreshold + amount);
      this.saveAndRefresh();
   }

   private void changeBlazeDaggerSlot(int amount) {
      MoonConfig.blazeDaggerSlot = MoonConfig.clampHotbarSlot(MoonConfig.blazeDaggerSlot + amount);
      this.saveAndRefresh();
   }

   private void changeBlazeDaggerDelay(int amount) {
      MoonConfig.blazeDaggerSwapDelayMs = MoonConfig.clampSwapDelay(MoonConfig.blazeDaggerSwapDelayMs + amount);
      this.saveAndRefresh();
   }

   private void saveAndRefresh() {
      MoonConfig.save();
      this.clearWidgets();
      this.init();
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
      graphics.centeredText(this.font, "AutoClick: " + MoonConfig.leftCps + " CPS", centerX - 145, top + 105, MoonScreenStyle.mutedColor());
      graphics.centeredText(this.font, "Air threshold: " + MoonConfig.oxygenThreshold, centerX - 145, top + 190, MoonScreenStyle.mutedColor());
      graphics.centeredText(this.font, "Hotbar Slot: " + MoonConfig.blazeDaggerSlot, centerX + 155, top + 160, MoonScreenStyle.mutedColor());
      graphics.centeredText(this.font, "Swap Delay: " + MoonConfig.blazeDaggerSwapDelayMs + " ms", centerX + 155, top + 190, MoonScreenStyle.mutedColor());
   }
}
