package io.olmosjt.message;

import io.olmosjt.server.ClientHandler;
import io.olmosjt.server.ClientManager;
import io.olmosjt.util.LoggerUtil;

/**
 * Responsible for routing messages between connected clients.
 * <p>
 * Uses {@link io.olmosjt.server.ClientManager} to look up recipients
 * and deliver {@link io.olmosjt.message.Message} instances.
 * <ul>
 *   <li>{@link io.olmosjt.message.MessageType#SYSTEM} and {@link io.olmosjt.message.MessageType#ERROR} → broadcast to all clients</li>
 *   <li>{@link io.olmosjt.message.MessageType#USER} → broadcast to a room (or all if no room logic attached)</li>
 *   <li>{@link io.olmosjt.message.MessageType#PRIVATE} → direct message to specific recipient</li>
 * </ul>
 * <p>
 * This class has no state of its own; it depends on {@link ClientManager}
 * for active connections and {@link io.olmosjt.message.Message} for message metadata.
 */

public class MessageDispatcher {
  private final ClientManager clientManager;

  public MessageDispatcher(ClientManager clientManager) {
    this.clientManager = clientManager;
  }

  public void dispatch(Message message) {
    switch (message.type()) {
      case SYSTEM, OK, NOK -> broadcast(message);
      case USER -> broadcastToRoom(message);
      case PRIVATE -> sendPrivate(message);
      default -> {}
    }
  }

  private void broadcast(Message message) {
    int count = 0;
    for (ClientHandler ch : clientManager.allClients()) {
      ch.send(message);
      count++;
    }
    LoggerUtil.info("ROUTE broadcast type=" + message.type() +
            " from=" + message.sender() +
            " delivered=" + count);
  }

  private void broadcastToRoom(Message message) {
    ClientHandler sender = clientManager.get(message.sender());
    if (sender != null) {
      sender.getCurrentRoom().ifPresentOrElse(
              room -> {
                room.broadcast(message);
                LoggerUtil.info("ROUTE room type=" + message.type() +
                        " from=" + message.sender() +
                        " roomId=" + room.getId() +
                        " roomName=" + room.getName() +
                        " members=" + room.getMemberNames().size());
              },
              () -> {
                LoggerUtil.warn("ROUTE room failed: sender not in a room. from=" + message.sender());
                sender.send(Message.serverNok(message.sender(), "You are not in a room."));
              }
      );
    } else {
      LoggerUtil.warn("ROUTE room failed: unknown sender=" + message.sender());
    }
  }

  private void sendPrivate(Message message) {
    if (message.recipient() == null) return;

    ClientHandler target = clientManager.get(message.recipient());
    if (target != null) {
      target.send(message);
      ClientHandler sender = clientManager.get(message.sender());
      if (sender != null && !sender.equals(target)) {
        sender.send(message);
      }
      LoggerUtil.info("ROUTE private from=" + message.sender() + " to=" + message.recipient());
    } else {
      ClientHandler sender = clientManager.get(message.sender());
      LoggerUtil.warn("ROUTE private failed: recipient offline or not found. from=" + message.sender() +
              " to=" + message.recipient());
      if (sender != null) {
        sender.send(Message.serverNok(sender.getUser().username(),
                "User '" + message.recipient() + "' not found or is offline."));
      }
    }
  }
}
