package de.funboyy.stream.chat.utils;

import com.github.twitch4j.helix.domain.User;
import de.funboyy.stream.chat.StreamChatAddon;
import de.funboyy.stream.chat.config.StreamChatConfig;
import de.funboyy.stream.chat.api.resource.BetterTtvEmotes;
import de.funboyy.stream.chat.api.resource.FrankerFaceZEmotes;
import de.funboyy.stream.chat.api.resource.SevenTvEmotes;
import de.funboyy.stream.chat.api.resource.TwitchBadges;
import java.util.Collections;
import java.util.List;
import net.labymod.api.client.gui.icon.Icon;

public class ResourceManager {

  private final StreamChatAddon addon;
  private final TwitchBadges badges;
  private final SevenTvEmotes sevenTv;
  private final BetterTtvEmotes betterTtv;
  private final FrankerFaceZEmotes frankerFaceZ;

  public ResourceManager(final StreamChatAddon addon) {
    this.addon = addon;
    this.badges = new TwitchBadges(this.addon.getTwitch());
    this.sevenTv = new SevenTvEmotes();
    this.betterTtv = new BetterTtvEmotes();
    this.frankerFaceZ = new FrankerFaceZEmotes();
  }

  public void updateChannel(final String channel) {
    final List<User> users = this.addon.getTwitch().getHelix().getUsers(null, null,
        Collections.singletonList(channel)).execute().getUsers();

    if (users.isEmpty()) {
      return;
    }

    final User user = users.get(0);

    this.badges.updateChannel(user.getId());
    this.sevenTv.updateChannel(user.getId());
    this.betterTtv.updateChannel(user.getId());
    this.frankerFaceZ.updateChannel(user.getId());
  }

  public Icon getBadge(final String id) {
    return this.badges.get(id);
  }

  public Icon getEmote(final String id) {
    final StreamChatConfig config = this.addon.configuration();
    Icon icon = config.showSevenTvEmotes().get() ? this.sevenTv.get(id) : null;

    if (icon != null) {
      return icon;
    }

    icon = config.showBetterTtvEmotes().get() ? this.betterTtv.get(id) : null;

    if (icon != null) {
      return icon;
    }

    return config.showFrankerFaceZ().get() ? this.frankerFaceZ.get(id) : null;
  }

}
