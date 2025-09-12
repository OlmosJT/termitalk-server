package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.room.ChatRoom;
import io.olmosjt.server.ClientHandler;

public class CreateRoomCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    if (!client.isLoggedIn()) {
      client.send(Message.error("You must be logged in to create a room."));
      return;
    }
    if (payload == null || payload.isBlank() || !payload.matches("^[a-zA-Z0-9_-]{3,20}$")) {
      client.send(Message.error("Invalid room name. Use 3-20 alphanumeric characters, underscores, or hyphens."));
      return;
    }
    ChatRoom chatRoom = context.roomManager().createRoom(payload);
    if (chatRoom != null) {
      client.send(Message.system("Room '" + payload + "' created."));
    } else {
      client.send(Message.error("Failed to create room '" + payload + "'."));
    }
  }
}
