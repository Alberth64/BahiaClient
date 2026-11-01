package dev.zeya.fabricmoon;

import dev.zeya.fabricmoon.hud.MoonHud;
import dev.zeya.fabricmoon.hud.MoonPingTracker;
import dev.zeya.fabricmoon.hud.MoonServerStats;
import dev.zeya.fabricmoon.inventory.MoonWardrobeQuickSwap;
import dev.zeya.fabricmoon.macros.MoonAutoClicker;
import dev.zeya.fabricmoon.macros.MoonAutoFishing;
import dev.zeya.fabricmoon.macros.MoonBlazeDaggerMacro;
import dev.zeya.fabricmoon.macros.MoonOxygenRecovery;
import dev.zeya.fabricmoon.ui.MoonSettingsScreen;
import dev.zeya.fabricmoon.visual.MoonHitboxRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ZeyaFabricMoonClient implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      MoonConfig.load();
      ClientTickEvents.START_CLIENT_TICK.register(MoonBlazeDaggerMacro::tick);
      ClientTickEvents.START_CLIENT_TICK.register(MoonAutoClicker::tick);
      ClientTickEvents.START_CLIENT_TICK.register(MoonServerStats::tick);
      ClientTickEvents.START_CLIENT_TICK.register(MoonWardrobeQuickSwap::tick);
      ClientTickEvents.END_CLIENT_TICK.register(MoonAutoFishing::tick);
      ClientTickEvents.END_CLIENT_TICK.register(MoonOxygenRecovery::tick);
      ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
         MoonAutoFishing.onChatMessage(message.getString());
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         MoonPingTracker.reset();
         MoonBlazeDaggerMacro.reset();
      });
      ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> MoonAutoFishing.onEntityLoad(entity));
      MoonHitboxRenderer.register();
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("zeyafabricmoon", "stats"), MoonHud::render);
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
         dispatcher.register(
            ClientCommands.literal("zeyafabric")
               .executes(context -> openSettings(((FabricClientCommandSource)context.getSource()).getClient()))
               .then(ClientCommands.literal("hud").executes(context -> openHudEditor(((FabricClientCommandSource)context.getSource()).getClient())))
         );
         dispatcher.register(
            ClientCommands.literal("z")
               .executes(context -> openSettings(((FabricClientCommandSource)context.getSource()).getClient()))
         );
      });
   }

   public static int openSettings(Minecraft client) {
      client.gui.setScreen(new MoonSettingsScreen());
      return 1;
   }

   public static int openHudEditor(Minecraft client) {
      client.gui.setScreen(new dev.zeya.fabricmoon.hud.MoonHudPositionScreen());
      return 1;
   }

   public static Component toggleLabel(String name, boolean enabled) {
      return Component.literal(name).withStyle(style -> style.withColor(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY));
   }
}
