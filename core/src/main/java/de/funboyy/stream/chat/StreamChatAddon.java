package de.funboyy.stream.chat;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.ChannelNoticeEvent;
import com.github.twitch4j.chat.events.channel.ClearChatEvent;
import com.github.twitch4j.chat.events.channel.DeleteMessageEvent;
import com.github.twitch4j.chat.events.channel.UserBanEvent;
import com.github.twitch4j.chat.events.channel.UserTimeoutEvent;
import de.funboyy.stream.chat.command.TwitchChatCommand;
import de.funboyy.stream.chat.config.StreamChatConfig;
import de.funboyy.stream.chat.listener.SettingsListener;
import de.funboyy.stream.chat.listener.TwitchListener;
import de.funboyy.stream.chat.utils.ResourceManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class StreamChatAddon extends LabyAddon<StreamChatConfig> {

  private static StreamChatAddon instance;

  public static StreamChatAddon getInstance() {
    return instance;
  }

  private ExecutorService service = null;
  private TwitchClient twitch = null;
  private ResourceManager resources = null;
  private TwitchClient sender = null;

  @Override
  protected void enable() {
    instance = this;
    this.service = Executors.newSingleThreadExecutor();

    registerSettingCategory();
    registerListener(new SettingsListener(this));
    registerCommand(new TwitchChatCommand(this));

    async(this::startTwitch);
  }

  public void async(final Runnable runnable) {
    if (this.service == null) {
      throw new NullPointerException("The ExecutorService is not set yet");
    }

    this.service.execute(runnable);
  }

  public void startTwitch() {
    if (this.twitch != null || !this.configuration().enabled().get()) {
      return;
    }

    final String token = this.configuration().tokenConfig().token().get();

    if (token.isBlank()) {
      return;
    }

    try {
      logger().info("Starting Twitch client...");

      final CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
      final OAuth2Credential credential = new OAuth2Credential("twitch", token);

      this.twitch = TwitchClientBuilder.builder()
          .withCredentialManager(credentialManager)
          .withDefaultAuthToken(credential)
          .withEnableChat(true)
          .withEnableHelix(true)
          .build();
      this.resources = new ResourceManager(this);

      final TwitchListener listener = new TwitchListener(this);
      this.twitch.getEventManager().onEvent(ChannelMessageEvent.class, listener::onMessage);
      this.twitch.getEventManager().onEvent(ChannelNoticeEvent.class, listener::onNotice);
      this.twitch.getEventManager().onEvent(DeleteMessageEvent.class, listener::onMessageDeleted);
      this.twitch.getEventManager().onEvent(ClearChatEvent.class, listener::onChatClear);
      this.twitch.getEventManager().onEvent(UserTimeoutEvent.class, listener::onUserTimedOut);
      this.twitch.getEventManager().onEvent(UserBanEvent.class, listener::onUserBanned);

      final TwitchChat chat = this.twitch.getChat();
      chat.connect();

      final String channel = configuration().channel().get();

      this.sender = TwitchClientBuilder.builder()
          .withDefaultAuthToken(credential)
          .withEnableChat(true)
          .withChatAccount(credential)
          .build();

      final TwitchChat senderChat = this.twitch.getChat();
      senderChat.connect();

      if (!channel.isEmpty()) {
        chat.joinChannel(channel);
        senderChat.joinChannel(channel);
        this.resources.updateChannel(channel);
      }

    } catch (final Exception exception) {
      logger().error("Failed to start Twitch client", exception);
    }
  }

  public void stopTwitch() {
    if (this.twitch == null) {
      return;
    }

    try {
      logger().info("Stopping Twitch client...");

      final TwitchChat chat = this.twitch.getChat();
      chat.disconnect();

      this.twitch = null;
    } catch (final Exception exception) {
      logger().error("Failed to start Twitch client", exception);
    }
  }

  public void restartTwitch() {
    stopTwitch();
    startTwitch();
  }

  public TwitchClient getTwitch() {
    return this.twitch;
  }

  public ResourceManager getResources() {
    return this.resources;
  }

  public TwitchClient getSender() {
    return this.sender;
  }

  @Override
  protected Class<? extends StreamChatConfig> configurationClass() {
    return StreamChatConfig.class;
  }

}
