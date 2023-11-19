package de.funboyy.stream.chat.command;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.client.websocket.domain.WebsocketConnectionState;
import de.funboyy.stream.chat.StreamChatAddon;
import de.funboyy.stream.chat.api.TwitchColor;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;

public class TwitchChatCommand extends Command {

  private final StreamChatAddon addon;

  public TwitchChatCommand(final StreamChatAddon addon) {
    super("twitchchat", "tc");
    this.addon = addon;
  }

  @Override
  public boolean execute(final String prefix, final String[] arguments) {
    if (arguments.length == 0) {
      this.addon.displayMessage(Component.empty()
          .append(Component.text("[TWITCH] ", TwitchColor.TWITCH))
          .append(Component.translatable(String.format("%s.command.usage",
              this.addon.addonInfo().getNamespace()), NamedTextColor.RED)));
      return true;
    }

    final String channel = this.addon.configuration().channel().get();

    if (channel.isBlank()) {
      this.addon.displayMessage(Component.empty()
          .append(Component.text("[TWITCH] ", TwitchColor.TWITCH))
          .append(Component.translatable(String.format("%s.command.channel",
              this.addon.addonInfo().getNamespace()), NamedTextColor.RED)));
      return true;
    }

    final TwitchClient client = this.addon.getTwitch();
    final TwitchClient sender = this.addon.getSender();

    if (client == null || !client.getChat().isChannelJoined(channel)
        || sender == null || !sender.getChat().isChannelJoined(channel)) {
      this.addon.displayMessage(Component.empty()
          .append(Component.text("[TWITCH] ", TwitchColor.TWITCH))
          .append(Component.translatable(String.format("%s.command.chat.disconnected",
              this.addon.addonInfo().getNamespace()), NamedTextColor.RED)));
      return true;
    }

    if (client.getChat().getState() != WebsocketConnectionState.CONNECTED
        || sender.getChat().getState() != WebsocketConnectionState.CONNECTED) {
      this.addon.displayMessage(Component.empty()
          .append(Component.text("[TWITCH] ", TwitchColor.TWITCH))
          .append(Component.translatable(String.format("%s.command.chat.connecting",
              this.addon.addonInfo().getNamespace()), NamedTextColor.RED)));
      return true;
    }

    final TwitchChat chat = sender.getChat();
    final String message = String.join(" ", arguments);

    chat.sendMessage(channel, message);
    return true;
  }

}
