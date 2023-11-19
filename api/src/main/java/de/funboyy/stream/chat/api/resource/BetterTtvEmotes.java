package de.funboyy.stream.chat.api.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Map;
import net.labymod.api.client.gui.icon.Icon;

public class BetterTtvEmotes extends ThirdPartyEmote {

  private static final String GLOBAL = "https://api.betterttv.net/3/cached/emotes/global";
  private static final String CHANNEL = "https://api.betterttv.net/3/cached/users/twitch/%s";

  public BetterTtvEmotes() {
    super();

    loadGlobal();
  }

  @Override
  protected void loadGlobal() {
    register(true, GLOBAL);
  }

  @Override
  protected void loadChannel() {
    register(false, CHANNEL, super.channelId);
  }

  @Override
  protected void register(final boolean global, final JsonElement json) {
    if (global) {
      register(super.global, json.getAsJsonArray());
    } else {
      register(super.channel, json.getAsJsonObject().getAsJsonArray("channelEmotes"));
      register(super.channel, json.getAsJsonObject().getAsJsonArray("sharedEmotes"));
    }
  }

  private void register(final Map<String, Icon> map, final JsonArray json) {
    json.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).forEach(emote -> {
      final String id = emote.get("code").getAsString();
      final String url = String.format("https://cdn.betterttv.net/emote/%s/1x",
          emote.get("id").getAsString());

      map.put(id, Icon.url(url, texture -> map.get(id).getResourceLocation()));
    });
  }

}
