package dev.bahiaclient.macros;

import dev.bahiaclient.MoonConfig;
import dev.bahiaclient.MoonNetworkPolicy;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

public final class MoonBlazeDaggerMacro {
   private static final String[] TWILIGHT_IDS = {"HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"};
   private static final String[] FIREDUST_IDS = {"HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"};
   private static String activeTwilightShield = "spirit";
   private static String activeFiredustShield = "ashen";
   private static boolean waitingForShieldUpdate;
   private static long lastActionMillis;

   private MoonBlazeDaggerMacro() {
   }

   public static void tick(Minecraft client) {
      if (!MoonNetworkPolicy.allowsModServerActions()
         || !MoonConfig.featuresEnabled
         || !MoonConfig.blazeDaggerAutoSwitch
         || client.player == null
         || client.level == null
         || client.gameMode == null
         || client.gui.screen() != null
         || !(client.hitResult instanceof EntityHitResult hit)) {
         return;
      }

      Entity target = hit.getEntity();
      String shield = findShield(client, target);
      if (shield == null) {
         return;
      }

      String[] daggerIds = switch (shield) {
         case "spirit", "crystal" -> TWILIGHT_IDS;
         case "ashen", "auric" -> FIREDUST_IDS;
         default -> null;
      };
      if (daggerIds == null) {
         return;
      }

      int daggerSlot = findDaggerSlot(client, daggerIds);
      if (daggerSlot < 0) {
         return;
      }

      long now = System.currentTimeMillis();
      if (now - lastActionMillis < MoonConfig.blazeDaggerSwapDelayMs) {
         return;
      }

      if (client.player.getInventory().getSelectedSlot() != daggerSlot) {
         client.player.getInventory().setSelectedSlot(daggerSlot);
         lastActionMillis = now;
         return;
      }

      String activeShield = daggerIds == TWILIGHT_IDS ? activeTwilightShield : activeFiredustShield;
      if (!shield.equals(activeShield) && !waitingForShieldUpdate) {
         client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
         lastActionMillis = now;
         waitingForShieldUpdate = true;
         setActiveShield(shield);
      }
   }

   public static void onSubtitle(String text) {
      String normalized = normalize(text);
      for (String shield : new String[]{"spirit", "crystal", "ashen", "auric"}) {
         if (normalized.contains(shield)) {
            setActiveShield(shield);
            waitingForShieldUpdate = false;
            return;
         }
      }
   }

   public static void reset() {
      activeTwilightShield = "spirit";
      activeFiredustShield = "ashen";
      waitingForShieldUpdate = false;
      lastActionMillis = 0L;
   }

   private static String findShield(Minecraft client, Entity target) {
      AABB area = target.getBoundingBox().inflate(5.0);
      for (Entity entity : client.level.getEntities(target, area)) {
         if (entity instanceof ArmorStand && entity.distanceToSqr(target) <= 25.0) {
            Component nameComponent = entity.getName();
            String name = nameComponent != null ? normalize(nameComponent.getString()) : "";
            for (String shield : new String[]{"spirit", "crystal", "ashen", "auric"}) {
               if (name.contains(shield)) {
                  return shield;
               }
            }
         }
      }
      return null;
   }

   private static int findDaggerSlot(Minecraft client, String[] ids) {
      int preferredSlot = MoonConfig.clampHotbarSlot(MoonConfig.blazeDaggerSlot) - 1;
      if (hasAnyId(client.player.getInventory().getItem(preferredSlot), ids)) {
         return preferredSlot;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (hasAnyId(client.player.getInventory().getItem(slot), ids)) {
            return slot;
         }
      }
      return -1;
   }

   private static boolean hasAnyId(ItemStack stack, String[] ids) {
      String id = skyblockId(stack);
      for (String candidate : ids) {
         if (id.equals(candidate)) {
            return true;
         }
      }
      return false;
   }

   private static String skyblockId(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? "" : data.copyTag().getStringOr("id", "");
   }

   private static void setActiveShield(String shield) {
      if ("spirit".equals(shield) || "crystal".equals(shield)) {
         activeTwilightShield = shield;
      } else if ("ashen".equals(shield) || "auric".equals(shield)) {
         activeFiredustShield = shield;
      }
   }

   private static String normalize(String text) {
      return text.replaceAll("\\u00A7.", "").toLowerCase(Locale.ROOT).trim();
   }
}
