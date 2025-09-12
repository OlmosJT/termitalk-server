package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.room.ChatRoom;
import io.olmosjt.server.ClientHandler;

import java.util.Optional;

public class MsgCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    if (!client.isLoggedIn()) {
      client.send(Message.error("You must be logged in to send messages."));
      return;
    }
    Optional<ChatRoom> roomOpt = client.getCurrentRoom();
    if (roomOpt.isEmpty()) {
      client.send(Message.error("You are not in a room. Use JOIN:<room_name> to join one."));
      return;
    }
    if (payload == null || payload.isBlank()) {
      client.send(Message.error("Cannot send an empty message."));
      return;
    }
    Message message = Message.userMsg(client.getUser().username(), payload);
    roomOpt.get().broadcast(message);
  }
}
