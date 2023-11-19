package de.funboyy.stream.chat.api.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.labymod.api.Laby;
import net.labymod.api.util.io.web.request.Response;
import net.labymod.api.util.io.web.request.WebResolver;
import net.labymod.api.util.io.web.request.types.StringRequest;

public abstract class ThirdPartyEmote extends Resource {

  private final WebResolver resolver;

  public ThirdPartyEmote() {
    this.resolver = Laby.references().webResolver();
  }

  protected void register(final boolean global, final String url, final Object... objects) {
    final StringRequest request = StringRequest.create().url(url, objects);
    final Response<String> response = this.resolver.resolveConnection(request);

    if (response.getStatusCode() != 200) {
      return;
    }

    final JsonElement json = JsonParser.parseString(response.get());
    register(global, json);
  }

  protected abstract void register(final boolean global, final JsonElement json);

}
