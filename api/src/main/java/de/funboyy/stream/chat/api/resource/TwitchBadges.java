package de.funboyy.stream.chat.api.resource;

import com.github.twitch4j.TwitchClient;
import net.labymod.api.client.gui.icon.Icon;

public class TwitchBadges extends Resource {

  private final TwitchClient client;

  public TwitchBadges(final TwitchClient client) {
    super();
    this.client = client;

    loadGlobal();
  }

  @Override
  protected void loadGlobal() {
    this.client.getHelix()
        .getGlobalChatBadges(null).execute().getBadgeSets().forEach(badgeSet ->
            badgeSet.getVersions().forEach(badge -> super.global.put(
                badgeSet.getSetId() + "/" + badge.getId(),
                Icon.url(badge.getSmallImageUrl(), texture -> super.global
                    .get(badgeSet.getSetId() + "/" + badge.getId()).getResourceLocation()))));
  }

  @Override
  protected void loadChannel() {
    this.client.getHelix()
        .getChannelChatBadges(null, this.channelId).execute().getBadgeSets().forEach(badgeSet ->
            badgeSet.getVersions().forEach(badge -> super.channel.put(
                badgeSet.getSetId() + "/" + badge.getId(),
                Icon.url(badge.getSmallImageUrl(), texture -> super.channel
                    .get(badgeSet.getSetId() + "/" + badge.getId()).getResourceLocation()))));
  }

}
