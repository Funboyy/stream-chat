package de.funboyy.stream.chat.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.funboyy.stream.chat.StreamChatAddon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.util.math.MathHelper;

public class Message {

  private final String text;
  private final List<Emote> emotes;

  public Message(final String text, final List<Emote> emotes) {
    this.text = text.replace("§", "&");
    this.emotes = new ArrayList<>(emotes);
  }

  public Component parseMessage() {
    final Component component = Component.empty();
    int lastEmote = -1;

    if (!StreamChatAddon.getInstance().configuration().showTwitchEmotes().get()) {
      return component.append(parseEmotes(this.text));
    }

    this.emotes.sort(Comparator.comparingInt(Emote::start));

    final int size = MathHelper.ceil(8 * Laby.labyAPI().minecraft().options().getChatScale());

    for (final Emote emote : this.emotes) {
      // ToDo: set width/height to the same aspect ratio as icon (as soon as it is possible)
      component.append(parseEmotes(this.text
              .substring(lastEmote == -1 ? 0 : lastEmote, emote.start())))
          .append(Component.icon(emote.icon(), size));
      lastEmote = emote.end() + 1;
    }

    if (lastEmote == -1) {
      component.append(parseEmotes(this.text));
    } else {
      component.append(parseEmotes(this.text.substring(lastEmote)));
    }

    return component;
  }

  private Component parseEmotes(final String message) {
    final ResourceManager manager = StreamChatAddon.getInstance().getResources();
    final int size = MathHelper.ceil(8 * Laby.labyAPI().minecraft().options().getChatScale());

    // ToDo: set width/height to the same aspect ratio as icon (as soon as it is possible)
    return parse(message, text -> manager.getEmote(text) == null,
        text -> Component.icon(manager.getEmote(text), size), this::parseLinks);
  }

  private Component parseLinks(final String message) {
    return parse(message, text ->
            !text.matches("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"),
        text -> Component.text(text, NamedTextColor.BLUE).clickEvent(ClickEvent.openUrl(text)),
        text -> Component.text(text, NamedTextColor.WHITE));
  }

  private Component parse(final String message, final Predicate<String> filter,
      final Function<String, Component> function, final Function<String, Component> finish) {
    final Component component = Component.empty();
    int lastMatch = -1;

    for (final String text : message.split(" ")) {
      if (filter.test(text)) {
        continue;
      }

      final int start = message.indexOf(text, lastMatch == -1 ? 0 : lastMatch);
      final int end = start + text.length();

      component.append(finish.apply(message.substring(lastMatch == -1 ? 0 : lastMatch, start)))
          .append(function.apply(text));
      lastMatch = end;
    }

    if (lastMatch == -1) {
      component.append(finish.apply(message));
    } else {
      component.append(finish.apply(message.substring(lastMatch)));
    }

    return component;
  }

  public record Emote(String id, int start, int end, Icon icon) {

    private static final LoadingCache<String, Icon> CACHE = Caffeine.newBuilder()
        .maximumSize(128)
        .build(Emote::loadEmote);

    private static Icon loadEmote(final String id) {
      return Icon.url(String.format("https://static-cdn.jtvnw.net/emoticons/v2/%s/default/dark/1.0",
          id), texture -> {
        final Icon icon = CACHE.getIfPresent(id);

        if (icon != null) {
          icon.getResourceLocation();
        }
      });
    }

    public static List<Emote> parse(final String tag) {
      if (tag == null) {
        return Collections.emptyList();
      }

      return Arrays.stream(tag.split("/")).flatMap(emote -> {
        final String[] emoteData = emote.split(":");

        return Arrays.stream(emoteData[1].split(",")).map(emoteInfo -> {
          final String[] pose = emoteInfo.split("-");

          return new Emote(emoteData[0], Integer.parseInt(pose[0]),
              Integer.parseInt(pose[1]), CACHE.get(emoteData[0]));
        });
      }).toList();
    }

  }

}
