package io.olmosjt.server;

import io.olmosjt.ServerContext;
import io.olmosjt.message.Message;
import io.olmosjt.protocol.MessageEncoder;
import io.olmosjt.room.ChatRoom;
import io.olmosjt.user.User;
import io.olmosjt.user.UserStatus;
import io.olmosjt.util.LoggerUtil;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles communication with a single client.
 * Designed to run inside a virtual thread.
 */
public class ClientHandler implements Runnable {
  private final Socket socket;
  private final ServerContext context;
  private ChatRoom currentRoom;

  private PrintWriter out;
  private BufferedReader in;
  private User user; // Still useful to hold user state
  private final AtomicBoolean running = new AtomicBoolean(true);


  public ClientHandler(Socket socket, ServerContext context) {
    this.socket = socket;
    this.context = context;
  }

  public Optional<ChatRoom> getCurrentRoom() {
    return Optional.ofNullable(currentRoom);
  }

  public void setCurrentRoom(ChatRoom room) {
    this.currentRoom = room;
  }

  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

      send(Message.system("Welcome! Please log in with: LOGIN:<username>"));

      String line;
      while (running.get() && (line = in.readLine()) != null) {
        // Pass the context to the command handler
        context.commandHandler().handle(context, this, line);
      }
    } catch (IOException e) {
      LoggerUtil.debug("Client connection error: " + e.getMessage());
    } finally {
      disconnect();
    }
  }

  /**
   * Finalizes user registration after a successful LOGIN command.
   * @param username The username to register.
   */
  public void registerUser(String username) {
    this.user = new User(username, UserStatus.ONLINE, Instant.now());
    context.clientManager().register(username, this);
  }

  public void send(Message message) {
    if (out != null && running.get()) {
      out.println(MessageEncoder.encode(message));
    }
  }

  public User getUser() {
    return user;
  }

  public boolean isLoggedIn() {
    return user != null;
  }



  public void disconnect() {
    if (!running.getAndSet(false)) {
      return;
    }

    // --- Leave the current room if in one ---
    if (currentRoom != null) {
      currentRoom.removeMember(this);
    }
    // ----------------------------------------

    try {
      if (user != null) {
        // We no longer need to broadcast a global departure message here,
        // as the room departure is more specific.
        context.clientManager().unregister(user.username());
        context.userRegistry().unregister(user.username());
        LoggerUtil.info("Client disconnected: " + user.username());
      } else {
        LoggerUtil.info("Client disconnected: (unauthenticated)");
      }

      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LoggerUtil.error("Error closing client: " + e.getMessage());
    }
  }
}
