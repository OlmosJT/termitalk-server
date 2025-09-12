package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

public class NickCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    client.send(Message.error("NICK not supported yet."));
  }
}
