package io.olmosjt.command;

import io.olmosjt.ServerContext;
import io.olmosjt.message.Message;
import io.olmosjt.protocol.CommandParser;
import io.olmosjt.protocol.ParsedRequest;
import io.olmosjt.server.ClientHandler;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry and executor for commands.
 * <p>
 * Commands are mapped from {@link CommandType} to their {@link Command} implementation.
 * This allows a pluggable design where each command can be registered independently.
 */
public class CommandHandler {

  private final Map<CommandType, Command> commands = new EnumMap<>(CommandType.class);

  public void register(CommandType type, Command command) {
    commands.put(type, command);
  }

  /**
   * Handles an incoming command from a client.
   * @param context The server context.
   * @param client  The client handler for the connection.
   * @param raw     The raw input string from the client.
   */
  public void handle(ServerContext context, ClientHandler client, String raw) {
    // REQ|COMMAND:PAYLOAD
    ParsedRequest parsed = CommandParser.parse(raw);

    var command = commands.get(parsed.type());
    if (command != null) {
      command.execute(context, client, parsed.payload());
    } else {
      String username = client.isLoggedIn() ? client.getUser().username() : null;
      client.send(Message.serverNok(username, "Unknown command or malformed request. Expected format: "
              + CommandParser.REQUEST_FORMAT));
    }
  }

}

