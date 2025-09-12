package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.server.ClientHandler;

public class QuitCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    client.disconnect();
  }
}
