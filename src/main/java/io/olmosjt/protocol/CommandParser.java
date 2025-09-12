package io.olmosjt.protocol;

import io.olmosjt.command.CommandType;

public final class CommandParser {

  private CommandParser() {}

  public static ParsedCommand parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String[] parts = raw.split(":", 2);
    CommandType type = CommandType.fromString(parts[0]);
    String payload = (parts.length > 1) ? parts[1].trim() : "";
    return new ParsedCommand(type, payload);
  }

}
