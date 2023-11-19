package de.funboyy.stream.chat.api;

import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.chat.ChatTrustLevel;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.options.ChatVisibility;
import net.labymod.api.metadata.Metadata;

public class TwitchMessage {

  private static final String CHANNEL_ID = "ChannelId";
  private static final String USER_ID = "UserId";
  private static final String MESSAGE_ID = "MessageId";

  private final ChatMessage message;
  private final String channelId;
  private final String userId;
  private final String messageId;

  public TwitchMessage(final ChatMessage message) {
    this.message = message;

    final Metadata metadata = this.message.metadata();

    if (!metadata.has(CHANNEL_ID) || !metadata.has(USER_ID) || !metadata.has(MESSAGE_ID)) {
      throw new IllegalArgumentException("The ChatMessage is not a TwitchMessage");
    }

    this.channelId = metadata.get(CHANNEL_ID);
    this.userId = metadata.get(USER_ID);
    this.messageId = metadata.get(MESSAGE_ID);
  }

  public TwitchMessage(final Component component, final String channelId, final String userId, final String messageId) {
    this.message = ChatMessage.builder().component(component).visibility(ChatVisibility.SHOWN)
        .trustLevel(ChatTrustLevel.SYSTEM).build();
    this.channelId = channelId;
    this.userId = userId;
    this.messageId = messageId;

    final Metadata metadata = this.message.metadata();
    metadata.set(CHANNEL_ID, this.channelId);
    metadata.set(USER_ID, this.userId);
    metadata.set(MESSAGE_ID, this.messageId);

    this.message.metadata(metadata);
  }

  public void send() {
    if (Laby.labyAPI().minecraft().isIngame()) {
      Laby.labyAPI().chatProvider().chatController().addMessage(this.message);
    }
  }

  public ChatMessage message() {
    return this.message;
  }

  public String channelId() {
    return this.channelId;
  }

  public String userId() {
    return this.userId;
  }

  public String messageId() {
    return this.messageId;
  }

  public static TwitchMessage fromChatMessage(final ChatMessage message) {
    final Metadata metadata = message.metadata();

    if (!metadata.has(CHANNEL_ID) || !metadata.has(USER_ID) || !metadata.has(MESSAGE_ID)) {
      return null;
    }

    return new TwitchMessage(message);
  }

}
