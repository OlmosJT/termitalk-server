package io.olmosjt.message;

import java.time.Instant;
import java.util.Objects;

public record Message(
        MessageType type,
        String sender,
        String recipient,   // TIP: null for broadcast/system messages
        String content,
        Instant timestamp
) {

  public Message {
    Objects.requireNonNull(type);
    Objects.requireNonNull(sender);
    Objects.requireNonNull(content);
    if (timestamp == null) timestamp = Instant.now();
  }

  /**
   * OK|SYSTEM|username|content
   */
  public static Message serverOk(String username, String content) {
    return new Message(MessageType.OK, "SYSTEM", username, content, Instant.now());
  }

  /**
   * NOK|SYSTEM|username|content
   */
  public static Message serverNok(String username, String content) {
    return new Message(MessageType.NOK, "SYSTEM", username, content, Instant.now());
  }

  /**
   * PRIVATE|from|to|content
   */
  public static Message privateMsg(String from, String to, String content) {
    return new Message(MessageType.PRIVATE, from, to, content, Instant.now());
  }

  /**
   * USER|from|null|content
   */
  public static Message userMsg(String from, String content) {
    return new Message(MessageType.USER, from, null, content, Instant.now());
  }

}
