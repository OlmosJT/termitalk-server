package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

public class LeaveCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    if (!client.isLoggedIn()) {
      client.send(Message.serverNok(client.getUser().username(),"You are not logged in."));
      return;
    }
    client.getCurrentRoom().ifPresentOrElse(
            room -> {
              room.removeMember(client);
              client.setCurrentRoom(null);
              client.send(Message.serverOk(client.getUser().username(),"You have left '" + room.getName() + "'."));
            },
            () -> client.send(Message.serverNok(client.getUser().username(), "You are not in a room."))
    );
  }
}
