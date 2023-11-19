package de.funboyy.stream.chat.api.resource;

import com.google.gson.JsonElement;
import java.util.Map;
import net.labymod.api.client.gui.icon.Icon;

public class FrankerFaceZEmotes extends ThirdPartyEmote {

  private static final String GLOBAL = "https://api.betterttv.net/3/cached/frankerfacez/emotes/global";
  private static final String CHANNEL = "https://api.betterttv.net/3/cached/frankerfacez/users/twitch/%s";

  public FrankerFaceZEmotes() {
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
    final Map<String, Icon> map = global ? super.global : super.channel;

    json.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).forEach(emote -> {
      final String id = emote.get("code").getAsString();
      final String url = emote.getAsJsonObject("images").get("1x").getAsString();

      map.put(id, Icon.url(url, texture -> map.get(id).getResourceLocation()));
    });
  }

}
