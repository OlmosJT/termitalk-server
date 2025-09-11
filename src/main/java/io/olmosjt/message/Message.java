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

  public static Message system(String content) {
    return new Message(MessageType.SYSTEM, "SYSTEM", null, content, Instant.now());
  }

  public static Message error(String content) {
    return new Message(MessageType.ERROR, "SYSTEM", null, content, Instant.now());
  }

  public static Message privateMsg(String from, String to, String content) {
    return new Message(MessageType.PRIVATE, from, to, content, Instant.now());
  }

  public static Message userMsg(String from, String content) {
    return new Message(MessageType.USER, from, null, content, Instant.now());
  }

}
