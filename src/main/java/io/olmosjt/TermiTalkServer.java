package io.olmosjt;

import io.olmosjt.command.CommandHandler;
import io.olmosjt.command.CommandType;
import io.olmosjt.message.Message;
import io.olmosjt.message.MessageDispatcher;
import io.olmosjt.room.ChatRoom;
import io.olmosjt.room.ChatRoomManager;
import io.olmosjt.server.ClientHandler;
import io.olmosjt.server.ClientManager;
import io.olmosjt.server.ServerConfig;
import io.olmosjt.user.UserRegistry;
import io.olmosjt.util.LoggerUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Main entrypoint for TermiTalk server.
 */
public class TermiTalkServer {

  private final ServerContext context;

  public TermiTalkServer(ServerConfig config) {
    var userRegistry = new UserRegistry();
    var clientManager = new ClientManager();
    var dispatcher = new MessageDispatcher(clientManager);
    var commandHandler = new CommandHandler();
    var roomManager = new ChatRoomManager();

    this.context = new ServerContext(userRegistry, clientManager, dispatcher, commandHandler, roomManager);

    roomManager.createRoom("general");

    registerCommands();
  }

  private void registerCommands() {
    CommandHandler handler = context.commandHandler();

    // --- LOGIN COMMAND ---
    handler.register(CommandType.LOGIN, (ctx, client, payload) -> {
      if (client.isLoggedIn()) {
        client.send(Message.error("You are already logged in."));
        return;
      }
      if (payload.isBlank() || !payload.matches("^[a-zA-Z0-9_]{3,15}$")) {
        client.send(Message.error("Invalid username. Use 3-15 alphanumeric characters/underscores."));
        return;
      }
      if (ctx.userRegistry().find(payload).isPresent() || ctx.clientManager().isOnline(payload)) {
        client.send(Message.error("Username '" + payload + "' is already taken."));
        return;
      }

      // Register user
      ctx.userRegistry().register(payload);
      client.registerUser(payload);

      // Announce and welcome
      client.send(Message.system("Welcome, " + payload + "!"));
      ctx.dispatcher().dispatch(Message.system(payload + " has joined the chat."));
    });

    // --- MSG COMMAND ---
    handler.register(CommandType.MSG, (ctx, client, payload) -> {
      if (!client.isLoggedIn()) {
        client.send(Message.error("You must be logged in to send messages."));
        return;
      }
      Optional<ChatRoom> roomOpt = client.getCurrentRoom();
      if (roomOpt.isEmpty()) {
        client.send(Message.error("You are not in a room. Use JOIN:<room_name> to join one."));
        return;
      }
      if (payload.isBlank()) {
        client.send(Message.error("Cannot send an empty message."));
        return;
      }

      // Create the message and broadcast it to the client's current room
      Message message = Message.userMsg(client.getUser().username(), payload);
      roomOpt.get().broadcast(message);
    });

    // --- CREATE_ROOM COMMAND ---
    handler.register(CommandType.CREATE_ROOM, (ctx, client, payload) -> {
      if (!client.isLoggedIn()) {
        client.send(Message.error("You must be logged in to create a room."));
        return;
      }
      if (payload.isBlank() || !payload.matches("^[a-zA-Z0-9_-]{3,20}$")) {
        client.send(Message.error("Invalid room name. Use 3-20 alphanumeric characters, underscores, or hyphens."));
        return;
      }
      ChatRoom chatRoom = ctx.roomManager().createRoom(payload);
      if (chatRoom != null) {
        client.send(Message.system("Room '" + payload + "' created."));
      } else {
        client.send(Message.error("Room '" + payload + "' already exists."));
      }
    });

    // --- JOIN COMMAND ---
    handler.register(CommandType.JOIN, (ctx, client, payload) -> {
      if (!client.isLoggedIn()) {
        client.send(Message.error("You must be logged in to join a room."));
        return;
      }
      if (payload.isBlank()) {
        client.send(Message.error("Usage: JOIN:<room_id>"));
        return;
      }

      try {

        int roomId = Integer.parseInt(payload);
        Optional<ChatRoom> roomToJoin = ctx.roomManager().getRoom(roomId);
        if (roomToJoin.isEmpty()) {
          client.send(Message.error("Room '" + payload + "' does not exist."));
          return;
        }

        // Leave the current room if they are in one
        client.getCurrentRoom().ifPresent(oldRoom -> oldRoom.removeMember(client));

        // Join the new room
        ChatRoom newRoom = roomToJoin.get();
        client.setCurrentRoom(newRoom);
        newRoom.addMember(client);

      } catch (NumberFormatException e) {
        client.send(Message.error("NumberFormatException. Usage: JOIN:<room_id>"));
      }
    });

    // --- LEAVE COMMAND
    handler.register(CommandType.LEAVE, (ctx, client, payload) -> {
      if (!client.isLoggedIn()) {
        client.send(Message.error("You are not logged in."));
        return;
      }
      client.getCurrentRoom().ifPresentOrElse(
              room -> {
                room.removeMember(client);
                client.setCurrentRoom(null);
                client.send(Message.system("You have left '" + room.getName() + "'."));
              },
              () -> client.send(Message.error("You are not in a room."))
      );
    });

    // --- LIST_ROOMS ---
    handler.register(CommandType.LIST_ROOMS, (ctx, client, payload) -> {
      Set<String> roomNames = ctx.roomManager().listRoomNames();
      String roomList = roomNames.isEmpty() ? "No rooms available." : String.join(", ", roomNames);
      client.send(Message.system("Available rooms: " + roomList));
    });

    handler.register(CommandType.WHO, (ctx, client, payload) -> {
      client.getCurrentRoom().ifPresentOrElse(
              room -> {
                String userList = String.join(", ", room.getMemberNames());
                client.send(Message.system("Users in '" + room.getName() + "': " + userList));
              },
              () -> client.send(Message.error("You are not in a room."))
      );
    });


    // --- PRIVMSG COMMAND ---
    handler.register(CommandType.PRIVMSG, (ctx, client, payload) -> {
      if (!client.isLoggedIn()) {
        client.send(Message.error("You must be logged in to send private messages."));
        return;
      }

      String[] parts = payload.split(" ", 2);
      if (parts.length < 2 || parts[1].isBlank()) {
        client.send(Message.error("Usage: PRIVMSG:<recipient> <message>"));
        return;
      }

      String recipient = parts[0];
      String content = parts[1];

      if (recipient.equalsIgnoreCase(client.getUser().username())) {
        client.send(Message.error("You cannot send a private message to yourself."));
        return;
      }

      Message privateMessage = Message.privateMsg(client.getUser().username(), recipient, content);
      ctx.dispatcher().dispatch(privateMessage);
    });

    // --- QUIT COMMAND ---
    handler.register(CommandType.QUIT, (ctx, client, payload) -> {
      client.disconnect();
    });

    // --- HELP COMMAND ---
    handler.register(CommandType.HELP, (ctx, client, payload) -> {
      StringBuilder helpMessage = new StringBuilder("Available commands:\n");
      Arrays.stream(CommandType.values())
              .filter(c -> c != CommandType.UNKNOWN)
              .forEach(c -> helpMessage.append(String.format("  %s: %s\n", c.name(), c.getDescription())));
      client.send(Message.system(helpMessage.toString()));
    });
  }


  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(ServerConfig.defaultConfig().port())) {
      LoggerUtil.info("TermiTalk server started on port " + serverSocket.getLocalPort());

      while (true) {
        Socket clientSocket = serverSocket.accept();
        LoggerUtil.info("New client connected: " + clientSocket.getInetAddress());

        // Pass the entire context to the handler
        ClientHandler handler = new ClientHandler(clientSocket, context);

        Thread.startVirtualThread(handler);
      }
    } catch (IOException e) {
      LoggerUtil.error("Server error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new TermiTalkServer(ServerConfig.defaultConfig()).start();
  }
}
