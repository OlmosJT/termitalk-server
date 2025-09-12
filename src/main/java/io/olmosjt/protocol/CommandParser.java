package io.olmosjt.protocol;

import io.olmosjt.command.CommandType;

public final class CommandParser {

  private CommandParser() {}

  public static final String REQUEST_FORMAT = "REQ|COMMAND|payload";

  public static ParsedRequest parse(String raw) {
    if (raw == null || raw.isBlank())return new ParsedRequest(CommandType.UNKNOWN, "");

    if (!raw.startsWith("REQ|")) {
      return new ParsedRequest(CommandType.UNKNOWN, raw);
    }


    // Split into at most 3 parts: REQ | COMMAND | payload
    String[] parts = raw.split("\\|", 3);
    // parts[0] = "REQ"
    String cmdToken = parts.length > 1 ? parts[1] : "";
    String payload  = parts.length > 2 ? parts[2] : "";

    return new ParsedRequest(CommandType.fromString(cmdToken), payload);

  }

}
