package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.room.ChatRoom;
import io.olmosjt.server.ClientHandler;

import java.util.Optional;

public class JoinCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    if (!client.isLoggedIn()) {
      client.send(Message.error("You must be logged in to join a room."));
      return;
    }
    if (payload == null || payload.isBlank()) {
      client.send(Message.error("Usage: JOIN:<room_id>"));
      return;
    }
    try {
      int roomId = Integer.parseInt(payload);
      Optional<ChatRoom> roomToJoin = context.roomManager().getRoom(roomId);
      if (roomToJoin.isEmpty()) {
        client.send(Message.error("Room '" + payload + "' does not exist."));
        return;
      }
      client.getCurrentRoom().ifPresent(oldRoom -> oldRoom.removeMember(client));
      ChatRoom newRoom = roomToJoin.get();
      client.setCurrentRoom(newRoom);
      newRoom.addMember(client);
    } catch (NumberFormatException e) {
      client.send(Message.error("NumberFormatException. Usage: JOIN:<room_id>"));
    }
  }
}
