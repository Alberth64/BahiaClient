package dev.zeya.fabricmoon.hud;

import dev.zeya.fabricmoon.MoonConfig;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class MoonHudPositionScreen extends Screen {
   private static final int STATS_WIDTH = 115;
   private static final int STATS_HEIGHT = 44;
   private static final int COMBAT_WIDTH = 210;
   private static final int FOOTER_HEIGHT = 34;
   private MoonHudPositionScreen.DragTarget dragging = MoonHudPositionScreen.DragTarget.NONE;
   private double dragOffsetX;
   private double dragOffsetY;

   public MoonHudPositionScreen() {
      super(Component.literal("Move HUD"));
   }

   protected void init() {
      this.addRenderableWidget(
         Button.builder(Component.literal("Reset Positions"), button -> this.resetPositions())
            .bounds(this.width / 2 - 104, this.height - 30, 128, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(this.width / 2 + 28, this.height - 30, 76, 20).build()
      );
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (event.button() == 0 && event.y() < this.height - 34) {
         int statsX = MoonHud.getStatsX(this.width);
         int statsY = MoonHud.getStatsY(this.height - 34);
         if (contains(event.x(), event.y(), statsX - 3, statsY - 3, STATS_WIDTH, STATS_HEIGHT)) {
            this.dragging = MoonHudPositionScreen.DragTarget.STATS;
            this.dragOffsetX = event.x() - MoonConfig.statsHudX;
            this.dragOffsetY = event.y() - statsY;
            return true;
         }

         if (MoonConfig.combatHudShowTargetHealth) {
            int combatX = MoonHud.getCombatX(this.width);
            int combatY = MoonHud.getCombatY(this.height - 34);
            int combatHeight = MoonHud.getCombatPanelHeight();
            if (contains(event.x(), event.y(), combatX - 4, combatY - 4, 210, combatHeight)) {
               this.dragging = MoonHudPositionScreen.DragTarget.COMBAT;
               this.dragOffsetX = event.x() - combatX;
               this.dragOffsetY = event.y() - combatY;
               return true;
            }
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      if (this.dragging != MoonHudPositionScreen.DragTarget.NONE && event.button() == 0) {
         int availableHeight = Math.max(0, this.height - 34);
         if (this.dragging == MoonHudPositionScreen.DragTarget.STATS) {
            MoonConfig.statsHudX = Mth.clamp((int)Math.round(event.x() - this.dragOffsetX), 0, Math.max(0, this.width - 115));
            MoonConfig.statsHudY = Mth.clamp((int)Math.round(event.y() - this.dragOffsetY), 0, Math.max(0, availableHeight - STATS_HEIGHT));
         } else {
            MoonConfig.combatHudX = Mth.clamp((int)Math.round(event.x() - this.dragOffsetX), 0, Math.max(0, this.width - 210));
            MoonConfig.combatHudY = Mth.clamp((int)Math.round(event.y() - this.dragOffsetY), 0, Math.max(0, availableHeight - MoonHud.getCombatPanelHeight()));
         }

         return true;
      } else {
         return super.mouseDragged(event, deltaX, deltaY);
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (event.button() == 0 && this.dragging != MoonHudPositionScreen.DragTarget.NONE) {
         this.dragging = MoonHudPositionScreen.DragTarget.NONE;
         MoonConfig.save();
         return true;
      } else {
         return super.mouseReleased(event);
      }
   }

   private void resetPositions() {
      MoonConfig.statsHudX = 4;
      MoonConfig.statsHudY = -1;
      MoonConfig.combatHudX = -1;
      MoonConfig.combatHudY = 8;
      MoonConfig.save();
   }

   private static boolean contains(double x, double y, int left, int top, int width, int height) {
      return x >= left && x <= left + width && y >= top && y <= top + height;
   }

   public void onClose() {
      MoonConfig.save();
      super.onClose();
   }

   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      graphics.fill(0, 0, this.width, this.height, -1728053248);
      this.renderStatsPreview(graphics);
      if (MoonConfig.combatHudShowTargetHealth) {
         this.renderCombatPreview(graphics);
      }
   }

   private void renderStatsPreview(GuiGraphicsExtractor graphics) {
      int x = MoonHud.getStatsX(this.width);
      int y = MoonHud.getStatsY(this.height - 34);
      if (MoonConfig.hudBackgroundEnabled) {
         graphics.fill(x - 3, y - 3, x + 112, y + 41, MoonConfig.hudBackgroundColor);
      }
      graphics.fill(x - 4, y - 4, x + 113, y - 3, MoonConfig.hudAccentColor);
      graphics.text(this.font, "FPS: " + this.minecraft.getFps(), x, y, MoonConfig.hudTextColor);
      graphics.text(this.font, "TPS: " + formatTps(), x, y + 13, MoonConfig.hudTextColor);
      graphics.text(this.font, "Ping: --", x, y + 26, MoonConfig.hudTextColor);
   }

   private void renderCombatPreview(GuiGraphicsExtractor graphics) {
      int x = MoonHud.getCombatX(this.width);
      int y = MoonHud.getCombatY(this.height - 34);
      if (MoonConfig.hudBackgroundEnabled) {
         graphics.fill(x - 3, y - 3, x + 112, y + 29, MoonConfig.hudBackgroundColor);
      }
      graphics.text(this.font, "Target: Mob", x, y, MoonConfig.hudTextColor);
      graphics.text(this.font, "HP: 20 / 20", x, y + 12, MoonConfig.hudTextColor);
      graphics.fill(x, y + 24, x + 100, y + 27, MoonConfig.hudAccentColor);
   }

   private static String formatTps() {
      double tps = MoonServerStats.getEstimatedTps();
      return tps < 0.0 ? "--" : String.format(Locale.ROOT, "%.1f", tps);
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
      graphics.centeredText(this.font, "Drag panels to move them. Positions are saved when released.", this.width / 2, 24, -4799016);
   }

   private enum DragTarget {
      NONE,
      STATS,
      COMBAT;
   }
}
