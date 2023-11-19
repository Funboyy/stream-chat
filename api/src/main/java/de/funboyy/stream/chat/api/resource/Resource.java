package de.funboyy.stream.chat.api.resource;

import java.util.HashMap;
import java.util.Map;
import net.labymod.api.client.gui.icon.Icon;

public abstract class Resource {

  protected final Map<String, Icon> global;
  protected final Map<String, Icon> channel;
  protected String channelId;

  public Resource() {
    this.global = new HashMap<>();
    this.channel = new HashMap<>();
  }

  protected abstract void loadGlobal();

  protected abstract void loadChannel();

  public void updateChannel(final String channelId) {
    this.channel.clear();
    this.channelId = channelId;

    loadChannel();
  }

  public Icon get(final String id) {
    final Icon icon = this.global.get(id);

    if (icon != null) {
      return icon;
    }

    return this.channel.get(id);
  }

}
