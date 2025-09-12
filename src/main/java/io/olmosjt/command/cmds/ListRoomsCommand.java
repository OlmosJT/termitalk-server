package io.olmosjt.command.cmds;

import io.olmosjt.ServerContext;
import io.olmosjt.command.Command;
import io.olmosjt.message.Message;
import io.olmosjt.server.ClientHandler;

import java.util.Set;

public class ListRoomsCommand implements Command {
  @Override
  public void execute(ServerContext context, ClientHandler client, String payload) {
    Set<String> roomNames = context.roomManager().listRoomNames();
    String roomList = roomNames.isEmpty() ? "No rooms available." : String.join(", ", roomNames);
    client.send(Message.system("Available rooms: " + roomList));
  }
}
