package dev.bahiaclient;

import dev.bahiaclient.ui.MoonSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class BahiaClientModMenu implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> new MoonSettingsScreen();
   }
}
