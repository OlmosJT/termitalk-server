package io.olmosjt;

import io.olmosjt.command.CommandHandler;
import io.olmosjt.command.CommandType;
import io.olmosjt.command.cmds.*;
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
    var handler = context.commandHandler();

    handler.register(CommandType.LOGIN, new LoginCommand());
    handler.register(CommandType.MSG, new MsgCommand());
    handler.register(CommandType.CREATE_ROOM, new CreateRoomCommand());
    handler.register(CommandType.JOIN, new JoinCommand());
    handler.register(CommandType.LEAVE, new LeaveCommand());
    handler.register(CommandType.LIST_ROOMS, new ListRoomsCommand());
    handler.register(CommandType.WHO, new WhoCommand());
    handler.register(CommandType.PRIVMSG, new PrivateMessageCommand());
    handler.register(CommandType.QUIT, new QuitCommand());
    handler.register(CommandType.HELP, new HelpCommand());
    handler.register(CommandType.NICK, new NickCommand());
    handler.register(CommandType.UNKNOWN, new UnknownCommand());
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
