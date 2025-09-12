package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.protocol.CommandParser;
import io.olmosjt.server.ClientHandler;

public class LoginCommand implements Command {

  @Override
  public void execute(ServerContext ctx, ClientHandler client, String payload) {
    if (client.isLoggedIn()) {
      client.send(Message.serverNok(client.getUser().username(),"You are already logged in."));
      return;
    }
    if (payload == null || payload.isBlank() || !payload.matches("^[a-zA-Z0-9_]{3,15}$")) {
      client.send(Message.serverNok(client.getUser().username(),"Invalid username. Use 3-15 alphanumeric characters/underscores."));
      return;
    }

    if (ctx.userRegistry().find(payload).isPresent() || ctx.clientManager().isOnline(payload)) {
      client.send(Message.serverNok(client.getUser().username(),"Username '" + payload + "' is already taken."));
      return;
    }

    ctx.userRegistry().register(payload);
    client.registerUser(payload);

    String tip = "\nPlease, use format to communicate with the server: " + CommandParser.REQUEST_FORMAT;
    client.send(Message.serverOk(client.getUser().username(),"Welcome, " + payload + "!" + tip));
  }
}
