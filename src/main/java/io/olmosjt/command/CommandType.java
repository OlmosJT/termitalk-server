package io.olmosjt.command;

/**
 * Represents all supported client commands.
 * <p>
 * Commands are sent from the client as plain strings, for example: "LOGIN:Alice".
 */
public enum CommandType {
  LOGIN("Log in with a username"),
  NICK("Change your nickname"),
  LIST_ROOMS("List available chat rooms"),
  CREATE_ROOM("Create a new chat room"),
  JOIN("Join a specific chat room"),
  LEAVE("Leave the current chat room"),
  WHO("List users in the current chat room"),
  MSG("Send a message to the current room"),
  PRIVMSG("Send a private message to a user"),
  QUIT("Disconnect from the server"),
  HELP("Show available commands"),
  UNKNOWN("Unknown or unsupported command");

  private final String description;

  CommandType(String description) {
    this.description = description;
  }

  /**
   * Parse a raw string into a {@link CommandType}.
   */
  public static CommandType fromString(String raw) {
    if (raw == null) return UNKNOWN;
    try {
      return CommandType.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }

  public String getDescription() {
    return description;
  }
}
