package de.funboyy.stream.chat.api.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.labymod.api.client.gui.icon.Icon;

public class SevenTvEmotes extends ThirdPartyEmote {

  private static final String GLOBAL = "https://7tv.io/v3/emote-sets/global";
  private static final String CHANNEL = "https://7tv.io/v3/users/twitch/%s";

  public SevenTvEmotes() {
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

    final JsonElement element = global ? json : json.getAsJsonObject().get("emote_set");
    final JsonArray emotes = element.getAsJsonObject().getAsJsonArray("emotes");

    emotes.asList().stream().map(JsonElement::getAsJsonObject).forEach(emote -> {
      final JsonObject host = emote.getAsJsonObject("data")
          .getAsJsonObject("host");
      final JsonArray files = host.getAsJsonArray("files");
      final JsonObject file = files.asList().stream().map(JsonElement::getAsJsonObject).filter(format ->
          format.get("format").getAsString().equals("WEBP")).findFirst().orElseThrow();

      final String id = emote.get("name").getAsString();
      final String url = String.format("https:%s/%s", host.get("url").getAsString(),
          file.get("name").getAsString());

      map.put(id, Icon.url(url, texture -> map.get(id).getResourceLocation()));
    });
  }

}
