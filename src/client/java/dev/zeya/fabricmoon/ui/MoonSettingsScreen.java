package dev.zeya.fabricmoon.ui;

import dev.zeya.fabricmoon.MoonConfig;
import dev.zeya.fabricmoon.ZeyaFabricMoonClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class MoonSettingsScreen extends Screen {
   private static final String[] CATEGORIES = {"HUD", "VISUAL", "MACROS", "UTILS"};
   private static final int PANEL_GAP = 8;
   private static final int HEADER_HEIGHT = 22;
   private static final int ROW_HEIGHT = 20;
   private static final int SETTINGS_ROW_HEIGHT = 24;
   private static final int MAX_CONTENT_HEIGHT = 350;
   private static final int[] PANEL_X = {-1, -1, -1, -1};
   private static final int[] PANEL_Y = {30, 30, 30, 30};
   private static final boolean[] COLLAPSED = {false, false, false, false};

   private int draggingCategory = -1;
   private int dragOffsetX;
   private int dragOffsetY;
   private int waitingForLoadoutKey = -1;
   private String detailMessage;
   private String openConfig;
   private boolean loadoutSettingsExpanded = true;

   public MoonSettingsScreen() {
      super(Component.literal("ZEYA MOON"));
   }

   @Override
   protected void init() {
      if (PANEL_X[0] < 0) {
         int panelWidth = this.panelWidth();
         int totalWidth = CATEGORIES.length * panelWidth + (CATEGORIES.length - 1) * PANEL_GAP;
         int left = Math.max(0, (this.width - totalWidth) / 2);
         for (int index = 0; index < CATEGORIES.length; index++) {
            PANEL_X[index] = left + index * (panelWidth + PANEL_GAP);
         }
      }

      this.clampPanels();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.openConfig != null && this.insideSettingsPanel(event.x(), event.y())) {
         if (event.button() == 0 && this.closeButtonContains(event.x(), event.y())) {
            this.openConfig = null;
            this.waitingForLoadoutKey = -1;
            return true;
         }
         int row = this.settingsRowAt(event.x(), event.y());
         if (event.button() == 0 && row >= 0) {
            this.activateSettingsRow(row, event.x());
         }
         return true;
      }
      if (this.openConfig != null) {
         this.openConfig = null;
         this.waitingForLoadoutKey = -1;
      }

      int category = this.categoryAtHeader(event.x(), event.y());
      if (category >= 0) {
         if (event.button() == 1) {
            COLLAPSED[category] = !COLLAPSED[category];
         } else if (event.button() == 0) {
            this.draggingCategory = category;
            this.dragOffsetX = (int)event.x() - PANEL_X[category];
            this.dragOffsetY = (int)event.y() - PANEL_Y[category];
         }
         return true;
      }

      int[] target = this.featureAt(event.x(), event.y());
      if (target != null) {
         FeatureRow row = this.rows(target[0]).get(target[1]);
         if (event.button() == 0 && row.toggle() != null) {
            row.toggle().run();
            MoonConfig.save();
         } else if (event.button() == 0 || event.button() == 1) {
            this.openConfig = row.id();
            this.detailMessage = row.description();
         }
         return true;
      }

      return super.mouseClicked(event, doubleClick);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      if (this.draggingCategory >= 0 && event.button() == 0) {
         int index = this.draggingCategory;
         PANEL_X[index] = (int)event.x() - this.dragOffsetX;
         PANEL_Y[index] = (int)event.y() - this.dragOffsetY;
         this.clampPanel(index);
         return true;
      }
      return super.mouseDragged(event, deltaX, deltaY);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (event.button() == 0) {
         this.draggingCategory = -1;
      }
      return super.mouseReleased(event);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.waitingForLoadoutKey >= 0) {
         MoonConfig.wardrobeQuickSwapKeysLoadout[this.waitingForLoadoutKey] = event.key() == 256 ? -1 : event.key();
         MoonConfig.save();
         this.waitingForLoadoutKey = -1;
         return true;
      }
      if (event.key() == 256 && this.openConfig != null) {
         this.openConfig = null;
         return true;
      }
      return super.keyPressed(event);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.openConfig != null && this.insideSettingsPanel(mouseX, mouseY)) {
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(graphics, mouseX, mouseY, partialTick);
      graphics.fill(0, 0, this.width, this.height, 0x96000000);

      for (int category = 0; category < CATEGORIES.length; category++) {
         if (category != this.draggingCategory) {
            this.drawCategory(graphics, category, mouseX, mouseY);
         }
      }
      if (this.draggingCategory >= 0) {
         this.drawCategory(graphics, this.draggingCategory, mouseX, mouseY);
      }

      int[] hovered = this.featureAt(mouseX, mouseY);
      String description = hovered == null
         ? this.detailMessage == null ? "Left-click: toggle | Right-click: settings | Drag categories; right-click a title to collapse." : this.detailMessage
         : this.rows(hovered[0]).get(hovered[1]).description();
      this.drawDescription(graphics, description);
      if (this.openConfig != null) {
         this.drawSettingsPanel(graphics, mouseX, mouseY);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
   }

   private void drawCategory(GuiGraphicsExtractor graphics, int category, int mouseX, int mouseY) {
      int x = PANEL_X[category];
      int y = PANEL_Y[category];
      int width = this.panelWidth();
      List<FeatureRow> rows = this.rows(category);
      int contentHeight = Math.min(rows.size() * ROW_HEIGHT, MAX_CONTENT_HEIGHT);
      boolean headerHovered = inside(mouseX, mouseY, x, y, width, HEADER_HEIGHT);

      graphics.fill(x - 2, y - 2, x + width + 2, y + HEADER_HEIGHT + (COLLAPSED[category] ? 0 : contentHeight) + 2, 0x4D000000);
      graphics.fill(x, y, x + width, y + HEADER_HEIGHT, 0xE6141414);
      graphics.fill(x, y, x + width, y + 2, 0xFF00E6DC);
      graphics.centeredText(this.font, CATEGORIES[category], x + width / 2, y + 7, headerHovered ? 0xFF00F2E6 : 0xFFFFFFFF);

      if (COLLAPSED[category]) {
         return;
      }

      for (int index = 0; index < rows.size(); index++) {
         FeatureRow row = rows.get(index);
         int top = y + HEADER_HEIGHT + index * ROW_HEIGHT;
         if (top + ROW_HEIGHT > y + HEADER_HEIGHT + MAX_CONTENT_HEIGHT) {
            break;
         }
         boolean hovered = inside(mouseX, mouseY, x, top, width, ROW_HEIGHT);
         graphics.fill(x, top, x + width, top + ROW_HEIGHT, 0xD10F0F0F);
         if (row.enabled() != null && row.enabled().getAsBoolean()) {
            graphics.fill(x, top, x + width, top + ROW_HEIGHT, 0x6400D8D0);
            graphics.fill(x, top, x + 2, top + ROW_HEIGHT, 0xFF00E6DC);
         }
         if (hovered) {
            graphics.fill(x, top, x + width, top + ROW_HEIGHT, 0x38FFFFFF);
         }

         graphics.text(this.font, row.label(), x + 7, top + 6, 0xFFFFFFFF);
         if (row.enabled() == null) {
            graphics.text(this.font, "›", x + width - this.font.width("›") - 6, top + 6, 0xFF00E6DC);
         }
      }
   }

   private void drawSettingsPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      List<ConfigRow> rows = this.settingsRows();
      int width = Math.min(310, this.width - 20);
      int height = Math.min(this.height - 44, 38 + rows.size() * SETTINGS_ROW_HEIGHT);
      int left = (this.width - width) / 2;
      int top = Math.max(22, (this.height - height) / 2);

      graphics.fill(left - 3, top - 3, left + width + 3, top + height + 3, 0x80000000);
      graphics.fill(left, top, left + width, top + height, 0xF00A0A0A);
      graphics.fill(left, top, left + width, top + 3, 0xFF2196E1);
      graphics.fill(left, top + 3, left + width, top + 29, 0xF0191919);
      graphics.centeredText(this.font, this.configTitle(), this.width / 2, top + 10, 0xFFFFFFFF);
      graphics.fill(left + width - 25, top + 6, left + width - 7, top + 23, 0x402196E1);
      graphics.centeredText(this.font, "x", left + width - 16, top + 9, 0xFFFFFFFF);

      for (int index = 0; index < rows.size(); index++) {
         ConfigRow row = rows.get(index);
         int rowTop = top + 31 + index * SETTINGS_ROW_HEIGHT;
         if (rowTop + SETTINGS_ROW_HEIGHT > top + height - 5) {
            break;
         }
         boolean hovered = inside(mouseX, mouseY, left + 6, rowTop, width - 12, SETTINGS_ROW_HEIGHT);
         graphics.fill(left + 6, rowTop, left + width - 6, rowTop + SETTINGS_ROW_HEIGHT - 1, hovered ? 0x302196E1 : 0x18000000);
         graphics.text(this.font, row.label(), left + 12, rowTop + 7, 0xFFFFFFFF);
         this.drawConfigValue(graphics, row, left, rowTop, width, index);
      }

   }

   private void drawConfigValue(GuiGraphicsExtractor graphics, ConfigRow row, int left, int top, int width, int index) {
      int right = left + width - 12;
      int centerY = top + SETTINGS_ROW_HEIGHT / 2;
      switch (row.kind()) {
         case TOGGLE -> {
            int buttonWidth = 28;
            int x = right - buttonWidth;
            boolean enabled = row.enabled().getAsBoolean();
            graphics.fill(x, centerY - 7, right, centerY + 7, enabled ? 0xFF00B8AD : 0xFF363636);
            int indicatorX = enabled ? right - 11 : x + 3;
            graphics.fill(indicatorX, centerY - 5, indicatorX + 8, centerY + 5, 0xFFFFFFFF);
         }
         case ACTION, KEY -> {
            String value = row.value().get();
            int buttonWidth = Math.max(64, Math.min(112, this.font.width(value) + 18));
            int x = right - buttonWidth;
            graphics.fill(x, centerY - 9, right, centerY + 9, 0xFF101010);
            graphics.fill(x, centerY - 9, right, centerY - 7, 0xFF2196E1);
            graphics.fill(x, centerY + 7, right, centerY + 9, 0xFF2196E1);
            graphics.fill(x, centerY - 9, x + 2, centerY + 9, 0xFF2196E1);
            graphics.fill(right - 2, centerY - 9, right, centerY + 9, 0xFF2196E1);
            graphics.centeredText(this.font, value, x + buttonWidth / 2, centerY - 4, 0xFFFFFFFF);
         }
         case COLOR -> {
            int swatchX = right - 28;
            graphics.fill(swatchX, centerY - 9, right, centerY + 9, 0xFF222222);
            graphics.fill(swatchX + 3, centerY - 6, right - 3, centerY + 6, 0xFF000000 | row.color().getAsInt() & 0xFFFFFF);
         }
         case STEPPER -> {
            int plusX = right - 22;
            int valueLeft = plusX - 56;
            int minusX = valueLeft - 22;
            graphics.fill(minusX, centerY - 9, minusX + 20, centerY + 9, 0xFF2196E1);
            graphics.fill(valueLeft, centerY - 9, plusX - 2, centerY + 9, 0xFF171717);
            graphics.fill(plusX, centerY - 9, right, centerY + 9, 0xFF2196E1);
            graphics.centeredText(this.font, "-", minusX + 10, centerY - 4, 0xFFFFFFFF);
            graphics.centeredText(this.font, "+", plusX + 10, centerY - 4, 0xFFFFFFFF);
            graphics.centeredText(this.font, row.value().get(), (valueLeft + plusX - 2) / 2, centerY - 4, 0xFFFFFFFF);
         }
         case HEADER -> {
            String value = this.loadoutSettingsExpanded ? "v" : ">";
            graphics.centeredText(this.font, value, right - 10, centerY - 4, 0xFF55B9F2);
         }
         case INFO -> {
            graphics.text(this.font, row.value().get(), right - this.font.width(row.value().get()), centerY - 4, 0xFFAAAAAA);
         }
      }
   }

   private void drawDescription(GuiGraphicsExtractor graphics, String description) {
      int width = Math.min(520, this.width - 24);
      int left = (this.width - width) / 2;
      int top = this.height - 34;
      String text = description;
      while (!text.isEmpty() && this.font.width(text) > width - 18) {
         text = text.substring(0, text.length() - 1);
      }
      if (!text.equals(description) && text.length() > 3) {
         text = text.substring(0, text.length() - 3) + "...";
      }
      graphics.fill(left, top, left + width, top + 22, 0xC40B0B0B);
      graphics.fill(left, top, left + 2, top + 22, 0xFF00E6DC);
      graphics.centeredText(this.font, text, this.width / 2, top + 7, 0xFFE0E0E0);
   }

   private List<FeatureRow> rows(int category) {
      return switch (category) {
         case 0 -> List.of(
            feature("FPS / Ping / TPS", "Shows performance stats. Configure colors, background, visibility, and position.", "hud", () -> MoonConfig.showHud, () -> MoonConfig.showHud = !MoonConfig.showHud),
            feature("Enemy Info", "Shows target information and lets you reposition its panel.", "enemy-info", () -> MoonConfig.combatHudShowTargetHealth, () -> MoonConfig.combatHudShowTargetHealth = !MoonConfig.combatHudShowTargetHealth)
         );
         case 1 -> List.of(
            feature("Fullbright", "Brightens the local game view.", "fullbright", () -> MoonConfig.fullbright, () -> MoonConfig.fullbright = !MoonConfig.fullbright),
            feature("Hide Armor", "Hide selected armor pieces and custom helmet models.", "hide-armor", () -> MoonConfig.hideArmor, () -> MoonConfig.hideArmor = !MoonConfig.hideArmor),
            feature("Mob Hitboxes", "Draws entity hitboxes without changing collisions.", "hitboxes", () -> MoonConfig.entityHitboxes, () -> MoonConfig.entityHitboxes = !MoonConfig.entityHitboxes),
            feature("Player Size", "Adjust the visual player scale.", "player-size", () -> MoonConfig.playerSizeEnabled, () -> MoonConfig.playerSizeEnabled = !MoonConfig.playerSizeEnabled),
            feature("Player Glow", "Toggle the player outline and choose its color.", "player-glow", () -> MoonConfig.playerGlow, () -> MoonConfig.playerGlow = !MoonConfig.playerGlow)
         );
         case 2 -> List.of(
            feature("AutoClick", "Automatically clicks the left mouse button while held, with an adjustable CPS.", "auto-click",
               () -> MoonConfig.leftAutoClick, () -> MoonConfig.leftAutoClick = !MoonConfig.leftAutoClick),
            feature("Auto Fishing", "Automatically casts and reels in the fishing rod; configure delays and Golden Fish handling.", "fishing", () -> MoonConfig.autoFishing, () -> MoonConfig.autoFishing = !MoonConfig.autoFishing),
            feature("Oxygen Recovery", "Swims upward when air falls below the configured threshold.", "oxygen", () -> MoonConfig.oxygenRecovery, () -> MoonConfig.oxygenRecovery = !MoonConfig.oxygenRecovery),
            feature("Blaze Dagger Swap", "Matches the dagger and attunement to the shield of the mob you are targeting.", "blaze-dagger", () -> MoonConfig.blazeDaggerAutoSwitch, () -> MoonConfig.blazeDaggerAutoSwitch = !MoonConfig.blazeDaggerAutoSwitch),
            feature("Loadout Keybinds", "Bind a key to each loadout and press it in-game to equip it.", "loadout", () -> MoonConfig.wardrobeQuickSwapEnabled, () -> MoonConfig.wardrobeQuickSwapEnabled = !MoonConfig.wardrobeQuickSwapEnabled)
         );
         default -> List.of();
      };
   }

   private List<ConfigRow> settingsRows() {
      List<ConfigRow> rows = new ArrayList<>();
      switch (this.openConfig) {
         case "hud" -> {
            rows.add(toggleSetting("Show HUD", () -> MoonConfig.showHud, () -> MoonConfig.showHud = !MoonConfig.showHud));
            rows.add(toggleSetting("Enemy Info", () -> MoonConfig.combatHudShowTargetHealth, () -> MoonConfig.combatHudShowTargetHealth = !MoonConfig.combatHudShowTargetHealth));
            rows.add(toggleSetting("Background", () -> MoonConfig.hudBackgroundEnabled, () -> MoonConfig.hudBackgroundEnabled = !MoonConfig.hudBackgroundEnabled));
            rows.add(colorSetting("Text Color", () -> MoonConfig.hudTextColor, () -> this.openColorPicker("Text Color", MoonConfig.hudTextColor, color -> MoonConfig.hudTextColor = 0xFF000000 | color)));
            rows.add(colorSetting("Accent Color", () -> MoonConfig.hudAccentColor, () -> this.openColorPicker("Accent Color", MoonConfig.hudAccentColor, color -> MoonConfig.hudAccentColor = 0xFF000000 | color)));
            rows.add(colorSetting("Background Color", () -> MoonConfig.hudBackgroundColor, () -> this.openColorPicker("Background Color", MoonConfig.hudBackgroundColor, color -> MoonConfig.hudBackgroundColor = (MoonConfig.hudBackgroundColor & 0xFF000000) | color)));
            rows.add(actionSetting("Move HUD", () -> ZeyaFabricMoonClient.openHudEditor(this.minecraft)));
         }
         case "enemy-info" -> {
            rows.add(toggleSetting("Show Enemy Info", () -> MoonConfig.combatHudShowTargetHealth, () -> MoonConfig.combatHudShowTargetHealth = !MoonConfig.combatHudShowTargetHealth));
            rows.add(actionSetting("Move HUD", () -> ZeyaFabricMoonClient.openHudEditor(this.minecraft)));
         }
         case "fullbright" -> rows.add(toggleSetting("Enabled", () -> MoonConfig.fullbright, () -> MoonConfig.fullbright = !MoonConfig.fullbright));
         case "hide-armor" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.hideArmor, () -> MoonConfig.hideArmor = !MoonConfig.hideArmor));
            rows.add(toggleSetting("Helmet / Head", () -> MoonConfig.hideHelmet, () -> MoonConfig.hideHelmet = !MoonConfig.hideHelmet));
            rows.add(toggleSetting("Chestplate", () -> MoonConfig.hideChestplate, () -> MoonConfig.hideChestplate = !MoonConfig.hideChestplate));
            rows.add(toggleSetting("Leggings", () -> MoonConfig.hideLeggings, () -> MoonConfig.hideLeggings = !MoonConfig.hideLeggings));
            rows.add(toggleSetting("Boots", () -> MoonConfig.hideBoots, () -> MoonConfig.hideBoots = !MoonConfig.hideBoots));
         }
         case "hitboxes" -> rows.add(toggleSetting("Enabled", () -> MoonConfig.entityHitboxes, () -> MoonConfig.entityHitboxes = !MoonConfig.entityHitboxes));
         case "player-size" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.playerSizeEnabled, () -> MoonConfig.playerSizeEnabled = !MoonConfig.playerSizeEnabled));
            rows.add(stepperSetting("Size", () -> String.format(Locale.ROOT, "%.1fx", MoonConfig.playerSize),
               () -> this.changePlayerSize(-0.1F), () -> this.changePlayerSize(0.1F)));
         }
         case "player-glow" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.playerGlow, () -> MoonConfig.playerGlow = !MoonConfig.playerGlow));
            rows.add(colorSetting("Glow Color", () -> MoonConfig.playerGlowColor,
               () -> this.openColorPicker("Player Glow Color", MoonConfig.playerGlowColor, color -> MoonConfig.playerGlowColor = color)));
         }
         case "auto-click" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.leftAutoClick, () -> MoonConfig.leftAutoClick = !MoonConfig.leftAutoClick));
            rows.add(stepperSetting("Clicks per second", () -> MoonConfig.leftCps + " CPS",
               () -> MoonConfig.leftCps = MoonConfig.clampCps(MoonConfig.leftCps - 1),
               () -> MoonConfig.leftCps = MoonConfig.clampCps(MoonConfig.leftCps + 1)));
         }
         case "fishing" -> {
            rows.add(toggleSetting("Auto Fishing", () -> MoonConfig.autoFishing, () -> MoonConfig.autoFishing = !MoonConfig.autoFishing));
            rows.add(stepperSetting("Cast Delay (ticks)", () -> Integer.toString(MoonConfig.fishingCastDelayTicks),
               () -> MoonConfig.fishingCastDelayTicks = MoonConfig.clampDelay(MoonConfig.fishingCastDelayTicks - 1),
               () -> MoonConfig.fishingCastDelayTicks = MoonConfig.clampDelay(MoonConfig.fishingCastDelayTicks + 1)));
            rows.add(stepperSetting("Reel Delay (ticks)", () -> Integer.toString(MoonConfig.fishingReelDelayTicks),
               () -> MoonConfig.fishingReelDelayTicks = MoonConfig.clampDelay(MoonConfig.fishingReelDelayTicks - 1),
               () -> MoonConfig.fishingReelDelayTicks = MoonConfig.clampDelay(MoonConfig.fishingReelDelayTicks + 1)));
            rows.add(stepperSetting("Min Fish Wait Delay (seconds)", () -> Integer.toString(MoonConfig.minFishWaitSeconds),
               () -> MoonConfig.minFishWaitSeconds = MoonConfig.clampSeconds(MoonConfig.minFishWaitSeconds - 1),
               () -> MoonConfig.minFishWaitSeconds = MoonConfig.clampSeconds(MoonConfig.minFishWaitSeconds + 1)));
            rows.add(toggleSetting("Disable While In Screen", () -> MoonConfig.disableFishingWhileOnScreen,
               () -> MoonConfig.disableFishingWhileOnScreen = !MoonConfig.disableFishingWhileOnScreen));
            rows.add(toggleSetting("Auto Golden Fish", () -> MoonConfig.autoGoldenFish, () -> MoonConfig.autoGoldenFish = !MoonConfig.autoGoldenFish));
            rows.add(stepperSetting("Golden Fish Action Delay (ticks)", () -> Integer.toString(MoonConfig.goldenFishActionDelayTicks),
               () -> MoonConfig.goldenFishActionDelayTicks = MoonConfig.clampDelay(MoonConfig.goldenFishActionDelayTicks - 1),
               () -> MoonConfig.goldenFishActionDelayTicks = MoonConfig.clampDelay(MoonConfig.goldenFishActionDelayTicks + 1)));
         }
         case "oxygen" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.oxygenRecovery, () -> MoonConfig.oxygenRecovery = !MoonConfig.oxygenRecovery));
            rows.add(stepperSetting("Air threshold", () -> Integer.toString(MoonConfig.oxygenThreshold),
               () -> MoonConfig.oxygenThreshold = MoonConfig.clampOxygen(MoonConfig.oxygenThreshold - 10),
               () -> MoonConfig.oxygenThreshold = MoonConfig.clampOxygen(MoonConfig.oxygenThreshold + 10)));
         }
         case "blaze-dagger" -> {
            rows.add(toggleSetting("Auto-switch", () -> MoonConfig.blazeDaggerAutoSwitch, () -> MoonConfig.blazeDaggerAutoSwitch = !MoonConfig.blazeDaggerAutoSwitch));
            rows.add(stepperSetting("Hotbar Slot", () -> Integer.toString(MoonConfig.blazeDaggerSlot),
               () -> MoonConfig.blazeDaggerSlot = MoonConfig.clampHotbarSlot(MoonConfig.blazeDaggerSlot - 1),
               () -> MoonConfig.blazeDaggerSlot = MoonConfig.clampHotbarSlot(MoonConfig.blazeDaggerSlot + 1)));
            rows.add(stepperSetting("Swap Delay", () -> MoonConfig.blazeDaggerSwapDelayMs + " ms",
               () -> MoonConfig.blazeDaggerSwapDelayMs = MoonConfig.clampSwapDelay(MoonConfig.blazeDaggerSwapDelayMs - 25),
               () -> MoonConfig.blazeDaggerSwapDelayMs = MoonConfig.clampSwapDelay(MoonConfig.blazeDaggerSwapDelayMs + 25)));
         }
         case "loadout" -> {
            rows.add(toggleSetting("Enabled", () -> MoonConfig.wardrobeQuickSwapEnabled, () -> MoonConfig.wardrobeQuickSwapEnabled = !MoonConfig.wardrobeQuickSwapEnabled));
            rows.add(headerSetting("Show Settings", () -> this.loadoutSettingsExpanded = !this.loadoutSettingsExpanded));
            if (this.loadoutSettingsExpanded) {
               for (int index = 0; index < MoonConfig.wardrobeQuickSwapKeysLoadout.length; index++) {
                  int slot = index;
                  rows.add(keySetting("Loadout " + (index + 1), () -> this.waitingForLoadoutKey == slot
                     ? "Press key..." : keyName(MoonConfig.wardrobeQuickSwapKeysLoadout[slot]),
                     () -> this.waitingForLoadoutKey = slot));
               }
            }
         }
         default -> {
         }
      }
      return rows;
   }

   private void activateSettingsRow(int index, double mouseX) {
      List<ConfigRow> rows = this.settingsRows();
      if (index < 0 || index >= rows.size()) {
         return;
      }
      ConfigRow row = rows.get(index);
      if (row.kind() == ConfigKind.STEPPER) {
         int right = (this.width + Math.min(310, this.width - 20)) / 2 - 12;
         int plusX = right - 22;
         int valueLeft = plusX - 56;
         int minusX = valueLeft - 22;
         if (mouseX >= minusX && mouseX < valueLeft - 2) {
            row.decrease().run();
         } else if (mouseX >= plusX) {
            row.increase().run();
         }
      } else if (row.activate() != null) {
         row.activate().run();
      }
      if (row.kind() != ConfigKind.KEY && row.kind() != ConfigKind.ACTION && row.kind() != ConfigKind.COLOR) {
         MoonConfig.save();
      } else if (row.kind() == ConfigKind.KEY) {
         this.detailMessage = null;
      }
   }

   private void openColorPicker(String title, int initialColor, java.util.function.IntConsumer onApply) {
      this.minecraft.gui.setScreen(new MoonColorPickerScreen(title, initialColor, this, color -> {
         onApply.accept(color);
         MoonConfig.save();
      }));
   }

   private void changePlayerSize(float amount) {
      MoonConfig.playerSize = MoonConfig.clampSize(MoonConfig.playerSize + amount);
      MoonConfig.save();
   }

   private String configTitle() {
      for (int category = 0; category < CATEGORIES.length; category++) {
         for (FeatureRow row : this.rows(category)) {
            if (row.id().equals(this.openConfig)) {
               return row.label();
            }
         }
      }
      return "Settings";
   }

   private boolean insideSettingsPanel(double mouseX, double mouseY) {
      int width = Math.min(310, this.width - 20);
      int height = Math.min(this.height - 44, 38 + this.settingsRows().size() * SETTINGS_ROW_HEIGHT);
      int left = (this.width - width) / 2;
      int top = Math.max(22, (this.height - height) / 2);
      return inside(mouseX, mouseY, left, top, width, height);
   }

   private boolean closeButtonContains(double mouseX, double mouseY) {
      int width = Math.min(310, this.width - 20);
      int height = Math.min(this.height - 44, 38 + this.settingsRows().size() * SETTINGS_ROW_HEIGHT);
      int left = (this.width - width) / 2;
      int top = Math.max(22, (this.height - height) / 2);
      return inside(mouseX, mouseY, left + width - 25, top + 6, 18, 17);
   }

   private int settingsRowAt(double mouseX, double mouseY) {
      int width = Math.min(310, this.width - 20);
      int height = Math.min(this.height - 44, 38 + this.settingsRows().size() * SETTINGS_ROW_HEIGHT);
      int left = (this.width - width) / 2;
      int top = Math.max(22, (this.height - height) / 2);
      if (mouseY < top + 31) {
         return -1;
      }
      int row = (int)((mouseY - top - 31) / SETTINGS_ROW_HEIGHT);
      if (mouseX < left + 6 || mouseX > left + width - 6 || row < 0 || row >= this.settingsRows().size()) {
         return -1;
      }
      return row;
   }

   private int[] featureAt(double mouseX, double mouseY) {
      for (int category = CATEGORIES.length - 1; category >= 0; category--) {
         if (COLLAPSED[category]) {
            continue;
         }
         int x = PANEL_X[category];
         int y = PANEL_Y[category] + HEADER_HEIGHT;
         int width = this.panelWidth();
         List<FeatureRow> rows = this.rows(category);
         int visibleRows = Math.min(rows.size(), MAX_CONTENT_HEIGHT / ROW_HEIGHT);
         for (int index = 0; index < visibleRows; index++) {
            int rowY = y + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, x, rowY, width, ROW_HEIGHT)) {
               return new int[]{category, index};
            }
         }
      }
      return null;
   }

   private int categoryAtHeader(double mouseX, double mouseY) {
      for (int category = CATEGORIES.length - 1; category >= 0; category--) {
         if (inside(mouseX, mouseY, PANEL_X[category], PANEL_Y[category], this.panelWidth(), HEADER_HEIGHT)) {
            return category;
         }
      }
      return -1;
   }

   private int panelWidth() {
      return Math.min(150, Math.max(90, (this.width - 40 - PANEL_GAP * (CATEGORIES.length - 1)) / CATEGORIES.length));
   }

   private void clampPanels() {
      for (int index = 0; index < CATEGORIES.length; index++) {
         this.clampPanel(index);
      }
   }

   private void clampPanel(int index) {
      PANEL_X[index] = Math.max(0, Math.min(PANEL_X[index], this.width - this.panelWidth()));
      PANEL_Y[index] = Math.max(0, Math.min(PANEL_Y[index], this.height - HEADER_HEIGHT - 22));
   }

   private static FeatureRow feature(String label, String description, String id, BooleanSupplier enabled, Runnable toggle) {
      return new FeatureRow(label, description, id, enabled, toggle);
   }

   private static FeatureRow action(String label, String description, String id) {
      return new FeatureRow(label, description, id, null, null);
   }

   private static ConfigRow toggleSetting(String label, BooleanSupplier enabled, Runnable toggle) {
      return new ConfigRow(label, ConfigKind.TOGGLE, null, toggle, null, null, enabled, null);
   }

   private static ConfigRow actionSetting(String label, Runnable action) {
      return actionSetting(label, action, () -> "Open");
   }

   private static ConfigRow actionSetting(String label, Runnable action, Supplier<String> value) {
      return new ConfigRow(label, ConfigKind.ACTION, value, action, null, null, null, null);
   }

   private static ConfigRow stepperSetting(String label, Supplier<String> value, Runnable decrease, Runnable increase) {
      return new ConfigRow(label, ConfigKind.STEPPER, value, null, decrease, increase, null, null);
   }

   private static ConfigRow colorSetting(String label, IntSupplier color, Runnable action) {
      return new ConfigRow(label, ConfigKind.COLOR, null, action, null, null, null, color);
   }

   private static ConfigRow keySetting(String label, Supplier<String> value, Runnable action) {
      return new ConfigRow(label, ConfigKind.KEY, value, action, null, null, null, null);
   }

   private static ConfigRow headerSetting(String label, Runnable action) {
      return new ConfigRow(label, ConfigKind.HEADER, null, action, null, null, null, null);
   }

   private static String keyName(int keyCode) {
      if (keyCode < 0) {
         return "--";
      }
      if (keyCode >= 65 && keyCode <= 90 || keyCode >= 48 && keyCode <= 57) {
         return "\"" + (char)keyCode + "\"";
      }
      String name = switch (keyCode) {
         case 32 -> "Space";
         case 256 -> "Esc";
         case 257 -> "Enter";
         case 258 -> "Tab";
         case 259 -> "Backspace";
         case 261 -> "Delete";
         case 262 -> "Right Arrow";
         case 263 -> "Left Arrow";
         case 264 -> "Down Arrow";
         case 265 -> "Up Arrow";
         case 45 -> "Minus";
         case 61 -> "Equals";
         default -> "Key " + keyCode;
      };
      return "\"" + name + "\"";
   }

   private static boolean inside(double x, double y, int left, int top, int width, int height) {
      return x >= left && x <= left + width && y >= top && y <= top + height;
   }

   private record FeatureRow(String label, String description, String id, BooleanSupplier enabled, Runnable toggle) {
   }

   private record ConfigRow(String label, ConfigKind kind, Supplier<String> value, Runnable activate, Runnable decrease, Runnable increase, BooleanSupplier enabled, IntSupplier color) {
   }

   private enum ConfigKind {
      TOGGLE, ACTION, STEPPER, COLOR, KEY, HEADER, INFO
   }
}
