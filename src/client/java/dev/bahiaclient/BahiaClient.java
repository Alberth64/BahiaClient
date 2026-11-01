package dev.bahiaclient;

import dev.bahiaclient.hud.MoonHud;
import dev.bahiaclient.hud.MoonPingTracker;
import dev.bahiaclient.hud.MoonServerStats;
import dev.bahiaclient.inventory.MoonWardrobeQuickSwap;
import dev.bahiaclient.macros.MoonAutoClicker;
import dev.bahiaclient.macros.MoonAutoFishing;
import dev.bahiaclient.macros.MoonBlazeDaggerMacro;
import dev.bahiaclient.macros.MoonOxygenRecovery;
import dev.bahiaclient.ui.MoonSettingsScreen;
import dev.bahiaclient.visual.MoonHitboxRenderer;
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

public final class BahiaClient implements ClientModInitializer {
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
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("bahiaclient", "stats"), MoonHud::render);
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
         dispatcher.register(
            ClientCommands.literal("bahia")
               .executes(context -> openSettings(((FabricClientCommandSource)context.getSource()).getClient()))
               .then(ClientCommands.literal("hud").executes(context -> openHudEditor(((FabricClientCommandSource)context.getSource()).getClient())))
         );
         dispatcher.register(
            ClientCommands.literal("b")
               .executes(context -> openSettings(((FabricClientCommandSource)context.getSource()).getClient()))
         );
      });
   }

   public static int openSettings(Minecraft client) {
      client.gui.setScreen(new MoonSettingsScreen());
      return 1;
   }

   public static int openHudEditor(Minecraft client) {
      client.gui.setScreen(new dev.bahiaclient.hud.MoonHudPositionScreen());
      return 1;
   }

   public static Component toggleLabel(String name, boolean enabled) {
      return Component.literal(name).withStyle(style -> style.withColor(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY));
   }
}
