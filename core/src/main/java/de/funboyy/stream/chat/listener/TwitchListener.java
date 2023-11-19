package de.funboyy.stream.chat.listener;

import com.github.twitch4j.chat.enums.NoticeTag;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.ChannelNoticeEvent;
import com.github.twitch4j.chat.events.channel.ClearChatEvent;
import com.github.twitch4j.chat.events.channel.DeleteMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.chat.events.channel.UserTimeoutEvent;
import de.funboyy.stream.chat.utils.Message;
import de.funboyy.stream.chat.utils.Message.Emote;
import de.funboyy.stream.chat.StreamChatAddon;
import de.funboyy.stream.chat.api.TwitchColor;
import de.funboyy.stream.chat.api.TwitchMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.labymod.api.Laby;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.util.math.MathHelper;

public class TwitchListener {

  private final StreamChatAddon addon;

  public TwitchListener(final StreamChatAddon addon) {
    this.addon = addon;
  }

  public void onMessage(final ChannelMessageEvent event) {
    final IRCMessageEvent ircMessage = event.getMessageEvent();

    final String channelId = event.getChannel().getId();
    final String userId = event.getUser().getId();
    final String messageId = ircMessage.getTagValue("id").orElse("");

    final String name = ircMessage.getTagValue("display-name").orElse("");
    final TextColor color = ircMessage.getTagValue("color")
        .map(TwitchColor::parseHex).orElse(TwitchColor.randomColor(event.getUser().getName()));
    final List<Icon> badges = parseBadges(ircMessage.getTagValue("badges").orElse(null));
    final Message message = new Message(event.getMessage(),
        Emote.parse(ircMessage.getTagValue("emotes").orElse(null)));

    final Component component = Component.empty()
        .append(Component.text("[TWITCH] ", TwitchColor.TWITCH));

    final int size = MathHelper.ceil(8 * this.addon.labyAPI().minecraft().options().getChatScale());

    badges.forEach(badge -> component.append(Component.icon(badge, size))
        .append(Component.icon(Icon.texture(Textures.EMPTY), size / 4)));

    component.append(Component.text(name, color))
        .append(Component.text(": ", NamedTextColor.DARK_GRAY))
        .append(message.parseMessage());

    final TwitchMessage twitchMessage = new TwitchMessage(component, channelId, userId, messageId);
    twitchMessage.send();

    if (this.addon.configuration().soundOnMessage().get()) {
      Laby.references().minecraftSounds().playChatFilterSound();
    }
  }

  public void onMessageDeleted(final DeleteMessageEvent event) {
    removeMessage(message -> message.messageId().equals(event.getMsgId()));
  }

  public void onChatClear(final ClearChatEvent event) {
    removeMessage(message -> message.channelId().equals(event.getChannel().getId()));

    final Component component = Component.empty()
        .append(Component.text("[TWITCH] ", TwitchColor.TWITCH))
        .append(Component.text("The chat has been cleared.", NamedTextColor.GRAY));

    final TwitchMessage message = new TwitchMessage(component, "", "", "");
    message.send();
  }

  public void onUserTimedOut(final UserTimeoutEvent event) {
    removeMessage(message -> message.userId().equals(event.getUser().getId()));
  }

  public void onUserBanned(final UserBanEvent event) {
    removeMessage(message -> message.userId().equals(event.getUser().getId()));
  }

  public void onNotice(final ChannelNoticeEvent event) {
    final String message = event.getMessage();
    final NoticeTag type = event.getType();

    if (type == null) {
      return;
    }

    if (message == null) {
      return;
    }

    final Component component = Component.empty()
        .append(Component.text("[TWITCH] ", TwitchColor.TWITCH));

    switch (type) {
      case EMOTE_ONLY_OFF:
      case EMOTE_ONLY_ON:
      case FOLLOWERS_OFF:
      case FOLLOWERS_ON:
      case FOLLOWERS_ONZERO:
      case R9K_OFF:
      case R9K_ON:
      case SLOW_OFF:
      case SLOW_ON:
      case SUBS_OFF:
      case SUBS_ON:
      case HOST_OFF:
      case HOST_ON:
        component.append(Component.text(message, NamedTextColor.GRAY));
        break;
      case MSG_REJECTED:
        component.append(Component.text(message, NamedTextColor.YELLOW));
        break;
      case NO_PERMISSION:
        component.append(Component.text("Action failed: " + message, NamedTextColor.RED));
        break;
      default:
        if (type.name().endsWith("SUCCESS")) {
          component.append(Component.text(message, NamedTextColor.GREEN));
        } else {
          component.append(Component.text(message, NamedTextColor.RED));
        }
    }

    final TwitchMessage twitchMessage = new TwitchMessage(component, "", "", "");
    twitchMessage.send();
  }

  private void removeMessage(final Predicate<TwitchMessage> filter) {
    this.addon.labyAPI().chatProvider().chatController().getMessages().stream()
        .map(TwitchMessage::fromChatMessage)
        .filter(Objects::nonNull)
        .filter(filter)
        .forEach(message -> this.addon.labyAPI().minecraft().executeOnRenderThread(() ->
            message.message().delete()));
  }

  private List<Icon> parseBadges(final String tag) {
    if (tag == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(tag.split(","))
        .map(badge -> this.addon.getResources().getBadge(badge))
        .filter(Objects::nonNull)
        .toList();
  }

}
