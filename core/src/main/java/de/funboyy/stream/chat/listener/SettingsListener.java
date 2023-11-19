package de.funboyy.stream.chat.listener;

import com.github.twitch4j.chat.TwitchChat;
import de.funboyy.stream.chat.StreamChatAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.labymod.config.ConfigurationSaveEvent;

public class SettingsListener {

  private final StreamChatAddon addon;

  public SettingsListener(final StreamChatAddon addon) {
    this.addon = addon;
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void onSave(final ConfigurationSaveEvent event) {
    this.addon.async(() -> {
      this.addon.restartTwitch();

      updateChannel();
    });
  }

  private void updateChannel() {
    if (this.addon.getTwitch() == null || this.addon.getSender() == null) {
      return;
    }

    final TwitchChat chat = this.addon.getTwitch().getChat();
    chat.getChannels().forEach(chat::leaveChannel);

    final TwitchChat senderChat = this.addon.getSender().getChat();
    senderChat.getChannels().forEach(senderChat::leaveChannel);

    final String channel = this.addon.configuration().channel().get();

    if (!channel.isBlank()) {
      chat.joinChannel(channel);
      senderChat.joinChannel(channel);

      this.addon.getResources().updateChannel(channel);
    }
  }

}
