package de.funboyy.stream.chat.config;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget.TextFieldSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

@ConfigName("settings")
public class StreamChatConfig extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @TextFieldSetting
  private final ConfigProperty<String> channel = new ConfigProperty<>("");

  private final TokenConfig tokenConfig = new TokenConfig();

  @SettingSection("message")
  @SwitchSetting
  private final ConfigProperty<Boolean> soundOnMessage = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showTwitchEmotes = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showSevenTvEmotes = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showBetterTtvEmotes = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showFrankerFaceZ = new ConfigProperty<>(true);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<String> channel() {
    return this.channel;
  }

  public TokenConfig tokenConfig() {
    return this.tokenConfig;
  }

  public ConfigProperty<Boolean> soundOnMessage() {
    return this.soundOnMessage;
  }

  public ConfigProperty<Boolean> showTwitchEmotes() {
    return this.showTwitchEmotes;
  }

  public ConfigProperty<Boolean> showSevenTvEmotes() {
    return this.showSevenTvEmotes;
  }

  public ConfigProperty<Boolean> showBetterTtvEmotes() {
    return this.showBetterTtvEmotes;
  }

  public ConfigProperty<Boolean> showFrankerFaceZ() {
    return this.showFrankerFaceZ;
  }

}
