package io.olmosjt.message;

import io.olmosjt.server.ClientHandler;
import io.olmosjt.server.ClientManager;

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
      case SYSTEM, ERROR -> broadcast(message);
      case USER -> broadcastToRoom(message);
      case PRIVATE -> sendPrivate(message);
      default -> {}
    }
  }

  private void broadcast(Message message) {
    for (ClientHandler ch : clientManager.allClients()) {
      ch.send(message);
    }
  }

  private void broadcastToRoom(Message message) {
    // TODO: integrate with ChatRoom logic
    broadcast(message); // fallback: send to all
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
    } else {
      // If recipient not found, send an error back to the sender
      ClientHandler sender = clientManager.get(message.sender());
      if (sender != null) {
        sender.send(Message.error("User '" + message.recipient() + "' not found or is offline."));
      }
    }
  }
}
