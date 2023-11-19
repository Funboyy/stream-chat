package de.funboyy.stream.chat.config;

import de.funboyy.stream.chat.StreamChatAddon;
import de.funboyy.stream.chat.activity.TwitchTokenActivity;
import de.funboyy.stream.chat.utils.OAuthServer;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget.TextFieldSetting;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.notification.Notification;
import net.labymod.api.util.MethodOrder;

public class TokenConfig extends Config {

  @TextFieldSetting
  private final ConfigProperty<String> token = new ConfigProperty<>("");

  @SuppressWarnings("unused")
  @MethodOrder(after = "token")
  @ButtonSetting
  public void authorize(final Setting setting) {
    Laby.labyAPI().minecraft().minecraftWindow()
        .displayScreen(new TwitchTokenActivity(StreamChatAddon.getInstance()));
  }

  @SuppressWarnings("unused")
  @MethodOrder(after = "authorize")
  @ButtonSetting
  public void revoke(final Setting setting) {
    final StreamChatAddon addon = StreamChatAddon.getInstance();

    addon.async(() -> {
      final Notification.Builder builder = Notification.builder()
          .icon(Icon.texture(ResourceLocation.create(addon.addonInfo().getNamespace(),
              "textures/icon.png"))).title(Component.text(addon.addonInfo().getDisplayName()));

      if (!OAuthServer.revoke()) {
        builder.text(Component.translatable(String.format("%s.notification.revoke.error",
                addon.addonInfo().getNamespace()), NamedTextColor.RED));
        builder.buildAndPush();
        return;
      }

      this.token().set("");
      addon.saveConfiguration();
      addon.stopTwitch();

      builder.text(Component.translatable(String.format("%s.notification.revoke.success",
          addon.addonInfo().getNamespace()), NamedTextColor.GREEN));
      builder.buildAndPush();
    });
  }

  public ConfigProperty<String> token() {
    return this.token;
  }

}
