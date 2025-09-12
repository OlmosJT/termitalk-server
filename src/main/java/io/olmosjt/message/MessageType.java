package io.olmosjt.message;

public enum MessageType {
  OK("successful response to a client request"),
  NOK("error response to a client request"),
  SYSTEM("server/system notifications (events)"),
  USER("normal chat messages in rooms"),
  PRIVATE("private messages between users"),
  COMMAND("commands sent from client to server");

  private final String description;

  MessageType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
