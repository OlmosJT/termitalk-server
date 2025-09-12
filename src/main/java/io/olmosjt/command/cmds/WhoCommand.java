package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

public class WhoCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    var currentRoom = client.getCurrentRoom();

    if(currentRoom.isEmpty()) {
      client.send(Message.serverNok(client.getUser().username(),"You are not in a room."));
      return;
    }

    var room = currentRoom.get();
    String userList = String.join(", ", room.getMemberNames());
    client.send(Message.serverOk(client.getUser().username(), "Users in '" + room.getName() + "': " + userList));
  }
}
