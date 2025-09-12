package io.olmosjt.protocol;

import io.olmosjt.message.Message;

public final class MessageEncoder {
  private MessageEncoder() {}

  /**
   * Serializes a Message object to the pipe-delimited wire format.
   * Format: TYPE|SENDER|RECIPIENT|CONTENT
   */
  public static String encode(Message message) {
    return String.join("|",
            message.type().name(),
            message.sender(),
            message.recipient() != null ? message.recipient() : "",
            message.content()
    );
  }
}
