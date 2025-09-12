package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.command.CommandType;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

public class HelpCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    StringBuilder help = new StringBuilder("Available commands:\n");
    for (CommandType c : CommandType.values()) {
      if (c != CommandType.UNKNOWN) {
        help.append("  ").append(c.name()).append(": ").append(c.getDescription()).append(",");
      }
    }
    client.send(Message.serverOk(client.getUser().username(), help.toString()));
  }
}
